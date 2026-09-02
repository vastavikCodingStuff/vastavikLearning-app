package com.vastavik.computer.ui.screens.chat

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ChatViewModel : ViewModel() {
    private val _messages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage("Hello! I am Vastavik AI. Ask me anything about Java, Python, JavaScript, or SQL for Class 5-12!", isUser = false)
        )
    )
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    fun addMessage(message: ChatMessage) {
        _messages.value = _messages.value + message
    }

    fun clearMessages() {
        _messages.value = listOf(
            ChatMessage("Hello! I am Vastavik AI. Ask me anything about Java, Python, JavaScript, or SQL for Class 5-12!", isUser = false)
        )
    }

    companion object {
        private var instance: ChatViewModel? = null
        fun getInstance(): ChatViewModel {
            if (instance == null) instance = ChatViewModel()
            return instance!!
        }
    }
}
