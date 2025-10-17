package com.emirhankarci.seninlemutfakta

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import com.emirhankarci.seninlemutfakta.data.repository.FirebaseRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var firebaseRepository: FirebaseRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Test: Firebase bağlantısı
        lifecycleScope.launch {
            testFirebase()
        }

        setContent {
            // Compose UI gelecek
        }
    }

    private suspend fun testFirebase() {
        val result = firebaseRepository.getAllCountries()

        result.onSuccess { countries ->
            Log.d("MainActivity", "✅ Firebase çalışıyor! Ülke sayısı: ${countries.size}")
            countries.forEach { country ->
                Log.d("MainActivity", "Ülke: ${country.name} - ${country.flagEmoji}")
            }
        }

        result.onFailure { error ->
            Log.e("MainActivity", "❌ Firebase hatası: ${error.message}")
        }
    }
}