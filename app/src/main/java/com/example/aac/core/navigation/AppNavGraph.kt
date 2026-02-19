package com.example.aac.core.navigation

import android.content.Intent
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import kotlinx.coroutines.delay

import com.example.aac.data.mapper.toAutoSentenceItem
import com.example.aac.data.mapper.toCreateRoutineRequest
import com.example.aac.data.mapper.toRoutineUpdateRequest
import com.example.aac.data.repository.SentenceDataRepository
import com.example.aac.ui.features.ai_sentence.ui.AiSentenceEditScreen
import com.example.aac.ui.features.ai_sentence.ui.AiSentenceScreen
import com.example.aac.ui.features.auth.AuthViewModel
import com.example.aac.ui.features.auto_sentence.*
import com.example.aac.ui.features.auto_sentence.components.RoutineModal
import com.example.aac.ui.features.category.CategoryManagementScreen
import com.example.aac.ui.features.main.MainScreen
import com.example.aac.ui.features.settings.SettingsScreen
import com.example.aac.ui.features.speak_setting.SpeakSettingScreen
import com.example.aac.ui.features.terms.TermsDetailScreen
import com.example.aac.ui.features.terms.TermsScreen
import com.example.aac.ui.features.login.LoginRoute
import com.example.aac.ui.features.usage_history.UsageHistoryActivity
import com.example.aac.ui.features.voice_setting.VoiceSettingScreen

