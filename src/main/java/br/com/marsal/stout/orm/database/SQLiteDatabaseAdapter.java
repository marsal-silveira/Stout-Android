package br.com.marsal.stout.orm.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import br.com.marsal.stout.orm.entity.EntityDef;
import br.com.marsal.stout.orm.entity.JoinTable;

/**
 * Manager all SQLite transaction
 */
public final class SQLiteDatabaseAdapter extends SQLiteOpenHelper
{
	/* *********************************************************************************************
	 * PROPERTIES / CONSTANTS / INTERNAL VARIABLES
	 * *********************************************************************************************/

	/** Provide the SQLite connection ({@link SQLiteDatabase}) to exec QUERY, DDL and DML statements */
	private SQLiteDatabase mDatabase = null;

	/** Full database path */
	private String mDatabasePath = null;
	public String getDatabasePath() {return mDatabasePath;}
	
	/** */
	private PersistenceProperties.PersistenceUnit mPersistenceUnit = null;
	
	/** Android context ({@link Context}) */
	private Context mContext = null;

	/**
	 * Flag used to controll the persistence transaction.<br>
	 * Ever time when {@link #startTransaction()} is called this flag will be incremented by one. 
	 * When {@link #endTransaction(boolean)} is called this will be decremented by one.
	 */
	private int mTransactionFlag = 0;

	/** Represents the transaction cache when all entities loaded in the current transaction will be cached to be reused before. */
	private TransactionCache mTransactionCache;

	/* *********************************************************************************************
	 * CONSTRUCTORS
	 * *********************************************************************************************/

	/** Default constructor. This is responsible for create and initialize all database resources. */
	public SQLiteDatabaseAdapter(Context context, PersistenceProperties.PersistenceUnit pu)
    {
		super(context, pu.getDatabaseName() + ".db", null, pu.getDatabaseVersion());
		mContext = context;
		mPersistenceUnit = pu;
		mTransactionFlag = 0;
		mTransactionCache = new TransactionCache();
		mDatabasePath = mContext.getDatabasePath(pu.getDatabaseName() + ".db").getAbsolutePath();

		//in the first time that open() method is called the database is created or updated... so we force this behavior here to create database 
		open();
		close();
	}
	
	/* *********************************************************************************************
	 * DATABASE CREATE / UPDATE / RELEASE
	 * *********************************************************************************************/

	/** Called when the database is created for the first time. There is where the creation of tables should happen. */
	@Override
	public void onCreate(SQLiteDatabase sqliteDatabase)
    {
//		Log.d(getClass().getSimpleName(), "[onCreate] '" + mPersistenceUnit.getDatabaseName() + "'(" + mPersistenceUnit.isAutoCreateDatabase() + ")");
		
		//if persistence context is auto-create database create all tables associated... otherwise do nothing 
		if (mPersistenceUnit.isAutoCreateDatabase()) {
			
			//create all entities tables and join tables from all persistence unit mapped entities
			for (EntityDef entityDef : mPersistenceUnit.getMappedEntitiesDef()) {
				//entity table
//				Log.d(getClass().getSimpleName(), "[onCreate][Table] " + entityDef.getCreateTableStatement());
				sqliteDatabase.execSQL(entityDef.getCreateTableStatement());
				
				//entity join tables
				if (entityDef.getJoinTables() != null) {
					for (JoinTable joinTable : entityDef.getJoinTables().values()) {
//						Log.d(getClass().getSimpleName(), "[onCreate][JoinTable] " + joinTable.getCreateTableStatement());
						sqliteDatabase.execSQL(joinTable.getCreateTableStatement());
					}
				}
			}
		} 
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase sqliteDatabase, int oldVer, int newVer)
    {
//		Log.d(getClass().getSimpleName(), "[onUpgrade] updating '" + mPersistenceUnit.getUnitName() + "." + mPersistenceUnit.getDatabaseName() + "' database from '" + oldVer + "' to '" + newVer + "'");
		// TODO check this code when this use case is necessary...
		// OrmLiteSqliteOpenHelper required that this method has a implementation...

		// Log.w(self.getClass().getSimpleName(), "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
		// db.execSQL("DROP TABLE IF EXISTS " + TABLE_COMMENTS);
		// onCreate(db);
	}
	
