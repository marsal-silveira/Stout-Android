package br.com.marsal.stout.orm.exception;

public class EPrimaryKeyNotFound extends Exception{
	private static final long serialVersionUID = -6934599676871090838L;

	/**
     * Constructs a new exception with {@code null} as its detail message.
     * The cause is not initialized, and may subsequently be initialized by a
     * call to {@link #initCause}.
     */
    public EPrimaryKeyNotFound() {
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
    public EPrimaryKeyNotFound(String message) {
        super(message);
    }
	
}