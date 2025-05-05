package org.paragontech

data class Environment(
    val publish: (String, String) -> Unit
)