package com.emirhankarci.cookmate.presentation.auth

import android.graphics.BlurMaskFilter
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import com.emirhankarci.cookmate.R

@Composable
fun LoginScreen(
    state: AuthState,
    onEvent: (AuthEvent) -> Unit,
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit,
    onBackToWelcome: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    // Handle back button - go to welcome
    BackHandler {
        onBackToWelcome()
    }

    // Login başarılı olduğunda navigation yap
    LaunchedEffect(state.isLoginSuccessful) {
        if (state.isLoginSuccessful) {
            onEvent(AuthEvent.ClearSuccess)
            onLoginSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE8E8E8))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .drawBehind {
                    drawIntoCanvas { canvas ->
                        val paint = Paint().asFrameworkPaint()
                        paint.color = Color(0xFFFF6B6B).copy(alpha = 0.3f).toArgb()
                        paint.maskFilter = BlurMaskFilter(40f, BlurMaskFilter.Blur.NORMAL)

                        canvas.nativeCanvas.drawRoundRect(
                            0f,
                            0f,
                            size.width,
                            size.height,
                            16.dp.toPx(),
                            16.dp.toPx(),
                            paint
                        )
                    }
                },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Logo
                Surface(
                    modifier = Modifier
                        .size(64.dp)
                        .drawBehind {
                            drawIntoCanvas { canvas ->
                                val paint = Paint().asFrameworkPaint()
                                paint.color = Color(0xFFFF6B6B).copy(alpha = 0.6f).toArgb()
                                paint.maskFilter = BlurMaskFilter(30f, BlurMaskFilter.Blur.NORMAL)

                                canvas.nativeCanvas.drawRoundRect(
                                    0f,
                                    0f,
                                    size.width,
                                    size.height,
                                    16.dp.toPx(),
                                    16.dp.toPx(),
                                    paint
                                )
                            }
                        },
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFFF6B6B)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Image(
                            painter = painterResource(id = R.drawable.appicon),
                            contentDescription = "Logo",
                            modifier = Modifier
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Welcome Text
                Text(
                    text = "Welcome",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Log in to CookMate",
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Email Input
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "Email",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .drawBehind {
                                if (email.isNotEmpty()) {
                                    drawIntoCanvas { canvas ->
                                        val paint = Paint().asFrameworkPaint()
                                        paint.color = Color(0xFFFF6B6B).copy(alpha = 0.4f).toArgb()
                                        paint.maskFilter = BlurMaskFilter(20f, BlurMaskFilter.Blur.NORMAL)

                                        canvas.nativeCanvas.drawRoundRect(
                                            0f,
                                            0f,
                                            size.width,
                                            size.height,
                                            8.dp.toPx(),
                                            8.dp.toPx(),
                                            paint
                                        )
                                    }
                                }
                            },
                        placeholder = { Text("Enter your email address", fontSize = 14.sp) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Color(0xFFF5F5F5),
                            focusedContainerColor = Color.White,
                            focusedBorderColor = Color(0xFFFF6B6B),
                            unfocusedBorderColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(8.dp),
                        enabled = !state.isLoading
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Password Input
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "Password",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .drawBehind {
                                if (password.isNotEmpty()) {
                                    drawIntoCanvas { canvas ->
                                        val paint = Paint().asFrameworkPaint()
                                        paint.color = Color(0xFFFF6B6B).copy(alpha = 0.4f).toArgb()
                                        paint.maskFilter = BlurMaskFilter(20f, BlurMaskFilter.Blur.NORMAL)

                                        canvas.nativeCanvas.drawRoundRect(
                                            0f,
                                            0f,
                                            size.width,
                                            size.height,
                                            8.dp.toPx(),
                                            8.dp.toPx(),
                                            paint
                                        )
                                    }
                                }
                            },
                        placeholder = { Text("Enter your password", fontSize = 14.sp) },
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (passwordVisible) "Şifreyi gizle" else "Şifreyi göster",
                                    tint = Color.Gray
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Color(0xFFF5F5F5),
                            focusedContainerColor = Color.White,
                            focusedBorderColor = Color(0xFFFF6B6B),
                            unfocusedBorderColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(8.dp),
                        enabled = !state.isLoading
                    )
                }

                // Error message
                if (state.error != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = state.error,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(12.dp),
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Sign In Button with glow
                Button(
                    onClick = {
                        if (email.isNotBlank() && password.isNotBlank()) {
                            onEvent(AuthEvent.Login(email.trim(), password))
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .drawBehind {
                            drawIntoCanvas { canvas ->
                                val paint = Paint().asFrameworkPaint()
                                paint.color = Color(0xFFFF6B6B).copy(alpha = 0.5f).toArgb()
                                paint.maskFilter = BlurMaskFilter(25f, BlurMaskFilter.Blur.NORMAL)

                                canvas.nativeCanvas.drawRoundRect(
                                    0f,
                                    0f,
                                    size.width,
                                    size.height,
                                    8.dp.toPx(),
                                    8.dp.toPx(),
                                    paint
                                )
                            }
                        },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF6B6B)
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 8.dp
                    ),
                    shape = RoundedCornerShape(8.dp),
                    enabled = !state.isLoading && email.isNotBlank() && password.isNotBlank()
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White
                        )
                    } else {
                        Text(
                            text = "Log In",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Sign Up Text
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Don’t have an account?",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    TextButton(
                        onClick = onNavigateToRegister,
                        contentPadding = PaddingValues(0.dp),
                        enabled = !state.isLoading
                    ) {
                        Text(
                            text = "Sign Up",
                            fontSize = 12.sp,
                            color = Color(0xFFFF6B6B),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Footer Text
        Text(
            text = "v 1.0",
            fontSize = 11.sp,
            color = Color.Gray,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        )
    }
}
