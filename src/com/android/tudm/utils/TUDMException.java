package com.android.tudm.utils;

/**
 * Extended Exception Class for CCM specific exception handling.
 */
public class TUDMException extends Exception {

    private static final long serialVersionUID = 1L;

    /** No error. */
    public static final int NO_ERROR = -1;

    /** Any exception that does not specify a specific issue */
    public static final int UNSPECIFIED_EXCEPTION = 0;

    /** IO Exception. */
    public static final int IO_EXCEPTION = 1;

    /** Illegal arguments Exception. */
    public static final int ILLEGAL_ARGUMENTS_EXCEPTION = 2;

    /** Activity not found Exception. */
    public static final int ACTIVITY_NOT_FOUND_EXCEPTION = 3;

    /** Security Exception when using device policy manager. */
    public static final int SECURITY_EXCEPTION = 4;

    /** Certificate Exception. */
    public static final int CERTIFICATE_EXCEPTION = 5;

    /** JSON Exception. */
    public static final int JSON_EXCEPTION = 6;

    /** Protocol Exception. */
    public static final int PROTOCOL_EXCEPTION = 7;

    /** IllegaState Exception. */
    public static final int ILLEGAL_STATE_EXCEPTION = 8;

    protected int mExceptionType;

    public TUDMException(String message) {
        super(message);
        mExceptionType = UNSPECIFIED_EXCEPTION;
    }

    public TUDMException(String message, Throwable throwable) {
        super(message, throwable);
        mExceptionType = UNSPECIFIED_EXCEPTION;
    }

    /**
     * Constructs a CCMException with an exceptionType and a null message.
     * @param exceptionType The exception type to set for this exception.
     */
    public TUDMException(int exceptionType) {
        super();
        mExceptionType = exceptionType;
    }

    /**
     * Constructs a CCMException with an exceptionType and a message.
     * @param exceptionType The exception type to set for this exception.
     */
    public TUDMException(int exceptionType, String message) {
        super(message);
        mExceptionType = exceptionType;
    }

    /**
     * Return the exception type. Will be OTHER_EXCEPTION if not explicitly set.
     * @return Returns the exception type.
     */
    public int getExceptionType() {
        return mExceptionType;
    }
}
