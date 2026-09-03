package com.vastavik.computer.data.api

import com.vastavik.computer.data.model.LessonModel
import retrofit2.http.GET
import retrofit2.http.Path

interface VastavikApiService {
    @GET("api/lessons/{lessonId}")
    suspend fun getLesson(@Path("lessonId") lessonId: String): LessonModel

    @GET("api/courses/{courseId}/parts/{partId}/subparts/{subpartId}/lessons")
    suspend fun getLessons(
        @Path("courseId") courseId: String,
        @Path("partId") partId: String,
        @Path("subpartId") subpartId: String
    ): List<LessonModel>
}
