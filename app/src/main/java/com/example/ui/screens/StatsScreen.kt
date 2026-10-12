package com.example.ui.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ColumnType
import com.example.data.DataFrame
import com.example.data.DatasetInfo
import com.example.stats.StatsEngine
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.MatrixGreen
import com.example.ui.theme.PolarsCyan
import com.example.ui.theme.RustOrange
import kotlin.math.sqrt

@Composable
fun StatsScreen(
    currentDataset: DatasetInfo,
    modifier: Modifier = Modifier
) {
    val df = remember(currentDataset) { DataFrame.fromDataset(currentDataset) }
    val numericCols = remember(currentDataset) {
        currentDataset.columns.filter { it.type == ColumnType.FLOAT || it.type == ColumnType.INT }.map { it.name }
    }

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Describe, 1: Distributions, 2: Hypothesis Tests, 3: Regression, 4: Matrix Lab

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        // Stats Subtabs (Scrollable)
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
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Describe", fontSize = 11.sp, fontWeight = FontWeight.Bold) })
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Distributions", fontSize = 11.sp, fontWeight = FontWeight.Bold) })
            Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("Hypothesis Tests", fontSize = 11.sp, fontWeight = FontWeight.Bold) })
            Tab(selected = selectedTab == 3, onClick = { selectedTab = 3 }, text = { Text("OLS Regression", fontSize = 11.sp, fontWeight = FontWeight.Bold) })
            Tab(selected = selectedTab == 4, onClick = { selectedTab = 4 }, text = { Text("Matrix Lab (salim-array)", fontSize = 11.sp, fontWeight = FontWeight.Bold) })
        }

        Spacer(modifier = Modifier.height(10.dp))

        when (selectedTab) {
            0 -> DescribeTab(df, numericCols)
            1 -> DistributionsTab()
            2 -> HypothesisTestsTab(df, numericCols)
            3 -> RegressionTab(df, numericCols)
            4 -> MatrixLabTab()
        }
    }
}

