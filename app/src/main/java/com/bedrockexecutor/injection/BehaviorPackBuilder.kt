package com.bedrockexecutor.injection

import android.content.Context
import com.bedrockexecutor.data.model.BehaviorPack
import com.bedrockexecutor.data.model.InjectionResult
import com.bedrockexecutor.data.model.Script
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID

object BehaviorPackBuilder {

    // MCBE's behavior pack directory on Android (no root needed for local worlds)
    private const val MCBE_PACKAGE = "com.mojang.minecraftpe"
    private const val BEHAVIOR_PACK_PATH = "games/com.mojang/behavior_packs"

    fun getMCBEBehaviorPackDir(): String {
        return "/sdcard/Android/data/$MCBE_PACKAGE/files/$BEHAVIOR_PACK_PATH"
    }

    /**
     * Builds a behavior pack directory from scripts and injects it into MCBE's folder.
     * No root required — MCBE's data folder is accessible on most Android devices.
     */
    fun buildAndInject(
        context: Context,
        scripts: List<Script>,
        packName: String = "BedrockExecutor"
    ): InjectionResult {
        return try {
            val packUUID = UUID.randomUUID().toString()
            val moduleUUID = UUID.randomUUID().toString()
            val packDir = File(getMCBEBehaviorPackDir(), packName.replace(" ", "_"))

            // Clean up old pack if it exists
            if (packDir.exists()) packDir.deleteRecursively()
            packDir.mkdirs()

            // Create scripts subfolder
            val scriptsDir = File(packDir, "scripts")
            scriptsDir.mkdirs()

            // Write each script as its own JS file
            scripts.filter { it.isEnabled }.forEach { script ->
                val safeFileName = script.name
                    .replace(" ", "_")
                    .replace(Regex("[^a-zA-Z0-9_]"), "")
                    .lowercase() + ".js"
                File(scriptsDir, safeFileName).writeText(buildScriptWrapper(script))
            }

            // Write the main entry point that imports all scripts
            val mainJs = buildMainEntryPoint(scripts.filter { it.isEnabled })
            File(scriptsDir, "main.js").writeText(mainJs)

            // Write manifest.json
            val manifest = buildManifest(packName, packUUID, moduleUUID)
            File(packDir, "manifest.json").writeText(manifest.toString(2))

            // Write pack_icon.png (blank placeholder — copy from assets if available)
            copyPackIcon(context, packDir)

            InjectionResult(
                success = true,
                message = "✅ Pack injected! Enable '${packName}' in Minecraft → Settings → Global Resources",
                packPath = packDir.absolutePath
            )
        } catch (e: SecurityException) {
            InjectionResult(
                success = false,
                message = "❌ Permission denied. Grant storage permission and make sure Minecraft is installed."
            )
        } catch (e: Exception) {
            InjectionResult(
                success = false,
                message = "❌ Error: ${e.message}"
            )
        }
    }

    private fun buildManifest(name: String, packUUID: String, moduleUUID: String): JSONObject {
        return JSONObject().apply {
            put("format_version", 2)
            put("header", JSONObject().apply {
                put("name", name)
                put("description", "Injected by Bedrock Executor")
                put("uuid", packUUID)
                put("version", JSONArray().apply { put(1); put(0); put(0) })
                put("min_engine_version", JSONArray().apply { put(1); put(20); put(0) })
            })
            put("modules", JSONArray().apply {
                put(JSONObject().apply {
                    put("description", "Script module")
                    put("type", "script")
                    put("language", "javascript")
                    put("uuid", moduleUUID)
                    put("version", JSONArray().apply { put(1); put(0); put(0) })
                    put("entry", "scripts/main.js")
                })
            })
            put("dependencies", JSONArray().apply {
                // @minecraft/server API dependency
                put(JSONObject().apply {
                    put("module_name", "@minecraft/server")
                    put("version", "1.8.0")
                })
                put(JSONObject().apply {
                    put("module_name", "@minecraft/server-ui")
                    put("version", "1.2.0")
                })
            })
        }
    }

    private fun buildMainEntryPoint(scripts: List<Script>): String {
        val imports = scripts.joinToString("\n") { script ->
            val safeName = script.name
                .replace(" ", "_")
                .replace(Regex("[^a-zA-Z0-9_]"), "")
                .lowercase()
            "import './${safeName}.js';"
        }
        return """
// Bedrock Executor — Auto-generated entry point
// Scripts: ${scripts.size} active

$imports

// Startup log
import { world, system } from "@minecraft/server";
system.runTimeout(() => {
    world.sendMessage("§a[Executor] §fScripts loaded: ${scripts.size} active");
}, 20);
""".trimIndent()
    }

    private fun buildScriptWrapper(script: Script): String {
        return """
// Script: ${script.name}
// Category: ${script.category.displayName}
// Generated by Bedrock Executor

${script.code}
""".trimIndent()
    }

    private fun copyPackIcon(context: Context, packDir: File) {
        try {
            val iconFile = File(packDir, "pack_icon.png")
            context.assets.open("pack_icon.png").use { input ->
                iconFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        } catch (e: Exception) {
            // No icon is fine, pack will still work
        }
    }

    /**
     * Check if MCBE is installed and its data folder is accessible
     */
    fun checkMCBEAccessible(): Boolean {
        val dir = File(getMCBEBehaviorPackDir())
        return dir.exists() || dir.parentFile?.exists() == true
    }

    /**
     * List all currently injected packs by this executor
     */
    fun listInjectedPacks(): List<String> {
        val dir = File(getMCBEBehaviorPackDir())
        if (!dir.exists()) return emptyList()
        return dir.listFiles()?.map { it.name } ?: emptyList()
    }

    /**
     * Remove an injected pack by name
     */
    fun removePackByName(packName: String): Boolean {
        val packDir = File(getMCBEBehaviorPackDir(), packName)
        return if (packDir.exists()) packDir.deleteRecursively() else false
    }
}
