package com.bedrockexecutor.data.model

import java.util.UUID

data class Script(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String = "",
    val code: String,
    val category: ScriptCategory = ScriptCategory.CUSTOM,
    val isEnabled: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

enum class ScriptCategory(val displayName: String, val emoji: String) {
    MOVEMENT("Movement", "🏃"),
    COMBAT("Combat", "⚔️"),
    VISUAL("Visual", "👁️"),
    UTILITY("Utility", "🔧"),
    SERVER("Server", "🌐"),
    CUSTOM("Custom", "📝")
}

data class BehaviorPack(
    val name: String,
    val description: String,
    val scripts: List<Script>,
    val uuid: String = UUID.randomUUID().toString(),
    val version: List<Int> = listOf(1, 0, 0)
)

data class InjectionResult(
    val success: Boolean,
    val message: String,
    val packPath: String? = null
)
