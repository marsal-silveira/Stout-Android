package br.com.marsal.stout.orm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * If a method is annotated with this annotation it will be used to get all join tables associated to its entity definition.<br>
 * The method must by static and without params.<p>
 *
 * <pre><b>Example:</b>
 *
 * {@literal @GetJoinTables}
 * private static Map<String, JoinTable> getJoinTables() {
 *
 *	Map<String, JoinTable> result = new HashMap<>();
 *
 *	//Entity1_Entities2
 *	JoinTable joinTable = new JoinTable("Entity1_Entities2", "entity1_id", "entity2_id");
 *	result.put(joinTable.getName(), joinTable);
 *	return result;
 * }</pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface GetJoinTables {
	
}