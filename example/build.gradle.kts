plugins {
    // We only need the Java library/app plugin to compile the code.
    // The main execution environment will be managed externally (via a script/spark-submit).
    id("java")
}

repositories {
    mavenCentral()
}

// Exclude conflicting SLF4J bindings globally
configurations.all {
    exclude(group = "org.apache.logging.log4j", module = "log4j-slf4j-impl")
    exclude(group = "org.slf4j", module = "slf4j-log4j12")
}

val sparkClasspath by configurations.creating {
    // This allows the configuration to be resolved into a set of files for the JavaExec task.
    isCanBeResolved = true 
    
    // Inherit the main runtime dependencies and the compile-only dependencies (Spark/Hadoop).
    extendsFrom(configurations.implementation.get())
    extendsFrom(configurations.compileOnly.get()) 
}

dependencies {
    // --- Test Dependencies ---
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation(libs.slf4j.simple)
    
    // Spark/Hadoop dependencies for Test compilation (since TableExample is in src/test)
    testImplementation(libs.spark.sql)
    testImplementation(libs.spark.hive) 
    testImplementation(libs.hadoop.aws)
    testImplementation(libs.hadoop.common) 
    testImplementation(libs.hadoop.client) 
    testImplementation(libs.aws.java.sdk.bundle)

    // --- Application Dependencies (Runtime by Cluster) ---
    // Use 'implementation' for your application code and library dependencies.
    implementation(libs.guava)
    implementation(libs.slf4j.api)
    implementation(project(":hudi"))
    implementation(project(":core"))

    // Hudi bundle must be bundled with the application code since the cluster doesn't provide it
    implementation(libs.hudi.spark.bundle)
    
    // This makes your final JAR small, as it doesn't include the 100MB+ of Spark/Hadoop.
    compileOnly(libs.spark.sql)
    compileOnly(libs.spark.hive) 
    compileOnly(libs.hadoop.aws)
    compileOnly(libs.hadoop.common) 
    compileOnly(libs.hadoop.client) 
    compileOnly(libs.aws.java.sdk.bundle)
}

// Read MinIO configuration from centralized version catalog
val minioContainerName = libs.versions.minio.container.name.get()
val minioImage = libs.versions.minio.image.get()
val minioBucketName = libs.versions.minio.bucket.name.get()
val minioPort = libs.versions.minio.port.get()
val minioAccessKey = libs.versions.minio.access.key.get()
val minioSecretKey = libs.versions.minio.secret.key.get()
val minioEndpoint = libs.versions.minio.endpoint.get()

// --- MinIO Setup Tasks ---

tasks.register("minioRemoveOld", Exec::class) {
    group = "verification"
    commandLine("docker", "rm", "-f", minioContainerName)
    isIgnoreExitValue = true
}

tasks.register("minioRunContainer", Exec::class) {
    group = "verification"
    dependsOn("minioRemoveOld")
    
    commandLine(
        "docker", "run", "-d", 
        "--name", minioContainerName,
        "-p", "$minioPort:$minioPort",
        "-e", "MINIO_ROOT_USER=$minioAccessKey", 
        "-e", "MINIO_ROOT_PASSWORD=$minioSecretKey",
        minioImage,
        "server", "/data",
        "--address", "0.0.0.0:$minioPort" 
    )
    
    doLast {
        // Wait for MinIO to start
        Thread.sleep(5000) 
    }
}

tasks.register("minioCreateBucket", Exec::class) {
    group = "verification"
    dependsOn("minioRunContainer")
    
    commandLine(
        "docker", "exec", minioContainerName, 
        "sh", "-c", 
        "/usr/bin/mc alias set myminio http://localhost:$minioPort $minioAccessKey $minioSecretKey && " +
        "/usr/bin/mc mb myminio/$minioBucketName --ignore-existing"
    )
    isIgnoreExitValue = true
}

tasks.register("minioStop", Exec::class) {
    group = "verification"
    commandLine("docker", "rm", "-f", minioContainerName)
    isIgnoreExitValue = true
}

