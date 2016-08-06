package br.com.marsal.stout.orm.field;

import java.lang.reflect.Field;
import java.util.Calendar;

import br.com.marsal.stout.orm.database.SQLiteHelper;
import br.com.marsal.stout.orm.exception.ExceptionUtils;

/**
 * Factory responsible to create the correct fiel definition instance using in a field datatype passed by param.
 */
public abstract class FieldDefFactory {
		
	/**
	 * Create the field definition class. If any field needs a custom behavior one specific 
	 * field definition class must be created and mapped here. For the general cases will be 
	 * created one "StandardField" instance.
	 */
	public static FieldDef createField(Field field) {

		FieldDef result = null;
		Class<?> fieldType = field.getType();
		
		//Enum field type
		if (fieldType.isEnum())
			result = new EnumField(field);
		
		//Boolean field
		else if (fieldType.equals(Boolean.class) || fieldType.equals(boolean.class))
			result = new BooleanField(field);
		
		//Class field
		else if (fieldType.equals(Class.class))
			result = new ClassField(field);
		
		//Date Time field
		else if (fieldType.equals(java.util.Date.class) || fieldType.equals(java.sql.Date.class) || fieldType.equals(Calendar.class))
			result = new DateTimeField(field);
		
		// For all others fields that cannot has a specific field class implemented check if its is a supported mapped datatype.
		// If "true" create a standard field type. Otherwise throw one exception. 
		else if (SQLiteHelper.isValidFieldType(fieldType))
			result = new FieldDef(field);
		else
			throw ExceptionUtils.newRuntimeException(FieldDefFactory.class, "Field type \"" + fieldType.getName() + "\" is not mapped yet.");
		
		return result;
	}
	
}