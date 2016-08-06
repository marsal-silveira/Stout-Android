package br.com.marsal.stout.orm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import br.com.marsal.stout.orm.enumeration.EnumType;

/**
 * Specifies that a persistent property or field should be persisted as a enumerated type. 
 * The Enumerated annotation may be used in conjunction with the Basic annotation, or in 
 * conjunction with the ElementCollection annotation when the element collection value is of basic type. 
 * If the enumerated type is not specified or the Enumerated annotation is not used, the EnumType value is assumed to be ORDINAL.<p>
 *
 * <pre><b>Example:</b>
 *
 * public enum EmployeeStatus {FULL_TIME, PART_TIME, CONTRACT}
 * 
 * public enum SalaryRate {JUNIOR, SENIOR, MANAGER, EXECUTIVE}
 *
 * {@literal @Entity} 
 * public class Employee {
 *     public EmployeeStatus getStatus() {...}
 *     ...
 *     {@literal @Enumerated}(STRING)
 *     private EmployeeStatus status;
 *     {@literal @Enumerated}(ORDINAL)
 *     private SalaryRate payScale;
 *     ...
 * }</pre>
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Enumerated {

	/**
	 * (Optional) The type used in mapping an enum type. 
	 *  
	 * @Default br.com.marsal.stout.orm.enumeration.EnumType.ORDINAL;
	 */
	 public EnumType value() default EnumType.ORDINAL;

}