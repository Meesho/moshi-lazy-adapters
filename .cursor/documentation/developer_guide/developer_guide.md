# Developer Guide

<details>
<summary>Relevant source files</summary>

The following files were used as context for generating this wiki page:

- [CHANGELOG.md](CHANGELOG.md)
- [LICENSE](LICENSE)
- [README.md](README.md)
- [gradle.properties](gradle.properties)

</details>



This guide provides technical documentation for developers who want to understand, modify, or contribute to the moshi-lazy-adapters library. It covers the internal architecture, development patterns, and contribution workflow.

For information about using the library as a consumer, see [User Guide](#2). For detailed documentation of individual adapters and their usage, see [Available Adapters](#2.2).

## Library Architecture Overview

The moshi-lazy-adapters library extends Moshi's JSON processing capabilities through a structured pattern of annotations, factories, and adapters. The following diagram shows how the library integrates with Moshi's core architecture using actual code entities:

```mermaid
graph TB
    subgraph "Moshi Core Framework"
        MoshiBuilder["Moshi.Builder"]
        MoshiInstance["Moshi"]
        JsonAdapterBase["JsonAdapter<T>"]
        JsonQualifierBase["JsonQualifier"]
        JsonAdapterFactory["JsonAdapter.Factory"]
    end
    
    subgraph "moshi-lazy-adapters Extension Points"
        WrappedAnnotation["@Wrapped"]
        FallbackAnnotation["@FallbackOnNull"]
        DeserializeOnlyAnnotation["@DeserializeOnly"]
        FirstElementAnnotation["@FirstElement"]
        LastElementAnnotation["@LastElement"]
        ElementAtAnnotation["@ElementAt"]
        SerializeNullsAnnotation["@SerializeNulls"]
        
        WrappedFactory["Wrapped.ADAPTER_FACTORY"]
        FallbackFactory["FallbackOnNull.ADAPTER_FACTORY"]
        DeserializeOnlyFactory["DeserializeOnly.ADAPTER_FACTORY"]
        FirstElementFactory["FirstElement.ADAPTER_FACTORY"]
        LastElementFactory["LastElement.ADAPTER_FACTORY"]
        ElementAtFactory["ElementAt.ADAPTER_FACTORY"]
        SerializeNullsFactory["SerializeNulls.ADAPTER_FACTORY"]
    end
    
    subgraph "Adapter Implementations"
        WrappedJsonAdapter["WrappedJsonAdapter"]
        FallbackOnNullJsonAdapter["FallbackOnNullJsonAdapter"]
        TransientJsonAdapter["TransientJsonAdapter"]
        FirstElementJsonAdapter["FirstElementJsonAdapter"]
        LastElementJsonAdapter["LastElementJsonAdapter"]
        ElementAtJsonAdapter["ElementAtJsonAdapter"]
        SerializeNullsJsonAdapter["SerializeNullsJsonAdapter"]
    end
    
    subgraph "Utility Classes"
        UtilClass["Util.kt"]
        PairClass["Pair.java"]
    end
    
    %% Extension relationships
    WrappedAnnotation -.-> JsonQualifierBase
    FallbackAnnotation -.-> JsonQualifierBase
    DeserializeOnlyAnnotation -.-> JsonQualifierBase
    FirstElementAnnotation -.-> JsonQualifierBase
    
    %% Factory relationships
    WrappedFactory -.-> JsonAdapterFactory
    FallbackFactory -.-> JsonAdapterFactory
    DeserializeOnlyFactory -.-> JsonAdapterFactory
    FirstElementFactory -.-> JsonAdapterFactory
    
    %% Factory creates Adapter relationships
    WrappedFactory --> WrappedJsonAdapter
    FallbackFactory --> FallbackOnNullJsonAdapter
    DeserializeOnlyFactory --> TransientJsonAdapter
    FirstElementFactory --> FirstElementJsonAdapter
    LastElementFactory --> LastElementJsonAdapter
    ElementAtFactory --> ElementAtJsonAdapter
    SerializeNullsFactory --> SerializeNullsJsonAdapter
    
    %% Adapter inheritance
    WrappedJsonAdapter -.-> JsonAdapterBase
    FallbackOnNullJsonAdapter -.-> JsonAdapterBase
    TransientJsonAdapter -.-> JsonAdapterBase
    FirstElementJsonAdapter -.-> JsonAdapterBase
    
    %% Utility usage
    UtilClass --> WrappedFactory
    UtilClass --> FallbackFactory
    PairClass --> UtilClass
    
    %% Integration points
    MoshiBuilder --> WrappedFactory
    MoshiBuilder --> FallbackFactory
    MoshiBuilder --> DeserializeOnlyFactory
    MoshiBuilder --> MoshiInstance
```

**Sources:** [README.md:21-26](), [README.md:53-58](), [gradle.properties:1-7]()

## Development Patterns

### Annotation-Factory-Adapter Pattern

Each feature in moshi-lazy-adapters follows a consistent three-layer pattern that enables modular extension of Moshi's capabilities:

```mermaid
flowchart TD
    subgraph "Layer 1: Annotations"
        AnnotationDef["@JsonQualifier annotation<br/>defines behavior contract"]
        TargetSpec["@Target specification<br/>FIELD, METHOD, PARAMETER"]
        RetentionPolicy["@Retention(RUNTIME)<br/>for runtime processing"]
    end
    
    subgraph "Layer 2: Factory"
        FactoryConst["public static final ADAPTER_FACTORY<br/>JsonAdapter.Factory constant"]
        CreateMethod["create() method implementation<br/>returns JsonAdapter or null"]
        AnnotationCheck["annotation detection logic<br/>via Util.nextAnnotations()"]
        DelegateCall["delegate to next adapter<br/>for composition support"]
    end
    
    subgraph "Layer 3: Adapter"
        AdapterImpl["JsonAdapter<T> implementation<br/>extends JsonAdapter"]
        FromJsonMethod["fromJson() method<br/>deserialization logic"]
        ToJsonMethod["toJson() method<br/>serialization logic"]
        DelegateAdapter["delegate adapter<br/>for chaining behavior"]
    end
    
    subgraph "Utility Support"
        UtilNextAnnotations["Util.nextAnnotations()<br/>removes processed annotations"]
        TypeSafePair["Pair<T1, T2><br/>type-safe tuple utility"]
    end
    
    %% Flow relationships
    AnnotationDef --> FactoryConst
    TargetSpec --> AnnotationCheck
    RetentionPolicy --> AnnotationCheck
    
    FactoryConst --> CreateMethod
    CreateMethod --> AnnotationCheck
    AnnotationCheck --> DelegateCall
    DelegateCall --> AdapterImpl
    
    AdapterImpl --> FromJsonMethod
    AdapterImpl --> ToJsonMethod
    FromJsonMethod --> DelegateAdapter
    ToJsonMethod --> DelegateAdapter
    
    AnnotationCheck --> UtilNextAnnotations
    UtilNextAnnotations --> TypeSafePair
```

**Sources:** [README.md:28-33](), [README.md:46-58]()

### Adapter Composition Chain

The library's key architectural feature is adapter composition, where multiple adapters can be chained together. The order of factory registration determines the execution order:

```mermaid
flowchart LR
    subgraph "Moshi.Builder Registration Order"
        Builder["Moshi.Builder()"]
        AddWrapped[".add(Wrapped.ADAPTER_FACTORY)"]
        AddFirst[".add(FirstElement.ADAPTER_FACTORY)"]
        BuildMoshi[".build()"]
    end
    
    subgraph "Runtime Adapter Chain"
        WrappedAdapter["WrappedJsonAdapter<List<MyData>>"]
        FirstAdapter["FirstElementJsonAdapter<MyData>"]
        BaseAdapter["Moshi's base adapter<br/>for MyData"]
    end
    
    subgraph "JSON Processing Flow"
        JsonInput["JSON with nested structure<br/>{result: {data: [item1, item2]}}"]
        UnwrapStep["Unwrap path: result.data<br/>extracts [item1, item2]"]
        FirstStep["Extract first element<br/>returns item1"]
        ParseStep["Parse to MyData object"]
    end
    
    %% Builder flow
    Builder --> AddWrapped
    AddWrapped --> AddFirst
    AddFirst --> BuildMoshi
    
    %% Adapter chain
    BuildMoshi --> WrappedAdapter
    WrappedAdapter --> FirstAdapter
    FirstAdapter --> BaseAdapter
    
    %% Processing flow
    JsonInput --> UnwrapStep
    UnwrapStep --> FirstStep
    FirstStep --> ParseStep
    
    %% Connections
    WrappedAdapter --> UnwrapStep
    FirstAdapter --> FirstStep
    BaseAdapter --> ParseStep
```

**Sources:** [README.md:34-58](), [README.md:102-107]()

## Development Workflow

### Project Structure and Key Files

The following table shows the essential files for library development:

| File Category | File Path | Purpose |
|---------------|-----------|---------|
| Library Metadata | `gradle.properties` | Version, artifact coordinates, licensing |
| Build Configuration | `build.gradle` | Dependencies, compilation, publishing |
| Dependencies | `dependencies.gradle` | Centralized dependency management |
| Source Code | `src/main/java/` | Annotation and adapter implementations |
| Tests | `src/test/java/` | Unit tests for adapter behavior |
| Documentation | `README.md`, `CHANGELOG.md` | User documentation and version history |

**Sources:** [gradle.properties:1-20](), [CHANGELOG.md:1-67]()

### Build and Release Process

The library uses a Gradle-based build system with automated CI/CD through Travis CI:

```mermaid
flowchart TD
    subgraph "Development Phase"
        CodeChange["Code changes in feature branch"]
        LocalTest["./gradlew test"]
        LocalBuild["./gradlew build"]
        PullRequest["Create pull request"]
    end
    
    subgraph "CI/CD Pipeline (Travis CI)"
        TravisTrigger["Travis CI triggered"]
        CleanCheck["./gradlew clean check"]
        TestExecution["Run unit tests with JUnit"]
        CoverageReport["Generate JaCoCo coverage"]
        CodecovUpload["Upload to codecov.io"]
        SnapshotCheck{{"Is master branch?<br/>Not a PR?"}}
        SnapshotDeploy["Deploy snapshot to artifactory"]
    end
    
    subgraph "Publishing System"
        ArtifactorySnapshot["JFrog Artifactory<br/>SNAPSHOT_REPO"]
        ArtifactoryRelease["JFrog Artifactory<br/>RELEASE_REPO"]
        MavenCentral["Maven Central<br/>com.serjltt.moshi:moshi-lazy-adapters"]
    end
    
    subgraph "Version Management"
        GradleProps["gradle.properties<br/>VERSION_NAME=2.3-SNAPSHOT"]
        ChangelogUpdate["CHANGELOG.md updates"]
        TagRelease["Git tag for release"]
    end
    
    %% Development flow
    CodeChange --> LocalTest
    LocalTest --> LocalBuild
    LocalBuild --> PullRequest
    PullRequest --> TravisTrigger
    
    %% CI flow
    TravisTrigger --> CleanCheck
    CleanCheck --> TestExecution
    TestExecution --> CoverageReport
    CoverageReport --> CodecovUpload
    CodecovUpload --> SnapshotCheck
    SnapshotCheck -->|Yes| SnapshotDeploy
    SnapshotCheck -->|No| End["End CI build"]
    
    %% Publishing
    SnapshotDeploy --> ArtifactorySnapshot
    ArtifactoryRelease --> MavenCentral
    
    %% Version management
    GradleProps --> SnapshotDeploy
    ChangelogUpdate --> TagRelease
    TagRelease --> ArtifactoryRelease
```

**Sources:** [gradle.properties:1-3](), [CHANGELOG.md:4-67]()

## Contribution Guidelines

### Adding New Adapters

To contribute a new adapter to the library, follow this established pattern:

1. **Create the annotation** - Implement a `@JsonQualifier` annotation with appropriate `@Target` and `@Retention` specifications
2. **Implement the factory** - Create a `JsonAdapter.Factory` with a public static `ADAPTER_FACTORY` constant
3. **Build the adapter** - Extend `JsonAdapter<T>` with custom `fromJson()` and `toJson()` logic
4. **Support composition** - Use `Util.nextAnnotations()` to enable chaining with other adapters
5. **Add comprehensive tests** - Create unit tests covering serialization, deserialization, and edge cases

### Code Quality Standards

The project maintains code quality through:

- **Checkstyle** enforcement for consistent code formatting
- **JaCoCo** code coverage reporting (target coverage levels enforced by CI)
- **Unit test requirements** for all new adapter functionality
- **Documentation standards** following existing patterns in README.md

**Sources:** [README.md:173-188](), [CHANGELOG.md:22-30](), [gradle.properties:14-16]()
