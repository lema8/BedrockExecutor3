package com.bedrockexecutor.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bedrockexecutor.data.model.InjectionResult
import com.bedrockexecutor.data.model.Script
import com.bedrockexecutor.data.model.ScriptCategory
import com.bedrockexecutor.data.repository.ScriptRepository
import com.bedrockexecutor.injection.BehaviorPackBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ConsoleEntry(
    val message: String,
    val type: ConsoleType = ConsoleType.INFO,
    val timestamp: Long = System.currentTimeMillis()
)

enum class ConsoleType { INFO, SUCCESS, ERROR, WARNING }

class MainViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = ScriptRepository(app)
    val scripts = repo.scripts

    private val _injecting = MutableStateFlow(false)
    val injecting: StateFlow<Boolean> = _injecting.asStateFlow()

    private val _lastResult = MutableStateFlow<InjectionResult?>(null)
    val lastResult: StateFlow<InjectionResult?> = _lastResult.asStateFlow()

    private val _consoleLog = MutableStateFlow<List<ConsoleEntry>>(emptyList())
    val consoleLog: StateFlow<List<ConsoleEntry>> = _consoleLog.asStateFlow()

    private val _mcbeAccessible = MutableStateFlow(false)
    val mcbeAccessible: StateFlow<Boolean> = _mcbeAccessible.asStateFlow()

    init {
        checkMCBEAccess()
        log("Bedrock Executor initialized", ConsoleType.INFO)
        log("MCBE path: ${BehaviorPackBuilder.getMCBEBehaviorPackDir()}", ConsoleType.INFO)
    }

    fun checkMCBEAccess() {
        viewModelScope.launch(Dispatchers.IO) {
            val accessible = BehaviorPackBuilder.checkMCBEAccessible()
            _mcbeAccessible.value = accessible
            if (accessible) {
                log("✅ Minecraft Bedrock detected", ConsoleType.SUCCESS)
            } else {
                log("⚠️ Minecraft not found — install it or check permissions", ConsoleType.WARNING)
            }
        }
    }

    fun toggleScript(scriptId: String) {
        repo.toggleScript(scriptId)
        val script = scripts.value.find { it.id == scriptId }
        val nowEnabled = !(script?.isEnabled ?: false)
        log("${if (nowEnabled) "✅ Enabled" else "⛔ Disabled"}: ${script?.name}", ConsoleType.INFO)
    }

    fun injectScripts() {
        viewModelScope.launch(Dispatchers.IO) {
            _injecting.value = true
            val enabled = repo.getEnabledScripts()

            if (enabled.isEmpty()) {
                log("⚠️ No scripts enabled — toggle some scripts first", ConsoleType.WARNING)
                _injecting.value = false
                return@launch
            }

            log("🔧 Building behavior pack with ${enabled.size} scripts...", ConsoleType.INFO)
            enabled.forEach { log("  • ${it.name}", ConsoleType.INFO) }

            delay(500) // Visual feedback

            val result = BehaviorPackBuilder.buildAndInject(
                context = getApplication(),
                scripts = enabled,
                packName = "BedrockExecutor"
            )

            _lastResult.value = result
            log(result.message, if (result.success) ConsoleType.SUCCESS else ConsoleType.ERROR)

            if (result.success) {
                log("📁 Pack path: ${result.packPath}", ConsoleType.INFO)
                log("➡️  Open Minecraft → Settings → Global Resources → Activate pack", ConsoleType.INFO)
            }

            _injecting.value = false
        }
    }

    fun removeAllPacks() {
        viewModelScope.launch(Dispatchers.IO) {
            val packs = BehaviorPackBuilder.listInjectedPacks()
            packs.forEach { BehaviorPackBuilder.removePackByName(it) }
            log("🗑️ Removed ${packs.size} injected pack(s)", ConsoleType.INFO)
        }
    }

    fun saveScript(script: Script) {
        repo.saveUserScript(script)
        log("💾 Saved script: ${script.name}", ConsoleType.SUCCESS)
    }

    fun deleteScript(scriptId: String) {
        val name = scripts.value.find { it.id == scriptId }?.name
        repo.deleteScript(scriptId)
        log("🗑️ Deleted: $name", ConsoleType.INFO)
    }

    fun clearConsole() {
        _consoleLog.value = emptyList()
    }

    private fun log(message: String, type: ConsoleType = ConsoleType.INFO) {
        viewModelScope.launch {
            _consoleLog.value = (_consoleLog.value + ConsoleEntry(message, type)).takeLast(200)
        }
    }
}
