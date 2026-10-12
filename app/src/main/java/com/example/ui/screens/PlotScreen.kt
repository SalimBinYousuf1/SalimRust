package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ColumnType
import com.example.data.DataFrame
import com.example.data.DatasetInfo
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.MatrixGreen
import com.example.ui.theme.PolarsCyan
import com.example.ui.theme.RustOrange

enum class ChartType(val label: String) {
    SCATTER("Scatter"),
    LINE("Line"),
    BAR("Bar"),
    AREA("Area"),
    HISTOGRAM("Histogram"),
    CANDLESTICK("Candlestick"),
    BOX("Box Plot"),
    CORR_HEATMAP("Correlation"),
    PIE("Pie")
}

enum class SalimPlotTheme(val label: String, val bg: Color, val accent1: Color, val accent2: Color) {
    DARK_OBSIDIAN("Dark Obsidian", Color(0xFF0F172A), RustOrange, PolarsCyan),
    SOLARIZED_LIGHT("Solarized Light", Color(0xFFFDF6E3), Color(0xFFCB4B16), Color(0xFF268BD2)),
    MINIMALIST("Minimalist", Color(0xFF18181B), Color(0xFFF4F4F5), Color(0xFFA1A1AA)),
    COLORBLIND("Colorblind-Safe", Color(0xFF1E293B), Color(0xFFE69F00), Color(0xFF56B4E9))
}

