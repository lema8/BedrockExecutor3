package com.bedrockexecutor.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import com.bedrockexecutor.data.model.Script
import com.bedrockexecutor.data.model.ScriptCategory
import com.bedrockexecutor.ui.ConsoleEntry
import com.bedrockexecutor.ui.ConsoleType
import com.bedrockexecutor.ui.MainViewModel
import com.bedrockexecutor.ui.theme.*

@Composable
fun HomeScreen(viewModel: MainViewModel) {
    val scripts by viewModel.scripts.collectAsState()
    val injecting by viewModel.injecting.collectAsState()
    val consoleLog by viewModel.consoleLog.collectAsState()
    val mcbeAccessible by viewModel.mcbeAccessible.collectAsState()

    var selectedCategory by remember { mutableStateOf<ScriptCategory?>(null) }
    var activeTab by remember { mutableStateOf(0) } // 0=Scripts, 1=Console, 2=Editor

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        // Header
        ExecutorHeader(mcbeAccessible = mcbeAccessible, injecting = injecting) {
            viewModel.injectScripts()
        }

        // Tab bar
        TabRow(
            selectedTabIndex = activeTab,
            containerColor = Surface,
            contentColor = AccentGreen,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[activeTab]),
                    color = AccentGreen,
                    height = 2.dp
                )
            }
        ) {
            listOf("SCRIPTS", "CONSOLE", "EDITOR").forEachIndexed { i, label ->
                Tab(
                    selected = activeTab == i,
                    onClick = { activeTab = i },
                    text = {
                        Text(
                            label,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = if (activeTab == i) AccentGreen else TextMuted
                        )
                    }
                )
            }
        }

        // Tab content
        when (activeTab) {
            0 -> ScriptsTab(
                scripts = scripts,
                selectedCategory = selectedCategory,
                onCategoryFilter = { selectedCategory = if (selectedCategory == it) null else it },
                onToggle = { viewModel.toggleScript(it) },
                onDelete = { viewModel.deleteScript(it) }
            )
            1 -> ConsoleTab(
                log = consoleLog,
                onClear = { viewModel.clearConsole() }
            )
            2 -> EditorTab(onSave = { viewModel.saveScript(it) })
        }
    }
}

@Composable
fun ExecutorHeader(
    mcbeAccessible: Boolean,
    injecting: Boolean,
    onInject: () -> Unit
) {
    val pulseAnim = rememberInfiniteTransition(label = "pulse")
    val glowAlpha by pulseAnim.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0E0E1A), Background)
                )
            )
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Logo area
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "BEDROCK",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 3.sp,
                    color = TextMuted
                )
                Text(
                    "EXECUTOR",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp,
                    color = AccentGreen
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(
                                if (mcbeAccessible) AccentGreen.copy(alpha = glowAlpha)
                                else ErrorRed,
                                RoundedCornerShape(50)
                            )
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        if (mcbeAccessible) "MCBE Connected" else "MCBE Not Found",
                        fontSize = 10.sp,
                        color = if (mcbeAccessible) AccentGreenDim else ErrorRed,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            // Inject button
            Button(
                onClick = onInject,
                enabled = !injecting,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentGreen,
                    contentColor = Color(0xFF001A0D),
                    disabledContainerColor = AccentGreen.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.height(48.dp)
            ) {
                if (injecting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Color(0xFF001A0D),
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("INJECTING", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                } else {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("INJECT", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ScriptsTab(
    scripts: List<Script>,
    selectedCategory: ScriptCategory?,
    onCategoryFilter: (ScriptCategory) -> Unit,
    onToggle: (String) -> Unit,
    onDelete: (String) -> Unit
) {
    val filtered = if (selectedCategory != null)
        scripts.filter { it.category == selectedCategory }
    else scripts

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Category filter chips
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 8.dp)
            ) {
                items(ScriptCategory.values()) { cat ->
                    val selected = selectedCategory == cat
                    FilterChip(
                        selected = selected,
                        onClick = { onCategoryFilter(cat) },
                        label = {
                            Text(
                                "${cat.emoji} ${cat.displayName}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AccentGreen.copy(alpha = 0.15f),
                            selectedLabelColor = AccentGreen,
                            containerColor = SurfaceVariant,
                            labelColor = TextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selected,
                            selectedBorderColor = AccentGreen.copy(alpha = 0.5f),
                            borderColor = BorderColor
                        )
                    )
                }
            }
        }

        // Script cards
        items(filtered, key = { it.id }) { script ->
            ScriptCard(
                script = script,
                onToggle = { onToggle(script.id) },
                onDelete = { onDelete(script.id) }
            )
        }

        if (filtered.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No scripts in this category", color = TextMuted)
                }
            }
        }
    }
}

