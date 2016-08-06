package br.com.marsal.stout.orm.entity;

import android.content.Context;
import android.database.Cursor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.com.marsal.stout.orm.database.PersistenceManager;
import br.com.marsal.stout.orm.database.PersistenceProperties;
import br.com.marsal.stout.orm.database.SQLiteHelper;

/**
 * Used to interact with the persistence context.<p>
 * 
 * A persistence context is a set of entity instances in which for any persistent entity identity there is a unique entity instance.
 * The EntityManager API is used to create and remove persistent entity instances, to find entities by their primary key and to query over entities.
 */
public class EntityManager
{
	/* *********************************************************************************************
	 * SINGLETON PROPERTIES
	 * *********************************************************************************************/
	
	/** Indicates that the entity manager singleton has been initialized. */
	private static boolean mInitialized = false;
	
	/** Android context ({@link Context}) */
	private static Context mContext = null;
	
	/** Store all Entities Managers instances initialized in the current app execution */
	private static Map<String, EntityManager> mInstances = new HashMap<>();
	
	/**	Provide a entity factory to get the {@link EntityDef} instance by entity class. */
	private static final EntityDefFactory mEntityDefFactory = EntityDefFactory.INSTANCE;
	
	/** Provide the database properties ({@link PersistenceProperties}) like database file name, database version, mapped classes ecc. */
	private static final PersistenceProperties mPersistenceProperties = PersistenceProperties.INSTANCE;
	
	/* *********************************************************************************************
	 * ENTITY MANAGER PROPERTIES
	 * *********************************************************************************************/

	/** Provide the database access to exec all database operations like find, insert, update, exec raw query ecc. */
	private PersistenceManager mPersistenceManager = null;
	
	/** */
	private PersistenceProperties.PersistenceUnit mPersistenceUnit = null;

	/** @return	the database path associated to this persistence context */
	public String getDatabasePath() {
		//just a wraper to PersistenceManager.getDatabasePath()
		return mPersistenceManager.getDatabasePath();
	}
	
	/** @return	the persistence unit */
	public PersistenceProperties.PersistenceUnit getPersistenceUnit() {
		return mPersistenceUnit;
	}

	/* *********************************************************************************************
	 * SINGLETON
	 * *********************************************************************************************/
	
    /** Return an {@link EntityManager} instance associated to the specified persistenceUnit. */
    public synchronized static EntityManager getInstance(String persistenceUnit)
    {
//    	Log.d(EntityManager.class.getSimpleName(), "[getInstance] persistenceUnit: '" + persistenceUnit + "'");
    	
    	//first check if is initialized... if not throw a new exception
    	if (!mInitialized) {
			throw new RuntimeException("EntityManager isn't initialized. Call 'EntityManager.init(context)' before 'EntityManager.getInstance()'");
		}
    	//
    	EntityManager instance = mInstances.get(persistenceUnit);
    	if (instance == null) {
    		instance = new EntityManager(persistenceUnit);
    		mInstances.put(persistenceUnit, instance);
    	}
    	return instance;
    }
    
    public static void releaseInstance(EntityManager entityManager)
    {
//    	Log.d(EntityManager.class.getSimpleName(), "[releaseInstance] persistenceUnit: '" + persistenceUnit + "'");
    	
    	if (mInstances.containsKey(entityManager.getPersistenceUnit().getUnitName())) {
    		mInstances.remove(entityManager.getPersistenceUnit().getUnitName());
    		entityManager.release();
    		entityManager = null;
    	}
    }
    
    /* *********************************************************************************************
	 * INITIALIZATION
	 * *********************************************************************************************/
    
	/** This must be the first method of this class to be called and is responsible to initalize and configure it */
	public static void init(Context context)
    {
		//initialize all persistent context resources
		mContext = context;
		mPersistenceProperties.init(mContext);
		mEntityDefFactory.init(mPersistenceProperties.getMappedClasses());
		mInitialized = true;
	}
	
    /* *********************************************************************************************
	 * CONSTRUCTOR
	 * *********************************************************************************************/
    
    /** To avoid create a new one without using the singleton strategy */
    private EntityManager() {}

	/** Create a new one instance of Entity Manager and configure it. */
	private EntityManager(String persistenceUnit)
    {
		//create the new persistence manager
		mPersistenceUnit = mPersistenceProperties.getPersistenceUnit(persistenceUnit);
		mPersistenceManager = new PersistenceManager(mContext, mPersistenceUnit);
	}
	
    /* *********************************************************************************************
	 * RELEASE
	 * *********************************************************************************************/
	
	private void release()
    {
		mPersistenceManager.release();
	}
	
	/* *********************************************************************************************
	 * PERSIST(INSERT) / MERGE(UPDATE) / REMOVE(DELETE)
	 * *********************************************************************************************/

	/**
	 * Persist the entity instance into database table.
	 * This is similar to insert action
	 */
	public void persist(Object entity)
    {
		//wrapper to persistenceManager.insert(entity) to save entity instance into database
		mPersistenceManager.insert(entity);
	}
	
	/**
	 * Merge the current state of entity instance to database.
	 * This is the similar to update action. 
	 */
	public void merge(Object entity)
    {
		//wrapper to persistenceManager.update(entity) to merge the entity instance with entity register in database
		mPersistenceManager.update(entity);
	}
	
