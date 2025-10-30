# 🚀 Test Nasıl Çalıştırılır? - Başlangıç Rehberi

## 📚 Testler Nedir?

Testler, kodunuzun doğru çalışıp çalışmadığını otomatik olarak kontrol eden küçük programlardır.
- ✅ Hataları erkenden yakalar
- ✅ Kodunuzun güvenle değiştirilebilmesini sağlar
- ✅ Dokümantasyon görevi görür

---

## 🎯 1. İLK TEST - Android Studio'da (EN KOLAY)

### Adım 1: Test Dosyasını Aç

1. Android Studio'yu aç
2. Sol tarafta **Project** panelinde şu yolu izle:
   ```
   app → src → test → java → com.emirhankarci.cookmate →
   presentation → auth → AuthViewModelTest
   ```
3. `AuthViewModelTest.kt` dosyasına çift tıkla

### Adım 2: Testi Çalıştır

**Seçenek 1 - Tek Bir Test:**
1. Bir test fonksiyonunun yanında **yeşil ok** (▶️) göreceksin
2. Yeşil ok'a tıkla
3. "Run 'initial state is correct'" seçeneğini seç
4. Test çalışacak! 🎉

**Seçenek 2 - Tüm Testler:**
1. Sınıf adının (`class AuthViewModelTest`) yanındaki **yeşil ok**'a tıkla
2. "Run 'AuthViewModelTest'" seçeneğini seç
3. 11 test birden çalışacak!

**Seçenek 3 - Klavye Kısayolu:**
1. İmleci test fonksiyonunun içine koy
2. Windows/Linux: `Ctrl + Shift + F10`
3. Mac: `Cmd + Shift + R`

### Adım 3: Sonuçları İzle

Test çalıştığında altta **Run** penceresi açılacak:

```
✅ Yeşil çubuk = Tüm testler başarılı!
❌ Kırmızı çubuk = Bazı testler başarısız
⚠️ Sarı = Testler atlandı

Örnek Başarılı Sonuç:
Tests passed: 11 of 11 tests - 2.5s
  ✓ initial state is correct (150ms)
  ✓ login success updates state correctly (200ms)
  ✓ register success updates state correctly (180ms)
  ...
```

---

## 🎓 2. BÜTÜN TESTLERİ ÇALIŞTIR

### Android Studio'da:

1. Sol tarafta **Project** panelinde:
   ```
   app → src → test → java
   ```
2. `java` klasörüne **sağ tıkla**
3. **"Run 'Tests in 'cookmate...'"** seçeneğini seç
4. Tüm 40 test çalışacak! 🚀

### Komut Satırı (Terminal):

1. Android Studio'da altta **Terminal** sekmesini aç
2. Şu komutu yaz:
   ```bash
   ./gradlew test
   ```
3. Enter'a bas ve bekle (1-2 dakika sürer)

**Sonuç nerede?**
- Terminal'de özet göreceksin
- Detaylı rapor: `app/build/reports/tests/testDebugUnitTest/index.html`
- Bu HTML dosyasına çift tıkla, tarayıcıda açılacak

---

## 🎯 3. HANGİ TESTLER VAR?

### Test Dosyaları ve Ne Test Eder:

#### ✅ **AuthRepositoryTest** (5 test)
**Ne Test Eder:** Kullanıcı giriş/çıkış işlemleri
```kotlin
✓ getCurrentUser returns current user when logged in
✓ getCurrentUser returns null when not logged in
✓ isUserLoggedIn returns true when user is logged in
✓ isUserLoggedIn returns false when user is not logged in
✓ logout calls firebase signOut
```
**Süre:** ~0.5 saniye

---

#### ✅ **AuthViewModelTest** (11 test) - EN ÖNEMLİ
**Ne Test Eder:** Login/Register mantığı ve state yönetimi
```kotlin
✓ initial state is correct
✓ login success updates state correctly
✓ login failure updates state with error
✓ register success updates state correctly
✓ register fails when auth registration fails
✓ register fails when couple creation fails
✓ sendPasswordReset updates state correctly on success
✓ sendPasswordReset updates state with error on failure
✓ logout clears user state
✓ clearError removes error from state
✓ clearSuccess removes success flags from state
```
**Süre:** ~2 saniye

---

#### ✅ **RecipeListViewModelTest** (9 test)
**Ne Test Eder:** Tarif listeleme, sıralama, filtreleme
```kotlin
✓ initial state is correct
✓ loadRecipes success updates state with country and recipes
✓ loadRecipes failure updates state with error
✓ changeSortType updates sort type in state
✓ loadCompletedRecipes updates completed recipes list
✓ retry event reloads recipes with same country code
✓ selectRecipe logs recipe selection
✓ state helpers work correctly
✓ getSortedRecipes sorts correctly by difficulty
```
**Süre:** ~1.5 saniye

---

#### ✅ **CookingSessionViewModelTest** (10 test)
**Ne Test Eder:** Pişirme session'ları, adım tamamlama, dialog'lar
```kotlin
✓ initial state is correct
✓ clearError removes error from state
✓ dismissCoopDialog updates dialog state
✓ dismissWaitingDialog updates dialog state
✓ dismissCompletionDialog updates dialog state
✓ showCoopModeDialog updates dialog state
✓ startSession loads recipe and creates session
✓ startSession shows error when recipe not found
✓ completeCurrentStep calls repository with correct parameters
✓ pauseSession calls repository pause
```
**Süre:** ~2 saniye

