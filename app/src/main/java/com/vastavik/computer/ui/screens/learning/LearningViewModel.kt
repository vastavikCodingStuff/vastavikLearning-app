package com.vastavik.computer.ui.screens.learning

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.vastavik.computer.data.api.model.PartItem
import com.vastavik.computer.data.api.model.SubpartItem
import com.vastavik.computer.data.model.CourseModel
import com.vastavik.computer.data.model.LessonModel
import com.vastavik.computer.data.model.PartModel
import com.vastavik.computer.data.model.StudentSelection
import com.vastavik.computer.data.model.SubpartModel
import com.vastavik.computer.data.repository.FirestoreRepository
import com.vastavik.computer.data.repository.VastavikApiRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class LearningViewModel @Inject constructor(
    private val firestoreRepository: FirestoreRepository,
    private val apiRepository: VastavikApiRepository
) : ViewModel() {

    private val uid: String
        get() = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    private val _selectedCourseId = MutableStateFlow("")
    val selectedCourseId = _selectedCourseId.asStateFlow()

    private val _courses = MutableStateFlow<List<CourseModel>>(emptyList())
    val courses = _courses.asStateFlow()

    private val _curriculumParts = MutableStateFlow<List<PartItem>>(emptyList())
    val curriculumParts = _curriculumParts.asStateFlow()

    private val _isLoadingCurriculum = MutableStateFlow(false)
    val isLoadingCurriculum = _isLoadingCurriculum.asStateFlow()

    private val _visitedParts = MutableStateFlow<List<String>>(emptyList())
    val visitedParts = _visitedParts.asStateFlow()

    private val _subpartsMap = MutableStateFlow<Map<String, List<SubpartModel>>>(emptyMap())
    val subpartsMap = _subpartsMap.asStateFlow()

    private val _lessonsMap = MutableStateFlow<Map<String, List<LessonModel>>>(emptyMap())
    val lessonsMap = _lessonsMap.asStateFlow()

    val coursesState: StateFlow<List<CourseModel>> = _courses.asStateFlow()

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
        // 1. Stream courses from Firestore
        viewModelScope.launch {
            firestoreRepository.streamCourses().collect { fsCourses ->
                if (fsCourses.isNotEmpty()) {
                    val currentList = _courses.value
                    val merged = (fsCourses + currentList).distinctBy { it.id }
                    _courses.value = merged
                    if (_selectedCourseId.value.isEmpty()) {
                        _selectedCourseId.value = merged.first().id
                    }
                }
            }
        }

        // 2. Fetch courses from Backend catalog (ensures dynamic courses from admin are available)
        viewModelScope.launch {
            try {
                val catalogResult = apiRepository.getHomeCatalog()
                catalogResult.getOrNull()?.courses?.let { apiCourses ->
                    if (apiCourses.isNotEmpty()) {
                        val mapped = apiCourses.map { c ->
                            CourseModel(
                                id = c.id,
                                title = c.title,
                                description = c.description,
                                iconName = c.iconName,
                                color = c.color,
                                order = c.order
                            )
                        }
                        val currentList = _courses.value
                        val merged = (currentList + mapped).distinctBy { it.id }
                        _courses.value = merged
                        if (_selectedCourseId.value.isEmpty() && merged.isNotEmpty()) {
                            _selectedCourseId.value = merged.first().id
                        }
                    }
                }
            } catch (_: Exception) {}
        }

        // 3. Collect user selection
        viewModelScope.launch {
            userSelection.collect { selection ->
                selection?.let {
                    if (_selectedCourseId.value.isEmpty() && it.courseId.isNotEmpty()) {
                        _selectedCourseId.value = it.courseId
                    }
                    _visitedParts.value = it.visitedParts
                }
            }
        }

        // 4. Automatically reload curriculum whenever selected course changes
        viewModelScope.launch {
            _selectedCourseId.collect { courseId ->
                if (courseId.isNotEmpty()) {
                    loadCurriculum(courseId)
                }
            }
        }
    }

    fun selectCourse(courseId: String, courseName: String) {
        _selectedCourseId.value = courseId
        if (uid.isNotEmpty()) {
            firestoreRepository.selectCourse(uid, courseId, courseName)
        }
        loadCurriculum(courseId)
    }

    fun loadCurriculum(courseId: String, force: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoadingCurriculum.value = true
            var loaded = false
            try {
                val result = apiRepository.getCurriculum(courseId, force)
                val resp = result.getOrNull()
                if (resp != null && resp.parts.isNotEmpty()) {
                    _curriculumParts.value = resp.parts
                    loaded = true
                }
            } catch (_: Exception) {}

            if (!loaded) {
                try {
                    val fallbackParts = withTimeoutOrNull(3000) {
                        firestoreRepository.streamParts(courseId).first()
                    }
                    if (fallbackParts != null && fallbackParts.isNotEmpty()) {
                        val converted = fallbackParts.map { p ->
                            PartItem(
                                partId = p.id,
                                title = p.title,
                                order = p.order,
                                subparts = emptyList()
                            )
                        }
                        _curriculumParts.value = converted
                    }
                } catch (_: Exception) {}
            }
            _isLoadingCurriculum.value = false
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _isLoadingCurriculum.value = true
            try {
                val catalogResult = apiRepository.getHomeCatalog(force = true)
                catalogResult.getOrNull()?.courses?.let { apiCourses ->
                    if (apiCourses.isNotEmpty()) {
                        val mapped = apiCourses.map { c ->
                            CourseModel(
                                id = c.id,
                                title = c.title,
                                description = c.description,
                                iconName = c.iconName,
                                color = c.color,
                                order = c.order
                            )
                        }
                        val currentList = _courses.value
                        val merged = (mapped + currentList).distinctBy { it.id }
                        _courses.value = merged
                    }
                }
            } catch (_: Exception) {}
            val courseId = _selectedCourseId.value
            if (courseId.isNotEmpty()) {
                loadCurriculum(courseId, force = true)
            } else {
                _isLoadingCurriculum.value = false
            }
        }
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