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

import com.serjltt.moshi.adapters.FallbackOnNull
import com.serjltt.moshi.adapters.FallbackOnNullJsonAdapterTest.Multiply.MultiplyAdapter
import kotlin.Throws
import com.serjltt.moshi.adapters.FallbackOnNullJsonAdapterTest.WrapsBool
import com.serjltt.moshi.adapters.FallbackOnNullJsonAdapterTest.WrapsByte
import com.serjltt.moshi.adapters.FallbackOnNullJsonAdapterTest.WrapsChar
import com.serjltt.moshi.adapters.FallbackOnNullJsonAdapterTest.WrapsDouble
import com.serjltt.moshi.adapters.FallbackOnNullJsonAdapterTest.WrapsFloat
import com.serjltt.moshi.adapters.FallbackOnNullJsonAdapterTest.WrapsInt
import com.serjltt.moshi.adapters.FallbackOnNullJsonAdapterTest.WrapsLong
import com.serjltt.moshi.adapters.FallbackOnNullJsonAdapterTest.WrapsShort
import com.serjltt.moshi.adapters.FallbackOnNullJsonAdapterTest.AnotherInt
import com.serjltt.moshi.adapters.FallbackOnNullJsonAdapterTest.Multiply
import com.serjltt.moshi.adapters.FallbackOnNullJsonAdapterTest
import com.serjltt.moshi.adapters.FallbackOnNullJsonAdapterTest.AndAnotherInt
import com.serjltt.moshi.adapters.FallbackOnNullJsonAdapterTest.AlwaysFallBackToTwoOnNull
import com.squareup.moshi.*
import org.assertj.core.api.Assertions
import org.junit.Test
import java.io.IOException
import java.lang.Exception
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.util.*

class FallbackOnNullJsonAdapterTest {
    // Lazy adapters work only within the context of moshi.
    private val moshi = Moshi.Builder()
        .add(FallbackOnNull.ADAPTER_FACTORY)
        .add(MultiplyAdapter())
        .build()

    @Test
    @Throws(Exception::class)
    fun booleanFallbacks() {
        assertForClass(WrapsBool::class.java, false, true, "{\"first\":false,\"second\":true}")
    }

    private class WrapsBool : Wrapper<Boolean> {
        @FallbackOnNull
        var first = false

        @FallbackOnNull(fallbackBoolean = true)
        var second = false
        override fun first(): Boolean {
            return first
        }

        override fun second(): Boolean {
            return second
        }
    }

    @Test
    @Throws(Exception::class)
    fun byteFallbacks() {
        assertForClass(
            WrapsByte::class.java,
            Byte.MIN_VALUE,
            42.toByte(),
            "{\"first\":128,\"second\":42}"
        )
    }

    private class WrapsByte : Wrapper<Byte> {
        @FallbackOnNull
        var first: Byte = 0

        @FallbackOnNull(fallbackByte = 42)
        var second: Byte = 0
        override fun first(): Byte {
            return first
        }

        override fun second(): Byte {
            return second
        }
    }

    @Test
    @Throws(Exception::class)
    fun charFallbacks() {
        assertForClass(
            WrapsChar::class.java,
            '\u0000',
            'a',
            "{\"first\":\"\\u0000\",\"second\":\"a\"}"
        )
    }

    @JsonClass(generateAdapter = true)
    private class WrapsChar : Wrapper<Char> {
        @FallbackOnNull
        var first = 0.toChar()

        @FallbackOnNull(fallbackChar = 'a')
        var second = 0.toChar()
        override fun first(): Char {
            return first
        }

        override fun second(): Char {
            return second
        }
    }

    @Test
    @Throws(Exception::class)
    fun doubleFallbacks() {
        assertForClass(
            WrapsDouble::class.java, Double.MIN_VALUE, 12.0,
            "{\"first\":4.9E-324,\"second\":12.0}"
        )
    }

    private class WrapsDouble : Wrapper<Double> {
        @FallbackOnNull
        var first = 0.0

        @FallbackOnNull(fallbackDouble = 12.0)
        var second = 0.0
        override fun first(): Double {
            return first
        }

        override fun second(): Double {
            return second
        }
    }

    @Test
    @Throws(Exception::class)
    fun floatFallbacks() {
        assertForClass(
            WrapsFloat::class.java, Float.MIN_VALUE, 16.0f,
            "{\"first\":1.4E-45,\"second\":16.0}"
        )
    }

    private class WrapsFloat : Wrapper<Float> {
        @FallbackOnNull
        var first = 0f

        @FallbackOnNull(fallbackFloat = 16.0f)
        var second = 0f
        override fun first(): Float {
            return first
        }

        override fun second(): Float {
            return second
        }
    }

    @Test
    @Throws(Exception::class)
    fun intFallbacks() {
        assertForClass(
            WrapsInt::class.java,
            Int.MIN_VALUE,
            -1,
            "{\"first\":-2147483648,\"second\":-1}"
        )
    }

    @Test
    @Throws(Exception::class)
    fun intFallbacksNoLocaleInfluence() {
        val defaultLocale = Locale.getDefault()
        Locale.setDefault(Locale("tr", "TR"))
        assertForClass(
            WrapsInt::class.java,
            Int.MIN_VALUE,
            -1,
            "{\"first\":-2147483648,\"second\":-1}"
        )
        Locale.setDefault(defaultLocale)
    }

    private class WrapsInt : Wrapper<Int> {
        @FallbackOnNull
        var first = 0

