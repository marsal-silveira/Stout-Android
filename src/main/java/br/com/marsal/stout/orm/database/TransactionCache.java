package br.com.marsal.stout.orm.database;

import java.util.LinkedHashMap;
import java.util.Map;

import br.com.marsal.stout.orm.entity.EntityDef;
import br.com.marsal.stout.orm.entity.EntityDefFactory;

/**
 * Cache used to storage all objects created in the current database transaction.
 */
public class TransactionCache {
	
	/* ********************************************************
	 * PROPERTIES / CONSTANTS / INTERNAL VARIABLES
	 **********************************************************/
	
	/** Represents all entities cached in current database transaction.<br>
	 *  This cache contains a map of entities instances for each class loaded from database in the current transaction.<p>
	 * 
	 *  When the current transaction is ended this cache will be cleaned automatically.	*/
	private Map<Class<?>, Map<Object, Object>> entityCache = new LinkedHashMap<Class<?>, Map<Object, Object>>();
	
	/* ********************************************************
	 * GENERAL
	 **********************************************************/
	
	public void clearCache() {

		entityCache.clear();
	}
	
	/* ********************************************************
	 * ENTITY CACHE
	 **********************************************************/
	
	public void putEntity(Object obj) {

		Class<?> entityClass = obj.getClass();
		Object primaryKeyValue = EntityDefFactory.INSTANCE.getEntityDef(entityClass).getPrimaryKey().getMappedFieldValue(obj);

		Map<Object, Object> entities = entityCache.get(entityClass);
		if (entities == null) {
			//
			entities = new LinkedHashMap<Object, Object>();
			entityCache.put(entityClass, entities);
		}
			
		entities.put(primaryKeyValue, obj);
	}
	
	/**
	 * Return the entity instance of entityClass param identify by primaryKeyValue param
	 *
	 * @param entityClass - class of the entity
	 * @param primaryKeyValue - primar key value to find the instance
	 */
	public Object getEntity(Class<?> entityClass, Object primaryKeyValue) {

		Object result = null;
		Map<Object, Object> classEntities = entityCache.get(entityClass);
		if (classEntities != null) 
			result = classEntities.get(primaryKeyValue);

		return result;	
	}
	
	/**
	 * Return the entity instance of entityClass param identify by field and value params
	 * 
	 * @param entityClass - class of the entity
	 * @param fieldName - field used to find the entity instace
	 * @param value - value used to find the entity instace
	 */
	public Object getEntity(Class<?> entityClass, String fieldName, Object value) {

		Object result = null;
		EntityDef entityDef = EntityDefFactory.INSTANCE.getEntityDef(entityClass);
		Map<Object, Object> entities = entityCache.get(entityClass);

		Object fieldValue;
		if (entities != null)  {

			for (Object entity : entities.values()) {

				fieldValue = entityDef.getField(fieldName).getMappedFieldValue(entity);
				if (fieldValue != null && fieldValue.toString().equals(value.toString())) {
					result = entity;
					break;
				}
			}
		}

		return result;
	}
	
}