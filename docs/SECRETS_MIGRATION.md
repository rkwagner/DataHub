# Configuration Migration Plan: Git Secrets

This document outlines which configuration values should be moved to GitHub Actions secrets or environment variables for production deployments.

## Values to Move to Git Secrets (Production)

### MinIO / S3 Credentials
**Current Location:** `gradle/libs.versions.toml`

| Variable Name | Current Value | Git Secret Name | Notes |
|--------------|---------------|-----------------|-------|
| `minio-access-key` | `minio` | `MINIO_ACCESS_KEY` or `S3_ACCESS_KEY` | S3-compatible access key |
| `minio-secret-key` | `minio123` | `MINIO_SECRET_KEY` or `S3_SECRET_KEY` | S3-compatible secret key |
| `minio-endpoint` | `http://host.docker.internal:9000` | `MINIO_ENDPOINT` or `S3_ENDPOINT` | S3-compatible endpoint URL |

### Database Credentials (Future)
**Note:** Not yet implemented, but will be needed for:
- Hive Metastore database connection
- Trino/Presto catalog connections
- Any application databases

| Variable Name | Git Secret Name | Notes |
|--------------|-----------------|-------|
| N/A | `HIVE_METASTORE_DB_PASSWORD` | PostgreSQL password for Hive Metastore |
| N/A | `TRINO_DB_PASSWORD` | If Trino needs database access |

## Values to Keep as Environment Variables (Non-Secret)

### MinIO Configuration (Non-Sensitive)
**Current Location:** `gradle/libs.versions.toml`

| Variable Name | Current Value | Environment Variable | Notes |
|--------------|---------------|---------------------|-------|
| `minio-container-name` | `minio-datahub` | `MINIO_CONTAINER_NAME` | Docker container name |
| `minio-image` | `minio/minio` | `MINIO_IMAGE` | Docker image to use |
| `minio-bucket-name` | `hudi-data` | `MINIO_BUCKET_NAME` or `S3_BUCKET_NAME` | Default bucket name |
| `minio-port` | `9000` | `MINIO_PORT` | Port for MinIO service |

## Implementation Strategy

### Phase 1: Local Development (Current)
- ✅ All values centralized in `gradle/libs.versions.toml`
- ✅ Environment variable overrides supported in `Spark.java` and `example/build.gradle.kts`
- ✅ Defaults provided for local development

### Phase 2: CI/CD (GitHub Actions)
1. **Create GitHub Secrets:**
   - Add secrets to repository settings
   - Use in GitHub Actions workflows via `${{ secrets.SECRET_NAME }}`

2. **Update Workflows:**
   ```yaml
   env:
     MINIO_ACCESS_KEY: ${{ secrets.MINIO_ACCESS_KEY }}
     MINIO_SECRET_KEY: ${{ secrets.MINIO_SECRET_KEY }}
     MINIO_ENDPOINT: ${{ secrets.MINIO_ENDPOINT }}
   ```

3. **Gradle Integration:**
   - Environment variables are already supported
   - No code changes needed

### Phase 3: Production Deployment
1. **Remove Defaults from `libs.versions.toml`:**
   - Keep structure but remove actual secret values
   - Document required environment variables in README

2. **Use External Secret Management:**
   - AWS Secrets Manager
   - Azure Key Vault
   - HashiCorp Vault
   - Kubernetes Secrets

## Current Implementation Status

### ✅ Completed
- Centralized all versions in `gradle/libs.versions.toml`
- Added environment variable support in `Spark.java`:
  - `MINIO_ENDPOINT` (default: `http://host.docker.internal:9000`)
  - `MINIO_ACCESS_KEY` (default: `minio`)
  - `MINIO_SECRET_KEY` (default: `minio123`)
- Added environment variable support in `example/build.gradle.kts` for test tasks
- All dependency versions centralized (Spark, Hadoop, Hudi, etc.)
- Java version centralized

### 🔄 Next Steps
1. Create `.env.example` file with all required environment variables
2. Update README with environment variable documentation
3. Create GitHub Actions workflow that uses secrets
4. Add validation to ensure required secrets are set in production

## Security Best Practices

1. **Never commit secrets to version control**
   - Use `.gitignore` for `.env` files
   - Rotate secrets regularly

2. **Use different secrets for different environments**
   - Development
   - Staging
   - Production

3. **Principle of least privilege**
   - Grant minimal necessary permissions
   - Use separate credentials for different services

4. **Audit and monitoring**
   - Log secret access (not values!)
   - Monitor for unauthorized access
   - Set up alerts for suspicious activity
