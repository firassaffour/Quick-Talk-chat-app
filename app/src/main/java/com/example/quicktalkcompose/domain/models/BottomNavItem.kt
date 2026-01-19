package com.example.quicktalkcompose.domain.models

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route : String,
    val icon : ImageVector,
    val title: String
) {
    object Home : BottomNavItem("home", Icons.Default.Home, "Home")
    object Calls : BottomNavItem("Calls", Icons.Default.Call, "Calls")

    companion object{
        val items = listOf(Home, Calls)
    }
}