package com.example.aac.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.aac.feature.ai_sentence.ui.AiSentenceScreen
import com.example.aac.feature.ai_sentence.ui.AiSentenceEditScreen  // ✅ 편집 화면 import

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.AI_SENTENCE
    ) {
        composable(Routes.AI_SENTENCE) {
            AiSentenceScreen(
                onBack = { navController.popBackStack() },
                onEditNavigate = { text ->
                    navController.navigate(Routes.aiSentenceEditRoute(text))
                }
            )
        }

        // ✅ 편집 화면 추가
        composable(
            route = Routes.AI_SENTENCE_EDIT_ROUTE,
            arguments = listOf(
                navArgument("text") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->
            val text = backStackEntry.arguments?.getString("text").orEmpty()

            AiSentenceEditScreen(
                initialText = text,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
