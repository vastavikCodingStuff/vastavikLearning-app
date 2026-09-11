package com.vastavik.computer.ui.screens.video

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vastavik.computer.data.model.LessonModel
import com.vastavik.computer.data.repository.FirestoreRepository
import com.vastavik.computer.data.repository.VastavikApiRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
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
     * Load lesson — tries backend /lessons/{id} first (title/desc/whiteboard/code/shorts
     * from admin), then legacy endpoints, then one-shot Firestore read with timeout.
     * Never hangs: always clears isLoading and sets a clear error on failure.
     */
    fun loadLesson(courseId: String, partId: String, subpartId: String, lessonId: String) {
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            // 1) Primary: backend canonical lesson endpoint (has title/description/whiteboard/code)
            try {
                val resp = apiRepository.getLessonV1(lessonId).getOrNull()
                if (resp != null && (resp.title.isNotBlank() || resp.youtubeVideoId.isNotBlank() || resp.youtubeUrl.isNotBlank())) {
                    _lessonData.value = LessonModel(
                        id = resp.id.ifBlank { lessonId },
                        title = resp.title,
                        description = resp.description,
                        youtubeUrl = resp.youtubeUrl,
                        whiteboardImageUrl = resp.whiteboardImageUrl,
                        codeSample = resp.codeSample,
                        notes = resp.notes,
                        order = resp.order,
                        youtubeVideoId = resp.youtubeVideoId,
                        durationSec = resp.durationSec,
                        isPremium = resp.isPremium,
                        videoFormat = resp.videoFormat.ifBlank { "screen_recording" }
                    )
                    _isLoading.value = false
                    _usingBackend.value = true
                    return@launch
                }
            } catch (_: Exception) { /* fall through */ }

            // 2) Legacy single-lesson endpoint
            try {
                val lesson = apiRepository.getLesson(lessonId)
                if (lesson.title.isNotBlank() || lesson.youtubeUrl.isNotBlank() || lesson.youtubeVideoId.isNotBlank()) {
                    _lessonData.value = lesson
                    _isLoading.value = false
                    _usingBackend.value = true
                    return@launch
                }
            } catch (_: Exception) { /* fall through */ }

            // 3) One-shot Firestore read with timeout (never infinite collect)
            try {
                val lessons: List<LessonModel>? = kotlinx.coroutines.withTimeoutOrNull(8000) {
                    firestoreRepository.streamLessons(courseId, partId, subpartId).first()
                }
                val found = lessons?.find { it.id == lessonId }
                if (found != null) {
                    _lessonData.value = found
                    _usingBackend.value = false
                } else {
                    _error.value = "Video not found. It may have been moved or deleted — pull to refresh Learn and try again."
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load lesson. Check connection and retry."
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}
