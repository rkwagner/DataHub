dependencies {
    implementation(project(":core"))
    
    // Apache Hudi dependencies for configuration classes
    implementation("org.apache.hudi:hudi-common:1.0.0")
    
    testImplementation(testFixtures(project(":core")))
}