---

## 📊 4. TEST SONUÇLARINI ANLAMA

### Başarılı Test:
```
✅ AuthViewModelTest > login success updates state correctly PASSED (250ms)
```
- ✅ Yeşil = Test başarılı
- 250ms = Test süresi

### Başarısız Test:
```
❌ AuthViewModelTest > login failure updates state with error FAILED (100ms)
   Expected: "Hatalı şifre"
   Actual: "Wrong password"
```
- ❌ Kırmızı = Test başarısız
- Hata mesajı ne beklediğini ve ne bulduğunu gösterir

### Test İstatistikleri:
```
Tests: 40
Passed: 40 ✅
Failed: 0
Skipped: 0
Duration: 8.5s
```

---

## 🎨 5. REPL (Debug Modu)

Test sırasında hata ayıklama:

1. Test kodunda bir satıra **breakpoint** koy (sol kenardaki satır numarasına tıkla)
2. Yeşil okun yanındaki **debug** ikonuna tıkla (🐛)
3. Test o satırda duracak
4. Variables panelinde değişkenlerin değerlerini görebilirsin

---

## 💡 6. İPUÇLARI VE PÜFLER

### Hızlı Test Çalıştırma:
- **Son çalıştırılan testi tekrar çalıştır:**
  - Windows/Linux: `Shift + F10`
  - Mac: `Ctrl + R`

### Test İsimleri:
Testler "backtick" (`) ile yazılmış, okunması kolay isimler:
```kotlin
@Test
fun `login success updates state correctly`() = runTest {
    // Test kodu...
}
```

### Coverage (Kapsama):
Test coverage'ı görmek için:
1. Test dosyasına sağ tıkla
2. **"Run '...' with Coverage"** seç
3. Hangi satırların test edildiğini göreceksin:
   - 🟢 Yeşil = Test edildi
   - 🔴 Kırmızı = Test edilmedi

---

## 🐛 7. SORUN GİDERME

### "Cannot resolve symbol 'runTest'"
**Çözüm:** Gradle sync yap
1. Üstteki **File → Sync Project with Gradle Files**

### "Task :app:test FAILED"
**Çözüm:** Clean build yap
```bash
./gradlew clean
./gradlew test
```

### Test çok yavaş çalışıyor
**Normal!** İlk çalıştırma yavaş olur:
- İlk çalıştırma: ~2 dakika
- Sonraki çalıştırmalar: ~10 saniye

### "No tests found"
**Çözüm:** Test klasörünü doğru seçtiğinden emin ol:
- Test dosyaları `src/test/java` içinde olmalı
- UI test dosyaları `src/androidTest/java` içinde

---

## 🎯 8. ŞİMDİ DENE!

### Basit Başlangıç:

1. **AuthViewModelTest.kt** dosyasını aç
2. İlk test fonksiyonunu (`initial state is correct`) bul
3. Yanındaki **yeşil ok**'a tıkla
4. 1-2 saniye sonra ✅ göreceksin!

### Sonra:

1. Tüm **AuthViewModelTest** testlerini çalıştır (11 test)
2. Sonuçları incele
3. Diğer test dosyalarını dene

---

## 📚 9. DAHA FAZLA ÖĞREN

### Test Yazma (İleri Seviye):

Kendi testini yazmak istersen:

```kotlin
@Test
fun `my first test`() = runTest {
    // Given (Hazırlık)
    val email = "test@test.com"

    // When (Aksiyon)
    viewModel.onEvent(AuthEvent.Login(email, "password"))

    // Then (Doğrulama)
    assertThat(viewModel.state.value.isLoading).isTrue()
}
```

### Test Türleri:

1. **Unit Test** (Yazdığımız): Tek bir fonksiyonu test eder
2. **Integration Test**: Birden fazla komponenti birlikte test eder
3. **UI Test**: Ekranda gördüğün butona basma, yazı yazma gibi işlemleri test eder

---

## ✅ ÖZET: ADIM ADIM

1. ✅ Android Studio'yu aç
2. ✅ `AuthViewModelTest.kt` dosyasını bul ve aç
3. ✅ Bir testin yanındaki yeşil ok'a tıkla
4. ✅ Alttaki Run penceresinde sonucu gör
5. ✅ Yeşil = Başarılı! 🎉

**İlk testini çalıştırdığında buraya dön ve bana sonucu söyle!** 😊

---

## 🎓 Test Çalıştırma Video Gibi Adımlar:

```
1. Android Studio > Project Panel (Sol)
   └─ app
      └─ src
         └─ test
            └─ java
               └─ com.emirhankarci.cookmate
                  └─ presentation
                     └─ auth
                        └─ 📄 AuthViewModelTest.kt (ÇİFT TIKLA)

2. Test Dosyası Açıldı!
   Gördüğün şey:
   ```kotlin
   class AuthViewModelTest {
       @Test                                    ← 👈 BURADA
       fun `initial state is correct`() = ...  ← YEŞİL OK VAR!
   ```

3. YEŞİL OK'A TIKLA ▶️

4. Menü Açıldı:
   ▶️ Run 'initial state is correct'  ← BUNA TIKLA
   🐛 Debug 'initial state is correct'

5. Test Çalışıyor... ⏳

6. SONUÇ! 🎉
   ✅ Tests passed: 1 of 1 tests - 0.2s
```

**Hadi dene! Çok kolay 😊**
