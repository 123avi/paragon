package org.paragontech.notifications

data class SampledValue(
    val value: String,
    val context: String? = null,
    val format: String? = null,
    val measurand: String? = null,
    val phase: String? = null,
    val location: String? = null,
    val unit: String? = null
)