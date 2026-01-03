package com.example.aac.ui.login

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

class LoginActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            LoginScreen(
                onKakaoLogin = { },
                onNaverLogin = { },
                onGoogleLogin = { },
                onGuestLogin = { }
            )
        }
    }
}