tasks.register("runExample", JavaExec::class) {

    // Lifecycle Hooks
    dependsOn("minioCreateBucket")
    finalizedBy("minioStop")

    // 1. Set the main class
    mainClass.set("datahub.TripsTableTest") // Updated to new test class name, though usually tests are run via 'test' task
    
    // 2. Set the classpath to include compiled code AND all dependencies (including compileOnly)
    // NOTE: This simulates the classpath that 'spark-submit' would assemble.
    classpath = sourceSets.test.get().output + sourceSets.main.get().output + configurations.getByName(sparkClasspath.name)
    
    // 3. Re-add all necessary Java 17 module access flags (CRITICAL FIX)
    // These tell the JVM to open up internal modules for Spark's legacy reflection usage.
    jvmArgs = listOf(
        "--add-opens=java.base/java.lang=ALL-UNNAMED",
        "--add-opens=java.base/java.lang.invoke=ALL-UNNAMED",
        "--add-opens=java.base/java.lang.reflect=ALL-UNNAMED",
        "--add-opens=java.base/java.io=ALL-UNNAMED",
        "--add-opens=java.base/java.nio=ALL-UNNAMED",
        "--add-opens=java.base/java.util=ALL-UNNAMED",
        "--add-opens=java.base/java.util.concurrent=ALL-UNNAMED",
        // Specific fix for sun.nio.ch (the cause of your IllegalAccessError)
        "--add-opens=java.base/sun.nio.ch=ALL-UNNAMED",
        "--add-opens=java.base/sun.misc=ALL-UNNAMED",
        "--add-opens=java.base/jdk.internal.misc=ALL-UNNAMED",
        
        // Include the Hadoop home path for Windows testing (optional but necessary for s3a)
        "-Djava.library.path=${project.rootDir}/hadoop/bin"
    )
    
    // Pass environment variables (allow override from system env)
    environment("MINIO_ENDPOINT", System.getenv("MINIO_ENDPOINT") ?: minioEndpoint)
    environment("MINIO_ACCESS_KEY", System.getenv("MINIO_ACCESS_KEY") ?: minioAccessKey)
    environment("MINIO_SECRET_KEY", System.getenv("MINIO_SECRET_KEY") ?: minioSecretKey)
}

// Configure test task to use the same JVM args and environment
tasks.test {
    // Only depend on MinIO Docker tasks if not running in CI
    // In CI, MinIO runs as a GitHub Actions service
    val isCI = System.getenv("CI") == "true"
    if (!isCI) {
        dependsOn("minioCreateBucket")
        finalizedBy("minioStop")
    }
    
    jvmArgs = listOf(
        "--add-opens=java.base/java.lang=ALL-UNNAMED",
        "--add-opens=java.base/java.lang.invoke=ALL-UNNAMED",
        "--add-opens=java.base/java.lang.reflect=ALL-UNNAMED",
        "--add-opens=java.base/java.io=ALL-UNNAMED",
        "--add-opens=java.base/java.nio=ALL-UNNAMED",
        "--add-opens=java.base/java.util=ALL-UNNAMED",
        "--add-opens=java.base/java.util.concurrent=ALL-UNNAMED",
        "--add-opens=java.base/sun.nio.ch=ALL-UNNAMED",
        "--add-opens=java.base/sun.misc=ALL-UNNAMED",
        "--add-opens=java.base/jdk.internal.misc=ALL-UNNAMED",
        "-Djava.library.path=${project.rootDir}/hadoop/bin"
    )
    
    // Pass environment variables (allow override from system env)
    environment("MINIO_ENDPOINT", System.getenv("MINIO_ENDPOINT") ?: minioEndpoint)
    environment("MINIO_ACCESS_KEY", System.getenv("MINIO_ACCESS_KEY") ?: minioAccessKey)
    environment("MINIO_SECRET_KEY", System.getenv("MINIO_SECRET_KEY") ?: minioSecretKey)
    
    testLogging {
        events("passed", "skipped", "failed")
        showStandardStreams = true
    }
}