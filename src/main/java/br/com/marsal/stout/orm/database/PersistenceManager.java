package br.com.marsal.stout.orm.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;

import java.util.ArrayList;
import java.util.List;

import br.com.marsal.stout.orm.entity.EntityDef;
import br.com.marsal.stout.orm.entity.EntityDefFactory;
import br.com.marsal.stout.orm.enumeration.PersistenceOperation;
import br.com.marsal.stout.orm.exception.ExceptionUtils;
import br.com.marsal.stout.orm.field.FieldDef;

/**
 * Main SQLite access class.<br>
 * This will provider all database services and methods to access database in Android app.
 */
public final class PersistenceManager {
	
	/* ********************************************************
	 * PROPERTIES / CONSTANTS / INTERNAL VARIABLES
	 **********************************************************/
	
	/**	Provide a entity factory to get the {@link EntityDef} instance by entity class. */
	private EntityDefFactory mEntityDefFactory = EntityDefFactory.INSTANCE;
		
	/** Provide the SQLite connection ({@link SQLiteDatabaseAdapter}) to exec QUERY, DDL and DML statements. */
	private SQLiteDatabaseAdapter mSQLiteDatabaseAdapter = null;
	
	public String getDatabasePath() {
		return mSQLiteDatabaseAdapter.getDatabasePath();
	}

	/* ********************************************************
	 * CONSTRUCTORS / RELEASE
	 **********************************************************/

	/** Default constructor. This is responsible for create and initialize all database manager resources. */
	public PersistenceManager(Context context, PersistenceProperties.PersistenceUnit pu) {
		//create a new one SQLiteDatabaseAdapter and open it... a new one database is created in the first time that 'open()' is called
		mSQLiteDatabaseAdapter = new SQLiteDatabaseAdapter(context, pu);
	}
	
	/** */
	public void release() {
		mSQLiteDatabaseAdapter.release();
	}
	
	/* ********************************************************
	 * OPEN/CLOSE TRANSACTION
	 **********************************************************/
	
	/** Wrapper to {@link SQLiteDatabaseAdapter#startTransaction()}. */
	public void startTransaction(){
		mSQLiteDatabaseAdapter.startTransaction();
	}
	
	/** Wrapper to {@link SQLiteDatabaseAdapter#endTransaction(boolean)}. */
	public void endTransaction(boolean commit) {
		mSQLiteDatabaseAdapter.endTransaction(commit);
	}
	
	/* ********************************************************
	 * INSERT / UPDATE / DELETE
	 **********************************************************/
	
	/** Insert the entity in database and return the new id generated */
	public void insert(Object entity)
    {
		//get the persistence entity for the entity class pass by param
		EntityDef entityDef = mEntityDefFactory.getEntityDef(entity.getClass());
		
		//start transaction for the current operation
		boolean commit = false;
		startTransaction();
		try {
			//get the old entity pk value to check if is necessary update it after insert
			Object oldPrimaryKeyValue = entityDef.getPrimaryKey().getMappedFieldValue(entity);
			
			//extract the entity values, insert it into database and get the new pk value
			Object newPrimaryKeyValue = mSQLiteDatabaseAdapter.insert(
					entityDef.getTableName(), 
					SQLiteHelper.getContentValues(entity, entityDef, PersistenceOperation.INSERT)).intValue();
			
			//if old pk value != new pk value update the entity pk with new value
			if (newPrimaryKeyValue != oldPrimaryKeyValue)
				entityDef.getPrimaryKey().setFieldValue(entity, newPrimaryKeyValue);
			
			//mark the current transaction to commit
			commit = true;
		} finally {
			//finally end the current transaction and commit it if marked to
			endTransaction(commit);
		}
	}
	
	/** Update the entity in database */
	public void update(Object entity)
    {
		//get the persistence entity for the entity class pass by param
		EntityDef entityDef = mEntityDefFactory.getEntityDef(entity.getClass());
		
		//start transaction for the current operation
		boolean commit = false;
		startTransaction();
		try {
			//extract the entity values and update it into database
			mSQLiteDatabaseAdapter.update(
					entityDef.getTableName(), 
					SQLiteHelper.getContentValues(entity, entityDef, PersistenceOperation.UPDATE), 
					SQLiteHelper.getPrimaryKeyWhereClause(entityDef.getPrimaryKey(), entityDef.getPrimaryKey().getMappedFieldValue(entity)));
			
			//mark the current sQLiteDatabaseAdapter to commit
			commit = true;
		} finally {
			//finally end the current sQLiteDatabaseAdapter and commit it if marked to
			endTransaction(commit);
		}
	}

	/** Delete the entity from database */
	public void delete(Object entity)
    {
		//get the persistence entity for the entity class pass by param
		EntityDef entityDef = mEntityDefFactory.getEntityDef(entity.getClass());
		
		//start transaction for the current operation
		boolean commit = false;
		startTransaction();
		try {
			//extract the entity values and remove it into database
			mSQLiteDatabaseAdapter.delete(
					entityDef.getTableName(), 
					SQLiteHelper.getPrimaryKeyWhereClause(entityDef.getPrimaryKey(), entityDef.getPrimaryKey().getMappedFieldValue(entity)));
			
			//mark the current sQLiteDatabaseAdapter to commit
			commit = true;
		} finally {
			//finally end the current sQLiteDatabaseAdapter and commit it if marked to
			endTransaction(commit);
		}
	}
	
