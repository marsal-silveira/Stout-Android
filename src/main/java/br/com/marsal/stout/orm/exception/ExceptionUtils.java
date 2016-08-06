package br.com.marsal.stout.orm.exception;

/**
 * Class helper to aux with system exceptions like throws and log exceptions
 */
public abstract class ExceptionUtils {
	
	/**
	 * Log the error message and throws one new {@link PersistenceRuntimeException} 
	 * 
	 * @param invokeClass - class when occurs the exception to be logged
	 * @param errorMsg - message describing the exception
	 * @return the new RuntimeExpection 
	 */
	public static RuntimeException newRuntimeException(Class<?> invokeClass, String errorMsg) {

//		Log.e(invokeClass.getSimpleName(), errorMsg);
		return new RuntimeException(errorMsg);
	}
	
	/**
	 * Log the exception message and throws one new {@link PersistenceRuntimeException} 
	 * 
	 * @param invokeClass - class when occurs the exception to be logged
	 * @param exception - the exception instance that represents the error
	 * @return the new RuntimeExpection 
	 */	
	public static RuntimeException newRuntimeException(Class<?> invokeClass, Throwable exception) {

//		Log.e(invokeClass.getSimpleName(), exception.getMessage());
//		exception.printStackTrace(); //TODO temporary
		return new RuntimeException(exception);
	}
	
}