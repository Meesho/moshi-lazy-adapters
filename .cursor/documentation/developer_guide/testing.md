# Testing

<details>
<summary>Relevant source files</summary>

The following files were used as context for generating this wiki page:

- [jacoco.gradle](jacoco.gradle)
- [src/unitTest/java/com/serjltt/moshi/adapters/DeserializeOnlyJsonAdapterTest.kt](src/unitTest/java/com/serjltt/moshi/adapters/DeserializeOnlyJsonAdapterTest.kt)
- [src/unitTest/java/com/serjltt/moshi/adapters/FallbackOnNullJsonAdapterTest.kt](src/unitTest/java/com/serjltt/moshi/adapters/FallbackOnNullJsonAdapterTest.kt)

</details>



This document covers the testing infrastructure, patterns, and code coverage setup used in the moshi-lazy-adapters library. It describes how adapter functionality is validated through unit tests and how test coverage is measured and reported. For information about the build system configuration that executes these tests, see [Build System](#3.2). For details about code quality enforcement, see [Code Quality](#3.4).

## Test Structure and Organization

The library uses a comprehensive unit testing approach to validate adapter behavior. All tests are located in the `src/unitTest/java/com/serjltt/moshi/adapters/` directory and follow consistent patterns for testing JSON adapter functionality.

```mermaid
graph TB
    subgraph "Test Organization"
        TestDir["src/unitTest/java/com/serjltt/moshi/adapters/"]
        FallbackTest["FallbackOnNullJsonAdapterTest.kt"]
        DeserializeTest["DeserializeOnlyJsonAdapterTest.kt"]
        OtherTests["[Other Adapter Tests]"]
    end
    
    subgraph "Test Dependencies"
        JUnit["JUnit Framework"]
        AssertJ["AssertJ Assertions"]
        Moshi["Moshi Core Library"]
        MockWebServer["MockWebServer"]
    end
    
    subgraph "Tested Components"
        FallbackAdapter["FallbackOnNullJsonAdapter"]
        DeserializeAdapter["TransientJsonAdapter (DeserializeOnly)"]
        AdapterFactories["ADAPTER_FACTORY instances"]
        Annotations["@FallbackOnNull, @DeserializeOnly, etc."]
    end
    
    TestDir --> FallbackTest
    TestDir --> DeserializeTest
    TestDir --> OtherTests
    
    FallbackTest --> FallbackAdapter
    DeserializeTest --> DeserializeAdapter
    
    FallbackTest --> JUnit
    FallbackTest --> AssertJ
    FallbackTest --> Moshi
    
    DeserializeTest --> JUnit
    DeserializeTest --> AssertJ
    DeserializeTest --> Moshi
```

**Sources:** [src/unitTest/java/com/serjltt/moshi/adapters/FallbackOnNullJsonAdapterTest.kt:1-390](), [src/unitTest/java/com/serjltt/moshi/adapters/DeserializeOnlyJsonAdapterTest.kt:1-58]()

## Testing Patterns

### Moshi Builder Setup Pattern

Each test class follows a consistent pattern for setting up the `Moshi` instance with the necessary adapter factories:

```mermaid
flowchart TD
    TestClass["Test Class"]
    MoshiBuilder["Moshi.Builder()"]
    AdapterFactory["ADAPTER_FACTORY"]
    CustomAdapters["Custom Test Adapters"]
    MoshiInstance["Moshi Instance"]
    TestExecution["Test Methods"]
    
    TestClass --> MoshiBuilder
    MoshiBuilder --> AdapterFactory
    MoshiBuilder --> CustomAdapters
    MoshiBuilder --> MoshiInstance
    MoshiInstance --> TestExecution
```

The `FallbackOnNullJsonAdapterTest` class demonstrates this pattern by initializing Moshi with both the target adapter factory and additional test adapters for composition testing [src/unitTest/java/com/serjltt/moshi/adapters/FallbackOnNullJsonAdapterTest.kt:45-48]().

**Sources:** [src/unitTest/java/com/serjltt/moshi/adapters/FallbackOnNullJsonAdapterTest.kt:45-48](), [src/unitTest/java/com/serjltt/moshi/adapters/DeserializeOnlyJsonAdapterTest.kt:17-20]()

### Primitive Type Testing Pattern

The `FallbackOnNullJsonAdapter` tests cover all primitive types using a systematic approach. Each primitive type has dedicated test wrapper classes and verification methods:

```mermaid
graph LR
    subgraph "Primitive Type Coverage"
        BoolTest["booleanFallbacks()"]
        ByteTest["byteFallbacks()"]
        CharTest["charFallbacks()"]
        DoubleTest["doubleFallbacks()"]
        FloatTest["floatFallbacks()"]
        IntTest["intFallbacks()"]
        LongTest["longFallbacks()"]
        ShortTest["shortFallbacks()"]
    end
    
    subgraph "Test Wrapper Classes"
        WrapsBool["WrapsBool"]
        WrapsByte["WrapsByte"]
        WrapsChar["WrapsChar"]
        WrapsDouble["WrapsDouble"]
        WrapsFloat["WrapsFloat"]
        WrapsInt["WrapsInt"]
        WrapsLong["WrapsLong"]
        WrapsShort["WrapsShort"]
    end
    
    BoolTest --> WrapsBool
    ByteTest --> WrapsByte
    CharTest --> WrapsChar
    DoubleTest --> WrapsDouble
    FloatTest --> WrapsFloat
    IntTest --> WrapsInt
    LongTest --> WrapsLong
    ShortTest --> WrapsShort
```

Each wrapper class contains two fields: one with default fallback behavior and another with custom fallback values [src/unitTest/java/com/serjltt/moshi/adapters/FallbackOnNullJsonAdapterTest.kt:56-69]().

**Sources:** [src/unitTest/java/com/serjltt/moshi/adapters/FallbackOnNullJsonAdapterTest.kt:50-258]()

### Adapter Composition Testing

Tests verify that multiple adapters work correctly together by using custom test adapters alongside the library adapters:

```mermaid
graph TD
    subgraph "Composition Test Setup"
        MultiplyAdapter["@Multiply CustomAdapter"]
        FallbackAnnotation["@FallbackOnNull"]
        TestField["willFallback field"]
        TestField2["willMultiply field"]
    end
    
    subgraph "Execution Flow"
        JSONInput["JSON with null value"]
        FallbackProcessing["FallbackOnNull processes null"]
        MultiplyProcessing["Multiply adapter processes result"]
        FinalValue["Final processed value"]
    end
    
    MultiplyAdapter --> TestField
    FallbackAnnotation --> TestField
    MultiplyAdapter --> TestField2
    FallbackAnnotation --> TestField2
    
    JSONInput --> FallbackProcessing
    FallbackProcessing --> MultiplyProcessing
    MultiplyProcessing --> FinalValue
```

The `factoryMaintainsOtherAnnotations()` test method validates that adapter composition preserves the functionality of other annotations [src/unitTest/java/com/serjltt/moshi/adapters/FallbackOnNullJsonAdapterTest.kt:262-275]().

**Sources:** [src/unitTest/java/com/serjltt/moshi/adapters/FallbackOnNullJsonAdapterTest.kt:262-275](), [src/unitTest/java/com/serjltt/moshi/adapters/FallbackOnNullJsonAdapterTest.kt:277-285]()

## Test Assertion Patterns

### Bidirectional JSON Testing

Tests verify both deserialization (JSON to object) and serialization (object to JSON) behavior using a common assertion pattern:

```mermaid
flowchart LR
    JSONString["JSON String with nulls"]
    Deserialization["fromJson()"]
    ObjectInstance["Object Instance"]
    FieldAssertions["Field Value Assertions"]
    Serialization["toJson()"]
    SerializedJSON["Serialized JSON"]
    SerializationAssertions["JSON String Assertions"]
    
    JSONString --> Deserialization
    Deserialization --> ObjectInstance
    ObjectInstance --> FieldAssertions
    ObjectInstance --> Serialization
    Serialization --> SerializedJSON
    SerializedJSON --> SerializationAssertions
```

The `assertForClass()` helper method implements this pattern, testing both directions of JSON conversion [src/unitTest/java/com/serjltt/moshi/adapters/FallbackOnNullJsonAdapterTest.kt:346-362]().

**Sources:** [src/unitTest/java/com/serjltt/moshi/adapters/FallbackOnNullJsonAdapterTest.kt:346-362]()

### Factory Behavior Validation

Tests include specific validation for adapter factory behavior, ensuring factories correctly identify applicable types and ignore non-applicable ones:

The `factoryIgnoresNonPrimitiveTypes()` test validates that `FallbackOnNull.ADAPTER_FACTORY` returns `null` for non-primitive types, following the Moshi adapter factory contract [src/unitTest/java/com/serjltt/moshi/adapters/FallbackOnNullJsonAdapterTest.kt:288-307]().

**Sources:** [src/unitTest/java/com/serjltt/moshi/adapters/FallbackOnNullJsonAdapterTest.kt:288-307]()

## Code Coverage Configuration

The library uses JaCoCo for code coverage measurement and reporting. The coverage configuration is defined in a separate Gradle file:

```mermaid
graph TB
    subgraph "Coverage Configuration"
        JacocoGradle["jacoco.gradle"]
        JacocoPlugin["jacoco plugin"]
        ToolVersion["toolVersion = '0.8.7'"]
    end
    
    subgraph "Report Generation"
        JacocoTestReport["jacocoTestReport task"]
        XMLReport["XML report (enabled)"]
        HTMLReport["HTML report (enabled)"]
    end
    
    subgraph "Build Integration"
        CheckTask["check task"]
        TestExecution["Test execution"]
        CoverageGeneration["Coverage generation"]
    end
    
    JacocoGradle --> JacocoPlugin
    JacocoPlugin --> ToolVersion
    JacocoPlugin --> JacocoTestReport
    
    JacocoTestReport --> XMLReport
    JacocoTestReport --> HTMLReport
    
    CheckTask --> TestExecution
    TestExecution --> CoverageGeneration
    CheckTask --> JacocoTestReport
```

The configuration enables both XML and HTML report formats, with XML reports typically used for CI/CD integration and HTML reports for local development review [jacoco.gradle:7-12]().

**Sources:** [jacoco.gradle:1-15]()

## Test Execution Integration

### Build System Integration

Tests are integrated into the Gradle build lifecycle through task dependencies:

- The `check` task depends on `jacocoTestReport` [jacoco.gradle:14]()
- Coverage reports are generated after test execution
- CI/CD pipeline executes tests through `./gradlew clean check`

### Locale Independence Testing

The test suite includes specific validation for locale-independent behavior, ensuring adapter functionality works correctly across different system locales [src/unitTest/java/com/serjltt/moshi/adapters/FallbackOnNullJsonAdapterTest.kt:184-195]().

**Sources:** [jacoco.gradle:14](), [src/unitTest/java/com/serjltt/moshi/adapters/FallbackOnNullJsonAdapterTest.kt:184-195]()
