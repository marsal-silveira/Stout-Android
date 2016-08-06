package br.com.marsal.stout.orm.exception;

/**
 * Custom RuntimeException to persistence framework context.
 *
 * @see RuntimeException
 */
public class PersistenceRuntimeException extends RuntimeException {
	private static final long serialVersionUID = 5349936402335749635L;

	/**
     * Constructs a new exception with {@code null} as its detail message.
     * The cause is not initialized, and may subsequently be initialized by a
     * call to {@link #initCause}.
     */
    public PersistenceRuntimeException() {
        super();
    }

    /**
     * Constructs a new exception with the specified detail message.  The
     * cause is not initialized, and may subsequently be initialized by
     * a call to {@link #initCause}.
     *
     * @param   message   the detail message. The detail message is saved for
     *          later retrieval by the {@link #getMessage()} method.
     */
    public PersistenceRuntimeException(String message) {
        super(message);
    }
	
}