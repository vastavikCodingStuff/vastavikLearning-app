package com.vastavik.computer.ui.screens.video

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vastavik.computer.data.model.LessonModel
import com.vastavik.computer.data.repository.FirestoreRepository
import com.vastavik.computer.data.repository.VastavikApiRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VideoLessonViewModel @Inject constructor(
    private val firestoreRepository: FirestoreRepository,
    private val apiRepository: VastavikApiRepository
) : ViewModel() {

    private val _lessonData = MutableStateFlow<LessonModel?>(null)
    val lessonData = _lessonData.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _usingBackend = MutableStateFlow(false)
    val usingBackend = _usingBackend.asStateFlow()

    /**
     * Load lesson — tries backend API first (with HMAC + Firebase token), falls back to Firestore
     * direct streaming if backend unreachable (offline/dev without server).
     */
    fun loadLesson(courseId: String, partId: String, subpartId: String, lessonId: String) {
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            // 1) Try backend API (single lesson endpoint or scoped list)
            try {
                val lesson = apiRepository.getLesson(lessonId)
                _lessonData.value = lesson
                _isLoading.value = false
                _usingBackend.value = true
                return@launch
            } catch (apiErr: Exception) {
                // 2) Fallback: try scoped lessons list via API
                try {
                    val list = apiRepository.getLessons(courseId, partId, subpartId)
                    val found = list.find { it.id == lessonId }
                    if (found != null) {
                        _lessonData.value = found
                        _isLoading.value = false
                        _usingBackend.value = true
                        return@launch
                    }
                } catch (_: Exception) { /* fall through to firestore */ }
            }

            // 3) Fallback to Firestore streaming
            try {
                firestoreRepository.streamLessons(courseId, partId, subpartId).collect { lessons ->
                    _lessonData.value = lessons.find { it.id == lessonId }
                    _isLoading.value = false
                    _usingBackend.value = false
                }
            } catch (e: Exception) {
                _isLoading.value = false
                _error.value = e.message ?: "Failed to load lesson"
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}
