package br.com.marsal.stout.orm.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Defines the inheritance strategy to be used for an entity class hierarchy is JOINED.
 * It is specified on the entity class that is the root of the entity class hierarchy.
 *
 * <pre>
 *   Example:
 *
 *   &#064;Entity
 *   &#064;InheritanceSingleTable
 *   public class Customer { ... }
 *
 *   &#064;Entity
 *   public class ValuedCustomer extends Customer { ... }
 * </pre>
 */
@Target({TYPE})
@Retention(RUNTIME)
public @interface InheritanceSingleTable {

}