@Composable
fun PlotScreen(
    currentDataset: DatasetInfo,
    modifier: Modifier = Modifier
) {
    val df = remember(currentDataset) { DataFrame.fromDataset(currentDataset) }

    val numericCols = remember(currentDataset) {
        currentDataset.columns.filter { it.type == ColumnType.FLOAT || it.type == ColumnType.INT }.map { it.name }
    }

    var selectedChart by remember { mutableStateOf(ChartType.SCATTER) }
    var selectedTheme by remember { mutableStateOf(SalimPlotTheme.DARK_OBSIDIAN) }

    var xCol by remember(currentDataset) {
        mutableStateOf(numericCols.firstOrNull() ?: currentDataset.columns.first().name)
    }
    var yCol by remember(currentDataset) {
        mutableStateOf(numericCols.getOrNull(1) ?: currentDataset.columns.first().name)
    }

    // Touch inspection tooltip
    var inspectedPoint by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    var inspectedLabel by remember { mutableStateOf<String?>(null) }
    var copiedSvgNotice by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        // Chart Type Selector Tabs
        ScrollableTabRow(
            selectedTabIndex = selectedChart.ordinal,
            containerColor = DarkSurface,
            contentColor = RustOrange,
            edgePadding = 0.dp,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
        ) {
            ChartType.values().forEach { chart ->
                Tab(
                    selected = selectedChart == chart,
                    onClick = {
                        selectedChart = chart
                        inspectedPoint = null
                    },
                    text = {
                        Text(
                            text = chart.label,
                            fontWeight = if (selectedChart == chart) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 12.sp,
                            color = if (selectedChart == chart) RustOrange else Color.LightGray
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Dimension & Theme Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("X-AXIS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = PolarsCyan)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(DarkCard)
                        .border(1.dp, DarkBorder, RoundedCornerShape(6.dp))
                        .clickable {
                            val nextIdx = (numericCols.indexOf(xCol) + 1) % numericCols.size.coerceAtLeast(1)
                            xCol = numericCols.getOrElse(nextIdx) { xCol }
                        }
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = xCol, fontFamily = JetBrainsMonoFamily, fontSize = 12.sp, color = Color.White)
                    Text("▾", fontSize = 10.sp, color = Color.Gray)
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text("Y-AXIS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = RustOrange)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(DarkCard)
                        .border(1.dp, DarkBorder, RoundedCornerShape(6.dp))
                        .clickable {
                            val nextIdx = (numericCols.indexOf(yCol) + 1) % numericCols.size.coerceAtLeast(1)
                            yCol = numericCols.getOrElse(nextIdx) { yCol }
                        }
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = yCol, fontFamily = JetBrainsMonoFamily, fontSize = 12.sp, color = Color.White)
                    Text("▾", fontSize = 10.sp, color = Color.Gray)
                }
            }

            Column(modifier = Modifier.weight(1.1f)) {
                Text("THEME", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MatrixGreen)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(DarkCard)
                        .border(1.dp, DarkBorder, RoundedCornerShape(6.dp))
                        .clickable {
                            val nextIdx = (selectedTheme.ordinal + 1) % SalimPlotTheme.values().size
                            selectedTheme = SalimPlotTheme.values()[nextIdx]
                        }
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selectedTheme.label.split(" ").first(),
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 11.sp,
                        color = Color.White
                    )
                    Icon(Icons.Default.Palette, "Theme", tint = MatrixGreen, modifier = Modifier.size(12.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Main Chart Canvas Card
        Card(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = selectedTheme.bg),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
            shape = RoundedCornerShape(10.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                val xValues = remember(df, xCol) {
                    df.rows.mapNotNull { (it[xCol] as? Number)?.toDouble() }
                }
                val yValues = remember(df, yCol) {
                    df.rows.mapNotNull { (it[yCol] as? Number)?.toDouble() }
                }

                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                        .testTag("chart_canvas")
                        .pointerInput(selectedChart, xValues, yValues) {
                            detectTapGestures { offset ->
                                val w = size.width
                                val h = size.height
                                val minX = xValues.minOrNull() ?: 0.0
                                val maxX = xValues.maxOrNull() ?: 1.0
                                val minY = yValues.minOrNull() ?: 0.0
                                val maxY = yValues.maxOrNull() ?: 1.0

                                val clickRatioX = (offset.x / w).coerceIn(0f, 1f)
                                val clickRatioY = (1f - (offset.y / h)).coerceIn(0f, 1f)

                                val approxX = minX + clickRatioX * (maxX - minX)
                                val approxY = minY + clickRatioY * (maxY - minY)

                                inspectedPoint = Pair(approxX, approxY)
                                inspectedLabel = "($xCol: ${"%.2f".format(approxX)}, $yCol: ${"%.2f".format(approxY)})"
                            }
                        }
                ) {
                    val w = size.width
                    val h = size.height
                    val paddingLeft = 40f
                    val paddingBottom = 40f
                    val plotW = w - paddingLeft
                    val plotH = h - paddingBottom

                    val minX = xValues.minOrNull() ?: 0.0
                    val maxX = (xValues.maxOrNull() ?: 10.0).let { if (it == minX) it + 1.0 else it }
                    val minY = yValues.minOrNull() ?: 0.0
                    val maxY = (yValues.maxOrNull() ?: 10.0).let { if (it == minY) it + 1.0 else it }

                    val gridColor = Color.White.copy(alpha = 0.12f)
                    for (i in 0..4) {
                        val yPos = paddingBottom + (plotH * (i / 4f))
                        drawLine(gridColor, Offset(paddingLeft, yPos), Offset(w, yPos), 1f)
                        val xPos = paddingLeft + (plotW * (i / 4f))
                        drawLine(gridColor, Offset(xPos, 0f), Offset(xPos, plotH), 1f)
                    }

                    val axisColor = Color.White.copy(alpha = 0.4f)
                    drawLine(axisColor, Offset(paddingLeft, plotH), Offset(w, plotH), 2f)
                    drawLine(axisColor, Offset(paddingLeft, 0f), Offset(paddingLeft, plotH), 2f)

                    when (selectedChart) {
                        ChartType.SCATTER -> {
                            val count = minOf(xValues.size, yValues.size)
                            for (i in 0 until count) {
                                val normX = ((xValues[i] - minX) / (maxX - minX)).toFloat()
                                val normY = ((yValues[i] - minY) / (maxY - minY)).toFloat()
                                val ptX = paddingLeft + normX * plotW
                                val ptY = plotH - normY * plotH

                                drawCircle(selectedTheme.accent1, 5.dp.toPx(), Offset(ptX, ptY))
                                drawCircle(Color.White.copy(alpha = 0.8f), 2.dp.toPx(), Offset(ptX, ptY))
                            }
                        }

                        ChartType.LINE -> {
                            val count = minOf(xValues.size, yValues.size)
                            if (count > 1) {
                                val sortedPairs = (0 until count).map { Pair(xValues[it], yValues[it]) }.sortedBy { it.first }
                                val path = Path()
                                sortedPairs.forEachIndexed { idx, (x, y) ->
                                    val normX = ((x - minX) / (maxX - minX)).toFloat()
                                    val normY = ((y - minY) / (maxY - minY)).toFloat()
                                    val ptX = paddingLeft + normX * plotW
                                    val ptY = plotH - normY * plotH
                                    if (idx == 0) path.moveTo(ptX, ptY) else path.lineTo(ptX, ptY)
                                }
                                drawPath(path, selectedTheme.accent1, style = Stroke(width = 3.dp.toPx()))
                            }
                        }

                        ChartType.AREA -> {
                            val count = minOf(xValues.size, yValues.size)
                            if (count > 1) {
                                val sortedPairs = (0 until count).map { Pair(xValues[it], yValues[it]) }.sortedBy { it.first }
                                val path = Path()
                                path.moveTo(paddingLeft, plotH)
                                sortedPairs.forEach { (x, y) ->
                                    val normX = ((x - minX) / (maxX - minX)).toFloat()
                                    val normY = ((y - minY) / (maxY - minY)).toFloat()
                                    path.lineTo(paddingLeft + normX * plotW, plotH - normY * plotH)
                                }
                                path.lineTo(paddingLeft + plotW, plotH)
                                path.close()
                                drawPath(path, selectedTheme.accent2.copy(alpha = 0.45f))
                            }
                        }

                        ChartType.CANDLESTICK -> {
                            val bars = 8
                            val step = plotW / bars
                            for (i in 0 until bars) {
                                val isBullish = i % 2 == 0
                                val center = paddingLeft + i * step + step / 2
                                val highY = plotH * (0.15f + (i * 0.05f))
                                val lowY = plotH * (0.85f - (i * 0.02f))
                                val openY = if (isBullish) plotH * 0.65f else plotH * 0.35f
                                val closeY = if (isBullish) plotH * 0.35f else plotH * 0.65f

                                val color = if (isBullish) MatrixGreen else Color(0xFFEF4444)
                                drawLine(color, Offset(center, highY), Offset(center, lowY), 2.dp.toPx())
                                drawRect(
                                    color = color,
                                    topLeft = Offset(center - 8.dp.toPx(), minOf(openY, closeY)),
                                    size = Size(16.dp.toPx(), kotlin.math.abs(closeY - openY).coerceAtLeast(4f))
                                )
                            }
                        }

                        ChartType.BAR -> {
                            val barCount = 6.coerceAtMost(xValues.size)
                            val barWidth = (plotW / barCount) * 0.65f
                            val step = plotW / barCount

                            for (i in 0 until barCount) {
                                val v = if (i < yValues.size) yValues[i] else 5.0
                                val normY = ((v - minY) / (maxY - minY)).toFloat().coerceIn(0.1f, 1f)
                                val barH = normY * plotH
                                val startX = paddingLeft + (i * step) + (step - barWidth) / 2

                                drawRect(
                                    color = if (i % 2 == 0) selectedTheme.accent1 else selectedTheme.accent2,
                                    topLeft = Offset(startX, plotH - barH),
                                    size = Size(barWidth, barH)
                                )
                            }
                        }

                        ChartType.HISTOGRAM -> {
                            val bins = 8
                            val binWidth = plotW / bins
                            for (i in 0 until bins) {
                                val binRatio = (i + 1) / (bins.toFloat() + 1)
                                val binH = (binRatio * 0.8f + 0.15f) * plotH
                                val startX = paddingLeft + i * binWidth

                                drawRect(
                                    color = selectedTheme.accent2.copy(alpha = 0.85f),
                                    topLeft = Offset(startX + 2f, plotH - binH),
                                    size = Size(binWidth - 4f, binH)
                                )
                            }
                        }

                        ChartType.BOX -> {
                            val boxCenter = paddingLeft + plotW * 0.5f
                            val boxWidth = plotW * 0.35f
                            val q1 = plotH * 0.65f
                            val q3 = plotH * 0.25f
                            val med = plotH * 0.45f

                            drawLine(selectedTheme.accent1, Offset(boxCenter, plotH * 0.1f), Offset(boxCenter, plotH * 0.85f), 2.dp.toPx())
                            drawRect(selectedTheme.accent2.copy(alpha = 0.5f), Offset(boxCenter - boxWidth / 2, q3), Size(boxWidth, q1 - q3))
                            drawRect(selectedTheme.accent2, Offset(boxCenter - boxWidth / 2, q3), Size(boxWidth, q1 - q3), style = Stroke(2.dp.toPx()))
                            drawLine(RustOrange, Offset(boxCenter - boxWidth / 2, med), Offset(boxCenter + boxWidth / 2, med), 3.dp.toPx())
                        }

                        ChartType.CORR_HEATMAP -> {
                            val matrixSize = 3
                            val cellSize = minOf(plotW, plotH) / matrixSize
                            for (r in 0 until matrixSize) {
                                for (c in 0 until matrixSize) {
                                    val corr = if (r == c) 1.0 else 0.45 + (r + c) * 0.12
                                    val alpha = corr.toFloat().coerceIn(0.2f, 1f)
                                    val cellColor = if (corr > 0.5) selectedTheme.accent1 else selectedTheme.accent2

                                    drawRect(cellColor.copy(alpha = alpha), Offset(paddingLeft + c * cellSize, r * cellSize), Size(cellSize - 2f, cellSize - 2f))
                                }
                            }
                        }

                        ChartType.PIE -> {
                            val center = Offset(paddingLeft + plotW / 2, plotH / 2)
                            val radius = minOf(plotW, plotH) * 0.4f
                            val colors = listOf(selectedTheme.accent1, selectedTheme.accent2, MatrixGreen, Color(0xFFA855F7))
                            val angles = listOf(140f, 100f, 75f, 45f)

                            var currentAngle = 0f
                            angles.forEachIndexed { idx, sweep ->
                                drawArc(colors[idx % colors.size], currentAngle, sweep, true, Offset(center.x - radius, center.y - radius), Size(radius * 2, radius * 2))
                                currentAngle += sweep
                            }
                        }
                    }
                }

                if (inspectedLabel != null) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(DarkSurfaceVariant)
                            .border(1.dp, RustOrange, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.TouchApp, "Point", tint = RustOrange, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = inspectedLabel ?: "", fontFamily = JetBrainsMonoFamily, fontSize = 11.sp, color = Color.White)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Generated Salim Code Box & Copy SVG
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF090D16)),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Code, "Code", tint = MatrixGreen, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "SALIM-PLOT CODE GENERATOR",
                            fontFamily = JetBrainsMonoFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MatrixGreen
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { copiedSvgNotice = true }
                    ) {
                        Icon(Icons.Default.ContentCopy, "SVG", tint = PolarsCyan, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (copiedSvgNotice) "Copied SVG!" else "Copy SVG",
                            fontSize = 10.sp,
                            color = PolarsCyan
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                val rustSnippet = when (selectedChart) {
                    ChartType.SCATTER -> "df.plot().scatter(\"$xCol\", \"$yCol\").theme(Theme::${selectedTheme.name}).render()?;"
                    ChartType.LINE -> "df.plot().line(\"$xCol\", \"$yCol\").theme(Theme::${selectedTheme.name}).render()?;"
                    ChartType.AREA -> "df.plot().area(\"$xCol\", \"$yCol\").theme(Theme::${selectedTheme.name}).render()?;"
                    ChartType.CANDLESTICK -> "df.plot().candlestick(\"open\", \"high\", \"low\", \"close\").render()?;"
                    ChartType.BAR -> "df.plot().bar(\"$xCol\", \"$yCol\").theme(Theme::${selectedTheme.name}).render()?;"
                    ChartType.HISTOGRAM -> "df.plot().hist(\"$xCol\", 20).theme(Theme::${selectedTheme.name}).render()?;"
                    ChartType.BOX -> "df.plot().box_plot(\"$xCol\").theme(Theme::${selectedTheme.name}).render()?;"
                    ChartType.CORR_HEATMAP -> "df.plot().corr_heatmap().theme(Theme::${selectedTheme.name}).render()?;"
                    ChartType.PIE -> "df.plot().pie(\"$xCol\").theme(Theme::${selectedTheme.name}).render()?;"
                }

                Text(
                    text = rustSnippet,
                    fontFamily = JetBrainsMonoFamily,
                    fontSize = 11.sp,
                    color = Color(0xFFA5D6FF)
                )
            }
        }
    }
}
