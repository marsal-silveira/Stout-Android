package br.com.marsal.stout.orm.entity;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import br.com.marsal.stout.orm.annotation.Entity;
import br.com.marsal.stout.orm.annotation.GetJoinTables;
import br.com.marsal.stout.orm.annotation.MappedSuperclass;
import br.com.marsal.stout.orm.annotation.Transient;
import br.com.marsal.stout.orm.database.SQLiteHelper;
import br.com.marsal.stout.orm.enumeration.PersistenceOperation;
import br.com.marsal.stout.orm.exception.EPrimaryKeyNotFound;
import br.com.marsal.stout.orm.exception.ExceptionUtils;
import br.com.marsal.stout.orm.field.FieldDef;
import br.com.marsal.stout.orm.field.FieldDefFactory;
import br.com.marsal.stout.orm.field.FieldDefList;

/**
 * This class represents the definition of the persistence entity mapped by <b>Stout Persistence Framework</b>.<p>
 * 
 * Through this is possible access the entity primary key, entity class and their field lists (QUERY, CREATE TABLE, INSERT ecc).
 */
public class EntityDef {
	
	/* ********************************************************
	 * PROPERTIES / CONSTANTS / INTERNAL VARIABLES
	 **********************************************************/

	/** 
	 * Final entity class mapped by framework and associated to this persistence entity.<br>
	 * All mapped entities must be annoted with {@link Entity} or {@link MappedSuperclass}.
	 */
	protected Class<?> entityClass = null;
	public final Class<?> getEntityClass() {return entityClass;}

	/** Name of table in SQLite database of mapped {@link #entityClass} where their fields will be stored. */
	protected String tableName = null;
	public final String getTableName() {return tableName;}
	
	/** Primary Key {@link FieldDef} of mapped {@link #entityClass}. */
	protected FieldDef primaryKey = null;
	public final FieldDef getPrimaryKey() {return primaryKey;}
	
	/**	All Join Tables associated to this Entity. */
	protected Map<String, JoinTable> joinTables = null;
	public final Map<String, JoinTable> getJoinTables() {return joinTables;}
	public final String getCreateJoinTableStatement(String jointTableName) {return joinTables.get(jointTableName).getCreateTableStatement();}
	
	/* ENTITY FIELD LISTS */
	
	/** Field list containing all {@link #entityClass} fields. This list is used only internally. */
	protected final FieldDefList<FieldDef> fields = new FieldDefList<>();
	public final List<FieldDef> getFields() {return fields;}
	
	//used in CRUD operations (CREATE, SELECT, INSERT and UPDATE)
	
	/** This is a subset of {@link #fields} containing only {@link #entityClass} fields used in {@link PersistenceOperation#CREATE_TABLE} statements. */
	protected FieldDefList<FieldDef> fieldsToCreateTable = new FieldDefList<>();
	public final List<FieldDef> getFieldsToCreateTable() {return fieldsToCreateTable;}
			
	/** This is a subset of {@link #fields} containing only {@link #entityClass} fields used in {@link PersistenceOperation#INSERT} statements. */
	protected final FieldDefList<FieldDef> fieldsToInsert = new FieldDefList<>();
	public final List<FieldDef> getFieldsToInsert() {return fieldsToInsert;}
	
	/** This is a subset of {@link #fields} containing only {@link #entityClass} fields used in {@link PersistenceOperation#UPDATE} statements. */
	protected final FieldDefList<FieldDef> fieldsToUpdate = new FieldDefList<>();
	public final List<FieldDef> getFieldsToUpdate() {return fieldsToUpdate;}
	
	/** This is a subset of {@link #fields} containing only {@link #entityClass} fields used in {@link PersistenceOperation#QUERY} statements. */
	protected final List<FieldDef> fieldsToQuery = new ArrayList<FieldDef>();
	public final List<FieldDef> getFieldsToQuery() {return fieldsToQuery;}
	
	/* CREATE STATEMENTS */
	
	/**	*/
	protected String createTableStatement = null;
	public final String getCreateTableStatement () {

		if (createTableStatement == null) {
            createTableStatement = SQLiteHelper.buildCreateTableStatement(this);
        }
		return createTableStatement;
	}
	
	/* ********************************************************
	 * CONSTRUCTORS
	 **********************************************************/

	/**
	 * Create one new mapped Entity Definiton and with his {@link #entityClass} reference.<br>
	 * This only create and set the {@link #entityClass} property. To extract the others properties and fields use {@link #init()} method.
	 */
	public EntityDef(Class<?> entityClass) {
		//set the entity class
		this.entityClass = entityClass;
		
		//extract the entity infos from @Entity annotation		
		Entity entity = entityClass.getAnnotation(Entity.class);
		tableName = entity.tableName() != "" ? entity.tableName() : entityClass.getSimpleName();
	}
	
	/* ********************************************************
	 * INIT
	 **********************************************************/
	
	/** Extract entity fields and primary Key */
	public void init() {
		extractFields();
		extractPrimaryKey();
	}
	
