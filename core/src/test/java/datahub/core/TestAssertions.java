package datahub.core;

import static org.junit.jupiter.api.Assertions.*;

import java.util.function.Supplier;

/**
 * Provides reusable, context-aware assertion wrappers for testing expected
 * failure modes.
 * This ensures that diagnostic logs generated during expected failures are
 * contextualized
 * for the reviewer in the test output.
 */
public class TestAssertions {

    /**
     * Asserts that a validation method fails, while wrapping the expected failure
     * in a decorative output message for build review readability.
     * * This method assumes the validation utility (TestRowUtils) logs its internal
     * failure diagnostic (e.g., the missing field message) at a high level (ERROR).
     * * @param validationMethod A boolean result of the validation method (e.g.,
     * validateRowIntegrity(schema, row)).
     * 
     * @param failureDescription The custom assertion message for JUnit if the
     *                           validation surprisingly passes (i.e., the test
     *                           fails).
     * @param failureContext     A short description of the intentional failure
     *                           (e.g., "Missing PK ID").
     */
    public static void assertValidationFailsWithContext(
            Supplier<Boolean> validationSupplier,
            String failureDescription,
            String failureContext) {
        // 1. Print decorator message to STANDARD_OUT (cleans up Gradle output)
        System.out.println("\n--- Starting Expected Failure Test: " + failureContext + " ---");
        System.out.println("The validation utility will log an ERROR message immediately following this line.");

        // 2. Perform the actual assertion (asserting that the validation MUST be false)
        assertFalse(validationSupplier.get(), failureDescription);

        // 3. Print cleanup message to STANDARD_OUT
        System.out.println("--- Expected Failure Test Complete ---\n");
    }
}