package br.com.marsal.stout.orm.field;

import java.lang.reflect.Field;

import br.com.marsal.stout.orm.annotation.Column;
import br.com.marsal.stout.orm.annotation.PrimaryKey;
import br.com.marsal.stout.orm.database.SQLiteHelper;
import br.com.marsal.stout.orm.enumeration.SQLiteDataType;
import br.com.marsal.stout.orm.exception.ExceptionUtils;

/**
 * Represents the definition of a field of mapped entity. 
 * This provide all field properties necessary to do all database operations.
 */
public class FieldDef {
	
	/* ********************************************************
	 * PROPERTIES / CONSTANTS / INTERNAL VARIABLES
	 **********************************************************/
	
	/** Represents the field as well the developer set it */
	protected Field field;
	protected Field getField() {return field;}
	
	/** Indicates if the field will be perssited into database. */
	protected boolean persistent;
	public boolean isPersistent() {return persistent;}
	
	/** 
	 * Column name in entity table that store the field value following these values:<br>
	 * <b><li>standard field:</b> has the field name</li>
	 * <b><li>mapped field</b> by default is the field name plus "_id" sufix</li> 
	 */
	protected String columnName = null;
	public String getColumnName() {return columnName;}
	
	/**
	 * Represents the field/column type in database following these rules:<br>
	 * <b><li>standard field:</b> has the field type</li>
	 * <b><li>mapped field:</b> has the target entity primary key type</li> 
	 */
	protected Class<?> columnType = null;
	public Class<?> getColumnType() {return columnType;}
	
	/** Indicates what is the SQLite datatype mapped to this field. */
	protected SQLiteDataType sqliteDataType = null;
	public SQLiteDataType getSQLiteDataType() {return sqliteDataType;}
	
	/** Indicates that this field is the primary key of entity. */
	protected boolean primaryKey = false;
	public boolean isPrimaryKey() {return primaryKey;}
	
	/** 
	 * Whether the database column is nullable.<br>
	 * If {@code false} this will generate a <b>not null</b> column on database table.
	 * @see Column#nullable()
	 */
	protected boolean nullable = true;
	public boolean isNullable() {return nullable;}
	
	/** 
	 * Whether the column is included in <code>SQL INSERT</code> statements.<br>
	 * Use this equals <code>false</code> to <b>primary key</b> fields, for example. 
	 * @see Column#insertable() 
	 */
	protected boolean insertable = true;
	public boolean isInsertable() {return insertable;}
	
	/** 
	 * Whether the column is included in <code>SQL UPDATE</code> statements.<br>
	 * Use this equals <code>false</code> to <b>primary key</b> fields, for example.
	 * @see Column#updatable() 
	 */
	protected boolean updatable = true;
	public boolean isUpdatable() {return updatable;}
	
	/** 
	 * Whether the column is unique constraint in database table.
	 * @see Column#unique() 
	 */
	protected boolean unique = false;
	public boolean isUnique() {return unique;}
	
	/** Whether the <b>INTEGER</b> column has their value generated automatically by SQLite. */
	protected boolean autoincrement = false;
	public boolean isAutoincrement() {return autoincrement;}

	/* ********************************************************
	 * CONSTRUCTORS
	 **********************************************************/
	
	/** Default constructor. Receive the mapped class raw field and extract all definitions. */
	public FieldDef(Field field) {
		
		//get the field references and column infos
		this.field = field;
		persistent = true; //default value... can be changed for descendants classes
		columnType = field.getType();
		sqliteDataType = SQLiteHelper.getSQLiteDataType(columnType);
		
		//extract the primary key infos from @PrimaryKey annotation
		primaryKey = field.isAnnotationPresent(PrimaryKey.class);
		
		//extract the field/column infos from @Column annotation		
		Column column = field.getAnnotation(Column.class);
		columnName = column != null && column.name() != "" ? column.name() : field.getName();
		nullable = column != null ? column.nullable() : true;
		insertable = column != null ? column.insertable() : true;
		updatable = column != null ? column.updatable() : true;
		unique = column != null ? column.unique() : false;
//		autoincrement = column != null && column.autoIncrement();
		
		//if field is AUTOINCREMENT check if it is a SQLite INTEGER datatype. 
		if (autoincrement && !SQLiteHelper.isSQLiteIntegerField(field.getType()))
			throw ExceptionUtils.newRuntimeException(SQLiteHelper.class, "Field \"" + field.getName() + " must be SQLite INTEGER datatype to be AUTOINCREMENT.");
	}
	
	/* ********************************************************
	 * FIELDS PROPERTIES AND VALUES
	 **********************************************************/
	
	/** Return the raw field name. */
	public String getFieldName() {
		return field.getName();
	}
	
	/** Return the raw field type. */
	public final Class<?> getFieldType() {
		return field.getType();
	}
	
	/** This will return the field value without apply any conversion. */
	protected final Object getFieldValue(Object entity) {
		//get the field value
		Object result = null;
		try {
			result = field.get(entity);
		} catch (Exception e) {
			throw ExceptionUtils.newRuntimeException(FieldDef.class, e);
		}
		return result;
	}
	
	/**
	 * This will get the mapped field value (value converted from Java datatype to SQLite datatype). 
	 * This value can be used in INSERT / UPDATE operations.<p>
	 * 
	 * By default return the same Java datatype value without conversion. This rules must be implemented by descendants classes.  
	 */
	public final Object getMappedFieldValue(Object entity) {
		//first get the entity field type
		Object value = getFieldValue(entity);
		
		//if value is not null convert the java datatype value to a mapped SQLite datatype value
		if (value != null) 
			value = convertJavaToSQLite(value);
		
		return value;
	}
		
	/** Sets the value of the field in the specified object to the value. */
	public final void setFieldValue(Object entity, Object value) {
		//
		try {
			//first convert the value to field type value (if needed)... after try to set it into entity field
			if (value != null)
				value = convertSQLiteToJava(value);
			
			field.setAccessible(true);
			field.set(entity, value);
		} catch (Exception e) {
			throw ExceptionUtils.newRuntimeException(FieldDef.class, e);
		}
	}
	
	/**
	 * Convert the value from SQLite to Java. In some cases the same field has different datatypes mapped between.
	 * By default only return the same value without apply any specific rule. 
	 */
	protected Object convertSQLiteToJava(Object value) {
		return value;
	}

	/**
	 * Convert the value from Java to SQLite. In some cases the same field has different datatypes mapped between.
	 * By default only return the same value without apply any specific rule. 
	 */
	protected Object convertJavaToSQLite(Object value) {
		return value;
	}
	
	/* ********************************************************
	 * OVERRIDE
	 **********************************************************/
	
    @Override
    public boolean equals(Object object) {
    	if (!(object instanceof FieldDef))
            return false;
    	
    	return (this.hashCode() == FieldDef.class.cast(object).hashCode());
    }

    @Override
    public int hashCode() {
    	return 31 * field.getName().hashCode();
    }
		
}