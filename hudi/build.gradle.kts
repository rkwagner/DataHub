dependencies {
    implementation(project(":core"))
    
    // Apache Hudi dependencies for configuration classes
    // Use compileOnly because the runtime (App) will provide the full Hudi bundle
    compileOnly(libs.hudi.common)
    
    // Tests need the actual Hudi classes at runtime
    testImplementation(libs.hudi.common)
    testImplementation(testFixtures(project(":core")))
}