@Composable
private fun DescribeTab(df: DataFrame, numericCols: List<String>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(numericCols) { colName ->
            val numbers = df.rows.mapNotNull { (it[colName] as? Number)?.toDouble() }
            val stats = remember(numbers) { StatsEngine.computeStats(numbers) }

            if (stats != null) {
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
                                text = colName,
                                fontFamily = JetBrainsMonoFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = PolarsCyan
                            )
                            Text(
                                text = "N = ${stats.count}",
                                fontFamily = JetBrainsMonoFamily,
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            MetricCell("Mean", "%.2f".format(stats.mean), Modifier.weight(1f))
                            MetricCell("Std Dev", "%.2f".format(stats.std), Modifier.weight(1f))
                            MetricCell("Median", "%.2f".format(stats.median), Modifier.weight(1f))
                            MetricCell("IQR", "%.2f".format(stats.iqr), Modifier.weight(1f))
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            MetricCell("Min", "%.2f".format(stats.min), Modifier.weight(1f))
                            MetricCell("25% (Q1)", "%.2f".format(stats.p25), Modifier.weight(1f))
                            MetricCell("75% (Q3)", "%.2f".format(stats.p75), Modifier.weight(1f))
                            MetricCell("Max", "%.2f".format(stats.max), Modifier.weight(1f))
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            MetricCell("Skewness", "%.3f".format(stats.skewness), Modifier.weight(1f))
                            MetricCell("Kurtosis", "%.3f".format(stats.kurtosis), Modifier.weight(1f))
                            MetricCell("Variance", "%.2f".format(stats.variance), Modifier.weight(2f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DistributionsTab() {
    var distType by remember { mutableStateOf("NORMAL") }
    var paramMean by remember { mutableDoubleStateOf(0.0) }
    var paramStd by remember { mutableDoubleStateOf(1.0) }
    var paramDf by remember { mutableDoubleStateOf(5.0) }
    var paramLambda by remember { mutableDoubleStateOf(1.0) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            DistPill("Normal", distType == "NORMAL") { distType = "NORMAL" }
            DistPill("Student's t", distType == "T") { distType = "T" }
            DistPill("Exponential", distType == "EXP") { distType = "EXP" }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                when (distType) {
                    "NORMAL" -> {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Mean (μ): ${"%.2f".format(paramMean)}", fontSize = 11.sp, color = RustOrange)
                            Text("Std Dev (σ): ${"%.2f".format(paramStd)}", fontSize = 11.sp, color = PolarsCyan)
                        }
                        Slider(
                            value = paramMean.toFloat(),
                            onValueChange = { paramMean = it.toDouble() },
                            valueRange = -5f..5f,
                            colors = SliderDefaults.colors(thumbColor = RustOrange, activeTrackColor = RustOrange)
                        )
                        Slider(
                            value = paramStd.toFloat(),
                            onValueChange = { paramStd = it.toDouble().coerceAtLeast(0.1) },
                            valueRange = 0.2f..4f,
                            colors = SliderDefaults.colors(thumbColor = PolarsCyan, activeTrackColor = PolarsCyan)
                        )
                    }

                    "T" -> {
                        Text("Degrees of Freedom (ν): ${"%.1f".format(paramDf)}", fontSize = 11.sp, color = MatrixGreen)
                        Slider(
                            value = paramDf.toFloat(),
                            onValueChange = { paramDf = it.toDouble().coerceAtLeast(1.0) },
                            valueRange = 1f..30f,
                            colors = SliderDefaults.colors(thumbColor = MatrixGreen, activeTrackColor = MatrixGreen)
                        )
                    }

                    "EXP" -> {
                        Text("Rate Parameter (λ): ${"%.2f".format(paramLambda)}", fontSize = 11.sp, color = RustOrange)
                        Slider(
                            value = paramLambda.toFloat(),
                            onValueChange = { paramLambda = it.toDouble().coerceAtLeast(0.1) },
                            valueRange = 0.2f..5f,
                            colors = SliderDefaults.colors(thumbColor = RustOrange, activeTrackColor = RustOrange)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Card(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
            shape = RoundedCornerShape(8.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .testTag("distribution_canvas")
            ) {
                val w = size.width
                val h = size.height
                val startX = if (distType == "EXP") 0.0 else -6.0
                val endX = if (distType == "EXP") 6.0 else 6.0
                val steps = 120
                val dx = (endX - startX) / steps

                val points = (0..steps).map { i ->
                    val x = startX + i * dx
                    val y = when (distType) {
                        "NORMAL" -> StatsEngine.normalPdf(x, paramMean, paramStd)
                        "T" -> StatsEngine.studentTPdf(x, paramDf)
                        "EXP" -> StatsEngine.exponentialPdf(x, paramLambda)
                        else -> 0.0
                    }
                    Pair(x, y)
                }

                val maxY = (points.maxOfOrNull { it.second } ?: 1.0).coerceAtLeast(0.4)

                drawLine(
                    color = Color.White.copy(alpha = 0.3f),
                    start = Offset(0f, h),
                    end = Offset(w, h),
                    strokeWidth = 2f
                )

                val path = Path()
                points.forEachIndexed { idx, (x, y) ->
                    val normX = ((x - startX) / (endX - startX)).toFloat() * w
                    val normY = h - ((y / maxY).toFloat() * (h * 0.85f))
                    if (idx == 0) path.moveTo(normX, normY) else path.lineTo(normX, normY)
                }

                drawPath(
                    path = path,
                    color = RustOrange,
                    style = Stroke(width = 3.dp.toPx())
                )
            }
        }
    }
}

@Composable
private fun HypothesisTestsTab(df: DataFrame, numericCols: List<String>) {
    var selectedColA by remember(numericCols) { mutableStateOf(numericCols.firstOrNull() ?: "") }
    var selectedColB by remember(numericCols) { mutableStateOf(numericCols.getOrNull(1) ?: "") }
    var testReport by remember { mutableStateOf<com.example.stats.HypothesisTestReport?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Hypothesis Testing Configuration:",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Sample A", fontSize = 10.sp, color = PolarsCyan)
                        Text(selectedColA, fontFamily = JetBrainsMonoFamily, fontSize = 12.sp, color = Color.White)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Sample B", fontSize = 10.sp, color = RustOrange)
                        Text(selectedColB, fontFamily = JetBrainsMonoFamily, fontSize = 12.sp, color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            val a = df.rows.mapNotNull { (it[selectedColA] as? Number)?.toDouble() }
                            val b = df.rows.mapNotNull { (it[selectedColB] as? Number)?.toDouble() }
                            testReport = StatsEngine.twoSampleTTest(a, b)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RustOrange),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.testTag("run_ttest_button")
                    ) {
                        Text("Run Two-Sample T-Test", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            val a = df.rows.mapNotNull { (it[selectedColA] as? Number)?.toDouble() }
                            val b = df.rows.mapNotNull { (it[selectedColB] as? Number)?.toDouble() }
                            testReport = StatsEngine.oneWayAnova(listOf(a, b))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PolarsCyan),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.testTag("run_anova_button")
                    ) {
                        Text("One-Way ANOVA", fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (testReport != null) {
            val r = testReport!!
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (r.isSignificant05) MatrixGreen else RustOrange),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(r.testName, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (r.isSignificant05) MatrixGreen.copy(alpha = 0.2f) else RustOrange.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (r.isSignificant05) "p < 0.05" else "p ≥ 0.05",
                                fontFamily = JetBrainsMonoFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = if (r.isSignificant05) MatrixGreen else RustOrange
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        MetricCell("${r.testStatisticName}-statistic", "%.4f".format(r.statistic), Modifier.weight(1f))
                        MetricCell("p-value", "%.5f".format(r.pValue), Modifier.weight(1f))
                        MetricCell("Degrees of Freedom", "%.1f".format(r.degreesOfFreedom), Modifier.weight(1f))
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(r.conclusion, fontFamily = JetBrainsMonoFamily, fontSize = 11.sp, color = Color(0xFFE2E8F0))
                }
            }
        }
    }
}

@Composable
private fun RegressionTab(df: DataFrame, numericCols: List<String>) {
    var predCol by remember(numericCols) { mutableStateOf(numericCols.firstOrNull() ?: "") }
    var targetCol by remember(numericCols) { mutableStateOf(numericCols.getOrNull(1) ?: "") }
    var regResult by remember { mutableStateOf<com.example.stats.RegressionResult?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Ordinary Least Squares (OLS) Linear Regression",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Feature (X)", fontSize = 10.sp, color = PolarsCyan)
                        Text(predCol, fontFamily = JetBrainsMonoFamily, fontSize = 12.sp, color = Color.White)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Target (Y)", fontSize = 10.sp, color = RustOrange)
                        Text(targetCol, fontFamily = JetBrainsMonoFamily, fontSize = 12.sp, color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        val x = df.rows.mapNotNull { (it[predCol] as? Number)?.toDouble() }
                        val y = df.rows.mapNotNull { (it[targetCol] as? Number)?.toDouble() }
                        regResult = StatsEngine.linearRegression(x, y)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MatrixGreen),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.testTag("fit_regression_button")
                ) {
                    Text("Fit Linear Regression", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (regResult != null) {
            val reg = regResult!!
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.dp, PolarsCyan),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Model Fit: ${reg.equation}",
                        fontFamily = JetBrainsMonoFamily,
                        fontWeight = FontWeight.Bold,
                        color = MatrixGreen,
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        MetricCell("R² Score", "%.4f".format(reg.rSquared), Modifier.weight(1f))
                        MetricCell("Pearson R", "%.4f".format(reg.pearsonR), Modifier.weight(1f))
                        MetricCell("Std Error", "%.4f".format(reg.stdError), Modifier.weight(1f))
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        MetricCell("Slope (m)", "%.4f".format(reg.slope), Modifier.weight(1f))
                        MetricCell("Intercept (b)", "%.4f".format(reg.intercept), Modifier.weight(1f))
                        MetricCell("p-value", "%.5f".format(reg.pValue), Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun MatrixLabTab() {
    var a11 by remember { mutableDoubleStateOf(4.0) }
    var a12 by remember { mutableDoubleStateOf(2.0) }
    var a21 by remember { mutableDoubleStateOf(3.0) }
    var a22 by remember { mutableDoubleStateOf(5.0) }

    // Determinant: ad - bc
    val det = (a11 * a22) - (a12 * a21)
    val trace = a11 + a22
    val frobNorm = sqrt(a11 * a11 + a12 * a12 + a21 * a21 + a22 * a22)

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Interactive 2×2 Matrix Workbench (salim-array)",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Live linear algebra operations powered by ndarray BLAS bindings:",
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // 2x2 Matrix Input Grid
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("a11", fontSize = 10.sp, color = PolarsCyan)
                        Slider(value = a11.toFloat(), onValueChange = { a11 = it.toDouble() }, valueRange = -10f..10f)
                        Text("${"%.1f".format(a11)}", fontFamily = JetBrainsMonoFamily, fontSize = 11.sp, color = Color.White)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("a12", fontSize = 10.sp, color = PolarsCyan)
                        Slider(value = a12.toFloat(), onValueChange = { a12 = it.toDouble() }, valueRange = -10f..10f)
                        Text("${"%.1f".format(a12)}", fontFamily = JetBrainsMonoFamily, fontSize = 11.sp, color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("a21", fontSize = 10.sp, color = RustOrange)
                        Slider(value = a21.toFloat(), onValueChange = { a21 = it.toDouble() }, valueRange = -10f..10f)
                        Text("${"%.1f".format(a21)}", fontFamily = JetBrainsMonoFamily, fontSize = 11.sp, color = Color.White)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("a22", fontSize = 10.sp, color = RustOrange)
                        Slider(value = a22.toFloat(), onValueChange = { a22 = it.toDouble() }, valueRange = -10f..10f)
                        Text("${"%.1f".format(a22)}", fontFamily = JetBrainsMonoFamily, fontSize = 11.sp, color = Color.White)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Matrix Results
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
            border = androidx.compose.foundation.BorderStroke(1.dp, PolarsCyan),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Matrix Properties & Inversion",
                    fontWeight = FontWeight.Bold,
                    color = MatrixGreen,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    MetricCell("Determinant det(A)", "%.3f".format(det), Modifier.weight(1f))
                    MetricCell("Trace Tr(A)", "%.3f".format(trace), Modifier.weight(1f))
                    MetricCell("Frobenius Norm", "%.3f".format(frobNorm), Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = if (det != 0.0) {
                        val inv11 = a22 / det
                        val inv12 = -a12 / det
                        val inv21 = -a21 / det
                        val inv22 = a11 / det
                        "Inverse A⁻¹:\n[ ${"%.2f".format(inv11)}  ${"%.2f".format(inv12)} ]\n[ ${"%.2f".format(inv21)}  ${"%.2f".format(inv22)} ]"
                    } else {
                        "Matrix is singular (det = 0). Inverse does not exist."
                    },
                    fontFamily = JetBrainsMonoFamily,
                    fontSize = 11.sp,
                    color = Color(0xFFA5D6FF)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF090D16))
                        .padding(8.dp)
                ) {
                    Text(
                        text = """// Salim Rust Equivalent:
let A = salim::matrix([[${"%.1f".format(a11)}, ${"%.1f".format(a12)}], [${"%.1f".format(a21)}, ${"%.1f".format(a22)}]]);
let det = A.det();
let inv = A.inv()?;""",
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 10.sp,
                        color = Color(0xFF86EFAC)
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricCell(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(vertical = 2.dp)) {
        Text(text = label, fontSize = 9.sp, color = Color(0xFF94A3B8))
        Text(text = value, fontFamily = JetBrainsMonoFamily, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, color = Color.White)
    }
}

@Composable
private fun DistPill(title: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (selected) RustOrange else DarkCard)
            .border(1.dp, if (selected) RustOrange else DarkBorder, RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (selected) Color.White else Color.LightGray
        )
    }
}
