package br.com.marsal.stout.orm.enumeration;

/**
 * Defines the possibles SQLite datatypes.<p> 
 * 
 * In SQLite, the datatype of a value is associated with the value itself, not with its container. 
 * The dynamic type system of SQLite is backwards compatible with the more common static type systems 
 * of other database engines in the sense that SQL statements that work on statically typed databases 
 * should work the same way in SQLite. 
 * However, the dynamic typing in SQLite allows it to do things which are not possible in traditional rigidly typed databases. <p>
 * 
 * Some datatypes does not have support on SQLite. For this cases the framework will following these rules:
 * <li><b>Boolean Datatype:</b> Booleans values are stored as {@code INTEGER} value 0 ({@code false}) or 1 ({@code true}).
 * <li><b>Data and Time Datatype:</b> These datatypes will be storing as {@code TEXT}, {@code REAL} or {@code INTEGER} values.<br>
 * <b>(source: www.sqlite.org)</b><br><br>
 */
public enum SQLiteDataType {
	
	/** The value is NULL value. */
	NULL,
	
	/** The value isa signed integer, stored in 1, 2, 3, 4, 5, 6, or 8 bytes 
	 *  depending on the magnitude of the value. */
	INTEGER,
	
	/** The value is a floating point value, stored as an 8-byte IEEE floating point number .*/
	REAL,
	
	/** The value is a text string, stored using the database encoding (UTF-e, UTF-16BE or UTF-16LE). */
	TEXT,
	
	/** The value is a blob data, stored exactly as it was input. */
	BLOB

}