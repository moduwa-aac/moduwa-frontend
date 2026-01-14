package com.example.aac

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.aac.core.navigation.AppNavGraph
import com.example.aac.ui.features.main.MainScreen
import com.example.aac.ui.theme.AacTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            AacTheme {
                AppNavGraph()
            }
        }
    }


}


                MainScreen()
            }
        }
    }
}