@Composable
fun AppNavGraph() {

    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val routineVm: AutoSentenceRoutineViewModel = viewModel()

    var voiceSettingId by remember { mutableStateOf("default_male") }

    val modalRoutine by routineVm.modalRoutine.collectAsState()
    val logoutCompleted by authViewModel.logoutCompleted.collectAsState()
    val withdrawCompleted by authViewModel.withdrawCompleted.collectAsState()

    /* ---------- 로그인 상태일 때만 1분 polling ---------- */
    val loginState by authViewModel.loginState.collectAsState()

    LaunchedEffect(loginState) {
        if (loginState != null) {
            while (loginState != null) {
                delay(60_000)
                routineVm.checkRoutineModal()
            }
        }
    }

    /* ---------- 로그아웃 처리 ---------- */
    LaunchedEffect(logoutCompleted) {
        if (logoutCompleted) {
            navController.navigate(Routes.LOGIN) {
                popUpTo(0) { inclusive = true }
            }
            authViewModel.consumeLogoutCompleted()
        }
    }

    /* ---------- 회원탈퇴 처리 ---------- */
    LaunchedEffect(withdrawCompleted) {
        if (withdrawCompleted) {
            navController.navigate(Routes.LOGIN) {
                popUpTo(0) { inclusive = true }
            }
            authViewModel.consumeWithdrawCompleted()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        NavHost(
            navController = navController,
            startDestination = Routes.LOGIN
        ) {

            /* ---------- LOGIN ---------- */
            composable(Routes.LOGIN) {
                LoginRoute(
                    viewModel = authViewModel,
                    onNavigateToTerms = {
                        navController.navigate(Routes.TERMS)
                    },
                    onNavigateToMain = {
                        navController.navigate(Routes.MAIN) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    }
                )
            }

            /* ---------- TERMS ---------- */
            composable(Routes.TERMS) {
                val signupCompleted by authViewModel.signupCompleted.collectAsState()

                LaunchedEffect(signupCompleted) {
                    if (signupCompleted) {
                        navController.navigate(Routes.MAIN) {
                            popUpTo(Routes.TERMS) { inclusive = true }
                        }
                        authViewModel.resetSignupState()
                    }
                }

                TermsScreen(
                    navController = navController,
                    authViewModel = authViewModel,
                    onBackClick = {
                        authViewModel.cancelSignupFlow()
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(Routes.TERMS) { inclusive = true }
                        }
                    }
                )
            }

            /* ---------- TERMS DETAIL ---------- */
            composable(
                route = "terms_detail/{termId}",
                arguments = listOf(navArgument("termId") { type = NavType.StringType })
            ) { backStackEntry ->
                val termId = backStackEntry.arguments?.getString("termId") ?: ""
                TermsDetailScreen(
                    termId = termId,
                    authViewModel = authViewModel,
                    navController = navController
                )
            }

            /* ---------- MAIN ---------- */
            composable(Routes.MAIN) {
                MainScreen(
                    onNavigateToAiSentence = {
                        navController.navigate(Routes.AI_SENTENCE)
                    },
                    onNavigateToSettings = {
                        navController.navigate(Routes.SETTINGS)
                    }
                )
            }

            /* ---------- SETTINGS ---------- */
            composable(Routes.SETTINGS) {
                val context = LocalContext.current

                SettingsScreen(
                    authViewModel = authViewModel,
                    onBackClick = { navController.popBackStack() },
                    onAutoSentenceSettingClick = {
                        navController.navigate(Routes.AUTO_SENTENCE_SETTING)
                    },
                    onVoiceSettingClick = {
                        navController.navigate(Routes.VOICE_SETTING)
                    },
                    onUsageHistoryClick = {
                        val intent = Intent(context, UsageHistoryActivity::class.java)
                        context.startActivity(intent)
                    },
                    onCategoryManagementClick = {
                        navController.navigate(Routes.CATEGORY_MANAGEMENT)
                    },
                    onSpeakSettingClick = {
                        navController.navigate(Routes.SPEAK_SETTING)
                    }
                )
            }

            /* ---------- VOICE SETTING ---------- */
            composable(Routes.VOICE_SETTING) {
                VoiceSettingScreen(
                    initialSelectedId = voiceSettingId,
                    onBackClick = { navController.popBackStack() },
                    onSave = { selectedId -> voiceSettingId = selectedId }
                )
            }

            /* ---------- AI SENTENCE ---------- */
            composable(Routes.AI_SENTENCE) {
                AiSentenceScreen(
                    initialWords = SentenceDataRepository.selectedWords,
                    onBack = { navController.popBackStack() },
                    onEditNavigate = { text ->
                        navController.navigate(Routes.aiSentenceEditRoute(text))
                    }
                )
            }

            /* ---------- AI SENTENCE EDIT ---------- */
            composable(
                route = Routes.AI_SENTENCE_EDIT_ROUTE,
                arguments = listOf(navArgument("text") {
                    type = NavType.StringType
                    defaultValue = ""
                })
            ) { backStackEntry ->
                val text = backStackEntry.arguments?.getString("text").orEmpty()
                AiSentenceEditScreen(
                    initialText = text,
                    onBack = { navController.popBackStack() }
                )
            }

            /* ---------- AUTO SENTENCE SETTING ---------- */
            composable(Routes.AUTO_SENTENCE_SETTING) {
                AutoSentenceSettingScreen(
                    onBack = { navController.popBackStack() },
                    onAddClick = {
                        navController.navigate(Routes.AUTO_SENTENCE_ADD)
                    },
                    onEditClick = { item ->
                        navController.navigate(
                            Routes.autoSentenceEditRoute(item.serverId)
                        )
                    },
                    onSelectDeleteClick = {
                        navController.navigate(Routes.AUTO_SENTENCE_SELECT_DELETE)
                    },
                    routineViewModel = routineVm,
                    routineToItem = { dto -> dto.toAutoSentenceItem() }
                )
            }

            /* ---------- AUTO SENTENCE ADD ---------- */
            composable(Routes.AUTO_SENTENCE_ADD) {
                AutoSentenceAddEditScreen(
                    mode = AutoSentenceMode.ADD,
                    initialItem = null,
                    onBack = { navController.popBackStack() },
                    onSave = { item ->
                        routineVm.createRoutine(
                            request = item.toCreateRoutineRequest(),
                            onSuccess = { navController.popBackStack() }
                        )
                    },
                    routineViewModel = routineVm,
                    voiceKey = voiceSettingId
                )
            }

            /* ---------- AUTO SENTENCE EDIT ---------- */
            composable(
                route = Routes.AUTO_SENTENCE_EDIT,
                arguments = listOf(navArgument("serverId") { type = NavType.StringType })
            ) { backStackEntry ->

                val serverId = backStackEntry.arguments?.getString("serverId").orEmpty()
                val routineUiState by routineVm.uiState.collectAsState()

                LaunchedEffect(serverId) {
                    if (routineUiState.routines.isEmpty()) {
                        routineVm.fetchRoutines()
                    }
                }

                val serverItems = routineUiState.routines.map { dto ->
                    dto.toAutoSentenceItem()
                }

                val targetItem = serverItems.find { item ->
                    item.serverId == serverId
                }

                targetItem?.let { item ->
                    AutoSentenceAddEditScreen(
                        mode = AutoSentenceMode.EDIT,
                        initialItem = item,
                        onBack = { navController.popBackStack() },
                        onSave = { updatedItem ->
                            routineVm.updateRoutine(
                                id = item.serverId,
                                request = updatedItem.toRoutineUpdateRequest(),
                                onSuccess = { navController.popBackStack() }
                            )
                        },
                        onDelete = {
                            routineVm.deleteRoutine(
                                id = item.serverId,
                                onSuccess = {
                                    navController.navigate(Routes.AUTO_SENTENCE_SETTING) {
                                        popUpTo(Routes.AUTO_SENTENCE_SETTING) { inclusive = false }
                                    }
                                }
                            )
                        },
                        // 추가: 미리듣기(TTS)용
                        routineViewModel = routineVm,
                        voiceKey = voiceSettingId
                    )
                }
            }


            /* ---------- AUTO SENTENCE SELECT DELETE ---------- */
            composable(Routes.AUTO_SENTENCE_SELECT_DELETE) {

                val routineUiState by routineVm.uiState.collectAsState()

                val items = routineUiState.routines.map {
                    it.toAutoSentenceItem()
                }

                AutoSentenceSelectDeleteScreen(
                    autoSentenceList = items,
                    onBack = { navController.popBackStack() },
                    onDeleteSelected = { selectedUiIds ->
                        val selectedServerIds = items
                            .filter { selectedUiIds.contains(it.id) }
                            .map { it.serverId }

                        routineVm.deleteRoutines(
                            ids = selectedServerIds,
                            onSuccess = { navController.popBackStack() }
                        )
                    }
                )
            }



            /* ---------- CATEGORY MANAGEMENT ---------- */
            composable(Routes.CATEGORY_MANAGEMENT) {
                CategoryManagementScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }

            /* ---------- SPEAK SETTING ---------- */
            composable(Routes.SPEAK_SETTING) {
                SpeakSettingScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }
        }

        /* ---------- 전역 모달 ---------- */
        val context = LocalContext.current

        modalRoutine?.let { routine ->
            RoutineModal(
                routine = routine,
                onSnoozeClick = { routineVm.snoozeRoutine(routine.id) },
                onDismissClick = { routineVm.dismissRoutine(routine.id) },
                onPlayClick = { routineVm.playRoutineTts(context, routine.message, null) }
            )
        }
    }
}
