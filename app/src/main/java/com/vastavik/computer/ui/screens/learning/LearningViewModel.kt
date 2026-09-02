package com.vastavik.computer.ui.screens.learning

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.vastavik.computer.data.model.CourseModel
import com.vastavik.computer.data.model.LessonModel
import com.vastavik.computer.data.model.PartModel
import com.vastavik.computer.data.model.StudentSelection
import com.vastavik.computer.data.model.SubpartModel
import com.vastavik.computer.data.repository.FirestoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class LearningViewModel @Inject constructor(
    private val firestoreRepository: FirestoreRepository
) : ViewModel() {

    private val uid: String
        get() = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    private val _selectedCourseId = MutableStateFlow("")
    val selectedCourseId = _selectedCourseId.asStateFlow()

    private val _visitedParts = MutableStateFlow<List<String>>(emptyList())
    val visitedParts = _visitedParts.asStateFlow()

    private val _subpartsMap = MutableStateFlow<Map<String, List<SubpartModel>>>(emptyMap())
    val subpartsMap = _subpartsMap.asStateFlow()

    private val _lessonsMap = MutableStateFlow<Map<String, List<LessonModel>>>(emptyMap())
    val lessonsMap = _lessonsMap.asStateFlow()

    val coursesState: StateFlow<List<CourseModel>> = firestoreRepository.streamCourses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val partsState: StateFlow<List<PartModel>> = _selectedCourseId
        .flatMapLatest { courseId ->
            if (courseId.isNotEmpty()) {
                firestoreRepository.streamParts(courseId)
            } else {
                kotlinx.coroutines.flow.flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userSelection: StateFlow<StudentSelection?> = if (uid.isNotEmpty()) {
        firestoreRepository.streamStudentSelection(uid)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    } else {
        MutableStateFlow(null)
    }

    init {
        viewModelScope.launch {
            userSelection.collect { selection ->
                selection?.let {
                    if (_selectedCourseId.value.isEmpty()) {
                        _selectedCourseId.value = it.courseId
                    }
                    _visitedParts.value = it.visitedParts
                }
            }
        }
    }

    fun selectCourse(courseId: String, courseName: String) {
        if (uid.isEmpty()) return
        _selectedCourseId.value = courseId
        firestoreRepository.selectCourse(uid, courseId, courseName)
    }

    fun loadSubparts(courseId: String, partId: String) {
        viewModelScope.launch {
            firestoreRepository.streamSubparts(courseId, partId).collect { subparts ->
                _subpartsMap.value = _subpartsMap.value + (partId to subparts)
            }
        }
    }

    fun loadLessons(courseId: String, partId: String, subpartId: String) {
        viewModelScope.launch {
            firestoreRepository.streamLessons(courseId, partId, subpartId).collect { lessons ->
                _lessonsMap.value = _lessonsMap.value + (subpartId to lessons)
            }
        }
    }

    fun markPartVisited(courseId: String, partId: String) {
        if (uid.isEmpty()) return
        val entry = "$courseId::$partId"
        if (!_visitedParts.value.contains(entry)) {
            _visitedParts.value = _visitedParts.value + entry
            viewModelScope.launch {
                firestoreRepository.markPartVisited(uid, courseId, partId)
            }
        }
    }

    fun restartCourse(courseId: String) {
        if (uid.isEmpty()) return
        val prefix = "$courseId::"
        _visitedParts.value = _visitedParts.value.filter { !it.startsWith(prefix) }
        viewModelScope.launch {
            firestoreRepository.restartCourse(uid, courseId)
        }
    }
}