package br.com.marsal.stout.orm.field;

import java.util.ArrayList;
import java.util.Iterator;

public class FieldDefList<E extends FieldDef> extends ArrayList<E> {
	private static final long serialVersionUID = -3571345562350141236L;
	
	/** Extract all fields/columns from this list and build a string separated with comma. This is used in SQLite statements. */
	public String toColumnsCommaSeparated() {
		
		//for each field element get their mapped column name and built one string with all of them.  
		String columns = "";
		E field;
		for (Iterator<E> iterator = iterator(); iterator.hasNext();) {
			//
			field = (E) iterator.next();
			columns += field.getColumnName().concat(",");  
		}
		
		//remove the last character (",")
		columns = columns.substring(0, columns.length()-1);
			
		return columns;
	}
	
	/** Return the String array with all mapped field columns. This is used in SQLite statements. */
	public final String[] toColumnsArray(){

		//initiate the string array with field list size
		String[] result = new String[size()];
		
		E field;
		int i = 0;
		for (Iterator<E> iterator = iterator(); iterator.hasNext();) {
			//
			field = (E) iterator.next();
			result[i] = field.getColumnName();
			i++;
		}
		
		return result;
	}
	
	/** Check if the object passed by param still is in the list and add it if not. */
	public boolean addIfNotContains(E object) {
		//
		boolean result = false;
		if (!contains(object))
			result = super.add(object);
		return result;
	}
	
}