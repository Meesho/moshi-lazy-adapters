# Internal Architecture

<details>
<summary>Relevant source files</summary>

The following files were used as context for generating this wiki page:

- [src/main/java/com/serjltt/moshi/adapters/DeserializeOnly.kt](src/main/java/com/serjltt/moshi/adapters/DeserializeOnly.kt)
- [src/main/java/com/serjltt/moshi/adapters/FallbackOnNull.kt](src/main/java/com/serjltt/moshi/adapters/FallbackOnNull.kt)
- [src/main/java/com/serjltt/moshi/adapters/FallbackOnNullJsonAdapter.kt](src/main/java/com/serjltt/moshi/adapters/FallbackOnNullJsonAdapter.kt)
- [src/main/java/com/serjltt/moshi/adapters/TransientJsonAdapter.kt](src/main/java/com/serjltt/moshi/adapters/TransientJsonAdapter.kt)
- [src/main/java/com/serjltt/moshi/adapters/Util.kt](src/main/java/com/serjltt/moshi/adapters/Util.kt)

</details>



This document provides a deep dive into the internal architecture of the moshi-lazy-adapters library, covering the design patterns, utility classes, and adapter implementation details. It is intended for developers who want to understand, modify, or contribute to the library codebase.

For information about how to use these adapters, see [User Guide](#2). For build system details, see [Build System](#3.2).

## Core Architecture Pattern

The moshi-lazy-adapters library follows a three-layer architecture pattern where annotations are processed by factories to create specialized adapters:

### Architecture Flow Diagram

```mermaid
flowchart TD
    subgraph "Annotation Layer"
        DeserializeOnly["@DeserializeOnly"]
        FallbackOnNull["@FallbackOnNull"]
        OtherAnnotations["@Wrapped, @FirstElement, etc."]
    end
    
    subgraph "Factory Layer"
        DeserializeOnlyFactory["DeserializeOnly.ADAPTER_FACTORY"]
        FallbackOnNullFactory["FallbackOnNull.ADAPTER_FACTORY"]
        UtilNextAnnotations["Util.nextAnnotations()"]
    end
    
    subgraph "Adapter Layer"
        TransientJsonAdapter["TransientJsonAdapter<T>"]
        FallbackOnNullJsonAdapter["FallbackOnNullJsonAdapter<T>"]
        DelegateAdapter["JsonAdapter<T> delegate"]
    end
    
    subgraph "Moshi Core"
        MoshiBuilder["Moshi.Builder"]
        JsonAdapterFactory["JsonAdapter.Factory"]
        JsonAdapter["JsonAdapter<T>"]
    end
    
    DeserializeOnly --> DeserializeOnlyFactory
    FallbackOnNull --> FallbackOnNullFactory
    
    DeserializeOnlyFactory --> UtilNextAnnotations
    FallbackOnNullFactory --> UtilNextAnnotations
    
    DeserializeOnlyFactory --> TransientJsonAdapter
    FallbackOnNullFactory --> FallbackOnNullJsonAdapter
    
    TransientJsonAdapter --> DelegateAdapter
    FallbackOnNullJsonAdapter --> DelegateAdapter
    
    DeserializeOnlyFactory -.-> JsonAdapterFactory
    FallbackOnNullFactory -.-> JsonAdapterFactory
    TransientJsonAdapter -.-> JsonAdapter
    FallbackOnNullJsonAdapter -.-> JsonAdapter
    
    JsonAdapterFactory --> MoshiBuilder
```

**Sources:** [src/main/java/com/serjltt/moshi/adapters/DeserializeOnly.kt:32-41](), [src/main/java/com/serjltt/moshi/adapters/FallbackOnNull.kt:68-83](), [src/main/java/com/serjltt/moshi/adapters/Util.kt:31-54]()

## Annotation Processing Chain

The library uses a consistent pattern for processing annotations through the `Util.nextAnnotations()` method:

### Annotation Processing Flow

```mermaid
sequenceDiagram
    participant MoshiBuilder as "Moshi.Builder"
    participant AdapterFactory as "ADAPTER_FACTORY"
    participant UtilNextAnnotations as "Util.nextAnnotations()"
    participant DelegateAdapter as "delegate JsonAdapter"
    participant CustomAdapter as "Custom JsonAdapter"
    
    MoshiBuilder->>AdapterFactory: "create(type, annotations, moshi)"
    AdapterFactory->>UtilNextAnnotations: "nextAnnotations(annotations, AnnotationClass)"
    UtilNextAnnotations->>AdapterFactory: "Pair<Annotation, Set<Annotation>>"
    AdapterFactory->>MoshiBuilder: "moshi.adapter(type, nextAnnotations)"
    MoshiBuilder->>DelegateAdapter: "create delegate adapter"
    DelegateAdapter->>AdapterFactory: "delegate instance"
    AdapterFactory->>CustomAdapter: "new CustomAdapter(delegate, ...)"
    CustomAdapter->>AdapterFactory: "custom adapter instance"
    AdapterFactory->>MoshiBuilder: "return custom adapter"
```

The `nextAnnotations()` method extracts the target annotation and returns the remaining annotations for delegation:

| Method | Purpose | Return Value |
|--------|---------|--------------|
| `Util.nextAnnotations()` | Extracts target annotation from annotation set | `Pair<A, Set<Annotation>>` or `null` |
| Annotation validation | Ensures target class has `@JsonQualifier` | Throws `IllegalArgumentException` if invalid |
| Delegate annotation support | Finds annotations that delegate to target annotation | Returns delegated annotation if found |

**Sources:** [src/main/java/com/serjltt/moshi/adapters/Util.kt:31-54](), [src/main/java/com/serjltt/moshi/adapters/DeserializeOnly.kt:34-35](), [src/main/java/com/serjltt/moshi/adapters/FallbackOnNull.kt:74-75]()

## Adapter Implementation Pattern

All custom adapters follow a delegation pattern where they wrap a delegate `JsonAdapter` and modify its behavior:

### Adapter Delegation Architecture

```mermaid
classDiagram
    class JsonAdapter {
        <<abstract>>
        +fromJson(reader) T
        +toJson(writer, value) void
    }
    
    class TransientJsonAdapter {
        -delegate: JsonAdapter~T~
        -serialize: boolean
        -deserialize: boolean
        +fromJson(reader) T
        +toJson(writer, value) void
        +toString() String
    }
    
    class FallbackOnNullJsonAdapter {
        -delegate: JsonAdapter~T~
        -fallback: T
        -fallbackType: String
        +PRIMITIVE_CLASSES: Set~Class~
        +fromJson(reader) T
        +toJson(writer, value) void
        +toString() String
    }
    
    JsonAdapter <|-- TransientJsonAdapter
    JsonAdapter <|-- FallbackOnNullJsonAdapter
    
    TransientJsonAdapter --> JsonAdapter : delegates to
    FallbackOnNullJsonAdapter --> JsonAdapter : delegates to
```

### TransientJsonAdapter Implementation

The `TransientJsonAdapter` conditionally delegates based on `serialize` and `deserialize` flags:

```mermaid
flowchart TD
    FromJson["fromJson(reader)"]
    ToJson["toJson(writer, value)"]
    
    DeserializeCheck{"deserialize == true?"}
    SerializeCheck{"serialize == true?"}
    
    DelegateFromJson["delegate.fromJson(reader)"]
    SkipValue["reader.skipValue() return null"]
    
    DelegateToJson["delegate.toJson(writer, value)"]
    ToJsonNull["delegate.toJson(writer, null)"]
    
    FromJson --> DeserializeCheck
    DeserializeCheck -->|Yes| DelegateFromJson
    DeserializeCheck -->|No| SkipValue
    
    ToJson --> SerializeCheck
    SerializeCheck -->|Yes| DelegateToJson
    SerializeCheck -->|No| ToJsonNull
```

**Sources:** [src/main/java/com/serjltt/moshi/adapters/TransientJsonAdapter.kt:18-35]()

## Factory Design Pattern

Each annotation provides a companion object with an `ADAPTER_FACTORY` that implements `JsonAdapter.Factory`:

### Factory Implementation Comparison

| Factory | Validation Logic | Adapter Creation |
|---------|-----------------|------------------|
| `DeserializeOnly.ADAPTER_FACTORY` | Uses `Types.nextAnnotations()` | Creates `TransientJsonAdapter(delegate, false, true)` |
| `FallbackOnNull.ADAPTER_FACTORY` | Uses `Util.nextAnnotations()` + primitive type check | Creates `FallbackOnNullJsonAdapter(delegate, fallback, fallbackType)` |

### FallbackOnNull Factory Logic

```mermaid
flowchart TD
    FactoryCreate["ADAPTER_FACTORY.create(type, annotations, moshi)"]
    NextAnnotations["Util.nextAnnotations(annotations, FallbackOnNull.class)"]
    CheckNull{"nextAnnotations == null?"}
    GetRawType["Types.getRawType(type)"]
    CheckPrimitive{"PRIMITIVE_CLASSES.contains(rawType)?"}
    FallbackType["fallbackType(rawType)"]
    RetrieveFallback["retrieveFallback(annotation, fallbackType)"]
    CreateAdapter["new FallbackOnNullJsonAdapter(delegate, fallback, fallbackType)"]
    
    FactoryCreate --> NextAnnotations
    NextAnnotations --> CheckNull
    CheckNull -->|Yes| ReturnNull["return null"]
    CheckNull -->|No| GetRawType
    GetRawType --> CheckPrimitive
    CheckPrimitive -->|No| ReturnNull
    CheckPrimitive -->|Yes| FallbackType
    FallbackType --> RetrieveFallback
    RetrieveFallback --> CreateAdapter
```

**Sources:** [src/main/java/com/serjltt/moshi/adapters/FallbackOnNull.kt:71-83](), [src/main/java/com/serjltt/moshi/adapters/DeserializeOnly.kt:33-40]()

## Utility Infrastructure

### Util.nextAnnotations() Implementation

The core utility method processes annotations using this algorithm:

```mermaid
flowchart TD
    NextAnnotations["nextAnnotations(annotations, jsonQualifier)"]
    ValidateQualifier["require(jsonQualifier.isAnnotationPresent(JsonQualifier))"]
    CheckEmpty{"annotations.isEmpty()?"}
    IterateAnnotations["for (annotation in annotations)"]
    DirectMatch{"jsonQualifier == annotation.annotationClass.java?"}
    FindDelegated["findDelegatedAnnotation(annotation, jsonQualifier)"]
    CheckDelegated{"delegate != null?"}
    CreatePair["Pair(annotation, delegateAnnotations)"]
    
    NextAnnotations --> ValidateQualifier
    ValidateQualifier --> CheckEmpty
    CheckEmpty -->|Yes| ReturnNull["return null"]
    CheckEmpty -->|No| IterateAnnotations
    IterateAnnotations --> DirectMatch
    DirectMatch -->|Yes| CreatePair
    DirectMatch -->|No| FindDelegated
    FindDelegated --> CheckDelegated
    CheckDelegated -->|Yes| CreatePair
    CheckDelegated -->|No| IterateAnnotations
```

### Primitive Type Support

The `FallbackOnNullJsonAdapter` maintains a static set of supported primitive types:

| Primitive Type | Kotlin Class | Default Fallback Value |
|----------------|--------------|-------------------------|
| `boolean` | `Boolean::class.javaPrimitiveType` | `false` |
| `byte` | `Byte::class.javaPrimitiveType` | `Byte.MIN_VALUE` |
| `char` | `Char::class.javaPrimitiveType` | `Character.MIN_VALUE` |
| `double` | `Double::class.javaPrimitiveType` | `Double.MIN_VALUE` |
| `float` | `Float::class.javaPrimitiveType` | `Float.MIN_VALUE` |
| `int` | `Int::class.javaPrimitiveType` | `Integer.MIN_VALUE` |
| `long` | `Long::class.javaPrimitiveType` | `Long.MIN_VALUE` |
| `short` | `Short::class.javaPrimitiveType` | `Short.MIN_VALUE` |

**Sources:** [src/main/java/com/serjltt/moshi/adapters/Util.kt:31-66](), [src/main/java/com/serjltt/moshi/adapters/FallbackOnNullJsonAdapter.kt:35-46](), [src/main/java/com/serjltt/moshi/adapters/FallbackOnNull.kt:49-66]()
