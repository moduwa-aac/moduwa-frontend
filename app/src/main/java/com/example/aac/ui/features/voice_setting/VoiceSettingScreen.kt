package com.example.aac.ui.features.voice_setting

import VoiceSettingViewModel
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.aac.ui.components.CustomTopBar
import com.example.aac.ui.components.CommonSaveDialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext

@Composable
fun VoiceSettingScreen(
    initialSelectedId: String,
    onBackClick: () -> Unit = {},
    onSave: (String) -> Unit = {},
    viewModel: VoiceSettingViewModel = viewModel()
) {
    val context = LocalContext.current
    val options = remember {
        listOf(
            VoiceOption("KID_MALE", "남자 아이 목소리"),
            VoiceOption("KID_FEMALE", "여자 아이 목소리"),
            VoiceOption("ADULT_MALE_DEFAULT", "기본 남성 목소리"),
            VoiceOption("ADULT_FEMALE_DEFAULT", "기본 여성 목소리"),
            VoiceOption("ELDER_MALE", "할아버지 목소리"),
            VoiceOption("ELDER_FEMALE", "할머니 목소리")
        )
    }

    val safeInitialId = when (initialSelectedId) {
        "boy" -> "KID_MALE"
        "girl" -> "KID_FEMALE"
        "default_male" -> "ADULT_MALE_DEFAULT"
        "default_female" -> "ADULT_FEMALE_DEFAULT"
        "grandpa" -> "ELDER_MALE"
        "grandma" -> "ELDER_FEMALE"
        else -> initialSelectedId
    }

    var selectedId by remember(safeInitialId) { mutableStateOf(safeInitialId) }
    val hasChanges = selectedId != safeInitialId
    var showSaveDialog by remember { mutableStateOf(false) }

    BackHandler {
        if (hasChanges) showSaveDialog = true
        else onBackClick()
    }

    Scaffold(
        containerColor = Color(0xFFF4F4F4),
        topBar = {
            CustomTopBar(
                title = "목소리 설정",
                onBackClick = {
                    if (hasChanges) showSaveDialog = true
                    else onBackClick()
                },
                actionText = "저장하기",
                onActionClick = {
                    // 🟢 뷰모델을 통해 서버에 저장 후 뒤로가기
                    viewModel.saveVoiceSetting(selectedId) {
                        onSave(selectedId) // NavGraph 등 상위에 변경 사항 알림
                        onBackClick()
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .padding(top = 25.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(options, key = { it.id }) { option ->
                VoiceOptionCard(
                    title = option.title,
                    selected = option.id == selectedId,
                    onCardClick = { selectedId = option.id },
                    onPreviewClick = { viewModel.playPreviewTts(context, option.id) } // 클릭 시 해당 ID 전달
                )
            }
        }
    }

    if (showSaveDialog) {
        CommonSaveDialog(
            message = "변경사항을\n저장하시겠어요?",
            onDismiss = { showSaveDialog = false },
            onSave = {
                showSaveDialog = false
                // 🟢 다이얼로그에서 저장할 때도 뷰모델 연결
                viewModel.saveVoiceSetting(selectedId) {
                    onSave(selectedId)
                    onBackClick()
                }
            }
        )
    }
}