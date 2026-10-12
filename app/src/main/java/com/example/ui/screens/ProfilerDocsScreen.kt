package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.benchmarks.BenchmarkData
import com.example.data.CleaningSuggestion
import com.example.data.DataCleaner
import com.example.data.DataFrame
import com.example.data.DataProfiler
import com.example.data.DatasetInfo
import com.example.data.SchemaRule
import com.example.data.SuggestionSeverity
import com.example.ui.components.ScoreCard
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.MatrixGreen
import com.example.ui.theme.PolarsCyan
import com.example.ui.theme.RustOrange

@Composable
fun ProfilerDocsScreen(
    currentDataset: DatasetInfo,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Auto Profiler, 1: Cleaner & Outliers, 2: Benchmarks, 3: Architecture & CLI

    var activeDf by remember(currentDataset) { mutableStateOf(DataFrame.fromDataset(currentDataset)) }
    val profileReport = remember(activeDf) { DataProfiler.profile(currentDataset.name, activeDf) }
    var wasAutoCleaned by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = DarkSurface,
            contentColor = RustOrange,
            edgePadding = 0.dp,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
        ) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Auto Profiler", fontSize = 11.sp, fontWeight = FontWeight.Bold) })
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Cleaner & Outliers", fontSize = 11.sp, fontWeight = FontWeight.Bold) })
            Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("Benchmarks", fontSize = 11.sp, fontWeight = FontWeight.Bold) })
            Tab(selected = selectedTab == 3, onClick = { selectedTab = 3 }, text = { Text("Architecture & CLI", fontSize = 11.sp, fontWeight = FontWeight.Bold) })
        }

        Spacer(modifier = Modifier.height(10.dp))

        when (selectedTab) {
            0 -> ProfilerView(profileReport)
            1 -> CleanerAndOutliersView(activeDf, onAutoClean = {
                activeDf = DataCleaner.autoClean(activeDf)
                wasAutoCleaned = true
            }, wasAutoCleaned = wasAutoCleaned)
            2 -> BenchmarksView()
            3 -> ArchitectureDocsView()
        }
    }
}

