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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
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
import com.example.notebook.NotebookCell
import com.example.notebook.NotebookEngine
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.MatrixGreen
import com.example.ui.theme.PolarsCyan
import com.example.ui.theme.RustOrange
import com.example.ui.components.KpiPill

@Composable
fun NotebookScreen(
    modifier: Modifier = Modifier
) {
    var selectedNotebookIndex by remember { mutableIntStateOf(0) }
    val activeNotebook = remember(selectedNotebookIndex) { NotebookEngine.SAMPLES[selectedNotebookIndex] }

    var editableCode by remember(activeNotebook) { mutableStateOf(activeNotebook.rustCode) }
    var showPythonCode by remember { mutableStateOf(false) }
    var isExecuted by remember { mutableStateOf(true) }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(12.dp)
    ) {
        // Notebook Tabs Carousel
        ScrollableTabRow(
            selectedTabIndex = selectedNotebookIndex,
            containerColor = DarkSurface,
            contentColor = RustOrange,
            edgePadding = 0.dp,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
        ) {
            NotebookEngine.SAMPLES.forEachIndexed { idx, nb ->
                Tab(
                    selected = selectedNotebookIndex == idx,
                    onClick = {
                        selectedNotebookIndex = idx
                        editableCode = nb.rustCode
                        isExecuted = true
                    },
                    text = {
                        Text(
                            text = nb.title.split(".").getOrNull(1)?.trim()?.split(" ")?.take(2)?.joinToString(" ") ?: nb.title,
                            fontSize = 11.sp,
                            fontWeight = if (selectedNotebookIndex == idx) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedNotebookIndex == idx) RustOrange else Color.LightGray
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Notebook Header Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = activeNotebook.title,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = activeNotebook.description,
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    KpiPill("SPEEDUP", activeNotebook.speedupFactor, MatrixGreen, Modifier.weight(1f))
                    KpiPill("RAM SAVINGS", activeNotebook.memorySavings, PolarsCyan, Modifier.weight(1f))
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Code Mode Toggle (Rust vs Python)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (!showPythonCode) RustOrange else DarkSurfaceVariant)
                        .clickable { showPythonCode = false }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "🦀 Salim Rust",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = if (!showPythonCode) Color.White else Color.Gray
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (showPythonCode) Color(0xFFFFD166) else DarkSurfaceVariant)
                        .clickable { showPythonCode = true }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "🐍 Python Pandas",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = if (showPythonCode) Color.Black else Color.Gray
                    )
                }
            }

            // Run Button
            Button(
                onClick = { isExecuted = true },
                colors = ButtonDefaults.buttonColors(containerColor = MatrixGreen),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.testTag("run_notebook_cell_button")
            ) {
                Icon(Icons.Default.PlayArrow, "Run", tint = Color.Black, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Execute Cell", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Code Editor Box
        OutlinedTextField(
            value = if (showPythonCode) activeNotebook.pythonEquivalent else editableCode,
            onValueChange = { if (!showPythonCode) editableCode = it },
            readOnly = showPythonCode,
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            textStyle = androidx.compose.ui.text.TextStyle(
                fontFamily = JetBrainsMonoFamily,
                fontSize = 11.sp,
                color = if (showPythonCode) Color(0xFFFFD166) else Color(0xFFA5D6FF),
                lineHeight = 16.sp
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PolarsCyan,
                unfocusedBorderColor = DarkBorder,
                focusedContainerColor = Color(0xFF090D16),
                unfocusedContainerColor = Color(0xFF090D16)
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Output & Execution Console
        if (isExecuted) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MatrixGreen.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, "Success", tint = MatrixGreen, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "CELL OUTPUT [Execution complete in 1.2ms]",
                                fontFamily = JetBrainsMonoFamily,
                                fontWeight = FontWeight.Bold,
                                color = MatrixGreen,
                                fontSize = 10.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = activeNotebook.outputSummary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Polars Execution Log:",
                        fontSize = 10.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF090D16))
                            .padding(8.dp)
                    ) {
                        activeNotebook.executionLog.forEach { logLine ->
                            Text(
                                text = logLine,
                                fontFamily = JetBrainsMonoFamily,
                                fontSize = 10.sp,
                                lineHeight = 14.sp,
                                color = Color(0xFF86EFAC)
                            )
                        }
                    }
                }
            }
        }
    }
}
