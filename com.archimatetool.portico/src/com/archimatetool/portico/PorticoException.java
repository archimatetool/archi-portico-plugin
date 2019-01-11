/**
 * This program and the accompanying materials
 * are made available under the terms of the License
 * which accompanies this distribution in the file LICENSE.txt
 */
package com.archimatetool.portico;




/**
 * Portico Exception
 * 
 * @author Phillip Beauvoir
 */
public class PorticoException extends Exception {
    
    /**
     * Constructs a {@code PorticoException} with {@code null}
     * as its error detail message.
     */
    public PorticoException() {
        super();
    }

    /**
     * Constructs a {@code PorticoException} with the specified detail message.
     *
     * @param message
     *        The detail message (which is saved for later retrieval
     *        by the {@link #getMessage()} method)
     */
    public PorticoException(String message) {
        super(message);
    }

    /**
     * Constructs a {@code PorticoException} with the specified detail message
     * and cause.
     *
     * @param message
     *        The detail message (which is saved for later retrieval
     *        by the {@link #getMessage()} method)
     *
     * @param cause
     *        The cause (which is saved for later retrieval by the
     *        {@link #getCause()} method).  (A null value is permitted,
     *        and indicates that the cause is nonexistent or unknown.)
     *
     */
    public PorticoException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a {@code PorticoException} with the specified cause and a
     * detail message of {@code (cause==null ? null : cause.toString())}
     * (which typically contains the class and detail message of {@code cause}).
     * This constructor is useful for exceptions that are little more
     * than wrappers for other throwables.
     *
     * @param cause
     *        The cause (which is saved for later retrieval by the
     *        {@link #getCause()} method).  (A null value is permitted,
     *        and indicates that the cause is nonexistent or unknown.)
     *
     */
    public PorticoException(Throwable cause) {
        super(cause);
    }
}
