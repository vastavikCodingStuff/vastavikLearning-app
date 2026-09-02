package com.vastavik.computer.ui.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.vastavik.computer.data.model.CourseModel
import com.vastavik.computer.data.model.UserModel
import com.vastavik.computer.data.repository.AuthRepository
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
class OnboardingViewModel @Inject constructor(
    private val firestoreRepository: FirestoreRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val uid: String
        get() = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    private val _userSetupData = MutableStateFlow(UserModel())
    val userSetupData = _userSetupData.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    val coursesState: StateFlow<List<CourseModel>> = firestoreRepository.streamCourses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateName(name: String) {
        _userSetupData.value = _userSetupData.value.copy(name = name)
    }

    fun updateClass(studentClass: String) {
        _userSetupData.value = _userSetupData.value.copy(studentClass = studentClass)
    }

    fun updateBoard(board: String) {
        _userSetupData.value = _userSetupData.value.copy(board = board)
    }

    fun updateSchool(school: String) {
        _userSetupData.value = _userSetupData.value.copy(school = school)
    }

    fun saveUserSetup() {
        if (uid.isEmpty()) return
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            try {
                val user = _userSetupData.value.copy(uid = uid)
                firestoreRepository.createUserProfile(user)
                _isLoading.value = false
            } catch (e: Exception) {
                _isLoading.value = false
                _error.value = e.message ?: "Failed to save user setup"
            }
        }
    }

    fun selectCourse(courseId: String, courseName: String) {
        if (uid.isEmpty()) return
        firestoreRepository.selectCourse(uid, courseId, courseName)
    }

    fun clearError() {
        _error.value = null
    }
}
