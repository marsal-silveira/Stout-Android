package br.com.marsal.stout.orm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Designates a class whose mapping information is applied to the entities that inherit from it. 
 * A mapped superclass has no separate table defined for it.<p>
 * <p>
 * A class designated with the MappedSuperclass annotation can be mapped in the same way as an 
 * entity except that the mappings will apply only to its subclasses since no table exists for the mapped superclass itself. 
 * When applied to the subclasses the inherited mappings will apply in the context of the subclass tables.
 * 
 * <pre><b>Example: Concrete class as a mapped superclass</b>
 * 
 * {@literal @MappedSuperclass}
 * public class Employee {
 * 
 *     {@literal @PrimaryKey}
 *     protected Integer empId;
 *     {@literal @ManyToOne}(joinColumn="ADDR")
 *     protected Address address;
 * 
 *     public Integer getEmpId() { ... }
 *     public void setEmpId(Integer id) { ... }
 *     public Address getAddress() { ... }
 *     public void setAddress(Address addr) { ... }
 * }
 * 
 * // Default table is FTEMPLOYEE table
 * {@literal @Entity}
 * public class FTEmployee extends Employee {
 * 
 * // Inherited empId field mapped to FTEMPLOYEE.EMPID
 * // Inherited address field mapped to FTEMPLOYEE.ADDR fk
 * 
 *     // Defaults to FTEMPLOYEE.SALARY
 *     protected Integer salary;
 * 
 *     public FTEmployee() {}
 *     public Integer getSalary() { ... }
 *     public void setSalary(Integer salary) { ... }
 * }</pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface MappedSuperclass {

}
