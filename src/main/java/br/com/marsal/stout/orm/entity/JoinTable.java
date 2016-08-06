package br.com.marsal.stout.orm.entity;

import br.com.marsal.stout.orm.database.SQLiteHelper;

public class JoinTable {
	
	/* ********************************************************
	 * PROPERTIES / CONSTANTS / INTERNAL VARIABLES
	 **********************************************************/
	
	/** Specifies the join table name. */
	private String name = "";
	public String getName() {return name;}
	
	/** Specifies the join column (owning side) mapped in {@link #JoinTable}. */
	private String joinColumn = "";
	public String getJoinColumn() {return joinColumn;}
	
	/** Specifies the inverse join column (non-owning side) mapped in {@link #JoinTable}. */
	private String inverseJoinColumn = "";
	public String getInverseJoinColumn() {return inverseJoinColumn;}
	
	/** Create table statement used to create join talbe into database. */
	private String createTableStatement = "";
	public String getCreateTableStatement() {return createTableStatement;}
	
	/* ********************************************************
	 * CONSTRUCTOR
	 **********************************************************/
	
	/** Empty constructor. This is necessary to avoid create a new instance without uses the complete constructor */
	protected JoinTable() {}
	
	/** Complete constructor. All instance must be created using this constructor to guarantee that all properties will be correctly initialized. */
	public JoinTable(String name, String joinColumn, String inverseJoinColumn) {

		this.name = name;
		this.joinColumn = joinColumn;
		this.inverseJoinColumn = inverseJoinColumn;
		
		//now create the create table statement
		createTableStatement = String.format(SQLiteHelper.CREATE_JOIN_TABLE, name, joinColumn, inverseJoinColumn, joinColumn, inverseJoinColumn);
	}
	
}