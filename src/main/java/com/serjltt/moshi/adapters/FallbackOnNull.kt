/*
 * Copyright 2016 Serj Lotutovici
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.serjltt.moshi.adapters

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonQualifier
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import java.lang.reflect.Type
import java.util.Locale

/**
 * Indicates that the annotated field may be `null` in the json source and thus requires a
 * fallback value.
 *
 *
 * To leverage from [FallbackOnNull] [FallbackOnNull.ADAPTER_FACTORY]
 * must be added to your [Moshi instance][Moshi]:
 *
 * <pre>`
 * Moshi moshi = new Moshi.Builder()
 * .add(FallbackOnNull.ADAPTER_FACTORY)
 * .build();
`</pre> *
 */
@MustBeDocumented
@JsonQualifier
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
@Target(
    AnnotationTarget.FIELD,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER,
    AnnotationTarget.ANNOTATION_CLASS
)
annotation class FallbackOnNull(
    /** Fallback value for `boolean` primitives. Default: `false`.  */
    val fallbackBoolean: Boolean = false,
    /** Fallback value for `byte` primitives. Default: `Byte.MIN_VALUE`.  */
    val fallbackByte: Byte = Byte.MIN_VALUE,
    /** Fallback value for `char` primitives. Default: `Character.MIN_VALUE`.  */
    val fallbackChar: Char = Character.MIN_VALUE,
    /** Fallback value for `double` primitives. Default: `Double.MIN_VALUE`.  */
    val fallbackDouble: Double = Double.MIN_VALUE,
    /** Fallback value for `float` primitives. Default: `Float.MIN_VALUE`.  */
    val fallbackFloat: Float = Float.MIN_VALUE,
    /** Fallback value for `int` primitives. Default: `Integer.MIN_VALUE`.  */
    val fallbackInt: Int = Int.MIN_VALUE,
    /** Fallback value for `long` primitives. Default: `Long.MIN_VALUE`.  */
    val fallbackLong: Long = Long.MIN_VALUE,
    /** Fallback value for `short` primitives. Default: `Short.MIN_VALUE`.  */
    val fallbackShort: Short = Short.MIN_VALUE
) {
    companion object {
        /** Builds an adapter that can process a types annotated with [FallbackOnNull].  */
        @JvmField
        val ADAPTER_FACTORY: JsonAdapter.Factory = object : JsonAdapter.Factory {
            override fun create(
                type: Type, annotations: Set<Annotation>, moshi: Moshi
            ): JsonAdapter<*>? {
                val nextAnnotations = Util.nextAnnotations(annotations, FallbackOnNull::class.java)
                    ?: return null
                val rawType = Types.getRawType(type)
                if (!FallbackOnNullJsonAdapter.PRIMITIVE_CLASSES.contains(rawType)) return null
                val fallbackType = fallbackType(rawType)
                val fallback = retrieveFallback(nextAnnotations.first, fallbackType)
                return FallbackOnNullJsonAdapter(
                    moshi.adapter(type, nextAnnotations.second), fallback, fallbackType
                )
            }

            /** Invokes the appropriate fallback method based on the `fallbackType`.  */
            private fun retrieveFallback(annotation: FallbackOnNull, fallbackType: String): Any {
                return try {
                    val fallbackMethod = FallbackOnNull::class.java.getMethod(fallbackType)
                    fallbackMethod.invoke(annotation)
                } catch (e: Exception) {
                    throw AssertionError(e)
                }
            }

            /** Constructs the appropriate fallback method name based on the `rawType`.  */
            private fun fallbackType(rawType: Class<*>): String {
                val typeName: String = rawType.simpleName
                val methodSuffix: String =
                    typeName.substring(0, 1).uppercase(Locale.US) + typeName.substring(1)
                return "fallback$methodSuffix"
            }
        }
    }
}