	/* **********************************************************
	 * ATTACH | DETACH DATABASES
	 ************************************************************/
	
	/**
	 * Use this to select a particular database, and after this command, all SQLite statements will be executed under the attached database.<br>
	 * Consider a case when you have multiple databases available and you want to use any one of them at a time.
	 * 
	 * <b>WARNING.</b> Is necessary to control open and close database connection.
	 */
	public void attach(String databasePath, String alias)
    {
		mSQLiteDatabaseAdapter.execSQL("ATTACH DATABASE '" + databasePath + "' as '" + alias + "'");
	}
	
	/**
	 * Use this to detach and dissociate a named database from a database connection which was previously attached using ATTACH statement.<br> 
	 * If the same database file has been attached with multiple aliases, then DETACH command will disconnect only given name and rest of the attachement will still continue.<p> 
	 *
	 * If the database is an in-memory or temporary database, the database will be destroyed and the contents will be lost.<p>
	 * 
	 * <b>WARNING.</b> Is necessary to control open and close database connection.
	 */
	public void detach(String alias)
    {
		mSQLiteDatabaseAdapter.execSQL("DETACH DATABASE '" + alias + "'");
	}
	
	/* **********************************************************
	 * QUERY STATEMENTS / DDL STATEMENTS / DML STATEMENTS
	 ************************************************************/
	
	/**
	 * Method to exec a QUERY statement and return only one instance using a custom WHERE clause.
	 * whereClause param must be formated as on SQL WHERE clause (excluding the WHERE itself). If this param is null or empty will be throwed one RuntimeException. 
	 */
	public <T> T queryWithSingleResult(EntityDef entityDef, String whereClause)
    {
		//check if where clause is null... if true throw a exception
		if (whereClause == null || whereClause == "")
			throw ExceptionUtils.newRuntimeException(getClass(), "The \"where clause\" param cannot be null/empty to find one instance of entityClass.");
		
		//return variable
		T result = null;
		
		//start transaction for the current operation
		boolean commit = false;
		startTransaction();
		try {
			//exec the statement and extract the single result from cursor
			Cursor cursor = mSQLiteDatabaseAdapter.query(
				entityDef.getTableName(), 
				entityDef.getColumnsToQuery(), 
				whereClause,
				entityDef.getPrimaryKey().getColumnName());
			result = extractSingleResultFromCursor(cursor, entityDef);
			
			// make sure to close the cursor and mark the current transaction to commit 
			cursor.close();
			commit = true;
		} finally {
			//finally end the current transaction and commit it if marked to
			endTransaction(commit);
		}
		return result;		
	}
	
	/** Method to exec a QUERY statement and return one list with all instances of entityClass found using a custom WHERE clause. */
	public <T> List<T> queryWithResultList(EntityDef entityDef, String whereClause)
    {
		//initiate the variable to return
		List<T> result = null;
	
		//start transaction for the current operation
		boolean commit = false;
		startTransaction();
		try {
			//exec the statement and extract the single result from cursor
			Cursor cursor = mSQLiteDatabaseAdapter.query(
					entityDef.getTableName(),
			    	entityDef.getColumnsToQuery(),
					whereClause,
					entityDef.getPrimaryKey().getColumnName());
			result = extractResultListFromCursor(cursor, entityDef);
			
			// make sure to close the cursor and mark the current transaction to commit 
			cursor.close();
			commit = true;
		} finally {
			//finally end the current sQLiteDatabaseAdapter and commit it if marked to
			endTransaction(commit);
		}
		return result;	
	}
	
	/**
	 * Execute a SELECT statement that return a single result entity.<br>
	 * 
	 * <b>WARNING.</b> {@code statement}  param must not be terminated with ";".
	 */
	public <T> T rawQueryWithSingleResult(Class<T> entityClass, String statement)
    {
		//initialize the return variable and get the persistence entity for the entity class pass by param 
		T result = null;
		EntityDef entityDef = mEntityDefFactory.getEntityDef(entityClass);
	
		//start transaction for the current operation
		boolean commit = false;
		startTransaction();
		try {
		    //exec the statement and extract the single result from cursor
			Cursor cursor = mSQLiteDatabaseAdapter.rawQuery(statement);
			result = extractSingleResultFromCursor(cursor, entityDef);
			
			// make sure to close the cursor and mark the current transaction to commit 
			cursor.close();
			commit = true;
		} finally {
			//finally end the current sQLiteDatabaseAdapter and commit it if marked to
			endTransaction(commit);
		}
		return result;	
	}
	
