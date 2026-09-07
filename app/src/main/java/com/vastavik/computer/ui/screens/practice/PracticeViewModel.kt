package com.vastavik.computer.ui.screens.practice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vastavik.computer.data.api.model.CodingExerciseDto
import com.vastavik.computer.data.api.model.MCQItemDto
import com.vastavik.computer.data.api.model.PredictOutputSetDto
import com.vastavik.computer.data.api.model.PYQResponse
import com.vastavik.computer.data.repository.VastavikApiRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PracticeViewModel @Inject constructor(
    private val repository: VastavikApiRepository
) : ViewModel() {

    private val _sirMcqs = MutableStateFlow<List<MCQItemDto>>(emptyList())
    val sirMcqs = _sirMcqs.asStateFlow()

    private val _sirCoding = MutableStateFlow<List<CodingExerciseDto>>(emptyList())
    val sirCoding = _sirCoding.asStateFlow()

    private val _sirPredict = MutableStateFlow<List<PredictOutputSetDto>>(emptyList())
    val sirPredict = _sirPredict.asStateFlow()

    private val _sirPyqs = MutableStateFlow<List<PYQResponse>>(emptyList())
    val sirPyqs = _sirPyqs.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    init {
        loadSirContent()
    }

    fun loadSirContent() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.getMcqs(source = "sir").onSuccess { list ->
                    if (list.isNotEmpty()) _sirMcqs.value = list
                }

                repository.getCodingExercises(source = "sir").onSuccess { list ->
                    if (list.isNotEmpty()) _sirCoding.value = list
                }

                repository.getPredictOutputSets(source = "sir").onSuccess { list ->
                    if (list.isNotEmpty()) _sirPredict.value = list
                }

                repository.getPyqs(source = "sir").onSuccess { list ->
                    if (list.isNotEmpty()) _sirPyqs.value = list
                }
            } catch (_: Exception) {
                // Offline fallback
            } finally {
                _isLoading.value = false
            }
        }
    }
}
