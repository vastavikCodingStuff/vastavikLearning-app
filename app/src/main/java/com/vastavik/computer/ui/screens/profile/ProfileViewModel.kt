package com.vastavik.computer.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.vastavik.computer.data.model.UserModel
import com.vastavik.computer.data.repository.AuthRepository
import com.vastavik.computer.data.repository.FirestoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val firestoreRepository: FirestoreRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val uid: String
        get() = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    private val _userProfile = MutableStateFlow<UserModel?>(null)
    val userProfile = _userProfile.asStateFlow()

    private val _selectedCourseName = MutableStateFlow("")
    val selectedCourseName = _selectedCourseName.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        if (uid.isEmpty()) return
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            try {
                firestoreRepository.streamUserProfile(uid).collect { user ->
                    _userProfile.value = user
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _isLoading.value = false
                _error.value = e.message ?: "Failed to load profile"
            }
        }
    }

    fun changeCourse(courseId: String, courseName: String) {
        if (uid.isEmpty()) return
        _selectedCourseName.value = courseName
        firestoreRepository.selectCourse(uid, courseId, courseName)
    }

    fun logout() {
        authRepository.signOut()
    }

    fun clearError() {
        _error.value = null
    }
}
