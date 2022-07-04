package com.serjltt.moshi.adapters

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import java.io.IOException

/**
 * [JsonAdapter] with transient functionality. The consumer can decide to ether serialize or
 * deserialize, or make the adapter completely transient.
 */
internal class TransientJsonAdapter<T>(
    private val delegate: JsonAdapter<T>,
    private val serialize: Boolean,
    private val deserialize: Boolean
) : JsonAdapter<T?>() {
    @Throws(IOException::class)
    override fun fromJson(reader: JsonReader): T? {
        return if (deserialize) {
            delegate.fromJson(reader)
        } else {
            reader.skipValue()
            null
        }
    }

    @Throws(IOException::class)
    override fun toJson(writer: JsonWriter, value: T?) {
        if (serialize) {
            delegate.toJson(writer, value)
        } else {
            // We'll need to consume this property otherwise we'll get an IllegalArgumentException.
            delegate.toJson(writer, null)
        }
    }

    override fun toString(): String {
        return delegate.toString() + if (serialize && deserialize) "" else if (serialize) ".serializeOnly()" else if (deserialize) ".deserializeOnly()" else ".transient()"
    }
}