@Composable
fun ScriptCard(
    script: Script,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val bgColor = if (script.isEnabled)
        AccentGreen.copy(alpha = 0.05f)
    else CardColor

    val borderColor = if (script.isEnabled)
        AccentGreen.copy(alpha = 0.3f)
    else BorderColor

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category emoji
            Text(
                script.category.emoji,
                fontSize = 24.sp,
                modifier = Modifier.padding(end = 12.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    script.name,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = if (script.isEnabled) AccentGreen else TextPrimary
                )
                if (script.description.isNotEmpty()) {
                    Text(
                        script.description,
                        fontSize = 11.sp,
                        color = TextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                Text(
                    script.category.displayName,
                    fontSize = 10.sp,
                    color = AccentBlue.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 4.dp),
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(Modifier.width(8.dp))

            // Delete button (only for user scripts)
            if (!script.id.startsWith("builtin_")) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = ErrorRed.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Toggle switch
            Switch(
                checked = script.isEnabled,
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color(0xFF001A0D),
                    checkedTrackColor = AccentGreen,
                    uncheckedThumbColor = TextMuted,
                    uncheckedTrackColor = SurfaceVariant
                )
            )
        }
    }
}

@Composable
fun ConsoleTab(log: List<ConsoleEntry>, onClear: () -> Unit) {
    val listState = rememberLazyListState()

    LaunchedEffect(log.size) {
        if (log.isNotEmpty()) listState.animateScrollToItem(log.size - 1)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(ConsoleBg)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "CONSOLE OUTPUT",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                color = TextMuted
            )
            TextButton(onClick = onClear) {
                Text("CLEAR", fontSize = 10.sp, color = TextMuted, letterSpacing = 1.sp)
            }
        }

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .background(ConsoleBg)
                .padding(horizontal = 12.dp),
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            items(log) { entry ->
                val color = when (entry.type) {
                    ConsoleType.SUCCESS -> AccentGreen
                    ConsoleType.ERROR -> ErrorRed
                    ConsoleType.WARNING -> WarningYellow
                    ConsoleType.INFO -> TextSecondary
                }
                Text(
                    "> ${entry.message}",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    color = color,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
fun EditorTab(onSave: (Script) -> Unit) {
    var scriptName by remember { mutableStateOf("") }
    var scriptCode by remember { mutableStateOf("import { world, system } from \"@minecraft/server\";\n\n// Write your script here\nsystem.runInterval(() => {\n    for (const player of world.getAllPlayers()) {\n        // Do something to player\n    }\n}, 20);") }
    var selectedCategory by remember { mutableStateOf(ScriptCategory.CUSTOM) }
    var showSaved by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            "SCRIPT EDITOR",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp,
            color = TextMuted
        )

        // Name field
        OutlinedTextField(
            value = scriptName,
            onValueChange = { scriptName = it },
            label = { Text("Script Name") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AccentGreen,
                unfocusedBorderColor = BorderColor,
                focusedLabelColor = AccentGreen,
                unfocusedLabelColor = TextMuted,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                cursorColor = AccentGreen
            ),
            shape = RoundedCornerShape(10.dp)
        )

        // Category selector
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ScriptCategory.values().take(3).forEach { cat ->
                FilterChip(
                    selected = selectedCategory == cat,
                    onClick = { selectedCategory = cat },
                    label = { Text("${cat.emoji} ${cat.displayName}", fontSize = 10.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AccentGreen.copy(alpha = 0.15f),
                        selectedLabelColor = AccentGreen,
                        containerColor = SurfaceVariant,
                        labelColor = TextSecondary
                    )
                )
            }
        }

        // Code editor
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp)
                .background(ConsoleBg, RoundedCornerShape(10.dp))
                .border(1.dp, BorderColor, RoundedCornerShape(10.dp))
        ) {
            OutlinedTextField(
                value = scriptCode,
                onValueChange = { scriptCode = it },
                modifier = Modifier.fillMaxSize().padding(4.dp),
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    color = ConsoleGreen,
                    lineHeight = 20.sp
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    cursorColor = AccentGreen
                )
            )
        }

        // Save button
        Button(
            onClick = {
                if (scriptName.isNotEmpty()) {
                    onSave(
                        Script(
                            name = scriptName,
                            code = scriptCode,
                            category = selectedCategory
                        )
                    )
                    showSaved = true
                    scriptName = ""
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            enabled = scriptName.isNotEmpty(),
            colors = ButtonDefaults.buttonColors(
                containerColor = AccentGreen,
                contentColor = Color(0xFF001A0D)
            ),
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("SAVE SCRIPT", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        }

        AnimatedVisibility(visible = showSaved) {
            LaunchedEffect(showSaved) {
                kotlinx.coroutines.delay(2000)
                showSaved = false
            }
            Card(
                colors = CardDefaults.cardColors(containerColor = AccentGreen.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().border(1.dp, AccentGreen.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            ) {
                Text(
                    "✅ Script saved! Enable it in the Scripts tab.",
                    modifier = Modifier.padding(12.dp),
                    color = AccentGreen,
                    fontSize = 13.sp
                )
            }
        }
    }
}
