package br.com.marsal.stout.orm.field;

import java.lang.reflect.Field;

/**
 * Custom implementation of {@link FieldDef} that represents the Boolean field behavior.
 */
public class BooleanField extends FieldDef {
	
	/* ********************************************************
	 * CONSTRUCTORS
	 **********************************************************/

	/** Default constructor. Only to call the superclass constructor passing the field. */
	public BooleanField(Field field) {
		super(field);
	}
	
	/* ********************************************************
	 * CUSTOM METHODS
	 **********************************************************/
	
	/**
	 * Custom implementation to Boolean field class of {@link FieldDef#convertJavaToSQLite(Object)}.
	 * This will convert the value from Java Booelan type to mapped SQLite datatype (INTEGER).
	 */
	@Override
	protected Object convertJavaToSQLite(Object value) {
		//parser the original value (Boolean) to mapped SQLite value (Integer)
		return value = Boolean.parseBoolean(value.toString()) ? 1 : 0;
	}
	
	/**
	 * Custom implementation to Boolean field class of {@link FieldDef#convertSQLiteToJava(Object)}.
	 * This will convert the value from mapped SQLite datatype (INTEGER) to Java Boolean type.
	 */
	@Override
	protected Object convertSQLiteToJava(Object value) {
		//parser the mapped SQLite value (String) to original value type (boolean)
		return value = Integer.parseInt(value.toString()) == 1 ? true : false;
	}

}