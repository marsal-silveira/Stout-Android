package br.com.marsal.stout.orm.exception;

public class EPersistenceContextNotInitialized extends Exception{
	private static final long serialVersionUID = -1105281739328170963L;

	/**
     * Constructs a new exception with {@code null} as its detail message.
     * The cause is not initialized, and may subsequently be initialized by a
     * call to {@link #initCause}.
     */
    public EPersistenceContextNotInitialized() {
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
    public EPersistenceContextNotInitialized(String message) {
        super(message);
    }
	
}