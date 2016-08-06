package br.com.marsal.stout.orm.entity;

import java.util.ArrayList;
import java.util.List;

import br.com.marsal.stout.orm.annotation.InheritanceSingleTable;
import br.com.marsal.stout.orm.exception.ExceptionUtils;
 
/**
 * Responsible to manager all mapped entityDefinitions. This is a Singleton implementation using a enum partner, so to use it follow the below example.
 * 
 * <pre>
 * <b>Example 1: get the entity definition using the INSTANCE directly.</b>
 * 
 * // On method body / variable declaration
 * EntityDef entity = EntityDefFactory.INSTANCE.getEntity(entityClass);
 * 
 * <b>Example 2: get the entity definition using a variable previously assigned.</b>
 * 
 * // On method body/ variable declaration
 * EntityDefFactory entityDefinitionFactory = EntityDefFactory.INSTANCE;
 * ...
 * EntityDef entity = entityDefinitionFactory.getEntity(entityClass); </pre>
 */
public enum EntityDefFactory
{
	INSTANCE; // instance of enum to access the singleton...

	/* *********************************************************************************************
	 * PROPERTIES / CONSTANTS / INTERNAL VARIABLES
	 * *********************************************************************************************/
	
	/** Indicates that the persistence properties has been initialized. */
	private boolean mInitialized = false;
	
	/** Entities managed by this framework. A mapped entity is a instance of {@link EntityDef}. */
	private List<EntityDef> entities;
	public List<EntityDef> getEntities() { return entities; }
	
	/* *********************************************************************************************
	 * INIT / PREPARE
	 * *********************************************************************************************/

	/**
	 * Receive the candidates mapped entityDefinitions and get the final mapped entity definition. 
	 * For each mapped class will be extracted their proeprties and fields to be used by this framework.   
	 */
	public void init(List<Class<?>> mappedClasses)
    {
		//only if not already initialized
		if (!mInitialized) {
		
			//only create all entities without extract their fields and insert its into entities def cache.
	    	entities = new ArrayList<EntityDef>();
			for (Class<?> clazz : mappedClasses) {
                entities.add(createEntityDef(clazz));
            }
			//extracting all entities properties and fields (realtionship fields partially)
			for (EntityDef entityDef : entities) {
                entityDef.init();
            }
			//configure all single tables with their subclasses...
			for (EntityDef entityDef : entities) {
                if (SingleTableSubclass.SingleTableSuperclass.class.isAssignableFrom(entityDef.getClass())) {
                    SingleTableSubclass.SingleTableSuperclass.class.cast(entityDef).initSubclasses();
                }
            }
			//to avoid call this method one more time...
			mInitialized = true;
		}
	}
	
	/** Create the correct entity definition using his entity class. */
	private EntityDef createEntityDef(Class<?> entityClass)
    {
		if (entityClass.isAnnotationPresent(InheritanceSingleTable.class)) {
            return new SingleTableSubclass.SingleTableSuperclass(entityClass);
        } else if (entityClass.getSuperclass().isAnnotationPresent(InheritanceSingleTable.class)) {
            return new SingleTableSubclass(entityClass);
        } else {
            return new EntityDef(entityClass);
        }
	}
	
	/* *********************************************************************************************
	 * GET ENTITY DEFINITION 
	 * *********************************************************************************************/

	/** Return the instance of {@link EntityDef} for {@code entityClass} param. */
	public EntityDef getEntityDef(Class<?> entityClass)
    {
		return getEntityDef(entityClass.getSimpleName());
	}
	
	/** Return the instance of {@link EntityDef} for {@code entityClassSimpleName} param. */
	public EntityDef getEntityDef(String entityClassSimpleName)
    {
		EntityDef result = null;
		for (EntityDef entity : entities) {
			if (entity.getEntityClass().getSimpleName().equals(entityClassSimpleName)) {
				result = entity;
				break;
			}
		}

		//check if has one mapped entity to entityClass passed by param... if not throw an exception
		if (result == null) {
            throw ExceptionUtils.newRuntimeException(getClass(), "Entity '" + entityClassSimpleName + "' doesn't have a mapped entity. Check if she is in 'persistence.xml'.");
        }
		return result;
	}
	
}