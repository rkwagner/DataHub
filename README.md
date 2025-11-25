# DataHub

**DataHub** is a modular metadata and DDL generation library designed to standardize table definitions across various data platforms (Hudi, etc.).

> **⚠️ NOTICE**: This repository is currently **CLOSED SOURCE** and under active development. It is not ready for external use or distribution.

## Project Structure

- **`core`**: Foundational interfaces (`Table`, `Schema`, `Field`) and reusable metadata definitions.
- **`hudi`**: Hudi-specific implementations, properties management, and DDL generation logic.
- **`api`**: (Planned) REST API definitions for metadata management.
- **`schema`**: (Planned) Schema registry integrations and converters.
- **`orchestration`**: (Planned) Airflow/Dagster integration patterns.
- **`observability`**: (Planned) Data quality and lineage tracking.

## Prerequisites

- JDK 17+
- Gradle (wrapper provided)

## Building the Project

To build the project and run tests:

```bash
./gradlew build
```

## Code Style

This project enforces code style using **Checkstyle**. Violations will cause the build to fail.
To run checks explicitly:

```bash
./gradlew check
```
