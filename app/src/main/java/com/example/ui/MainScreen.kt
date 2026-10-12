package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DatasetInfo
import com.example.data.SampleDatasets
import com.example.ui.components.SalimTopBar
import com.example.ui.screens.DataFrameScreen
import com.example.ui.screens.MLScreen
import com.example.ui.screens.NotebookScreen
import com.example.ui.screens.PlotScreen
import com.example.ui.screens.ProfilerDocsScreen
import com.example.ui.screens.StatsScreen
import com.example.ui.screens.TranslatorScreen
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.RustOrange

enum class SalimNavDestination(
    val title: String,
    val icon: ImageVector,
    val tag: String
) {
    FRAMES("Frames", Icons.Default.TableChart, "nav_frames"),
    PLOTS("Plots", Icons.Default.ShowChart, "nav_plots"),
    STATS("Stats", Icons.Default.Functions, "nav_stats"),
    ML("ML", Icons.Default.Psychology, "nav_ml"),
    NOTEBOOK("Notebook", Icons.Default.MenuBook, "nav_notebook"),
    TRANSLATOR("Py->Rs", Icons.Default.Translate, "nav_translator"),
    PROFILER("Profiler", Icons.Default.Assessment, "nav_profiler")
}

@Composable
fun MainScreen() {
    var currentDataset by remember { mutableStateOf(SampleDatasets.TITANIC) }
    var currentDestination by remember { mutableStateOf(SalimNavDestination.FRAMES) }

    // Predictive back handler: if not on Home screen (FRAMES), back returns to FRAMES
    if (currentDestination != SalimNavDestination.FRAMES) {
        BackHandler {
            currentDestination = SalimNavDestination.FRAMES
        }
    }

    Scaffold(
        topBar = {
            SalimTopBar(
                currentDataset = currentDataset,
                onSelectDataset = { currentDataset = it }
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .navigationBarsPadding()
                    .testTag("main_navigation_bar"),
                containerColor = DarkSurface,
                tonalElevation = 6.dp
            ) {
                SalimNavDestination.values().forEach { destination ->
                    val isSelected = currentDestination == destination
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentDestination = destination },
                        icon = {
                            Icon(
                                imageVector = destination.icon,
                                contentDescription = destination.title,
                                modifier = Modifier.size(19.dp)
                            )
                        },
                        label = {
                            Text(
                                text = destination.title,
                                fontSize = 9.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = RustOrange,
                            indicatorColor = RustOrange,
                            unselectedIconColor = Color(0xFF94A3B8),
                            unselectedTextColor = Color(0xFF94A3B8)
                        ),
                        modifier = Modifier.testTag(destination.tag)
                    )
                }
            }
        },
        containerColor = DarkBackground
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(DarkBackground)
        ) {
            when (currentDestination) {
                SalimNavDestination.FRAMES -> DataFrameScreen(currentDataset = currentDataset)
                SalimNavDestination.PLOTS -> PlotScreen(currentDataset = currentDataset)
                SalimNavDestination.STATS -> StatsScreen(currentDataset = currentDataset)
                SalimNavDestination.ML -> MLScreen(currentDataset = currentDataset)
                SalimNavDestination.NOTEBOOK -> NotebookScreen()
                SalimNavDestination.TRANSLATOR -> TranslatorScreen()
                SalimNavDestination.PROFILER -> ProfilerDocsScreen(currentDataset = currentDataset)
            }
        }
    }
}
