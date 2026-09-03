package com.vastavik.computer.data.repository

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.vastavik.computer.data.model.AppUpdateInfo
import com.vastavik.computer.data.model.BannerModel
import com.vastavik.computer.data.model.CodingChallenge
import com.vastavik.computer.data.model.CourseModel
import com.vastavik.computer.data.model.LessonModel
import com.vastavik.computer.data.model.PYQModel
import com.vastavik.computer.data.model.PartModel
import com.vastavik.computer.data.model.PopularTopicModel
import com.vastavik.computer.data.model.QuizModel
import com.vastavik.computer.data.model.StudentSelection
import com.vastavik.computer.data.model.SubpartModel
import com.vastavik.computer.data.model.UserModel
import com.vastavik.computer.utils.Constants
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreRepository {
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    // --- Courses ---
    fun streamCourses(): Flow<List<CourseModel>> = callbackFlow {
        val reg = db.collection(Constants.COLLECTION_COURSES)
            .orderBy(Constants.FIELD_ORDER, Query.Direction.ASCENDING)
            .addSnapshotListener { snap, err ->
                if (err != null) { trySend(emptyList()); return@addSnapshotListener }
                val list = snap?.documents?.map { CourseModel.fromSnapshot(it) } ?: emptyList()
                trySend(list)
            }
        awaitClose { reg.remove() }
    }

    fun streamParts(courseId: String): Flow<List<PartModel>> = callbackFlow {
        val reg = db.collection(Constants.COLLECTION_COURSES).document(courseId)
            .collection(Constants.COLLECTION_PARTS)
            .orderBy(Constants.FIELD_ORDER, Query.Direction.ASCENDING)
            .addSnapshotListener { snap, err ->
                if (err != null) { trySend(emptyList()); return@addSnapshotListener }
                val list = snap?.documents?.map { PartModel.fromSnapshot(it) } ?: emptyList()
                trySend(list)
            }
        awaitClose { reg.remove() }
    }

    fun streamSubparts(courseId: String, partId: String): Flow<List<SubpartModel>> = callbackFlow {
        val reg = db.collection(Constants.COLLECTION_COURSES).document(courseId)
            .collection(Constants.COLLECTION_PARTS).document(partId)
            .collection(Constants.COLLECTION_SUBPARTS)
            .orderBy(Constants.FIELD_ORDER, Query.Direction.ASCENDING)
            .addSnapshotListener { snap, err ->
                if (err != null) { trySend(emptyList()); return@addSnapshotListener }
                val list = snap?.documents?.map { SubpartModel.fromSnapshot(it) } ?: emptyList()
                trySend(list)
            }
        awaitClose { reg.remove() }
    }

    fun streamLessons(courseId: String, partId: String, subpartId: String): Flow<List<LessonModel>> = callbackFlow {
        val reg = db.collection(Constants.COLLECTION_COURSES).document(courseId)
            .collection(Constants.COLLECTION_PARTS).document(partId)
            .collection(Constants.COLLECTION_SUBPARTS).document(subpartId)
            .collection(Constants.COLLECTION_LESSONS)
            .orderBy(Constants.FIELD_ORDER, Query.Direction.ASCENDING)
            .addSnapshotListener { snap, err ->
                if (err != null) { trySend(emptyList()); return@addSnapshotListener }
                val list = snap?.documents?.map { LessonModel.fromSnapshot(it) } ?: emptyList()
                trySend(list)
            }
        awaitClose { reg.remove() }
    }

    // --- Banners & Popular ---
    fun streamBanners(): Flow<List<BannerModel>> = callbackFlow {
        val reg = db.collection(Constants.COLLECTION_BANNERS)
            .orderBy(Constants.FIELD_ORDER, Query.Direction.ASCENDING)
            .addSnapshotListener { snap, err ->
                if (err != null) { trySend(emptyList()); return@addSnapshotListener }
                val list = snap?.documents?.map { BannerModel.fromSnapshot(it) } ?: emptyList()
                trySend(list)
            }
        awaitClose { reg.remove() }
    }

    fun streamPopularTopics(): Flow<List<PopularTopicModel>> = callbackFlow {
        val reg = db.collection(Constants.COLLECTION_POPULAR_TOPICS)
            .orderBy(Constants.FIELD_ORDER, Query.Direction.ASCENDING)
            .addSnapshotListener { snap, err ->
                if (err != null) { trySend(emptyList()); return@addSnapshotListener }
                val list = snap?.documents?.map { PopularTopicModel.fromSnapshot(it) } ?: emptyList()
                trySend(list)
            }
        awaitClose { reg.remove() }
    }

    // --- Quizzes / Challenges / PYQ ---
    fun streamQuizzes(): Flow<List<QuizModel>> = callbackFlow {
        val reg = db.collection(Constants.COLLECTION_QUIZZES)
            .addSnapshotListener { snap, err ->
                if (err != null) { trySend(emptyList()); return@addSnapshotListener }
                val list = snap?.documents?.map { QuizModel.fromSnapshot(it) } ?: emptyList()
                trySend(list)
            }
        awaitClose { reg.remove() }
    }

    fun streamCodingChallenges(): Flow<List<CodingChallenge>> = callbackFlow {
        val reg = db.collection(Constants.COLLECTION_CODING_CHALLENGES)
            .addSnapshotListener { snap, err ->
                if (err != null) { trySend(emptyList()); return@addSnapshotListener }
                val list = snap?.documents?.map { CodingChallenge.fromSnapshot(it) } ?: emptyList()
                trySend(list)
            }
        awaitClose { reg.remove() }
    }

    fun streamPYQs(): Flow<List<PYQModel>> = callbackFlow {
        val reg = db.collection(Constants.COLLECTION_PYQ)
            .addSnapshotListener { snap, err ->
                if (err != null) { trySend(emptyList()); return@addSnapshotListener }
                val list = snap?.documents?.map { PYQModel.fromSnapshot(it) } ?: emptyList()
                trySend(list)
            }
        awaitClose { reg.remove() }
    }

    suspend fun createQuiz(quiz: QuizModel) {
        db.collection(Constants.COLLECTION_QUIZZES).add(quiz.toMap()).await()
    }

    // --- App Update (self-hosted APK) ---
    suspend fun getAppUpdateInfo(): AppUpdateInfo {
        val doc = db.collection(Constants.COLLECTION_ADMIN_SETTINGS)
            .document(Constants.ADMIN_SETTINGS_UPDATE_DOC).get().await()
        return if (doc.exists()) AppUpdateInfo.fromSnapshot(doc) else AppUpdateInfo()
    }

    // --- User ---
    fun streamUserProfile(uid: String): Flow<UserModel?> = callbackFlow {
        val reg = db.collection(Constants.COLLECTION_USERS).document(uid)
            .addSnapshotListener { snap, err ->
                if (err != null) { trySend(null); return@addSnapshotListener }
                if (snap != null && snap.exists()) trySend(UserModel.fromSnapshot(snap)) else trySend(null)
            }
        awaitClose { reg.remove() }
    }

    suspend fun createUserProfile(user: UserModel) {
        db.collection(Constants.COLLECTION_USERS).document(user.uid).set(user.toMap()).await()
    }

    // --- Student selection / progress ---
    fun streamStudentSelection(uid: String): Flow<StudentSelection?> = callbackFlow {
        val reg = db.collection(Constants.COLLECTION_STUDENT_SELECTIONS).document(uid)
            .addSnapshotListener { snap, err ->
                if (err != null) { trySend(null); return@addSnapshotListener }
                if (snap != null && snap.exists()) trySend(StudentSelection.fromSnapshot(snap)) else trySend(null)
            }
        awaitClose { reg.remove() }
    }

    fun selectCourse(uid: String, courseId: String, courseName: String) {
        db.collection(Constants.COLLECTION_STUDENT_SELECTIONS).document(uid)
            .set(mapOf("courseId" to courseId, "courseName" to courseName, "selectedAt" to FieldValue.serverTimestamp()), com.google.firebase.firestore.SetOptions.merge())
    }

    fun markPartVisited(uid: String, courseId: String, partId: String) {
        val entry = "$courseId::$partId"
        db.collection(Constants.COLLECTION_STUDENT_SELECTIONS).document(uid)
            .update("visitedParts", FieldValue.arrayUnion(entry))
    }

    fun restartCourse(uid: String, courseId: String) {
        // Remove visitedParts entries for this course — fetch then filter
        db.collection(Constants.COLLECTION_STUDENT_SELECTIONS).document(uid).get()
            .addOnSuccessListener { snap ->
                val list = (snap.get("visitedParts") as? List<String>) ?: emptyList()
                val prefix = "$courseId::"
                val filtered = list.filter { !it.startsWith(prefix) }
                snap.reference.update("visitedParts", filtered)
            }
    }
}
