package datahub.core.services;

import datahub.core.contract.DBType;
import datahub.core.contract.Table;

/**
 * Interface for services that generate DDL (Data Definition Language)
 * statements.
 *
 * @param <T> The specific database type (e.g., HudiType).
 */
public interface DdlGenerator<T extends DBType> {

    /**
     * Generates a 'CREATE TABLE' statement for the given table definition.
     *
     * @param table The table definition.
     * @return The DDL string.
     */
    String generateCreateStatement(Table<T> table);
}
