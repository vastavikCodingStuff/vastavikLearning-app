package com.vastavik.computer.ui.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vastavik.computer.data.api.model.NoteResponse
import com.vastavik.computer.data.repository.VastavikApiRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotesViewModel @Inject constructor(
    private val repository: VastavikApiRepository
) : ViewModel() {

    private val _notes = MutableStateFlow<List<NoteResponse>>(emptyList())
    val notes = _notes.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    init {
        loadNotes()
    }

    fun loadNotes() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val result = repository.listNotes()
            result.onSuccess { list ->
                if (list.isNotEmpty()) {
                    _notes.value = list
                } else if (_notes.value.isEmpty()) {
                    _notes.value = listOf(
                        NoteResponse(
                            id = "default_1",
                            title = "OOP Notes",
                            content = "Classes, objects, inheritance, polymorphism, abstraction.",
                            tag = "Java"
                        ),
                        NoteResponse(
                            id = "default_2",
                            title = "Array Methods",
                            content = "sort(), binarySearch(), copyOf(), fill().",
                            tag = "Java"
                        )
                    )
                }
            }.onFailure { err ->
                _error.value = err.message
                if (_notes.value.isEmpty()) {
                    _notes.value = listOf(
                        NoteResponse(
                            id = "offline_1",
                            title = "OOP Notes",
                            content = "Classes, objects, inheritance, polymorphism, abstraction.",
                            tag = "Java"
                        )
                    )
                }
            }
            _isLoading.value = false
        }
    }

    fun createNote(title: String, content: String, tag: String = "General") {
        viewModelScope.launch {
            val result = repository.createNote(title, content, tag)
            result.onSuccess { created ->
                _notes.value = listOf(created) + _notes.value.filter { it.id != created.id }
            }.onFailure {
                // Offline fallback note
                val localNote = NoteResponse(
                    id = "local_${System.currentTimeMillis()}",
                    title = title,
                    content = content,
                    tag = tag
                )
                _notes.value = listOf(localNote) + _notes.value
            }
        }
    }

    fun deleteNote(noteId: String) {
        viewModelScope.launch {
            _notes.value = _notes.value.filter { it.id != noteId }
            repository.deleteNote(noteId)
        }
    }
}
