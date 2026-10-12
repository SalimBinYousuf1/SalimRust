package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.translator.CheatSheetItem
import com.example.translator.Py2RsTranslator
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.MatrixGreen
import com.example.ui.theme.PolarsCyan
import com.example.ui.theme.RustOrange

@Composable
fun TranslatorScreen(
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Translator, 1: Cheat Sheet, 2: Friendly Errors

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        // Tab Selector
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = DarkSurface,
            contentColor = RustOrange,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Code Translator", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Cheat Sheet (200+)", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("Friendly Errors", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        when (selectedTab) {
            0 -> InteractiveTranslatorView()
            1 -> CheatSheetView()
            2 -> FriendlyErrorsView()
        }
    }
}

@Composable
private fun InteractiveTranslatorView() {
    var activePreset by remember { mutableStateOf(Py2RsTranslator.PRESETS[1]) }
    var inputPythonCode by remember { mutableStateOf(activePreset.pythonSnippet) }
    var translatedRustCode by remember { mutableStateOf(activePreset.rustSnippet) }
    var speedupInfo by remember { mutableStateOf(activePreset.speedup) }
    var explanationInfo by remember { mutableStateOf(activePreset.explanation) }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "PRESET PYTHON SAMPLES:",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = PolarsCyan
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Preset Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Py2RsTranslator.PRESETS.take(3).forEach { preset ->
                val isSelected = activePreset.title == preset.title
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSelected) RustOrange else DarkCard)
                        .border(1.dp, if (isSelected) RustOrange else DarkBorder, RoundedCornerShape(6.dp))
                        .clickable {
                            activePreset = preset
                            inputPythonCode = preset.pythonSnippet
                            translatedRustCode = preset.rustSnippet
                            speedupInfo = preset.speedup
                            explanationInfo = preset.explanation
                        }
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = preset.title.split(" ").first(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.White else Color.LightGray
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Python Input Box
        Text(
            text = "Python (Pandas / NumPy / Matplotlib):",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color.LightGray
        )

        Spacer(modifier = Modifier.height(4.dp))

        OutlinedTextField(
            value = inputPythonCode,
            onValueChange = {
                inputPythonCode = it
                val (rust, speed) = Py2RsTranslator.translateCode(it)
                translatedRustCode = rust
                speedupInfo = speed
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
                .testTag("python_input_field"),
            textStyle = androidx.compose.ui.text.TextStyle(
                fontFamily = JetBrainsMonoFamily,
                fontSize = 11.sp,
                color = Color(0xFFFFD166)
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = RustOrange,
                unfocusedBorderColor = DarkBorder,
                focusedContainerColor = Color(0xFF090D16),
                unfocusedContainerColor = Color(0xFF090D16)
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Translate Button
        Button(
            onClick = {
                val (rust, speed) = Py2RsTranslator.translateCode(inputPythonCode)
                translatedRustCode = rust
                speedupInfo = speed
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = RustOrange),
            shape = RoundedCornerShape(6.dp)
        ) {
            Icon(Icons.Default.Translate, "Translate", modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Translate to Idiomatic Salim Rust", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Rust Output Card
        Card(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF090D16)),
            border = androidx.compose.foundation.BorderStroke(1.dp, PolarsCyan),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🦀 SALIM RUST OUTPUT",
                        fontFamily = JetBrainsMonoFamily,
                        fontWeight = FontWeight.Bold,
                        color = PolarsCyan,
                        fontSize = 11.sp
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Bolt, "Speedup", tint = MatrixGreen, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = speedupInfo,
                            fontFamily = JetBrainsMonoFamily,
                            fontWeight = FontWeight.Bold,
                            color = MatrixGreen,
                            fontSize = 10.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF0B0F19))
                        .padding(8.dp)
                ) {
                    Text(
                        text = translatedRustCode,
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 11.sp,
                        lineHeight = 16.sp,
                        color = Color(0xFFA5D6FF)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "💡 $explanationInfo",
                    fontSize = 10.sp,
                    color = Color(0xFF94A3B8)
                )
            }
        }
    }
}

@Composable
private fun CheatSheetView() {
    var searchQuery by remember { mutableStateOf("") }

    val filteredItems = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            Py2RsTranslator.CHEAT_SHEET
        } else {
            Py2RsTranslator.CHEAT_SHEET.filter {
                it.operation.contains(searchQuery, ignoreCase = true) ||
                it.category.contains(searchQuery, ignoreCase = true) ||
                it.python.contains(searchQuery, ignoreCase = true) ||
                it.rust.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search 200+ operations (e.g. groupby, query, plot)...", fontSize = 12.sp) },
            leadingIcon = { Icon(Icons.Default.Search, "Search", tint = Color.Gray, modifier = Modifier.size(16.dp)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PolarsCyan,
                unfocusedBorderColor = DarkBorder,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredItems) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = item.operation,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 12.sp
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(PolarsCyan.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = item.category,
                                    fontFamily = JetBrainsMonoFamily,
                                    fontSize = 9.sp,
                                    color = PolarsCyan
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Python
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Py: ", fontFamily = JetBrainsMonoFamily, fontWeight = FontWeight.Bold, color = Color(0xFFFFD166), fontSize = 10.sp)
                            Text(item.python, fontFamily = JetBrainsMonoFamily, color = Color(0xFFFFE8A3), fontSize = 11.sp)
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        // Rust
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Rs: ", fontFamily = JetBrainsMonoFamily, fontWeight = FontWeight.Bold, color = MatrixGreen, fontSize = 10.sp)
                            Text(item.rust, fontFamily = JetBrainsMonoFamily, color = Color(0xFFA5D6FF), fontSize = 11.sp)
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "ℹ️ ${item.notes}",
                            fontSize = 10.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FriendlyErrorsView() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(Py2RsTranslator.FRIENDLY_ERRORS) { err ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ErrorOutline, "Error", tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = err.title,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 13.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Python Developer's Mistake:",
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF1E1E24))
                            .padding(6.dp)
                    ) {
                        Text(
                            text = err.badCode,
                            fontFamily = JetBrainsMonoFamily,
                            color = Color(0xFFFF7B72),
                            fontSize = 11.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Salim Friendly Error Response:",
                        fontSize = 10.sp,
                        color = PolarsCyan
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF090D16))
                            .border(1.dp, DarkBorder, RoundedCornerShape(4.dp))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = err.salimErrorMessage,
                            fontFamily = JetBrainsMonoFamily,
                            color = MatrixGreen,
                            fontSize = 10.sp,
                            lineHeight = 14.sp
                        )
                    }
                }
            }
        }
    }
}
