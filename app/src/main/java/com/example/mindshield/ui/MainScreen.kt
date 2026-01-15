package com.example.mindshield.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mindshield.model.AppTab
import com.example.mindshield.ui.screens.InterventionScreen
import com.example.mindshield.ui.screens.*
import com.example.mindshield.ui.theme.*
import com.example.mindshield.ui.viewmodel.OnboardingScreenViewModel

@Composable
fun MainScreen(viewModel: OnboardingScreenViewModel, showClick: () -> Unit) {
    var activeTab by remember { mutableStateOf(AppTab.SHIELD) }

    Scaffold(
        containerColor = BeigeBackground,
        bottomBar = {
            NavigationBar(containerColor = BeigeBackground, contentColor = Stone500, tonalElevation = 8.dp) {
                @Composable
                fun TabItem(tab: AppTab, icon: ImageVector, label: String) {
                    NavigationBarItem(
                        selected = activeTab == tab,
                        onClick = { activeTab = tab },
                        icon = { Icon(icon, contentDescription = label) },
                        label = { Text(label, fontSize = 10.sp, fontWeight = FontWeight.Medium) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Emerald800,
                            selectedTextColor = Emerald800,
                            indicatorColor = Color.Transparent,
                            unselectedIconColor = Stone500,
                            unselectedTextColor = Stone500
                        )
                    )
                }
                TabItem(AppTab.SHIELD, Icons.Outlined.Shield, "Shield")
                TabItem(AppTab.INSIGHT, Icons.Outlined.BarChart, "Insight")
                TabItem(AppTab.INTERVENTION, Icons.Outlined.Tune, "Intervention")
                TabItem(AppTab.PROFILE, Icons.Outlined.Person, "Profile")
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            when (activeTab) {
                AppTab.SHIELD -> ShieldScreen()
                AppTab.INSIGHT -> InsightScreen()
                AppTab.INTERVENTION -> InterventionScreen()
                AppTab.PROFILE -> ProfileScreen(viewModel, showClick)
            }
        }
    }
}