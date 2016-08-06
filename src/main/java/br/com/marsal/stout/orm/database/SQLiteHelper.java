package br.com.marsal.stout.orm.database;

import android.content.ContentValues;
import android.database.Cursor;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.com.marsal.stout.orm.entity.EntityDef;
import br.com.marsal.stout.orm.enumeration.SQLiteDataType;
import br.com.marsal.stout.orm.enumeration.PersistenceOperation;
import br.com.marsal.stout.orm.exception.ExceptionUtils;
import br.com.marsal.stout.orm.field.FieldDef;

/**
 * Util class used to helper with SQLite context like fields, statements and types. 
 */
public abstract class SQLiteHelper {

	/* ********************************************************
	 * PROPERTIES, CONSTANTS AND INTERNAL VARIABLES
	 **********************************************************/

	/** List containing all types actually suported by this framework. */
	private static List<Class<?>> supportedTypes = new ArrayList<>();
	static {
		supportedTypes.add(String.class);
		supportedTypes.add(Boolean.class);
		supportedTypes.add(boolean.class);
		supportedTypes.add(Integer.class);
		supportedTypes.add(int.class);
		supportedTypes.add(Long.class);
		supportedTypes.add(long.class);
		supportedTypes.add(Float.class);
		supportedTypes.add(float.class);
		supportedTypes.add(Double.class);
		supportedTypes.add(double.class);
		supportedTypes.add(Short.class);
		supportedTypes.add(short.class);
		supportedTypes.add(java.util.Date.class);
		supportedTypes.add(java.sql.Date.class);
		supportedTypes.add(Calendar.class);
	}

	/** Used to map java types to SQLite types to be used on create table SQLite stetaments */
	private static Map<Class<?>, SQLiteDataType> javaSQLiteTypeMap = new HashMap<>();
	static {
		//Boolean types will be stored as SQLite INTEGER datatype because the SQLite3 doesn't properly support boolean values.
		javaSQLiteTypeMap.put(Boolean.class, SQLiteDataType.INTEGER);
		javaSQLiteTypeMap.put(boolean.class, SQLiteDataType.INTEGER);
		//Enum types will be stored as SQLite TEXT datatype for both EnumTypes (ORDINAL and STRING). This simplifies the model and gives more flexibility.
		javaSQLiteTypeMap.put(Enum.class, SQLiteDataType.TEXT);
		javaSQLiteTypeMap.put(String.class, SQLiteDataType.TEXT);
		javaSQLiteTypeMap.put(Integer.class, SQLiteDataType.INTEGER);
		javaSQLiteTypeMap.put(int.class, SQLiteDataType.INTEGER);
		javaSQLiteTypeMap.put(Long.class, SQLiteDataType.INTEGER);
		javaSQLiteTypeMap.put(long.class, SQLiteDataType.INTEGER);
		javaSQLiteTypeMap.put(Float.class, SQLiteDataType.REAL);
		javaSQLiteTypeMap.put(float.class, SQLiteDataType.REAL);
		javaSQLiteTypeMap.put(Double.class, SQLiteDataType.REAL);
		javaSQLiteTypeMap.put(double.class, SQLiteDataType.REAL);
		javaSQLiteTypeMap.put(Short.class, SQLiteDataType.INTEGER);
		javaSQLiteTypeMap.put(short.class, SQLiteDataType.INTEGER);
		javaSQLiteTypeMap.put(java.util.Date.class, SQLiteDataType.TEXT);
		javaSQLiteTypeMap.put(java.sql.Date.class, SQLiteDataType.TEXT);
		javaSQLiteTypeMap.put(Calendar.class, SQLiteDataType.TEXT);
//		javaSQLiteTypeMap.put(Class.class, SQLiteDataType.TEXT);
	}

	/* Constants used to build all SQLite statements */
	private final static String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS %s (%s)";
	private final static String AUTOINCREMENT = " AUTOINCREMENT";
	private final static String PRIMARY_KEY = " PRIMARY KEY";
	private final static String NOT_NULL = " NOT NULL";
	private final static String UNIQUE = " UNIQUE";
	public final static String CREATE_JOIN_TABLE = "CREATE TABLE IF NOT EXISTS %s (%s INTEGER NOT NULL, %s INTEGER NOT NULL, PRIMARY KEY (%s, %s))";
	public final static String QUERY_JOIN_TABLE = "SELECT %s FROM %s AS t INNER JOIN %s AS jt ON jt.%s = t.%s AND jt.%s = %s";

