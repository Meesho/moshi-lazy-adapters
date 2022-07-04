/*
 * Copyright 2014 Square, Inc.
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

import com.squareup.moshi.JsonQualifier
import java.util.Collections
import java.util.LinkedHashSet
import kotlin.Pair

object Util {
    /**
     * Checks if `annotations` contains `jsonQualifier`.
     * Returns a pair containing the subset of `annotations` without `jsonQualifier`
     * and the `jsonQualified` instance, or null if `annotations` does not contain
     * `jsonQualifier`.
     */
    fun <A : Annotation> nextAnnotations(
        annotations: Set<Annotation>, jsonQualifier: Class<A>
    ): Pair<A, Set<Annotation?>>? {
        require(jsonQualifier.isAnnotationPresent(JsonQualifier::class.java)) { "$jsonQualifier is not a JsonQualifier." }
        if (annotations.isEmpty()) {
            return null
        }
        for (annotation in annotations) {
            if (jsonQualifier == annotation.annotationClass.java) {
                val delegateAnnotations: LinkedHashSet<Annotation?> = linkedSetOf()
                delegateAnnotations.addAll(annotations)
                delegateAnnotations.remove(annotation)
                return Pair(annotation as A, Collections.unmodifiableSet(delegateAnnotations))
            }
            val delegate = findDelegatedAnnotation(annotation, jsonQualifier)
            if (delegate != null) {
                val delegateAnnotations: LinkedHashSet<Annotation?> = linkedSetOf()
                delegateAnnotations.addAll(annotations)
                delegateAnnotations.remove(annotation)
                return Pair(delegate, Collections.unmodifiableSet(delegateAnnotations))
            }
        }
        return null
    }

    private fun <A : Annotation?> findDelegatedAnnotation(
        annotation: Annotation, jsonQualifier: Class<A>
    ): A? {
        for (delegatedAnnotation in annotation.javaClass.annotations) {
            if (jsonQualifier == delegatedAnnotation.annotationClass.java) {
                return delegatedAnnotation as A
            }
        }
        return null
    }
}