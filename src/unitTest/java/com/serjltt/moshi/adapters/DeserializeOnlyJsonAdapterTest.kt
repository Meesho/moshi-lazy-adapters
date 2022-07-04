package com.serjltt.moshi.adapters

import com.squareup.moshi.Moshi
import com.serjltt.moshi.adapters.DeserializeOnly
import com.serjltt.moshi.adapters.Custom.CustomAdapter
import kotlin.Throws
import com.squareup.moshi.JsonAdapter
import com.serjltt.moshi.adapters.DeserializeOnlyJsonAdapterTest.Data1
import com.serjltt.moshi.adapters.DeserializeOnlyJsonAdapterTest.Data2
import com.serjltt.moshi.adapters.Custom
import org.assertj.core.api.Assertions
import org.junit.Test
import java.lang.Exception

class DeserializeOnlyJsonAdapterTest {
    // Lazy adapters work only within the context of moshi.
    private val moshi = Moshi.Builder()
        .add(DeserializeOnly.ADAPTER_FACTORY)
        .add(CustomAdapter()) // We need to check that other annotations are not lost.
        .build()

    @Test
    @Throws(Exception::class)
    fun deserializeOnly() {
        val adapter = moshi.adapter(Data1::class.java)
        val fromJson = adapter.fromJson("{\"data\": \"test\"}")
        Assertions.assertThat(fromJson!!.data).isEqualTo("test")
        Assertions.assertThat(adapter.toJson(fromJson)).isEqualTo("{}")
    }

    @Test
    @Throws(Exception::class)
    fun factoryMaintainsOtherAnnotations() {
        val adapter = moshi.adapter(Data2::class.java)
        val fromJson = adapter.fromJson("{\"data\": \"test\"}")
        Assertions.assertThat(fromJson!!.data).isEqualTo("testCustom")
        Assertions.assertThat(adapter.toJson(fromJson)).isEqualTo("{}")
    }

    @Test
    @Throws(Exception::class)
    fun toStringReflectsInnerAdapter() {
        val adapter = moshi.adapter<String>(String::class.java, DeserializeOnly::class.java)
        Assertions.assertThat(adapter.toString())
            .isEqualTo("JsonAdapter(String).nullSafe().deserializeOnly()")
    }

    private class Data1 {
        @DeserializeOnly
        var data: String? = null
    }

    private class Data2 {
        @DeserializeOnly
        @Custom
        var data: String? = null
    }
}