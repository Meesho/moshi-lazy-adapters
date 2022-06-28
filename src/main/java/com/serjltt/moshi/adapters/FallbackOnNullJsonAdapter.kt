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
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import java.io.IOException
import java.util.LinkedHashSet

/**
 * [JsonAdapter] that fallbacks to a default value of a primitive field annotated with
 * [FallbackOnNull].
 */
internal class FallbackOnNullJsonAdapter<T>(
    val delegate: JsonAdapter<T>,
    val fallback: T,
    private val fallbackType: String
) : JsonAdapter<T?>() {
    companion object {
        /** Set of primitives classes that are supported by **this** adapter.  */
        @JvmField val PRIMITIVE_CLASSES: MutableSet<Class<*>?> = LinkedHashSet()

        init {
            PRIMITIVE_CLASSES.add(Boolean::class.javaPrimitiveType)
            PRIMITIVE_CLASSES.add(Byte::class.javaPrimitiveType)
            PRIMITIVE_CLASSES.add(Char::class.javaPrimitiveType)
            PRIMITIVE_CLASSES.add(Double::class.javaPrimitiveType)
            PRIMITIVE_CLASSES.add(Float::class.javaPrimitiveType)
            PRIMITIVE_CLASSES.add(Int::class.javaPrimitiveType)
            PRIMITIVE_CLASSES.add(Long::class.javaPrimitiveType)
            PRIMITIVE_CLASSES.add(Short::class.javaPrimitiveType)
        }
    }

    @Throws(IOException::class)
    override fun fromJson(reader: JsonReader): T? {
        if (reader.peek() == JsonReader.Token.NULL) {
            reader.nextNull<Any>() // We need to consume the value.
            return fallback
        }
        return delegate.fromJson(reader)
    }

    @Throws(IOException::class)
    override fun toJson(writer: JsonWriter, value: T?) {
        delegate.toJson(writer, value)
    }

    override fun toString(): String {
        return "$delegate.fallbackOnNull($fallbackType=$fallback)"
    }
}