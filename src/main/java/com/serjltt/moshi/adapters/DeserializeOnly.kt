package com.serjltt.moshi.adapters

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonQualifier
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

/**
 * Indicates that the annotated field may only be deserialized.
 *
 *
 * To leverage from [DeserializeOnly] [DeserializeOnly.ADAPTER_FACTORY] must be
 * added to your [Moshi instance][Moshi]:
 *
 * <pre>`
 * Moshi moshi = new Moshi.Builder()
 * .add(DeserializeOnly.ADAPTER_FACTORY)
 * .build();
`</pre> *
 */
@MustBeDocumented
@JsonQualifier
@Retention(AnnotationRetention.RUNTIME)
@Target(
    AnnotationTarget.FIELD,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER
)
annotation class DeserializeOnly {
    companion object {
        /** Builds an adapter that can process a types annotated with [DeserializeOnly].  */
        @JvmField
        val ADAPTER_FACTORY = JsonAdapter.Factory { type, annotations, moshi ->
            val nextAnnotations = Types.nextAnnotations(annotations, DeserializeOnly::class.java)
                ?: return@Factory null
            TransientJsonAdapter(moshi.adapter<Any>(type, nextAnnotations),
                serialize = false,
                deserialize = true
            )
        }
    }
}