@Composable
private fun ProfilerView(report: com.example.data.DatasetProfileReport) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "DATASET PROFILE: ${report.datasetName.uppercase()}",
                            fontFamily = JetBrainsMonoFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PolarsCyan
                        )
                        Text(
                            text = "CLI: salim profile",
                            fontFamily = JetBrainsMonoFamily,
                            fontSize = 10.sp,
                            color = MatrixGreen
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ScoreCard("ROWS", "${report.rowCount}", MatrixGreen, Modifier.weight(1f))
                        ScoreCard("COLUMNS", "${report.columnCount}", PolarsCyan, Modifier.weight(1f))
                        ScoreCard("MISSING CELLS", "${report.totalMissingCells} (${"%.1f".format(report.missingPercentage)}%)", if (report.missingPercentage > 5) RustOrange else MatrixGreen, Modifier.weight(1.3f))
                        ScoreCard("MEMORY", "${report.memoryUsageBytes / 1024} KB", Color(0xFFA855F7), Modifier.weight(1f))
                    }

                    if (report.generalWarnings.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        report.generalWarnings.forEach { warn ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Warning, "Warn", tint = RustOrange, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(warn, fontSize = 10.sp, color = RustOrange)
                            }
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "COLUMN-LEVEL DIAGNOSTICS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.LightGray
            )
        }

        items(report.columns) { col ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
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
                            text = col.name,
                            fontFamily = JetBrainsMonoFamily,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 13.sp
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(PolarsCyan.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = col.type.label,
                                fontFamily = JetBrainsMonoFamily,
                                fontSize = 9.sp,
                                color = PolarsCyan
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Distinct", fontSize = 9.sp, color = Color.Gray)
                            Text("${col.distinctCount}", fontFamily = JetBrainsMonoFamily, fontSize = 11.sp, color = Color.White)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Missing", fontSize = 9.sp, color = Color.Gray)
                            Text("${col.nullCount} (${"%.1f".format(col.nullPercentage)}%)", fontFamily = JetBrainsMonoFamily, fontSize = 11.sp, color = if (col.nullCount > 0) RustOrange else MatrixGreen)
                        }
                        if (col.min != null && col.max != null) {
                            Column(modifier = Modifier.weight(1.5f)) {
                                Text("Range [Min, Max]", fontSize = 9.sp, color = Color.Gray)
                                Text("[${"%.1f".format(col.min)}, ${"%.1f".format(col.max)}]", fontFamily = JetBrainsMonoFamily, fontSize = 11.sp, color = Color.White)
                            }
                        }
                    }

                    if (col.warnings.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        col.warnings.forEach { w ->
                            Text("⚠️ $w", fontSize = 10.sp, color = RustOrange)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CleanerAndOutliersView(
    df: DataFrame,
    onAutoClean: () -> Unit,
    wasAutoCleaned: Boolean
) {
    val suggestions = remember(df) { DataCleaner.suggestCleaning(df) }
    val firstNumCol = remember(df) { df.columnNames.firstOrNull { df.columnTypes[it]?.name?.contains("FLOAT") == true } ?: df.columnNames.first() }
    val outliers = remember(df, firstNumCol) { DataCleaner.detectOutliers(df, firstNumCol) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Auto-Clean Action Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, MatrixGreen),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoFixHigh, "Auto Clean", tint = MatrixGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "df.suggest_cleaning() Engine",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 13.sp
                            )
                        }

                        if (wasAutoCleaned) {
                            Text(
                                text = "Cleaned! ✓",
                                fontFamily = JetBrainsMonoFamily,
                                fontWeight = FontWeight.Bold,
                                color = MatrixGreen,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Automated type coercion, median imputation, and extreme value boundary clipping.",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = onAutoClean,
                        colors = ButtonDefaults.buttonColors(containerColor = MatrixGreen),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text("Apply Auto-Cleaning to Active DataFrame", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }
        }

        // Suggestions List
        item {
            Text(
                text = "CLEANING RECOMMENDATIONS (${suggestions.size})",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.LightGray
            )
        }

        items(suggestions) { sug ->
            val color = when (sug.severity) {
                SuggestionSeverity.HIGH -> Color(0xFFEF4444)
                SuggestionSeverity.MEDIUM -> RustOrange
                SuggestionSeverity.LOW -> PolarsCyan
            }
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Column: ${sug.column}",
                            fontFamily = JetBrainsMonoFamily,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 12.sp
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(color.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = sug.severity.name,
                                fontFamily = JetBrainsMonoFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp,
                                color = color
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(text = "Issue: ${sug.issue}", fontSize = 11.sp, color = Color(0xFFE2E8F0))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "💡 Action: ${sug.recommendedAction}", fontFamily = JetBrainsMonoFamily, fontSize = 10.sp, color = MatrixGreen)
                }
            }
        }

        // Outliers Section
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "STATISTICAL OUTLIERS IN '$firstNumCol' (|Z| ≥ 2.5)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = PolarsCyan
            )
        }

        if (outliers.isEmpty()) {
            item {
                Text(
                    text = "No severe statistical outliers detected in '$firstNumCol'. Distribution is well-behaved.",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
        } else {
            items(outliers) { out ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF090D16)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Row #${out.rowIndex}", fontFamily = JetBrainsMonoFamily, fontSize = 11.sp, color = Color.Gray)
                        Text("Value: ${"%.2f".format(out.value)}", fontFamily = JetBrainsMonoFamily, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = RustOrange)
                        Text("Z-Score: ${"%.2f".format(out.zScore)}", fontFamily = JetBrainsMonoFamily, fontSize = 10.sp, color = MatrixGreen)
                    }
                }
            }
        }
    }
}

