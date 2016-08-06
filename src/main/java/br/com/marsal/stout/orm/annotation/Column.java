package br.com.marsal.stout.orm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * It's used to specify the mapped column for a persistent property or field.
 * If no Column annotation is specified, the default values apply.
 * 
 * <pre><b>Example:</b>
 * 
 * ...
 * {@literal @Column}(nullable=false, unique=true)
 * public String description;
 * ...</pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Column {
	
	/**
	 * (Optional) The name of column.
	 * If not informed the column name will be the entity property or field name.<p>
	 * 
	 * @default ""
	 */
	public String name() default "";

	/**
	 * (Optional) Whether the database column is nullable.
	 * If <code>false</code> this will generate a <b>not null</b> column on database table.<p>
	 * 
	 * @default true
	 */
	public boolean nullable() default true;
	
	/**
	 * (Optional) Whether the column is included in <code>SQL INSERT</code> statements.
	 * Use this equals <code>false</code> to <b>primary key</b> fields, for example.<p>
	 *  
	 * @default true
	 */
	 public boolean insertable() default true;
	 
	/**
	 * (Optional) Whether the column is included in <code>SQL UPDATE</code> statements.
	 * Use this equals <code>false</code> to <b>primary key</b> fields, for example.<p>
	 *  
	 * @default true
	 */
	 public boolean updatable() default true;
	 
	/**
	 * (Optional) Whether the column is unique constraint in database table.<p>
	 * 
	 * @default false
	 */
	 public boolean unique() default false;

}