	public void release()
    {
		close();
	}

	/* *********************************************************************************************
	 * OPEN/CLOSE DATABASE
	 * *********************************************************************************************/

	/**
	 * Create and/or open a database that will be used for reading and writing.
	 * The first time this is called, the database will be opened and onCreate(), onUpgrade() and/or onOpen() will be called.
	 * <p>
	 * Once opened successfully, the database is cached, so you can call this method every time you need to write to the database. 
	 * (Make sure to call closeDatabase() when you no longer need the database) 
	 * Errors such as bad permissions or a full disk may cause this method to fail, but future attempts may succeed if the problem is fixed.
	 */
	public void open()
    {
//		Log.d(getClass().getSimpleName(), "[open] '" + mPersistenceUnit.getDatabaseName() + "'@'" + mDatabase + "'");
		
		// to avoid open when database already is opened
		if (mDatabase == null || !mDatabase.isOpen())
			mDatabase = getWritableDatabase();
	}

	/** Close any open database and transaction */
	@Override
	public void close()
    {
//		Log.d(getClass().getSimpleName(), "[close] '" + mPersistenceUnit.getDatabaseName() + "'@'" + mDatabase + "'");

		// to avoid close when database already is closed
		if (mDatabase != null) {
			super.close();
			mDatabase.close();
			mDatabase = null;
		}
		// reset the transaction flag and cache (if necessary)
		if (mTransactionFlag > 0) {
			mTransactionFlag = 0;
			clearTransactionCache();
		}
	}
	
	/* *********************************************************************************************
	 * OPEN/CLOSE TRANSACTION
	 * *********************************************************************************************/
	
	/**
	 * Begins a transaction in EXCLUSIVE mode.
	 * <p>
	 * 
	 * Transactions can be nested. When the outer transaction is ended all of the work done in that transaction and all of 
	 * the nested transactions will be committed or rolled back. 
	 * The changes will be commited when {@link #endTransaction(boolean)} is called passing {@code true} in any transaction. 
	 * Otherwise they will be rolled back.
	 * <p>
	 * 
	 * <pre>
	 * <b>Here is the standard idiom for transactions manager:</b>
	 * 
	 * // On method body 
	 * boolean commit = false;
	 * persisteneManager.startTransaction();
	 * try {
	 *     ...do something...
	 *     commit = true;
	 * } finally {
	 *     persistenceManager.endTransaction(commit);
	 * }
	 * </pre>
	 */
	public void startTransaction()
    {
		// increments the transaction flag and start a transaction if already has not been started
		mTransactionFlag++;
		if (mTransactionFlag == 1) {
			open();
			mDatabase.beginTransaction();
		}
	}

	/**
	 * End the current transaction and clear the cache.
	 * 
	 * @see {@link #startTransaction()} for notes about how to use this and when transactions are committed and rolled back.
	 */
	public void endTransaction(boolean commit)
    {
		// decrements the flag and end the current transaction if flag == 0
		mTransactionFlag--;
		if (mTransactionFlag == 0) {

			// confirma all data if marked to commit
			if (commit)
				mDatabase.setTransactionSuccessful();

			// finally end the current transaction, clear cache and close the database
			mDatabase.endTransaction();
			clearTransactionCache();
			close();
		}
	}

	/** Clear all cached data in the current transaction. This is automatically called when the current transaction is ended. */
	public void clearTransactionCache()
    {
		mTransactionCache.clearCache();
	}
	
	/* *********************************************************************************************
	 * TRANSACTION CACHE
	 * *********************************************************************************************/

	/**
	 * Put a entity into a current transaction cache. 
	 * If any process in the same transaction needs this entity the framewrod dont need call the database to get it.
	 * 
	 * @param entity - the object to be cached in the current transaction.
	 */
	public void cacheEntity(Object entity)
    {
		mTransactionCache.putEntity(entity);
	}

