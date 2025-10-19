package com.emirhankarci.seninlemutfakta.presentation.navigation


// Değişiklik 1: Sealed class'ın constructor'ına bir 'title' parametresi ekliyoruz.
sealed class Screen(val title: String) {
    // Değişiklik 2: Her bir 'object' için bu 'title' değerini sağlıyoruz.
    object CountryList : Screen("Mutfaklar")
    object RecipeList : Screen("Tarifler")
    object Profile : Screen("Profil")
    object Login : Screen("Giriş Yap")
    object Register : Screen("Kayıt Ol")
    object UserSelection : Screen("Kullanıcı Seçimi")
    object CoopModeSelection : Screen("Oyun Modu")
    object CookingSession : Screen("Pişirme Ekranı")
}