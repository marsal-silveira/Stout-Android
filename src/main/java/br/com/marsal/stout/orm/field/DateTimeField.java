package br.com.marsal.stout.orm.field;

import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Custom implementation of {@link FieldDef} that represents the Date field behavior.
 */
public class DateTimeField extends FieldDef {
	
	/* *********************************************************
	 * CONSTRUCTORS
	 * *********************************************************/

	/**
	 * Default constructor. Only to call the superclass constructor passing the field.
	 * 
	 * @param field - the java class field used to extract all mapped entity field properties
	 */
	public DateTimeField(Field field) {
		super(field);
	}
	
	/* *********************************************************
	 * CONVERT VALUES
	 * *********************************************************/
	
	/**
	 * Custom implementation to DateTime field class of {@link FieldDef#convertJavaToSQLite(Object)}.
	 * This will convert the value from Java DateTime type to mapped SQLite datatype (STRING).
	 */
	@Override
	protected Object convertJavaToSQLite(Object value) {
		//
		return value = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(value);
	}
	
	/**
	 * Custom implementation to DateTime field class of {@link FieldDef#convertSQLiteToJava(Object)}.
	 * This will convert the value from mapped SQLite datatype (STRING) to Date type.
	 */
	@Override
	protected Object convertSQLiteToJava(Object value) {
		//
		try {
			value = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).parse(value.toString());
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
		return value;
	}
			
}