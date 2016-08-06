package br.com.marsal.stout.orm.field;

import java.lang.reflect.Field;

import br.com.marsal.stout.orm.annotation.Enumerated;
import br.com.marsal.stout.orm.database.SQLiteHelper;
import br.com.marsal.stout.orm.enumeration.EnumType;

/**
 * Custom implementation of {@link FieldDef} that represents the Enum field behavior.
 */
public class EnumField extends FieldDef {
	
	/* ********************************************************
	 * PROPERTIES / CONSTANTS / INTERNAL VARIABLES
	 **********************************************************/
	
	/** Indicates what is the enum type value stored in this field, {@code ORDINAL} or {@code STRING}. */
	protected EnumType enumType = null;
	
	/* ********************************************************
	 * PROPERTIES GETTERS AND SETTERS
	 **********************************************************/

	/** @return the value stored in {@link #enumType} property. */
	public EnumType getEnumType() {
		return enumType;
	}
	
	/* ********************************************************
	 * CONSTRUCTORS
	 **********************************************************/

	/**
	 * Default constructor. Only to call the superclass constructor passing the field.
	 */
	public EnumField(Field field) {
		//
		super(field);
		
		//extract the enum type annoted in field declaration
		Enumerated enumerated = field.getAnnotation(Enumerated.class);
		this.enumType = enumerated.value();
		
		//redefine some superclass properties
		columnType = Enum.class; //set the columnType as Enum base class
		sqliteDataType = SQLiteHelper.getSQLiteDataType(columnType);
	}

	/* ********************************************************
	 * CUSTOM METHODS
	 **********************************************************/

	/**
	 * Custom implementation to Enum field class of {@link FieldDef#convertJavaToSQLite(Object)}.
	 * This will convert the value from Java Enum type to mapped SQLite datatype (INTEGER or STRING).
	 */
	@Override
	protected Object convertJavaToSQLite(Object value) {
		
		//check what is the enum type to decides what will be the SQLite datatype to convert 
		switch (enumType) {
		case ORDINAL:
			value = getEnum(field.getType(), value.toString()).ordinal();
			break;

		default: //STRING
			value = value.toString();
			break;
		}
		
		return value;
	}
	
	/**
	 * Custom implementation to Enum field class of {@link FieldDef#convertSQLiteToJava(Object)}.
	 * This will convert the value from mapped SQLite datatype (INTEGER or STRING) to Java Enum type.
	 */
	@Override
	protected Object convertSQLiteToJava(Object value) {
		
		//check what is the enum type to decides what will be the SQLite datatype to convert
		switch (enumType) {
		case ORDINAL:
			value = field.getType().getEnumConstants()[Integer.parseInt(value.toString())];
			break;

		default: //STRING
			value = getEnum(field.getType(), value.toString());
			break;
		}
		
		return value;
	}
	
	/**
	 * Return the Enum value from enumClass associated to enumName. 
	 */
	private static Enum<?> getEnum(Class<?> enumClass, String enumName) {
		//
		@SuppressWarnings({"rawtypes", "unchecked"})
		Enum result = Enum.valueOf((Class<Enum>) enumClass, enumName);
		return result;
	}

}