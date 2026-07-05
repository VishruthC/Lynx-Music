package com.example.player

import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.Virtualizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class EqualizerState(
    val isEnabled: Boolean = false,
    val presets: List<String> = emptyList(),
    val currentPreset: Int = 0,
    val bands: List<EqualizerBand> = emptyList(),
    val bassBoostEnabled: Boolean = false,
    val bassBoostStrength: Int = 0,
    val virtualizerEnabled: Boolean = false,
    val virtualizerStrength: Int = 0
)

data class EqualizerBand(
    val index: Short,
    val lowerBandLevel: Short,
    val upperBandLevel: Short,
    val centerFreq: Int,
    val level: Short
)

object AudioEffectManager {
    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null

    private val _state = MutableStateFlow(EqualizerState())
    val state: StateFlow<EqualizerState> = _state.asStateFlow()

    fun initEffects(audioSessionId: Int) {
        if (audioSessionId == 0) return
        
        try {
            releaseEffects()

            equalizer = Equalizer(0, audioSessionId).apply {
                enabled = _state.value.isEnabled
            }
            bassBoost = BassBoost(0, audioSessionId).apply {
                enabled = _state.value.bassBoostEnabled
                if (strengthSupported) {
                    setStrength(_state.value.bassBoostStrength.toShort())
                }
            }
            virtualizer = Virtualizer(0, audioSessionId).apply {
                enabled = _state.value.virtualizerEnabled
                if (strengthSupported) {
                    setStrength(_state.value.virtualizerStrength.toShort())
                }
            }

            updateState()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updateState() {
        val eq = equalizer ?: return
        try {
            val presets = (0 until eq.numberOfPresets).map { eq.getPresetName(it.toShort()) }
            val bands = (0 until eq.numberOfBands).map { i ->
                val index = i.toShort()
                val freqRange = eq.getBandLevelRange()
                EqualizerBand(
                    index = index,
                    lowerBandLevel = freqRange[0],
                    upperBandLevel = freqRange[1],
                    centerFreq = eq.getCenterFreq(index),
                    level = eq.getBandLevel(index)
                )
            }
            _state.update {
                it.copy(
                    presets = presets,
                    currentPreset = eq.currentPreset.toInt(),
                    bands = bands,
                    bassBoostStrength = bassBoost?.roundedStrength?.toInt() ?: 0,
                    virtualizerStrength = virtualizer?.roundedStrength?.toInt() ?: 0
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setEqualizerEnabled(enabled: Boolean) {
        try {
            equalizer?.enabled = enabled
            _state.update { it.copy(isEnabled = enabled) }
        } catch (e: Exception) {}
    }

    fun setBandLevel(band: Short, level: Short) {
        try {
            equalizer?.setBandLevel(band, level)
            updateState()
        } catch (e: Exception) {}
    }

    fun usePreset(preset: Short) {
        try {
            equalizer?.usePreset(preset)
            updateState()
        } catch (e: Exception) {}
    }

    fun setBassBoostEnabled(enabled: Boolean) {
        try {
            bassBoost?.enabled = enabled
            _state.update { it.copy(bassBoostEnabled = enabled) }
        } catch (e: Exception) {}
    }

    fun setBassBoostStrength(strength: Int) {
        try {
            if (bassBoost?.strengthSupported == true) {
                bassBoost?.setStrength(strength.toShort())
                _state.update { it.copy(bassBoostStrength = strength) }
            }
        } catch (e: Exception) {}
    }

    fun setVirtualizerEnabled(enabled: Boolean) {
        try {
            virtualizer?.enabled = enabled
            _state.update { it.copy(virtualizerEnabled = enabled) }
        } catch (e: Exception) {}
    }

    fun setVirtualizerStrength(strength: Int) {
        try {
            if (virtualizer?.strengthSupported == true) {
                virtualizer?.setStrength(strength.toShort())
                _state.update { it.copy(virtualizerStrength = strength) }
            }
        } catch (e: Exception) {}
    }

    fun releaseEffects() {
        equalizer?.release()
        bassBoost?.release()
        virtualizer?.release()
        equalizer = null
        bassBoost = null
        virtualizer = null
    }
}
