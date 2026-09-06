package com.trustmesh.integration

import org.junit.jupiter.api.DisplayName
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

/**
 * Base class for integration tests using TestContainers.
 * Sets up PostgreSQL database container for testing.
 */
@Testcontainers
@DisplayName("Integration Test Base")
abstract class IntegrationTestBase {

    companion object {
        @Container
        private val postgreSQLContainer = PostgreSQLContainer<Nothing>("postgres:15-alpine").apply {
            withDatabaseName("trustmesh_test")
            withUsername("test_user")
            withPassword("test_password")
        }

        init {
            System.setProperty("spring.datasource.url", postgreSQLContainer.jdbcUrl)
            System.setProperty("spring.datasource.username", postgreSQLContainer.username)
            System.setProperty("spring.datasource.password", postgreSQLContainer.password)
            System.setProperty("spring.datasource.driver-class-name", "org.postgresql.Driver")
        }
    }
}