	/**
	 * Return the cached entity (instance of) identify by his primary key or null if he isn't.
	 * 
	 * @param entityClass - entity class definition
	 * @param primaryKeyValue - value used to find the entity
	 */
	public Object getCachedEntity(Class<?> entityClass, Object primaryKeyValue)
    {
		return mTransactionCache.getEntity(entityClass, primaryKeyValue);
	}

	/**
	 * Return the cached entity (instance of) identify by field param or null if he isn't.
	 * 
	 * @param entityClass - entity class definition
	 * @param fieldName - field usud to find the entity
	 * @param value - field value used to find the entity
	 */
	public Object getCachedEntity(Class<?> entityClass, String fieldName, Object value)
    {
		return mTransactionCache.getEntity(entityClass, fieldName, value);
	}

	/* ********************************************************
	 * INSERT / UPDATE / DELETE
	 **********************************************************/

	/**
	 * Exec the INSERT statement in SQLite database and return the new id generated.
	 * 
	 * @param table - table name to insert the row into
	 * @param values - map containing the column values for the row. The keys should be the column names and values the column values
	 * @return the entity Id of the newly inserted entity, or -1 if an error occurred
	 */
	public Long insert(String table, ContentValues values)
    {
		return mDatabase.insert(table, null, values);
	}

	/**
	 * Exec the UPDATE statement in SQLite database and return the number of entities affected
	 * 
	 * @param table - table name to update in
	 * @param values - map containing the column values for the row. The keys should be the column names and values the column values
	 * @param whereClause - the custom where clause to be inserted in UPDATE statement
	 * @return the number of entities affected
	 */
	public int update(String table, ContentValues values, String whereClause)
    {
		return mDatabase.update(table, values, whereClause, null);
	}

	/**
	 * Exec the DELETE statement in SQLite database and return the number of entities affected
	 * 
	 * @param table - table name to delete from
	 * @param whereClause - the custom where clause to be inserted in DELETE statement
	 * @return the number of entities affected
	 */
	public int delete(String table, String whereClause)
    {
		return mDatabase.delete(table, whereClause, null);
	}

	/* ********************************************************
	 * RAW QUERY / DDL STATEMENT / DML STATEMENT
	 **********************************************************/

	/**
	 * Query the given table, returning a {@link Cursor} over the result set.
	 * 
	 * @param table - table name to compile the quey against
	 * @param columns - a list a wich columns to return
	 * @param whereClause - a filter declaring which rows to return, formated as on SQL WHERE clause (excluding the WHERE itself). Passing null will returnt all rows for the given table.
	 * @param orderBy - how to order the rows, formatted as an SQL ORDER BY clause (excluding the ORDER BY itself). Passing null will use the default sort order, which may be unordered.
	 * @return A {@link Cursor} object, which is positioned before the first entry. Note that Cursors are not synchronized, see the documentation for more details.
	 */
	public Cursor query(String table, String[] columns, String whereClause, String orderBy)
    {
		return mDatabase.query(table, columns, whereClause, null, null, null, orderBy);
	}

	/**
	 * Runs the provided SQL and returns a {@link Cursor} over the result set.
	 * 
	 * @param statement - the SQL query. The SQL string must not be ";" terminated
	 * @return a {@link Cursor} object, which is positioned before the first entry. Note that Cursors are not synchronized, see the documentation for more details.
	 */
	public Cursor rawQuery(String statement)
    {
		return mDatabase.rawQuery(statement, null);
	}

	/**
	 * Execute a single SQL statement that is NOT a SELECT or any other SQL statement that returns data.
	 * <p>
	 * 
	 * It has no means to return any data (such as the number of affected rows). 
	 * Instead, you're encouraged to use insert(String, String, ContentValues), update(String, ContentValues, String, String[]), et al, when possible.
	 * 
	 * @param statement - the SQL statement to be executed. Multiple statements separated by semicolons are not supported.
	 */
	public void execSQL(String statement)
    {
		mDatabase.execSQL(statement);
	}
	
}