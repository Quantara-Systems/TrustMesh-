# TrustMesh Testing Guide

## Overview

This document outlines the testing strategy and best practices for the TrustMesh project.

## Testing Framework Stack

- **JUnit 5 (Jupiter)**: Primary testing framework
- **Mockito + Mockito-Kotlin**: Mocking and stubbing
- **TestContainers**: Docker-based integration testing
- **RestAssured**: API endpoint testing
- **Kotest**: Kotlin-native testing DSL
- **JaCoCo**: Code coverage reporting

## Test Categories

### Unit Tests

Focus on testing individual components in isolation.

```bash
./gradlew test
```

**Test Location**: `src/test/kotlin/com/trustmesh/**`

**Example**: Testing business logic, domain models, and utilities.

### Integration Tests

Test interactions between components, using TestContainers for database.

**Test Location**: `src/test/kotlin/com/trustmesh/integration/**`

**Example**: Database operations, API endpoints with real database.

### End-to-End Tests

Test complete user workflows and critical paths.

## Running Tests

### Run All Tests
```bash
./gradlew test
```

### Run Specific Test Class
```bash
./gradlew test --tests "com.trustmesh.api.AuthControllerTest"
```

### Run Tests with Coverage Report
```bash
./gradlew test jacocoTestReport
```

### View Coverage Report
After running tests with coverage:
```bash
open build/reports/jacoco/test/html/index.html
```

## Code Coverage Requirements

- **Minimum Coverage**: 70% across all packages
- **Critical Paths**: 100% coverage required
- **Excluded from Coverage**:
  - Configuration classes
  - Entity/DTO classes
  - Auto-generated code

## Writing Tests

### Test Naming Convention

```kotlin
@DisplayName("Feature: Descriptive test name")
fun testMethodName() { }
```

### Arrange-Act-Assert Pattern

```kotlin
@Test
fun testExample() {
    // Arrange: Set up test data
    val input = createTestData()
    
    // Act: Perform the action
    val result = function(input)
    
    // Assert: Verify the result
    result shouldBe expectedValue
}
```

### Using Mocks

```kotlin
class ExampleTest : TestBaseClass() {
    @Mock
    private lateinit var mockService: MyService
    
    @Test
    fun testWithMock() {
        // Setup mock behavior
        whenever(mockService.getData()).thenReturn("mocked data")
        
        // Use in test
        val result = mockService.getData()
        result shouldBe "mocked data"
    }
}
```

## CI/CD Integration

Tests run automatically on:
- **Push to main/develop branches**
- **Pull requests to main/develop**
- **On feature branches**

See `.github/workflows/test.yml` for CI/CD configuration.

## Best Practices

1. **Write Tests First**: Follow TDD principles when possible
2. **Keep Tests Simple**: One assertion per test when possible
3. **Use Descriptive Names**: Test name should describe what is tested
4. **Isolate Tests**: Each test should be independent
5. **Mock External Dependencies**: Don't rely on external services
6. **Clean Up**: Use `@BeforeEach` and `@AfterEach` for setup/teardown
7. **Use Test Data Builders**: Create test data consistently

## Troubleshooting

### Tests Fail with Database Connection Error
- Ensure PostgreSQL is running (for integration tests)
- Check `application-test.yaml` configuration
- Verify TestContainers Docker is available

### Coverage Report Not Generated
```bash
./gradlew clean test jacocoTestReport
```

### Slow Tests
- Consider mocking expensive operations
- Use parallel test execution: `./gradlew test --parallel`

## Resources

- [JUnit 5 Documentation](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito Kotlin Documentation](https://github.com/mockito/mockito-kotlin)
- [TestContainers Documentation](https://www.testcontainers.org/)
- [Kotest Documentation](https://kotest.io/)
