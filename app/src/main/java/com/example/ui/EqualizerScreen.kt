package com.example.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.player.AudioEffectManager
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EqualizerScreen(onBack: () -> Unit) {
    val state by AudioEffectManager.state.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Equalizer") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Switch(
                        checked = state.isEnabled,
                        onCheckedChange = { AudioEffectManager.setEqualizerEnabled(it) }
                    )
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            
            // Presets
            if (state.presets.isNotEmpty()) {
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = state.presets.getOrNull(state.currentPreset) ?: "Custom",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Preset") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        state.presets.forEachIndexed { index, preset ->
                            DropdownMenuItem(
                                text = { Text(preset) },
                                onClick = {
                                    AudioEffectManager.usePreset(index.toShort())
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Equalizer Bands
            if (state.bands.isNotEmpty()) {
                Text(
                    text = "Frequencies",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    state.bands.forEach { band ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = if (band.centerFreq >= 1000000) "${band.centerFreq / 1000000}k" 
                                       else "${band.centerFreq / 1000}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            val range = (band.upperBandLevel - band.lowerBandLevel).toFloat()
                            val value = if (range == 0f) 0f else (band.level - band.lowerBandLevel) / range
                            
                            Slider(
                                value = value,
                                onValueChange = { newValue ->
                                    val newLevel = (newValue * range + band.lowerBandLevel).toInt().toShort()
                                    AudioEffectManager.setBandLevel(band.index, newLevel)
                                },
                                modifier = Modifier.height(150.dp)
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = "${(band.level / 100)} dB",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }
            
            Divider()

            // Bass Boost
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Bass Boost",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Switch(
                        checked = state.bassBoostEnabled,
                        onCheckedChange = { AudioEffectManager.setBassBoostEnabled(it) }
                    )
                }
                
                Slider(
                    value = state.bassBoostStrength.toFloat(),
                    onValueChange = { AudioEffectManager.setBassBoostStrength(it.roundToInt()) },
                    valueRange = 0f..1000f,
                    enabled = state.bassBoostEnabled
                )
            }

            // Virtualizer
            Column(modifier = Modifier.padding(bottom = 32.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Virtual Surround",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Switch(
                        checked = state.virtualizerEnabled,
                        onCheckedChange = { AudioEffectManager.setVirtualizerEnabled(it) }
                    )
                }
                
                Slider(
                    value = state.virtualizerStrength.toFloat(),
                    onValueChange = { AudioEffectManager.setVirtualizerStrength(it.roundToInt()) },
                    valueRange = 0f..1000f,
                    enabled = state.virtualizerEnabled
                )
            }
        }
    }
}
