package br.com.marsal.stout.orm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies the primary key of an entity. 
 * The field or property to which the <code>{@literal @PrimaryKey}</code> annotation is applied should be one of the following types:
 * <li> any Java primitive type;
 * <li> any primitive wrapper type; 
 * <li> {@code String; java.util.Date; java.sql.Date; java.math.BigDecimal; java.math.BigInteger}.<br><br>
 * 
 * The mapped column for the primary key of the entity is assumed to be the primary key of the primary table. 
 * If no Column annotation is specified, the primary key column name is assumed to be the name of the primary key property or field.
 * 
 * <pre><b>Example:</b>
 * 
 * {@literal @PrimaryKey}
 * public Long id;</pre>
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PrimaryKey {
	
	/**
	 * (Optional) If "True" indicates that the column value will be generated automatically.
	 * The keyword <b>AUTOINCREMENT</b> can be used with <b>INTEGER</b> field only.<p>
	 *  
	 * @default false
	 */
	 public boolean autoIncrement() default false;

}