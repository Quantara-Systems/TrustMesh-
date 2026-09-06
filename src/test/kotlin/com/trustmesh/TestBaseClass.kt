package com.trustmesh

import org.junit.jupiter.api.BeforeEach
import org.mockito.MockitoAnnotations

/**
 * Base test class providing common setup and utilities for all unit tests.
 */
abstract class TestBaseClass {

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
    }

    /**
     * Helper method to create test data
     */
    protected fun createTestData(): Map<String, String> {
        return mapOf(
            "id" to "test-id-123",
            "name" to "Test User",
            "email" to "test@example.com"
        )
    }
}
