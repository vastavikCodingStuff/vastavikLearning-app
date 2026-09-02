package com.vastavik.computer.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.vastavik.computer.data.model.BannerModel
import com.vastavik.computer.data.model.CourseModel
import com.vastavik.computer.data.model.PopularTopicModel
import com.vastavik.computer.data.model.StudentSelection
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
class HomeViewModel @Inject constructor(
    private val firestoreRepository: FirestoreRepository
) : ViewModel() {

    private val uid: String
        get() = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    private val _selectedCourseId = MutableStateFlow("")
    val selectedCourseId = _selectedCourseId.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    val coursesState: StateFlow<List<CourseModel>> = firestoreRepository.streamCourses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bannersState: StateFlow<List<BannerModel>> = firestoreRepository.streamBanners()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val popularTopicsState: StateFlow<List<PopularTopicModel>> = firestoreRepository.streamPopularTopics()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userSelection: StateFlow<StudentSelection?> = if (uid.isNotEmpty()) {
        firestoreRepository.streamStudentSelection(uid)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    } else {
        MutableStateFlow(null)
    }

    fun selectCourse(courseId: String, courseName: String) {
        if (uid.isEmpty()) return
        _selectedCourseId.value = courseId
        firestoreRepository.selectCourse(uid, courseId, courseName)
    }

    fun searchCourses(query: String) {
        _searchQuery.value = query
    }
}
