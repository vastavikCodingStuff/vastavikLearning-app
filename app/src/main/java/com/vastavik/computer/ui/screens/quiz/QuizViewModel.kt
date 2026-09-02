package com.vastavik.computer.ui.screens.quiz

import androidx.lifecycle.ViewModel
import com.vastavik.computer.data.model.QuizModel
import com.vastavik.computer.data.model.QuizQuestion
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class QuizViewModel @Inject constructor() : ViewModel() {

    private val _quizData = MutableStateFlow<QuizModel?>(null)
    val quizData = _quizData.asStateFlow()

    private val _currentQuestionIndex = MutableStateFlow(0)
    val currentQuestionIndex = _currentQuestionIndex.asStateFlow()

    private val _selectedAnswers = MutableStateFlow<Map<Int, Int>>(emptyMap())
    val selectedAnswers = _selectedAnswers.asStateFlow()

    private val _isFinished = MutableStateFlow(false)
    val isFinished = _isFinished.asStateFlow()

    private val _score = MutableStateFlow(0)
    val score = _score.asStateFlow()

    fun setQuizData(quiz: QuizModel) {
        _quizData.value = quiz
        _currentQuestionIndex.value = 0
        _selectedAnswers.value = emptyMap()
        _isFinished.value = false
        _score.value = 0
    }

    fun selectAnswer(questionIndex: Int, answerIndex: Int) {
        _selectedAnswers.value = _selectedAnswers.value + (questionIndex to answerIndex)
    }

    fun nextQuestion() {
        val quiz = _quizData.value ?: return
        val nextIndex = _currentQuestionIndex.value + 1
        if (nextIndex < quiz.questions.size) {
            _currentQuestionIndex.value = nextIndex
        }
    }

    fun submitQuiz() {
        val quiz = _quizData.value ?: return
        var correct = 0
        quiz.questions.forEachIndexed { index, question ->
            val selected = _selectedAnswers.value[index]
            if (selected == question.answerIndex) {
                correct++
            }
        }
        _score.value = correct
        _isFinished.value = true
    }

    fun resetQuiz() {
        _currentQuestionIndex.value = 0
        _selectedAnswers.value = emptyMap()
        _isFinished.value = false
        _score.value = 0
    }
}
