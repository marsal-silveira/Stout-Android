package br.com.marsal.stout.orm.field;

import java.lang.reflect.Field;

import br.com.marsal.stout.orm.database.SQLiteHelper;
import br.com.marsal.stout.orm.exception.ExceptionUtils;

/**
 * Custom implementation of {@link FieldDef} that represents the Class<?> field behavior.
 */
public class ClassField extends FieldDef {
	
	/* ********************************************************
	 * CONSTRUCTORS
	 **********************************************************/

	/**
	 * Default constructor. Only to call the superclass constructor passing the field.
	 */
	public ClassField(Field field) {
		//
		super(field);
		
		//redefine some superclass properties
		columnType = String.class; //set the columnType as String class
		sqliteDataType = SQLiteHelper.getSQLiteDataType(columnType);
	}

	/* ********************************************************
	 * CUSTOM METHODS
	 **********************************************************/
	
	/**
	 * Custom implementation to Enum field class of {@link FieldDef#convertJavaToSQLite(Object)}.
	 * This will convert the value from Java Class<?> type to mapped SQLite datatype (STRING).
	 */
	@Override
	protected Object convertJavaToSQLite(Object value) {
		
		//parser the original value (Class<?>) to mapped SQLite value (String)
		value = value.toString();//all class name
		return value;
	}
	
	/**
	 * Custom implementation to Enum field class of {@link FieldDef#convertSQLiteToJava(Object)}.
	 * This will convert the value from mapped SQLite datatype (STRING) to Java Class<?> type.
	 */
	@Override
	protected Object convertSQLiteToJava(Object value) {
		
		//parser the mapped SQLite value (String) to original value type (Java Class<?> type)
		try {
			value = Class.forName(value.toString());
		} catch (ClassNotFoundException e) {
			throw ExceptionUtils.newRuntimeException(ClassField.class, "Class \"" + value.toString() + "\" saved in database not found.");
		}
		
		return value;
	}
	
}