	/**
	 * Execute a SELECT statement that return a entity result list.
	 * <b>WARNING.</b> {@code statement} param must not be terminated with ";".
	 */
	public <T> List<T> rawQueryWithResultList(Class<T> entityClass, String statement)
    {
		//initialize the return variable and get the persistence entity for the entity class pass by param
		List<T> result = null;
		EntityDef entityDef = mEntityDefFactory.getEntityDef(entityClass);
	
		//start transaction for the current operation
		boolean commit = false;
		startTransaction();
		try {
		    //exec the statement and extract the single result from cursor
			Cursor cursor = mSQLiteDatabaseAdapter.rawQuery(statement);
			result = extractResultListFromCursor(cursor, entityDef);
			
			// make sure to close the cursor and mark the current transaction to commit 
			cursor.close();
			commit = true;
		} finally {
			//finally end the current sQLiteDatabaseAdapter and commit it if marked to
			endTransaction(commit);
		}
		return result;	
	}
	
	/**
	 * Execute a SELECT statement that return a raw result (Android SQLite Cursor). This result will be handled by caller.<p>
	 * 
	 * <b>WARNING.</b> The {@link Cursor} result must be closed manually after used it using {@link Cursor#close()} mehtod and to control open and close database connection. <br>
	 * {@code statement} param must not be terminated with ";".
	 */
	public Cursor rawQuery(String statement)
    {
		//exec the statement and return the cursor result
		return mSQLiteDatabaseAdapter.rawQuery(statement);
	}
	
	/**
	 * Execute a single SQL statement that is NOT a SELECT or any other SQL statement that returns data.<p>
	 * 
	 * It has no means to return any data (such as the number of affected rows). Instead, you're encouraged to use 
	 * insert(...), update(...), et al, when possible.
	 * 
	 * Multiple statements separated by semicolons are not supported.
	 */
	public void execSQL(String statement)
	{
//		System.out.println("[execSQL] " + statement);
	
		//start transaction for the current operation
		boolean commit = false;
		startTransaction();
		try {
		    //exec the statement
			mSQLiteDatabaseAdapter.execSQL(statement);
			
			//mark the current sQLiteDatabaseAdapter to commit
			commit = true;
		} finally {
			//finally end the current sQLiteDatabaseAdapter and commit it if marked to
			endTransaction(commit);
		}
	}
	
	/* **********************************************************
	 * EXTRACT QUERY RESULT
	 ************************************************************/
	
	/** Get the cursor value and extract it into a new instance of {@link EntityDef}. If cursor has more than one result one new {@link SQLiteException} will be throwed. */
	private <T> T extractSingleResultFromCursor(Cursor cursor, EntityDef entityDef)
    {
		//to extrack the single result the curosr must have only one row... if has more than one a exception are throwed 
		T entity = null;
		if (cursor.getCount() > 0) {
			//
			if (cursor.getCount() > 1)
				throw new SQLiteException("The sql statement return more than one result");

			//get the result and cast to entity
			cursor.moveToFirst();
			entity = cursorRowToEntity(cursor, entityDef);
		}
		return entity;
	}
	
	/** Get the cursor value and extract it into a new instance of {@link EntityDef}. */
	private <T> List<T> extractResultListFromCursor(Cursor cursor, EntityDef entityDef) {
		//
		List<Object> entities = new ArrayList<Object>();
		if (cursor.getCount() > 0) {
			//
			cursor.moveToFirst();
			while (!cursor.isAfterLast()) {
				entities.add(cursorRowToEntity(cursor, entityDef));
				cursor.moveToNext();
			}
		}
		
		@SuppressWarnings("unchecked")
		List<T> finalEntities = (List<T>) entities;
		return finalEntities;
	}

	/**
	 * Receive one {@link Cursor} and extrac its current row into a new instance of {@link EntityDef}. 
	 * Each field type (standards and relationship) will be treated in a different way, following these rules:
	 * 
	 * <li><b>standard fields:</b> All standards fields will be insert in their specifics fields in entity instance. 
	 * In the end of this step the entity result will be cached in the current transaction.
	 * <br><br>
	 * <li><b>Relationship fields:</b> These fields must be loaded from database, but before exec a query
	 * in database we check if the target entity already is in current transaction cache. Is yes only put it into 
	 * specific field; if not get the entity from database, put it into a field and cache it in a current transaction.
	 * <br><br>
	 */
	private <T> T cursorRowToEntity(Cursor cursor, EntityDef entityDef) {

		//create a new instance of entityClass to get all cursor values and bind his fields with values
		@SuppressWarnings("unchecked")
		T entity = (T) entityDef.newInstance();
		bindFields(entity, cursor, entityDef);
				
		return entity;
	}
	
	/** Populate all entity standard fields with their respective values from current row cursor. */
	private <T> void bindFields(T entity, Cursor cursor, EntityDef entityDef) {
		//
		Object value = null;
		for (FieldDef field : entityDef.getFields()) {
			//get the cursor column value using the field column name and if its is not null put it into specific entity field 
			value = SQLiteHelper.getCursorColumnValue(cursor, cursor.getColumnIndex(field.getColumnName()));
			if (value != null)
				field.setFieldValue(entity, value);
		}
		//finally put the entity into transaction cache
		mSQLiteDatabaseAdapter.cacheEntity(entity);
	}
	
}