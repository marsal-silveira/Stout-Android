package br.com.marsal.stout.orm.exception;

public class EInvalidMappedEntity extends Exception{
	private static final long serialVersionUID = 4974102015406525996L;

	/**
     * Constructs a new exception with {@code null} as its detail message.
     * The cause is not initialized, and may subsequently be initialized by a
     * call to {@link #initCause}.
     */
    public EInvalidMappedEntity() {
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
    public EInvalidMappedEntity(String message) {
        super(message);
    }
	
}