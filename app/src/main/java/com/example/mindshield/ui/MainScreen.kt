package com.example.mindshield.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import com.example.mindshield.model.AppTab
import com.example.mindshield.ui.screens.InsightScreen
import com.example.mindshield.ui.screens.InterventionScreen
import com.example.mindshield.ui.screens.ProfileScreen
import com.example.mindshield.ui.screens.ShieldScreen
import com.example.mindshield.ui.theme.BeigeBackground
import com.example.mindshield.ui.theme.Emerald800
import com.example.mindshield.ui.theme.Stone500
import com.example.mindshield.ui.viewmodel.InterventionScreenViewModel
import f
import w

@Composable
fun MainScreen(
    interventionScreenViewModel: InterventionScreenViewModel,
    onNavigateToCalibration: () -> Unit
) {
    var activeTab by rememberSaveable { mutableStateOf(AppTab.SHIELD) }

    Scaffold(
        containerColor = BeigeBackground,
        bottomBar = {
                NavigationBar(containerColor = BeigeBackground, contentColor = Stone500, tonalElevation = 8.w) {
                @Composable
                fun TabItem(tab: AppTab, icon: ImageVector, label: String) {
                    NavigationBarItem(
                        selected = activeTab == tab,
                        onClick = { activeTab = tab },
                        icon = { Icon(icon, contentDescription = label) },
                        label = { Text(label, fontSize = 12.f, fontWeight = FontWeight.Medium, softWrap = false) },
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
                AppTab.INTERVENTION -> InterventionScreen(
                    interventionScreenViewModel
                )
                AppTab.PROFILE -> ProfileScreen(
                    onCalibrationClick = onNavigateToCalibration
                )
            }
        }
    }
}