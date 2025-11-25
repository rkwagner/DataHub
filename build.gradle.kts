subprojects {
    apply(plugin = "java-library")
    apply(plugin = "checkstyle")

    repositories {
        mavenCentral()
    }

    configure<JavaPluginExtension> {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(17))
        }
    }

    val log4jVersion = "2.23.1"
    val avroVersion = "1.11.3"

    dependencies {
        // Logging implementation (put on implementation scope for all modules)
        "implementation"("org.apache.logging.log4j:log4j-api:$log4jVersion")
        "implementation"("org.apache.logging.log4j:log4j-core:$log4jVersion")
        "implementation"("org.apache.logging.log4j:log4j-slf4j2-impl:$log4jVersion")

        // Test dependencies
        "testImplementation"("org.junit.jupiter:junit-jupiter:5.12.1")
        "testRuntimeOnly"("org.junit.platform:junit-platform-launcher")

        "implementation"("org.apache.avro:avro:$avroVersion")
        "implementation"("org.apache.avro:avro-compiler:$avroVersion")
    }

    // Fallback: Apply the exclusion outside the dependencies block if the above fails
    // This is often required when mixing Groovy/Kotlin closure styles in a subprojects block.
    configurations.all {
        resolutionStrategy {
            // Force Log4j's SLF4J 2 implementation over the old slf4j-log4j12
            eachDependency {
                if (requested.group == "org.slf4j" && requested.name == "slf4j-log4j12") {
                    // Replace the old, conflicting 1.7.x binding with the new 2.x bridge.
                    // However, the simplest fix is often just to exclude the old binding entirely,
                    // as we explicitly added the correct 'log4j-slf4j2-impl' above.
                    useTarget("org.slf4j:slf4j-api:2.0.12") // Just ensure API version is new/compatible
                }
            }
            
            // Explicitly excluding the conflicting artifact name globally is the most reliable way.
            // This is equivalent to your previous logic, but in a more robust location.
            exclude(group = "org.slf4j", module = "slf4j-log4j12")
        }
    }

    tasks.named<Test>("test") {
        useJUnitPlatform()

        testLogging {
            events("passed", "skipped", "failed", "standardOut", "standardError")
            showExceptions = true
            showCauses = true
            showStackTraces = true
            exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        }

        afterSuite(KotlinClosure2({ desc: TestDescriptor, result: TestResult ->
            if (desc.parent == null) {
                println("\nTest Result: ${result.resultType}")
                println("Summary: ${result.testCount} tests, " +
                        "${result.successfulTestCount} succeeded, " +
                        "${result.failedTestCount} failed, " +
                        "${result.skippedTestCount} skipped")
            }
            null
        }))
        }

    configure<CheckstyleExtension> {
        configFile = rootProject.file("config/checkstyle/checkstyle.xml")
        toolVersion = "10.12.5"
        // Fail the build if checkstyle errors are found
        isIgnoreFailures = false
        // Show violations in the console
        isShowViolations = true
    }
}