@Composable
private fun BenchmarksView() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, MatrixGreen.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Speed, "Benchmarks", tint = MatrixGreen, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "EMPIRICAL BENCHMARKS: SALIM VS PANDAS",
                            fontFamily = JetBrainsMonoFamily,
                            fontWeight = FontWeight.Bold,
                            color = MatrixGreen,
                            fontSize = 11.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Measurements executed on AMD Ryzen 9 7950X, 16 Cores, NVMe SSD.",
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                }
            }
        }

        items(BenchmarkData.ITEMS) { item ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = item.operation, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                            Text(text = item.datasetSize, fontSize = 10.sp, color = Color.LightGray)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(MatrixGreen.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${"%.1f".format(item.speedupFactor)}x FASTER",
                                fontFamily = JetBrainsMonoFamily,
                                fontWeight = FontWeight.Bold,
                                color = MatrixGreen,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Pandas (Python)", fontSize = 10.sp, color = Color(0xFFFF7B72), modifier = Modifier.width(90.dp))
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(12.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(Color(0xFFEF4444).copy(alpha = 0.7f))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("${item.pandasMetric.toInt()} ${item.pandasUnit}", fontFamily = JetBrainsMonoFamily, fontSize = 10.sp, color = Color.White)
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Salim (Rust)", fontSize = 10.sp, color = MatrixGreen, fontWeight = FontWeight.Bold, modifier = Modifier.width(90.dp))
                            Box(
                                modifier = Modifier
                                    .weight((1f / item.speedupFactor.toFloat()).coerceAtLeast(0.04f))
                                    .height(12.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(MatrixGreen)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("${item.salimMetric.toInt()} ${item.salimUnit}", fontFamily = JetBrainsMonoFamily, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MatrixGreen)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

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

@Composable
private fun ArchitectureDocsView() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "🚀 SALIM DATA KIT ARCHITECTURE",
                        fontFamily = JetBrainsMonoFamily,
                        fontWeight = FontWeight.Bold,
                        color = RustOrange,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Salim is built as a modular Cargo workspace designed for high throughput data science without Python's runtime overhead.",
                        fontSize = 11.sp,
                        color = Color(0xFFCBD5E1),
                        lineHeight = 16.sp
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "CRATE WORKSPACE LAYOUT",
                        fontFamily = JetBrainsMonoFamily,
                        fontWeight = FontWeight.Bold,
                        color = PolarsCyan,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    val crates = listOf(
                        "salim" to "Umbrella crate and prelude (`use salim::prelude::*;`)",
                        "salim-frame" to "Pandas-style DataFrame & Series layer over Polars",
                        "salim-array" to "NumPy-like N-dimensional arrays & linear algebra",
                        "salim-stats" to "Statistics, distributions, and hypothesis tests",
                        "salim-plot" to "Matplotlib-like 1-line plotting with modern themes",
                        "salim-ml" to "Scikit-learn style ML pipelines, trees, and clustering",
                        "salim-io" to "High-speed CSV, Parquet, JSON, Arrow, and SQLite I/O",
                        "salim-errors" to "Friendly errors with fuzzy column name suggestions",
                        "salim-py2rs" to "Python-to-Rust migration engine and cheat sheets",
                        "salim-cli" to "Command-line tools (head, profile, query, translate)"
                    )
                    crates.forEach { (name, desc) ->
                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                            Text(name, fontFamily = JetBrainsMonoFamily, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MatrixGreen)
                            Text(desc, fontSize = 10.sp, color = Color(0xFF94A3B8))
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF090D16)),
                border = androidx.compose.foundation.BorderStroke(1.dp, PolarsCyan),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "📦 SINGLE-BINARY DEPLOYMENT",
                        fontFamily = JetBrainsMonoFamily,
                        fontWeight = FontWeight.Bold,
                        color = PolarsCyan,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Unlike Python pipelines that require virtualenvs, Docker images, and 500MB dependencies, Salim compiles into a single, self-contained binary:",
                        fontSize = 10.sp,
                        color = Color.LightGray
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF0B0F19))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = """$ cargo build --release
   Compiling salim v0.1.0
   Finished release in 8.4s
$ ls -lh target/release/salim
-rwxr-xr-x 1 user 18M salim
$ ./salim profile data.csv
>>> Generated interactive profile in 42ms!""",
                            fontFamily = JetBrainsMonoFamily,
                            fontSize = 10.sp,
                            lineHeight = 14.sp,
                            color = MatrixGreen
                        )
                    }
                }
            }
        }
    }
}