	/** Extract all {@link #entityClass} fields. */
	@SuppressWarnings("unchecked")
	protected final void extractFields() {
//		Log.d(getClass().getSimpleName(), "[extractFields] entity: '" + entityClass.getSimpleName() + "'");
		
		//get all declared fields non static and non @Transient from class "entityClass" until base class (object) to build the "entityFields"
		Class<?> clazz = entityClass;
		while (clazz != Object.class) {
			//only classes annoted with @Entity or @MappedSuperclass
			if (clazz.isAnnotationPresent(Entity.class) || clazz.isAnnotationPresent(MappedSuperclass.class)) {
				//extract all fields
				Field[] sourceFields = clazz.getDeclaredFields();
				for (Field field : sourceFields) {
					field.setAccessible(true);
					if (!Modifier.isStatic(field.getModifiers()) && !field.isAnnotationPresent(Transient.class))
						fields.add(FieldDefFactory.createField(field));
				}
				
				//extract join table list
				Method[] sourceMethods = clazz.getDeclaredMethods();
				for (Method method : sourceMethods) {
					method.setAccessible(true);
					if (method.isAnnotationPresent(GetJoinTables.class)) {
						try {
							joinTables = (Map<String, JoinTable>) method.invoke(null, new Object[]{});
						} catch (Exception e) {
							new RuntimeException("Error getting join table list from entity: '" + getEntityClass().getSimpleName() + "'.", e);
						}
					}
				}
			}
			//get the superclass 
			clazz = clazz.getSuperclass();
		}
		//finally build all helpers fields list
		buildFieldLists();
	 }
	 
	 /** Extract the {@link #entityClass} primary key and set the {@link #primaryKey} property. */
	 protected final void extractPrimaryKey() {
		 //get the primary key field
		 for (FieldDef field : fields) {
			 if (field.isPrimaryKey()) {
				 primaryKey = field;
				 break;
			 }
		 }	
		 // check if primary key field is found and valid...
		 if (primaryKey == null)
			 throw ExceptionUtils.newRuntimeException(EntityDef.class, new EPrimaryKeyNotFound("Primary Key not found for class \"" + entityClass + "\""));
	 }
	 
	 /* ********************************************************
	  * ENTITIES
	  **********************************************************/
	
	 /** Create and return a new instance of {@link #entityClass} class. */
	 public final Object newInstance() {
		 //try create a new instance of entity class... if occurs a error we throw one RuntimeException to abort operation
		 try {
			 return entityClass.newInstance();
		 } catch (Exception e) {
			 throw ExceptionUtils.newRuntimeException(getClass(), "Error on creating new instance of \"" + entityClass.getName() + "\". Details: " + e.getMessage());
		 }
	 }
	
	 /* ********************************************************
	  * ENTITY FIELDS AND COLUMNS
	  **********************************************************/
		
	 /** Call this after all entity fields are extracted. This will load all lists to helper the framework operations. */
	 protected void buildFieldLists() {
		 
		 // first clear all field lists
		 fieldsToCreateTable.clear();
		 fieldsToQuery.clear();
		 fieldsToInsert.clear();
		 fieldsToUpdate.clear();
		
		 // fill specifics field lists using some field properties
		 for (FieldDef field : fields) {
			 
			 //fields to "create table"
			 if (field.isPersistent())
				 fieldsToCreateTable.add(field);
						
			 //fields to "insert"
			 if (field.isPersistent() && field.isInsertable())
				 fieldsToInsert.add(field);
			
			 //fields to "update"
			 if (field.isPersistent() && field.isUpdatable())
				 fieldsToUpdate.add(field);
			
			 //fields to "query"
			 if (field.isPersistent())
				 fieldsToQuery.add(field);
		 }
	 }
	
	 /** Return the {@link FieldDef} instance identify by <code>fieldName</code> param or <code>null</code> if not found. */
	 public final FieldDef getField(String fieldName) {
		 //
		 FieldDef result = null;
		 for (FieldDef field : fields) {
			 //if field is founded... break the loop and return it
			 if (field.getFieldName().equals(fieldName)){
				 result = field;
				 break;
			 }
		 }
		 return result;
	 }
			
	 /** Return the String array with all entity columns names to be used in QUERY operations. */
	 public final String[] getColumnsToQuery(){
		 //first get the list fields to extract only the column names
		 List<FieldDef> fields = getFieldsByOperation(PersistenceOperation.QUERY);
		
		 String[] result = new String[fields.size()];
		 int i = 0;
		 for (FieldDef field : fields) {
			 result[i] = field.getColumnName();
			 i++;
		 }
		 return result;
	 }
		
	 /**
	  * Return the List with all entity columns following the operation param.
	  * Operation must be {@link PersistenceOperation#CREATE_TABLE}, {@link PersistenceOperation#QUERY}, {@link PersistenceOperation#INSERT} and {@link PersistenceOperation#UPDATE}. 
	  */
	 public final List<FieldDef> getFieldsByOperation(PersistenceOperation operation){
		 //
		 switch (operation) {
		 case CREATE_TABLE:
			 return fieldsToCreateTable;
		 case QUERY:
			 return fieldsToQuery;
		 case INSERT:
			 return fieldsToInsert;
		 case UPDATE:
			 return fieldsToUpdate;
		 default:
			 throw ExceptionUtils.newRuntimeException(EntityDef.class, "Persistence Operation \"" + operation + "\" is not valid on getFieldsByOperation.");
		 }
	 }
	 
}