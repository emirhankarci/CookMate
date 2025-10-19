package com.emirhankarci.seninlemutfakta.data.model

enum class Gender {
    FEMALE,
    MALE
}

enum class SessionStatus {
    WAITING,        // Eş henüz katılmadı
    IN_PROGRESS,    // İkisi de aktif
    PAUSED,         // Birisi bağlantıyı kaybetti
    COMPLETED,      // Tarif tamamlandı
    CANCELLED       // Session iptal edildi
}

enum class BadgeType {
    FIRST_TWO_RECIPES,
    COMPLETE_ALL_RECIPES,
    SPEED_MASTER,
    TEAMWORK_CHAMPION
}

enum class RecipeDifficulty(val level: Int) {
    VERY_EASY(1),
    EASY(2),
    MEDIUM(3),
    HARD(4),
    VERY_HARD(5)
}