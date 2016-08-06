package br.com.marsal.stout.orm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies that the property or field is not persistent. 
 * It is used to annotate a property or field of an entity class, mapped superclass, or embeddable class.
 * 
 * <pre><b>Example:</b>
 * 
 * {@literal @Entity}
 * public class Employee {
 *     {@literal @PrimaryKey}
 *     public int id;
 *     
 *     {@literal @Transient}
 *     public User currentUser;
 *     ...
 * }</pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Transient {

}