	/* ********************************************************
	 * SQLite STATEMENT BUILDER
	 **********************************************************/

	/** Extract all fields from field list and build a string separated with comma. This is used in SQLite statements. */
	public static String fieldsToCommaSeparated(List<FieldDef> fields) {
		List<String> columns = new ArrayList<>();
		for (FieldDef field : fields)
			columns.add(field.getColumnName());
		
		return StringUtils.join(columns, ',');
	}

	/** */
	public static String buildCreateTableStatement(EntityDef entityDef) {
		//build the column stetament
		List<String> columns = new ArrayList<String>();
		for (FieldDef field : entityDef.getFieldsToCreateTable())
			columns.add(SQLiteHelper.buildColumnStatement(field));
		
		//build the final CREATE TABLE statement
		return String.format(SQLiteHelper.CREATE_TABLE, entityDef.getTableName(), StringUtils.join(columns, ','));
	}

	/** Build the field statement used to create a column in a SQLite table. */
	private static String buildColumnStatement(FieldDef field) {
		//check if field has one valid mapped SQLite type... if not abort operation and throw a excetpion
		if (field.getSQLiteDataType() == null)
			throw ExceptionUtils.newRuntimeException(FieldDef.class, "field \"" + field.getColumnName() + "\" has no SQLite type mapped.");
				
		//build the field statement
		String statement = field.getColumnName() + " " + field.getSQLiteDataType();
				
		//here we defines all COLUMN CONSTRAINTS like PRIMARY KEY, NOT NULL, UNIQUE
		
		//PRIMARY KEY
		if (field.isPrimaryKey())
			statement += SQLiteHelper.PRIMARY_KEY;
		
		//AUTOINCREMENT
		//TODO check if field is a numeric type to define it as autoincrement
		if (field.isAutoincrement())
			statement += SQLiteHelper.AUTOINCREMENT;
		
		//NOT NULL
		if (!field.isNullable() && !field.isPrimaryKey())
			statement += SQLiteHelper.NOT_NULL;
			
		//UNIQUE
		if (field.isUnique() && !field.isPrimaryKey())
			statement += SQLiteHelper.UNIQUE;
		
		//return the column statement
		return statement;
	}

	/* ********************************************************
	 * UTILS
	 **********************************************************/

	/** */
	public static String getPrimaryKeyWhereClause(FieldDef primaryKey, Object value) {
		return " " + primaryKey.getColumnName() + " = " + value.toString();
	}

	/** Only check if fieldType param is a valid field type supported by framework. */
	public static boolean isValidFieldType(Class<?> fieldType) {

		return supportedTypes.contains(fieldType) || fieldType.isEnum();
	}

	/** Check if field is a SQLite INTEGER field. */
	public static boolean isSQLiteIntegerField(Class<? extends Object> type) {
		return type.equals(Integer.class) || type.equals(int.class) ||
			   type.equals(Long.class) || type.equals(long.class) ||
			   type.equals(Short.class) || type.equals(short.class);
	}

	/** */
	public static SQLiteDataType getSQLiteDataType(Class<?> javaType) {
		return javaSQLiteTypeMap.get(javaType);
	}

	/** Check the cursor column datatype and return its value into a Object result. */
	public static Object getCursorColumnValue(Cursor cursor, int columnIndex) {
		switch (cursor.getType(columnIndex)) {
			case Cursor.FIELD_TYPE_INTEGER:
				return cursor.getInt(columnIndex);
			case Cursor.FIELD_TYPE_FLOAT:
				return cursor.getFloat(columnIndex);
			case Cursor.FIELD_TYPE_STRING:
				return cursor.getString(columnIndex);
			case Cursor.FIELD_TYPE_BLOB:
				return cursor.getBlob(columnIndex);
			default: //Cursor.FIELD_TYPE_NULL:
				return null;
		}
	}

	/** */
	public static ContentValues getContentValues(Object entity, EntityDef definition, PersistenceOperation operation){
		//for each field we get their converted value and put it into ContentValues. 
		Object value;
		ContentValues values = new ContentValues();
		for (FieldDef field : definition.getFieldsByOperation(operation)) {
			//get the mapped/converted field value and check if put it on values or put a "null" value.
			value = field.getMappedFieldValue(entity);
			if (value != null)
				values.put(field.getColumnName(), value.toString());
			else
				values.putNull(field.getColumnName());
		}
		return values;
	}

}