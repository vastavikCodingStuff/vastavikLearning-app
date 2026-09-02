package com.vastavik.computer.ui.screens.practice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vastavik.computer.data.model.CodingChallenge
import com.vastavik.computer.data.model.PYQModel
import com.vastavik.computer.data.model.QuizModel
import com.vastavik.computer.data.repository.FirestoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PracticeViewModel @Inject constructor(
    private val firestoreRepository: FirestoreRepository
) : ViewModel() {

    private val _generatedQuiz = MutableStateFlow<QuizModel?>(null)
    val generatedQuiz = _generatedQuiz.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    val quizzesState: StateFlow<List<QuizModel>> = firestoreRepository.streamQuizzes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val challengesState: StateFlow<List<CodingChallenge>> = firestoreRepository.streamCodingChallenges()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pyqsState: StateFlow<List<PYQModel>> = firestoreRepository.streamPYQs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun generateQuiz(quiz: QuizModel) {
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            try {
                firestoreRepository.createQuiz(quiz)
                _generatedQuiz.value = quiz
                _isLoading.value = false
            } catch (e: Exception) {
                _isLoading.value = false
                _error.value = e.message ?: "Failed to generate quiz"
            }
        }
    }

    fun loadChallenges() {
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            try {
                _isLoading.value = false
            } catch (e: Exception) {
                _isLoading.value = false
                _error.value = e.message ?: "Failed to load challenges"
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}
