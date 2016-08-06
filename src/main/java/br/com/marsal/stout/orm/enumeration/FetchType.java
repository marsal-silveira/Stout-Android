package br.com.marsal.stout.orm.enumeration;

/**
 * Defines strategies for fetching data from the database. 
 * The EAGER strategy is a requirement on the persistence provider runtime that data must be eagerly fetched. 
 * The LAZY strategy is a hint to the persistence provider runtime that data should be fetched lazily when it is first accessed. 
 * The implementation is permitted to eagerly fetch data for which the LAZY strategy hint has been specified.<p>
 * 
 * <b> WARNING</b> LAZY IS NOT IMPLEMENTED<p>
 * If any field uses fetch type equals {@link FetchType#LAZY} this field will not be loaded automatically by the framework. 
 * These fields must be loaded manually and on demand by the developer using one specific DAO.  
 */
//TODO not all implemented yet. Check how to implements LAZY behavior... maybe instrumentation or aspect ???
public enum FetchType {
	
	/** Defines that data must be eagerly fetched. */
	EAGER,
	
	/** Defines that data can be lazily fetched. */
	LAZY

}