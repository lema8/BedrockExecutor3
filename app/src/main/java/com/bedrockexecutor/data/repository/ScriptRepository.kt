package com.bedrockexecutor.data.repository

import android.content.Context
import com.bedrockexecutor.data.BuiltInScripts
import com.bedrockexecutor.data.model.Script
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ScriptRepository(context: Context) {

    private val prefs = context.getSharedPreferences("scripts", Context.MODE_PRIVATE)
    private val gson = Gson()

    private val _scripts = MutableStateFlow<List<Script>>(emptyList())
    val scripts: StateFlow<List<Script>> = _scripts

    init {
        loadScripts()
    }

    private fun loadScripts() {
        val json = prefs.getString("user_scripts", null)
        val userScripts: List<Script> = if (json != null) {
            val type = object : TypeToken<List<Script>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } else emptyList()

        // Merge built-ins + user scripts
        _scripts.value = BuiltInScripts.all + userScripts
    }

    fun saveUserScript(script: Script) {
        val current = getUserScripts().toMutableList()
        val existingIndex = current.indexOfFirst { it.id == script.id }
        if (existingIndex >= 0) {
            current[existingIndex] = script
        } else {
            current.add(script)
        }
        prefs.edit().putString("user_scripts", gson.toJson(current)).apply()
        loadScripts()
    }

    fun deleteScript(scriptId: String) {
        val current = getUserScripts().filter { it.id != scriptId }
        prefs.edit().putString("user_scripts", gson.toJson(current)).apply()
        loadScripts()
    }

    fun toggleScript(scriptId: String) {
        val allScripts = _scripts.value
        val script = allScripts.find { it.id == scriptId } ?: return
        val updated = script.copy(isEnabled = !script.isEnabled)

        // If it's a built-in, store override in prefs
        val overrides = getToggledOverrides().toMutableMap()
        overrides[scriptId] = updated.isEnabled
        prefs.edit().putString("toggles", gson.toJson(overrides)).apply()

        // If it's a user script, update it
        val userScripts = getUserScripts()
        if (userScripts.any { it.id == scriptId }) {
            saveUserScript(updated)
        } else {
            // Refresh with toggle applied
            _scripts.value = _scripts.value.map {
                if (it.id == scriptId) it.copy(isEnabled = !it.isEnabled) else it
            }
        }
    }

    fun getEnabledScripts(): List<Script> {
        val toggles = getToggledOverrides()
        return _scripts.value.map { script ->
            val override = toggles[script.id]
            if (override != null) script.copy(isEnabled = override) else script
        }.filter { it.isEnabled }
    }

    private fun getUserScripts(): List<Script> {
        val json = prefs.getString("user_scripts", null) ?: return emptyList()
        val type = object : TypeToken<List<Script>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }

    private fun getToggledOverrides(): Map<String, Boolean> {
        val json = prefs.getString("toggles", null) ?: return emptyMap()
        val type = object : TypeToken<Map<String, Boolean>>() {}.type
        return gson.fromJson(json, type) ?: emptyMap()
    }
}
