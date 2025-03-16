package com.beachape.quiam.domain.errors;

/**
 * A custom exception class for domain-specific exceptions that are meant to be caught (checked).
 * This exception class overrides the {@link Throwable#fillInStackTrace()} method to avoid the
 * performance penalty of filling in the stack trace.
 */
public abstract class DomainException extends Exception {

  public DomainException() {
    super();
  }

  public DomainException(String message) {
    super(message);
  }

  public DomainException(String message, Throwable cause) {
    super(message, cause);
  }

  @Override
  public synchronized Throwable fillInStackTrace() {
    return this;
  }
}