	/** Remove the entity instance from database */
	public void remove(Object entity)
    {
		//wrapper to persistenceManager.delete(entity) to remove the entity register from database
		mPersistenceManager.delete(entity);
	}
	
	/* *********************************************************************************************
	 * TRANSACTION MANAGER
	 * *********************************************************************************************/
	
	/** Wrapper to {@link PersistenceManager#startTransaction()}. */
	public void startTransaction()
    {
		mPersistenceManager.startTransaction();
	}
	
	/** Wrapper to {@link PersistenceManager#endTransaction(boolean)}. */
	public void endTransaction(boolean commit)
    {
		mPersistenceManager.endTransaction(commit);
	}
	
	/* *********************************************************************************************
	 * FIND / FIND ALL / RAW QUERY / DDL / DML STATEMENTS
	 * *********************************************************************************************/
	
	/*  QUERY WITH SINGLE RESULT */
	
	/** Find the instance of entityClass saved in database identify by primary key value. */
	public <T> T find(Class<T> entityClass, Object primaryKeyValue)
    {
		//get the persistence entity for the entity class pass by param
		EntityDef entityDef = mEntityDefFactory.getEntityDef(entityClass);
		
		//wrapper to persistenceManager.queryWithSingleResult(EntityDef, String)
		return mPersistenceManager.queryWithSingleResult(entityDef, SQLiteHelper.getPrimaryKeyWhereClause(entityDef.getPrimaryKey(), primaryKeyValue));
	}
	
	/**
	 * Method to exec a QUERY statement and return only one instance using a custom WHERE clause. <br>
	 * whereClause param can not be the WHERE word. If this param is null or empty will be throwed one RuntimeException.
	 */
	public <T> T queryWithSingleResult(Class<T> entityClass, String whereClause)
    {
		//get the persistence entity for the entity class pass by param
		EntityDef entityDef = mEntityDefFactory.getEntityDef(entityClass);
		
		//wrapper to persistenceManager.queryWithSingleResult(EntityDef, String)
        return mPersistenceManager.queryWithSingleResult(entityDef, whereClause);
	}
	
	/**
	 * Execute a SELECT statement that return a single result entity.<br>
	 * Statement param cannot be finished with ";"
	 */
	public <T> T rawQueryWithSingleResult(Class<T> entityClass, String statement)
    {
		//wrapper to persistenceManager.rawQueryWithSingleResult(Class<T>, String)
		return mPersistenceManager.rawQueryWithSingleResult(entityClass, statement);
	}
	
	/* QUERY WITH RESULT LIST */
	
	/** Find all instances of entityClass param. */
	public <T> List<T> findAll(Class<T> entityClass)
    {
		//get the persistence entity for the entity class pass by param
		EntityDef entityDef = mEntityDefFactory.getEntityDef(entityClass);
			
		//wrapper to persistenceManager.queryWithResultList(EntityDef, String)
		return mPersistenceManager.queryWithResultList(entityDef, null);
	}
	
	/** Method to exec a QUERY statement and return one list with all instances of entityClass found using a custom WHERE clause. */
	public <T> List<T> queryWithResultList(Class<T> entityClass, String whereClause)
    {
		//get the persistence entity for the entity class pass by param
		EntityDef entityDef = mEntityDefFactory.getEntityDef(entityClass);
				
		//wrapper to persistenceManager.queryWithResultList(EntityDef, String)
		return mPersistenceManager.queryWithResultList(entityDef, whereClause);
	}
	
	/**
	 * Execute a SELECT statement that return a entity result list.
	 * Statement param cannot be finished with ";"
	 */
	public <T> List<T> rawQueryWithResultList(Class<T> entityClass, String statement)
    {
		// wrapper to persistenceManager.rawQueryWithResultList(Class<T>, String)
		return mPersistenceManager.rawQueryWithResultList(entityClass, statement);
	}
	
	/**
	 * Execute a SELECT statement that return a raw result (Android SQLite Cursor). This result will be handled by caller.<p>
	 * 
	 * <b>WARNING.</b> The {@link Cursor} result must be closed manually after used it. <br>
	 * <pre><b>Example</b><br>
	 * 
	 * //get the cursor result
	 * Cursor cursor = EntityManager.INSTANCE.execRawQuery("select id from Tag");
	 * try {
	 *   //do something
	 * } finally {
	 *   // make sure to close the cursor
	 *   cursor.close();
	 * } </pre>
	 *  
	 * Statement param cannot be finished with ";"
	 */
	public Cursor rawQuery(String statement)
    {
		// wrapper to persistenceManager.rawQuery(String)
		return mPersistenceManager.rawQuery(statement);
	}

	/**
	 * Execute a single SQL statement that is NOT a SELECT or any other SQL statement that returns data.
	 * It has no means to return any data (such as the number of affected rows).
	 *  
	 * Instead, you're encouraged to use insert(String, String, ContentValues), update(String, ContentValues, String, String[]), et al, when possible.
	 */
	public void execSQL(String statement)
    {
//		Log.e(this.getClass().getSimpleName(), "[execSQL] statement: " + statement);
		//wrapper to persistenceManager.execSQL(String)
		mPersistenceManager.execSQL(statement);
	}
	
	/** */
	public void attach(String databasePath, String alias)
    {
		mPersistenceManager.attach(databasePath, alias);
	}
	
	/** */
	public void detach(String alias)
    {
		mPersistenceManager.detach(alias);
	}
	
}