// Testing dependencies
dependencies {
    // JUnit 5 (Jupiter)
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.3")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.3")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.9.3")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.9.3")
    
    // Mockito
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.1.0")
    testImplementation("org.mockito:mockito-core:5.3.1")
    testImplementation("org.mockito:mockito-junit-jupiter:5.3.1")
    
    // TestContainers for integration tests
    testImplementation("org.testcontainers:testcontainers:1.19.3")
    testImplementation("org.testcontainers:junit-jupiter:1.19.3")
    testImplementation("org.testcontainers:postgresql:1.19.3")
    
    // RestAssured for API testing
    testImplementation("io.rest-assured:rest-assured:5.3.2")
    
    // Kotest for Kotlin testing
    testImplementation("io.kotest:kotest-runner-junit5:5.6.2")
    testImplementation("io.kotest:kotest-assertions-core:5.6.2")
}

// JaCoCo Code Coverage Plugin
plugins {
    jacoco
}

jacoco {
    toolVersion = "0.8.10"
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    
    afterEvaluate {
        classDirectories.setFrom(files(classDirectories.files.map { directoryTree(dir: it, excludes: [
            "**/config/**",
            "**/entity/**",
            "**/dto/**"
        ]) }))
    }
    
    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }
}

tasks.test {
    finalizedBy(tasks.jacocoTestReport)
    useJUnitPlatform()
}

// Code Coverage Verification
tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
            element = "PACKAGE"
            excludes = listOf(
                "**/config/**",
                "**/entity/**",
                "**/dto/**"
            )
            limit {
                minimum = "0.70".toBigDecimal()
            }
        }
    }
}
