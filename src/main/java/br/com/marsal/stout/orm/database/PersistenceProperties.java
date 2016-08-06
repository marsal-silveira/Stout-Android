package br.com.marsal.stout.orm.database;

import android.content.Context;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.com.marsal.stout.orm.entity.EntityDef;
import br.com.marsal.stout.orm.entity.EntityDefFactory;
import br.com.marsal.stout.orm.annotation.Entity;
import br.com.marsal.stout.orm.exception.EInvalidMappedEntity;
import br.com.marsal.stout.orm.exception.ExceptionUtils;

/**
 * Manages all persistence context property defined on "Persistence.xml" file located on app "assets" folder. 
 * This file defines some database properties like database name, database version, mapped classes ecc.
 */
public enum PersistenceProperties
{
	INSTANCE; // instance of enum to access the singleton...
	
	/* *********************************************************************************************
	 * INNER CLASS / PERSISTENCE UNIT
	 * *********************************************************************************************/

	public class PersistenceUnit
	{
		/* *****************************************************************************************
		 * PROPERTIES / CONSTANTS / INTERNAL VARIABLES
		 * *****************************************************************************************/

		/** Provide a entity factory to get the {@link EntityDef} instance by entity class. */
		private EntityDefFactory mEntityDefFactory = EntityDefFactory.INSTANCE;
		
		private String unitName = null;
		public String getUnitName() {return unitName;}
		public void setUnitName(String unitName) {this.unitName = unitName;}
		
		private String databaseName = null;
		public String getDatabaseName() {return databaseName;}
		public void setDatabaseName(String databaseName) {this.databaseName = databaseName;}
		
		private int databaseVersion = 0;
		public int getDatabaseVersion() {return databaseVersion;}
		public void setDatabaseVersion(int databaseVersion) {this.databaseVersion = databaseVersion;}
		
		private boolean autoCreateDatabase = false;
		public boolean isAutoCreateDatabase() {return autoCreateDatabase;}
		public void setAutoCreateDatabase(boolean autoCreateDatabase) {this.autoCreateDatabase = autoCreateDatabase;}
		
		private boolean useTransaction = false;
		public boolean isUseTransaction() {return useTransaction;}
		public void setUseTransaction(boolean useTransaction) {this.autoCreateDatabase = useTransaction;}
		
		private List<String> mappedEntities = new ArrayList<>();
		public List<String> getMappedEntities() {return mappedEntities;}
		public void addMappedEntity(String mappedEntity) {mappedEntities.add(mappedEntity);}
		
		/* *****************************************************************************************
		 * UTILS
		 * *****************************************************************************************/

		/** */
		public List<EntityDef> getMappedEntitiesDef()
		{
			List<EntityDef> result = new ArrayList<>();
			for (String entityClassSimpleName : mappedEntities)
				result.add(mEntityDefFactory.getEntityDef(entityClassSimpleName));
			return result;
		}
	}
	
	/* *********************************************************************************************
	 * PROPERTIES, CONSTANTS AND INTERNAL VARIABLES
	 * *********************************************************************************************/

	/** 
	 * File when android persistence properties are defined. 
	 * by default this file must be stored on assets aplication root path. 
	 */
	private final String PERSISTENCE_XML_FILE = "persistence.xml";
	private final int ATTRIBUTE_NAME_INDEX = 0;
	private final int ATTRIBUTE_VALUE_INDEX = 1;
	private final String PERSISTENCE_UNIT = "persistence-unit";
	private final String MAPPED_ENTITY = "mapped-entity";
	private final String ENTITIES_TAG = "entities";
	
	/** Indicates that the persistence properties has been initialized. */
	private boolean mInitialized = false;
	
	/** Store all persistence units defined in "persistence.xml" file configuration. */
	private Map<String, PersistenceUnit> mPersistenceUnits = new HashMap<>(); 

	/** List of mapped classes annoted with {@link Entity}. This classes will be controled by framework. */
	private List<Class<?>> mMappedClasses = new ArrayList<Class<?>>();
	
	/* Getters and Setters */

	/** @return the value stored in {@link #mMappedClasses} property. */
	public final List<Class<?>> getMappedClasses() {
		return mMappedClasses;
	}
	
	/** @return the persistence unit associated to key equals "name". */
	public final PersistenceUnit getPersistenceUnit(String name) {
		return mPersistenceUnits.get(name);
	}
	
	/* *********************************************************************************************
	 * INITIALIZATIONS
	 * *********************************************************************************************/

	/**
	 * Init method responsible to get all properties defined in "persistence.xml" file.
	 */
	public void init(Context context)
	{
		//only if not already initialized
		try {
			if (!mInitialized) {
				loadProperties(context);
				mInitialized = true;
			}
		} catch (IOException | XmlPullParserException e) {
			throw ExceptionUtils.newRuntimeException(getClass(), "Can't initialize persistence context. Details: " + e.getMessage());
		}
	}
	
	/* *********************************************************************************************
	 * LOAD XML PROPERTIES
	 * *********************************************************************************************/
	
