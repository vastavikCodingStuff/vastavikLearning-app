package com.vastavik.computer.data.repository

import com.vastavik.computer.data.api.VastavikApiService
import com.vastavik.computer.data.model.LessonModel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VastavikApiRepository @Inject constructor(
    private val api: VastavikApiService
) {
    suspend fun getLesson(lessonId: String): LessonModel = api.getLesson(lessonId)
    suspend fun getLessons(courseId: String, partId: String, subpartId: String): List<LessonModel> =
        api.getLessons(courseId, partId, subpartId)
}
