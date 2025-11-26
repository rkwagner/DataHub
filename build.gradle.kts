plugins {
    id("jacoco")
}

// Centralized Java version (defined in gradle/libs.versions.toml)
val javaVersion = "17"

allprojects {
    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "java-library")
    apply(plugin = "checkstyle")
    apply(plugin = "jacoco")

    configure<JavaPluginExtension> {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(javaVersion))
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
    configurations.configureEach {
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
        finalizedBy(tasks.named("jacocoTestReport"))

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

    tasks.named<JacocoReport>("jacocoTestReport") {
        dependsOn(tasks.named("test"))
        reports {
            xml.required.set(true)
            html.required.set(true)
            csv.required.set(true)
        }
        
        doLast {
            val reportDir = reports.html.outputLocation.get().asFile
            println("\nJacoco Report: ${reportDir.toURI()}index.html")
            
            val csvFile = reports.csv.outputLocation.get().asFile
            if (csvFile.exists()) {
                val lines = csvFile.readLines()
                if (lines.size > 1) {
                    var missed = 0
                    var covered = 0
                    // Skip header (line 0)
                    for (i in 1 until lines.size) {
                        val columns = lines[i].split(",")
                        // INSTRUCTION_MISSED is index 3, INSTRUCTION_COVERED is index 4
                        if (columns.size > 4) {
                            missed += columns[3].toIntOrNull() ?: 0
                            covered += columns[4].toIntOrNull() ?: 0
                        }
                    }
                    val total = missed + covered
                    val percentage = if (total > 0) (covered.toDouble() / total) * 100 else 0.0
                    println(String.format("Total Instruction Coverage: %.2f%%", percentage))
                }
            }
        }
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

// Task to merge all coverage reports into one
tasks.register<JacocoReport>("jacocoRootReport") {
    dependsOn(subprojects.map { it.tasks.named("jacocoTestReport") })
    dependsOn(subprojects.map { it.tasks.named("test") })

    // Collect source directories and class outputs from all subprojects
    val mainSrcDirs = subprojects.map { project ->
        project.extensions.getByType<JavaPluginExtension>().sourceSets.getByName("main").allSource.srcDirs
    }
    val mainOutputs = subprojects.map { project ->
        project.extensions.getByType<JavaPluginExtension>().sourceSets.getByName("main").output
    }

    additionalSourceDirs.setFrom(mainSrcDirs)
    sourceDirectories.setFrom(mainSrcDirs)
    classDirectories.setFrom(mainOutputs)
    executionData.setFrom(project.fileTree(".") {
        include("**/build/jacoco/test.exec")
    })

    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(true)
    }
    
    doLast {
        val reportDir = reports.html.outputLocation.get().asFile
        println("\nCombined Jacoco Report: ${reportDir.toURI()}index.html")
    }
}