        @FallbackOnNull(fallbackInt = -1)
        var second = 0
        override fun first(): Int {
            return first
        }

        override fun second(): Int {
            return second
        }
    }

    @Test
    @Throws(Exception::class)
    fun longFallbacks() {
        assertForClass(
            WrapsLong::class.java, Long.MIN_VALUE, -113L,
            "{\"first\":-9223372036854775808,\"second\":-113}"
        )
    }

    private class WrapsLong : Wrapper<Long> {
        @FallbackOnNull
        var first: Long = 0

        @FallbackOnNull(fallbackLong = -113)
        var second: Long = 0
        override fun first(): Long {
            return first
        }

        override fun second(): Long {
            return second
        }
    }

    @Test
    @Throws(Exception::class)
    fun shortFallbacks() {
        assertForClass(
            WrapsShort::class.java, Short.MIN_VALUE, 121.toShort(),
            "{\"first\":-32768,\"second\":121}"
        )
    }

    private class WrapsShort : Wrapper<Short> {
        @FallbackOnNull
        var first: Short = 0

        @FallbackOnNull(fallbackShort = 121)
        var second: Short = 0
        override fun first(): Short {
            return first
        }

        override fun second(): Short {
            return second
        }
    }

    @Test
    @Throws(Exception::class)
    fun factoryMaintainsOtherAnnotations() {
        val adapter = moshi.adapter(AnotherInt::class.java)
        val fromJson = adapter.fromJson(
            """{
  "willFallback": null,
  "willMultiply": 3
}"""
        )
        Assertions.assertThat(fromJson!!.willFallback).isEqualTo(2)
        Assertions.assertThat(fromJson.willMultiply).isEqualTo(6)
        val toJson = adapter.toJson(fromJson)
        // Both values should be serialized by the Multiply json adapter.
        Assertions.assertThat(toJson).isEqualTo("{\"willFallback\":1,\"willMultiply\":3}")
    }

    private class AnotherInt {
        @FallbackOnNull(fallbackInt = 2)
        @Multiply
        var willFallback = 0

        @FallbackOnNull(fallbackInt = 2)
        @Multiply
        var willMultiply = 0
    }

    @Test
    fun factoryIgnoresNonPrimitiveTypes() {
        val classes: ArrayList<Class<*>?> = object : ArrayList<Class<*>?>() {
            init {
                add(Boolean::class.java)
                add(Byte::class.java)
                add(Char::class.java)
                add(Double::class.java)
                add(Float::class.java)
                add(Int::class.java)
                add(Long::class.java)
                add(Short::class.java)
                add(String::class.java)
                add(Any::class.java)
            }
        }
        for (cls in classes) {
            Assertions.assertThat(FallbackOnNull.ADAPTER_FACTORY.create(cls, ANNOTATIONS, moshi))
                .isNull()
        }
    }

    @Test
    @Throws(Exception::class)
    fun fallbackOnNullIsDelegated() {
        val adapter = moshi.adapter(AndAnotherInt::class.java)
        val fromJson = adapter.fromJson(
            """{
  "willFallback": null
}"""
        )
        Assertions.assertThat(fromJson!!.willFallback).isEqualTo(2)
    }

    private class AndAnotherInt {
        @AlwaysFallBackToTwoOnNull
        var willFallback = 0
    }

    @JsonQualifier
    @FallbackOnNull(fallbackInt = 2)
    @Retention(RetentionPolicy.RUNTIME)
    @Target(
        AnnotationTarget.FIELD,
        AnnotationTarget.FUNCTION,
        AnnotationTarget.PROPERTY_GETTER,
        AnnotationTarget.PROPERTY_SETTER,
        AnnotationTarget.VALUE_PARAMETER
    )
    internal annotation class AlwaysFallBackToTwoOnNull

    @Test
    @Throws(Exception::class)
    fun toStringReflectsInnerAdapter() {
        val adapter = moshi.adapter<Int>(Int::class.javaPrimitiveType, ANNOTATIONS)
        Assertions.assertThat(adapter.toString())
            .isEqualTo("JsonAdapter(Integer).fallbackOnNull(fallbackInt=-1)")
    }

    @Throws(IOException::class)
    private fun <T : Wrapper<P>?, P> assertForClass(
        cls: Class<T>, first: P, second: P,
        asJson: String
    ) {
        val adapter = moshi.adapter(cls)
        val fromJson = adapter.fromJson(
            """{
  "first": null,
  "second": null
}"""
        )
        Assertions.assertThat(fromJson!!.first()).isEqualTo(first)
        Assertions.assertThat(fromJson.second()).isEqualTo(second)
        val toJson = adapter.toJson(fromJson)
        Assertions.assertThat(toJson).isEqualTo(asJson)
    }

    private interface Wrapper<P> {
        fun first(): P
        fun second(): P
    }

    @JsonQualifier
    @Retention(RetentionPolicy.RUNTIME)
    private annotation class Multiply {
        class MultiplyAdapter {
            @Multiply
            @FromJson
            fun fromJson(`val`: Int): Int {
                return `val` * 2
            }

            @ToJson
            fun toJson(@Multiply `val`: Int): Int {
                return `val` / 2
            }
        }
    }

    companion object {
        private val ANNOTATIONS: Set<Annotation?> =
            setOf(FallbackOnNull(false, 0, '0', 0.0, 0.0f, -1, 0, 0))
    }
}