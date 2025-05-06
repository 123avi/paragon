package org.paragontech.notifications

data class MeterValue(
    val timestamp: String,
    val sampledValue: List<SampledValue>
)