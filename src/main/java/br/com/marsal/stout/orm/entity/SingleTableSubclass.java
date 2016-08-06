package br.com.marsal.stout.orm.entity;

import java.util.ArrayList;
import java.util.List;

import br.com.marsal.stout.orm.annotation.Entity;
import br.com.marsal.stout.orm.annotation.MappedSuperclass;
import br.com.marsal.stout.orm.field.FieldDef;

public final class SingleTableSubclass extends EntityDef {
	
	/* ****************************************************** *
	 * PROPERTIES / CONSTANTS / INTERNAL VARIABLES
	 * ****************************************************** */
	
	/**	Provide a entity factory to get the {@link EntityDef} instance by entity class. */
	private EntityDefFactory entityDefFactory = EntityDefFactory.INSTANCE;
	
	private SingleTableSuperclass singleTableSuperclass = null;
	
	/* ****************************************************** *
	 * PROPERTIES GETTERS AND SETTERS
	 * ****************************************************** */
	
	public SingleTableSuperclass getSingleTableSuperclass() {
		return singleTableSuperclass;
	}

	public void setSingleTableSuperclass(SingleTableSuperclass singleTableSuperclass) {
		this.singleTableSuperclass = singleTableSuperclass;
	}
	
	/* ****************************************************** *
	 * CONSTRUCTORS
	 * ****************************************************** */

	/**
	 * Receive a {@link #entityClass} references and extract all their properties.
	 * 
	 * @param entityClass - entity class annoted with {@link Entity} or {@link MappedSuperclass}
	 */
	public SingleTableSubclass(Class<?> entityClass) {
		//call the superclass constructor
		super(entityClass);
	}
	
	/* ********************************************************
	 * INIT
	 **********************************************************/
	
	/**
	 * Extract entity fields and primary Key
	 */
	public void init() {
		
		//get the super class (single table) that has inheritance strategy equals SINGLE_TABLE and set yourself as his subclass
		singleTableSuperclass = SingleTableSuperclass.class.cast(entityDefFactory.getEntityDef(entityClass.getSuperclass()));
		singleTableSuperclass.addSubclass(this);
								
		//get the table name using the @Table.name if defined... this property is defined by his superclass (single table)
		Entity entity = singleTableSuperclass.getEntityClass().getAnnotation(Entity.class);
		tableName = entity.tableName() != "" ? entity.tableName() : entityClass.getSimpleName();

		//now call the super class init method to extract and prepare all fields 
		super.init();
	}

    public static final class SingleTableSuperclass extends EntityDef {

        /* ****************************************************** *
         * PROPERTIES / CONSTANTS / INTERNAL VARIABLES
         * ****************************************************** */

        private List<SingleTableSubclass> subclasses = new ArrayList<>();

        /* ****************************************************** *
         * CONSTRUCTORS
         * ****************************************************** */

        /**
         * Receive a {@link #entityClass} references and extract all their properties.
         *
         * @param entityClass - entity class annoted with {@link Entity} or {@link MappedSuperclass}
         */
        public SingleTableSuperclass(Class<?> entityClass) {
            //call the superclass constructor
            super(entityClass);
        }

        /* ****************************************************** *
         * SUBCLASSES
         * ****************************************************** */

        /**
         * Just add the subclass into list
         */
        public void addSubclass(SingleTableSubclass subclass) {
            subclasses.add(subclass);
        }

        /* ****************************************************** *
         * FIELDS AND COLUMNS
         * ****************************************************** */

        /**
         * After all entities definitions are finished... call this method to extract all subclasses fields.<br>
         * This is necessary because a single table is a union of all his fields and subclasses fields.
         */
        public void initSubclasses() {
    //		Log.d(getClass().getSimpleName(), "[initSubclasses] superclass/table = " + entityClass.getSimpleName() + "/" + tableName);

            //for each subclass extract his fields and insert its into fields list... super class must have all subclasses fields (SINGLE TABLE)
            for (SingleTableSubclass subclass : subclasses)
                for (FieldDef field : subclass.getFields())
                    fields.addIfNotContains(field);

    //		//log
    //		for (FieldDef field : fields)
    //			log("[extractAllSubclassesFields] field = " + field.getFieldName() + "/" + field.getFieldType().getSimpleName() + " | column = " + field.getColumnName() + "/" + field.getColumnType().getSimpleName());

            //re-build all fields list
            buildFieldLists();

            //now configure his subclasses overriding the subclass create fields list with superclass create fields list (SINGLE TABLE)
            for (SingleTableSubclass subclass : subclasses)
                subclass.fieldsToCreateTable = this.fieldsToCreateTable;

    //		//log
    //		for (SingleTableSubclass subclass : subclasses) {
    //			log("[extractAllSubclassesFields] subclass = " + subclass.getEntityClass().getSimpleName() + " ------- fields to create");
    //			for (FieldDef field : subclass.getFieldsToCreate())
    //				log("[extractAllSubclassesFields] field = " + field.getFieldName() + "/" + field.getFieldType().getSimpleName() + " | column = " + field.getColumnName() + "/" + field.getColumnType().getSimpleName());
    //		}
        }

    }
}