	/** Responsible to extract all persistence properties and mapped class list. */
	private final void loadProperties(Context context) throws IOException, XmlPullParserException
	{
//		Log.d(getClass().getSimpleName(), "[loadProperties]");
		
		//create ResourceParser for XML file
		InputStream istr = context.getAssets().open(PERSISTENCE_XML_FILE);
		XmlPullParserFactory factory = XmlPullParserFactory.newInstance(); 
		factory.setNamespaceAware(true); 
		XmlPullParser xpp = factory.newPullParser(); 
		xpp.setInput(istr, "UTF-8");
		
        //for each element check it to get all properties and mapped classes
        String mappedClassName;
        Class<?> mappedClass;
        int eventType = xpp.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
        	
        	//root tag... 
        	if (eventType == XmlPullParser.START_TAG) {
        		
        		//<persistence-unit> tag... get the persistence properties like database name and version
            	if (xpp.getName().equals(PERSISTENCE_UNIT)) {
            		
        			//create an new persistence unit and put it into pu list
        			PersistenceUnit pu = new PersistenceUnit();
        			pu.setUnitName(xpp.getAttributeValue(ATTRIBUTE_NAME_INDEX));
        			mPersistenceUnits.put(pu.getUnitName(), pu);
        			
        			//move to database name property
        			xpp.next();
        			xpp.next();
        			pu.setDatabaseName(xpp.getAttributeValue(ATTRIBUTE_VALUE_INDEX));
        			
        			//move to database version property
                	xpp.next();
                	xpp.next();
                	xpp.next();
                	pu.setDatabaseVersion(Integer.parseInt(xpp.getAttributeValue(ATTRIBUTE_VALUE_INDEX)));
                	
                	//move to auto-create property
                	xpp.next();
                	xpp.next();
                	xpp.next();
                	pu.setAutoCreateDatabase(Boolean.parseBoolean(xpp.getAttributeValue(ATTRIBUTE_VALUE_INDEX)));
                	
                	//extract all mapped entities (if nedded)
                	if (pu.isAutoCreateDatabase()) {
                		xpp.next();//property - END_TAG(3)
                    	xpp.next();//null
                    	xpp.next();//entities - START_TAG(2)
                    	xpp.next();//null
                		xpp.next();//entity - START_TAG(2)

                    	int event;
                    	String name;
                    	while (true) {
                    		xpp.next();//entity value - TEXT(4)
                    		pu.addMappedEntity(xpp.getText());
	                		xpp.next();//entity - END_TAG(3)
	                		xpp.next();//null
	                		
	                		event = xpp.next();//???
	                		name = xpp.getName();
                    		if (name != null && name.equals(ENTITIES_TAG) && event == XmlPullParser.END_TAG) {
								break;
							}
						}
                	}
        		//<mapped-class> tag... get all mapped classes
        		} else if (xpp.getName().equals(MAPPED_ENTITY)) {
        			//get the mapped class name and try find it into declared classes in JVM...
        			mappedClassName = xpp.getAttributeValue(ATTRIBUTE_VALUE_INDEX);
        			try {
        				//get the class identify by className defined on xml file and check if class is anonted with @Entity.
        				//only annoted classes can be defined as mapped class and controled by framework.
        				mappedClass = Class.forName(mappedClassName);
        				if (mappedClass.isAnnotationPresent(Entity.class)) {
							mMappedClasses.add(mappedClass);
						} else {
							throw ExceptionUtils.newRuntimeException(
									PersistenceProperties.class,
									new EInvalidMappedEntity("Class \"" + mappedClassName + "\" is presents in \"persistence.xml\" but isn't annoted with @Entity."));
						}
					} catch (ClassNotFoundException e) {
    					throw ExceptionUtils.newRuntimeException(
    							PersistenceProperties.class,
    							"Class \"" + mappedClassName + "\" defined on persistence properties file not found");
					}
        		}
          	}
        	//get next event
        	eventType = xpp.next();
        }

//        //log
//        PersistenceUnit pu;
//        for (String key : mPersistenceUnits.keySet()) {
//        	pu = mPersistenceUnits.get(key);
//        	Log.d(getClass().getSimpleName(), "[loadProperties] -------------------------------");
//        	Log.d(getClass().getSimpleName(), "[loadProperties] Persistence Unit = " + pu.getUnitName());
//        	Log.d(getClass().getSimpleName(), "[loadProperties] database name = " + pu.getDatabaseName());
//        	Log.d(getClass().getSimpleName(), "[loadProperties] database version = " + pu.getDatabaseVersion());
//        	Log.d(getClass().getSimpleName(), "[loadProperties] auto-create = " + pu.isAutoCreateDatabase());
//        	for (String clazz : pu.getMappedEntities()) {
//                Log.d(getClass().getSimpleName(), "[loadProperties] mapped entity = " + clazz);
//            }
//        }
//        Log.d(getClass().getSimpleName(), "[loadProperties] -------------------------------");
//        for (Class<?> clazz : mMappedClasses)
//        	Log.d(getClass().getSimpleName(), "[loadProperties] class = " + clazz);
    }
	
	/* *********************************************************************************************
	 * PERSISTENCE UNIT PROPERTIES
	 * *********************************************************************************************/
	
	/** @return the database name associated to the perssitence unit param. */
	public final String getDatabaseName(String persistenceUnit)
    {
		return mPersistenceUnits.get(persistenceUnit).databaseName;
	}

	/** @return the database version associated to the perssitence unit param. */
	public final int getDatabaseVersion(String persistenceUnit)
    {
		return mPersistenceUnits.get(persistenceUnit).databaseVersion;
	}

}