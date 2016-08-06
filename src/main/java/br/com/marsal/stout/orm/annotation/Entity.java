package br.com.marsal.stout.orm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies that the class is an entity and will be persisted on database.
 * This annotation is applied to the entity class.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Entity {
	
	/**
	 * (Optional) The entity table name.<br>
	 * This name is used to refer to the entity in queries and table on database.<br> 
	 * The name must not be a reserved literal in the Java Persistence query language.<br>
	 * If not informed the table name will be the unqualified name of the entity class.
	 * 
	 * @Default ""
	 */
	public String tableName() default "";
	
}