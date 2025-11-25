package datahub.core.exceptions;

/**
 * Custom exception for reporting failures related to schema rules or
 * constraints.
 */
public class SchemaValidationException extends RuntimeException {
    public SchemaValidationException(String message) {
        super(message);
    }
}