package com.example.aac.ui.features.login

import android.content.res.Configuration
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aac.R

/* ======================================================
   UI Constants
   - 로그인 화면에서 공통으로 사용하는 값들
   ====================================================== */
private const val LANDSCAPE_BUTTON_WIDTH = 560

/* ======================================================
   LoginScreen
   - 로그인 화면 진입점
   - 화면 방향에 따라 가로/세로 레이아웃 분기
   ====================================================== */
@Composable
fun LoginScreen(
    onKakaoLogin: () -> Unit,
    onNaverLogin: () -> Unit,
    onGoogleLogin: () -> Unit,
    onGuestLogin: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val isLandscape =
        configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    if (isLandscape) {
        LoginLandscape(
            onKakaoLogin,
            onNaverLogin,
            onGoogleLogin,
            onGuestLogin
        )
    } else {
        LoginPortrait(
            onKakaoLogin,
            onNaverLogin,
            onGoogleLogin,
            onGuestLogin
        )
    }
}

// LoginLandscape - 가로 화면 전용 로그인 UI
@Composable
private fun LoginLandscape(
    onKakaoLogin: () -> Unit,
    onNaverLogin: () -> Unit,
    onGoogleLogin: () -> Unit,
    onGuestLogin: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            Text(
                text = "모두와 AAC",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(10.dp))

            Image(
                painter = painterResource(R.drawable.ic_app_logo),
                contentDescription = "App Logo",
                modifier = Modifier.size(72.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            LoginImageButton(
                drawableRes = R.drawable.ic_kakao_login_button_land,
                modifier = Modifier.width(LANDSCAPE_BUTTON_WIDTH.dp),
                onClick = onKakaoLogin
            )

            Spacer(modifier = Modifier.height(10.dp))

            LoginImageButton(
                drawableRes = R.drawable.ic_naver_login_button_land,
                modifier = Modifier.width(LANDSCAPE_BUTTON_WIDTH.dp),
                onClick = onNaverLogin
            )

            Spacer(modifier = Modifier.height(10.dp))

            LoginImageButton(
                drawableRes = R.drawable.ic_google_login_button_land,
                modifier = Modifier.width(LANDSCAPE_BUTTON_WIDTH.dp),
                onClick = onGoogleLogin
            )

            Spacer(modifier = Modifier.height(10.dp))

            LoginImageButton(
                drawableRes = R.drawable.ic_guest_login_button_land,
                modifier = Modifier.width(LANDSCAPE_BUTTON_WIDTH.dp),
                onClick = onGuestLogin
            )
        }
    }
}

// LoginPortrait - 세로 화면 전용 로그인 UI
@Composable
private fun LoginPortrait(
    onKakaoLogin: () -> Unit,
    onNaverLogin: () -> Unit,
    onGoogleLogin: () -> Unit,
    onGuestLogin: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = "사용자의 맥락을 이해하는 똑똑한 AI AAC",
                fontSize = 13.sp,
                color = Color(0xFF2678C8),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "모두와 AAC",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(20.dp))

            Image(
                painter = painterResource(R.drawable.ic_app_logo),
                contentDescription = "App Logo",
                modifier = Modifier.size(120.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            LoginDivider()

            Spacer(modifier = Modifier.height(24.dp))

            LoginImageButton(
                drawableRes = R.drawable.ic_kakao_login_button_port,
                modifier = Modifier.fillMaxWidth(),
                onClick = onKakaoLogin
            )

            Spacer(modifier = Modifier.height(12.dp))

            LoginImageButton(
                drawableRes = R.drawable.ic_naver_login_button_port,
                modifier = Modifier.fillMaxWidth(),
                onClick = onNaverLogin
            )

            Spacer(modifier = Modifier.height(12.dp))

            LoginImageButton(
                drawableRes = R.drawable.ic_google_login_button_port,
                modifier = Modifier.fillMaxWidth(),
                onClick = onGoogleLogin
            )

            Spacer(modifier = Modifier.height(12.dp))

            LoginImageButton(
                drawableRes = R.drawable.ic_guest_login_button_port,
                modifier = Modifier.fillMaxWidth(),
                onClick = onGuestLogin
            )
        }
    }
}

/* ======================================================
   LoginDivider
   - 세로 화면 로그인 영역 구분용
   ====================================================== */
@Composable
private fun LoginDivider() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Divider(
            modifier = Modifier.weight(1f),
            color = Color(0xFF7E7E7E)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = "로그인/회원가입",
            fontSize = 13.sp,
            color = Color(0xFF7E7E7E)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Divider(
            modifier = Modifier.weight(1f),
            color = Color(0xFF7E7E7E)
        )
    }
}

/* ======================================================
   LoginImageButton
   - 공통 로그인 이미지 버튼
   - orientation에 따라 ContentScale만 분기
   ====================================================== */
@Composable
private fun LoginImageButton(
    @DrawableRes drawableRes: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val isPortrait =
        configuration.orientation == Configuration.ORIENTATION_PORTRAIT

    Image(
        painter = painterResource(drawableRes),
        contentDescription = null,
        contentScale = if (isPortrait) {
            ContentScale.FillWidth
        } else {
            ContentScale.Fit
        },
        modifier = modifier
            .then(
                if (isPortrait) Modifier.wrapContentHeight() else Modifier
            )
            .clickable(onClick = onClick)
    )
}

/* ======================================================
   Preview
   ====================================================== */
@Preview(widthDp = 800, heightDp = 360, showBackground = true)
@Composable
fun LoginLandscapePreview() {
    LoginScreen({}, {}, {}, {})
}

@Preview(showBackground = true)
@Composable
fun LoginPortraitPreview() {
    LoginScreen({}, {}, {}, {})
}
