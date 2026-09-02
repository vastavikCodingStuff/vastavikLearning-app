package com.vastavik.computer.utils

object Constants {
    // Firebase Collections
    const val COLLECTION_USERS = "users"
    const val COLLECTION_COURSES = "courses"
    const val COLLECTION_PARTS = "parts"
    const val COLLECTION_SUBPARTS = "subparts"
    const val COLLECTION_LESSONS = "lessons"
    const val COLLECTION_QUIZZES = "quizzes"
    const val COLLECTION_BANNERS = "banners"
    const val COLLECTION_POPULAR_TOPICS = "popularTopics"
    const val COLLECTION_NOTES = "notes"
    const val COLLECTION_TRANSACTIONS = "transactions"
    const val COLLECTION_SUBSCRIPTIONS = "subscriptions"
    const val COLLECTION_SUBSCRIPTION_PLANS = "subscriptionPlans"
    const val COLLECTION_CHAT_SESSIONS = "chatSessions"
    const val COLLECTION_STUDENT_SELECTIONS = "studentSelections"
    const val COLLECTION_PYQ = "pyq"
    const val COLLECTION_CODING_CHALLENGES = "codingChallenges"
    const val COLLECTION_ADMIN_SETTINGS = "adminSettings"
    const val COLLECTION_FCM_TOKENS = "fcmTokens"

    // Firestore Fields
    const val FIELD_USER_ID = "uid"
    const val FIELD_COURSE_ID = "courseId"
    const val FIELD_PART_ID = "partId"
    const val FIELD_SUBPART_ID = "subpartId"
    const val FIELD_LESSON_ID = "lessonId"
    const val FIELD_ORDER = "order"
    const val FIELD_CREATED_AT = "createdAt"
    const val FIELD_UPDATED_AT = "updatedAt"
    const val FIELD_IS_PUBLISHED = "isPublished"
    const val FIELD_IS_PREMIUM = "isPremium"
    const val FIELD_STREAK_COUNT = "streakCount"
    const val FIELD_LAST_ACTIVE_DATE = "lastActiveDate"
    const val FIELD_TOTAL_LESSONS_COMPLETED = "totalLessonsCompleted"

    // Default Values
    const val DEFAULT_BOARD = "ICSE"
    const val DEFAULT_LANGUAGE = "Java"
    const val DEFAULT_ROLE = "student"
    const val DEFAULT_THEME = "system"
    const val DEFAULT_SUBSCRIPTION_STATUS = "free"
    const val DEFAULT_PLAN_ID = "none"

    // Quiz Defaults
    const val DEFAULT_QUIZ_TIME_LIMIT = 30
    const val DEFAULT_QUIZ_DIFFICULTY = "Medium"
    const val DEFAULT_QUIZ_TYPE = "mcq"
    const val DEFAULT_QUIZ_SUBJECT = "General"

    // Subscription
    const val SUBSCRIPTION_STATUS_FREE = "free"
    const val SUBSCRIPTION_STATUS_ACTIVE = "active"
    const val SUBSCRIPTION_STATUS_EXPIRED = "expired"
    const val SUBSCRIPTION_STATUS_CANCELLED = "cancelled"

    // Transaction Status
    const val TRANSACTION_STATUS_PENDING = "pending"
    const val TRANSACTION_STATUS_SUCCESS = "success"
    const val TRANSACTION_STATUS_FAILED = "failed"
    const val TRANSACTION_STATUS_REFUNDED = "refunded"

    // Deep Link
    const val DEEP_LINK_HOST = "vastavikcomputers.firebaseapp.com"
    const val DEEP_LINK_SCHEME = "https"

    // Notification
    const val NOTIFICATION_CHANNEL_ID = "vastavik_notifications"
    const val NOTIFICATION_CHANNEL_NAME = "Vastavik Notifications"

    // Shared Preferences
    const val PREFS_NAME = "vastavik_prefs"
    const val PREF_USER_ID = "user_id"
    const val PREF_IS_LOGGED_IN = "is_logged_in"
    const val PREF_FIRST_LAUNCH = "first_launch"
    const val PREF_DARK_MODE = "dark_mode"
    const val PREF_LANGUAGE = "language"

    // Timeouts
    const val SPLASH_TIMEOUT_MS = 2000L
    const val DEBOUNCE_MS = 300L

    // Pagination
    const val PAGE_SIZE = 20
    const val MAX_CHAT_MESSAGES = 50
}
