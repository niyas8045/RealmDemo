package com.mongodb.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mongodb.app.presentation.AddOrUpdateReminderScreen
import com.mongodb.app.presentation.ReminderListScreen
import com.mongodb.app.ui.Screen
import com.mongodb.app.ui.theme.MyApplicationTheme

const val ARG_IS_UPDATE = "arg_is_update"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background
                ) {
                    val navController = rememberNavController()
                    NavHost(
                        navController = navController,
                        startDestination = Screen.ReminderList.route
                    ) {
                        composable(Screen.ReminderList.route) { ReminderListScreen(navController) }
                        composable(
                            route = Screen.AddOrUpdateReminder.route + "/{$ARG_IS_UPDATE}",
                            arguments = listOf(
                                navArgument(ARG_IS_UPDATE) {
                                    type = NavType.BoolType
                                },
                            )
                        ) { backStackEntry ->
                            val isUpdate = backStackEntry.arguments?.getBoolean(ARG_IS_UPDATE)
                            AddOrUpdateReminderScreen(navController, isUpdate!!)
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MyApplicationTheme {

    }
}