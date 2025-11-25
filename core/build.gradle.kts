plugins {
    `java-library`
    `java-test-fixtures`
}

// Define versions consistently (must match hudi/build.gradle.kts)
val scalaVersion = "2.12"
val sparkVersion = "3.5.1" 
val hudiVersion = "1.1.0"
val junitVersion = "5.12.1"

dependencies {
    // Core has no internal dependencies yet
    testImplementation("org.apache.hudi:hudi-spark3.5-bundle_$scalaVersion:$hudiVersion")
    testImplementation("org.apache.spark:spark-core_$scalaVersion:$sparkVersion")
    
    // We also add JUnit here for completeness, though it's likely pulled in by the root build:
    testImplementation("org.junit.jupiter:junit-jupiter:$junitVersion")
}
