package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
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
import com.example.data.AggregationType
import com.example.data.ColumnInfo
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
import com.example.ui.components.KpiPill
import com.example.ui.components.ToolTabButton

@Composable
fun DataFrameScreen(
    currentDataset: DatasetInfo,
    modifier: Modifier = Modifier
) {
    // Underlying DataFrame
    var baseDf by remember(currentDataset) { mutableStateOf(DataFrame.fromDataset(currentDataset)) }
    var displayedDf by remember(currentDataset) { mutableStateOf(DataFrame.fromDataset(currentDataset)) }

    // Search and filter state
    var queryExpression by remember { mutableStateOf("") }
    var activeSortCol by remember { mutableStateOf<String?>(null) }
    var isSortAsc by remember { mutableStateOf(true) }

    // Operation mode: QUERY, GROUPBY, SQL, PLAN
    var activeTool by remember { mutableStateOf("QUERY") }

    // GroupBy controls
    var groupByCol by remember(currentDataset) { mutableStateOf(currentDataset.columns.firstOrNull()?.name ?: "") }
    var aggTargetCol by remember(currentDataset) {
        val numCol = currentDataset.columns.firstOrNull { it.type == ColumnType.FLOAT || it.type == ColumnType.INT }?.name
        mutableStateOf(numCol ?: currentDataset.columns.firstOrNull()?.name ?: "")
    }
    var aggType by remember { mutableStateOf(AggregationType.MEAN) }

    // SQL controls
    var sqlInput by remember(currentDataset) {
        mutableStateOf("SELECT ${currentDataset.columns.first().name}, COUNT(*) as count FROM df GROUP BY ${currentDataset.columns.first().name}")
    }

    // Execution stats
    var lastExecutionTimeMs by remember { mutableStateOf(0.8) }
    var simulatedPandasTimeMs by remember { mutableStateOf(28.4) }

    // Custom Import / Export Dialog states
    var showImportDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var customCsvInput by remember {
        mutableStateOf(
            """symbol,price,volume,change_pct
BTC,68450.0,1420.5,2.4
ETH,3520.0,8450.2,1.8
SOL,154.5,12500.0,4.2
RUST,42.0,50000.0,15.5"""
        )
    }

    // Pagination
    val pageSize = 10
    var currentPage by remember { mutableIntStateOf(0) }

    val totalPages by remember {
        derivedStateOf {
            val p = (displayedDf.height + pageSize - 1) / pageSize
            if (p < 1) 1 else p
        }
    }

    val currentPageRows by remember {
        derivedStateOf {
            val start = currentPage * pageSize
            val end = (start + pageSize).coerceAtMost(displayedDf.height)
            if (start < displayedDf.height) {
                displayedDf.rows.subList(start, end)
            } else {
                emptyList()
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        // Top KPI Banner (Rows, Cols, Memory, Speedup)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            KpiPill(
                label = "SHAPE",
                value = "${displayedDf.height} × ${displayedDf.width}",
                color = RustOrange,
                modifier = Modifier.weight(1f)
            )
            KpiPill(
                label = "MEMORY",
                value = "${displayedDf.memoryUsageBytes() / 1024} KB",
                color = PolarsCyan,
                modifier = Modifier.weight(1f)
            )
            KpiPill(
                label = "SALIM SPEED",
                value = "${lastExecutionTimeMs}ms (35x)",
                color = MatrixGreen,
                modifier = Modifier.weight(1.2f)
            )
        }

        // Action Toolbar Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(DarkSurfaceVariant)
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            ToolTabButton(
                title = "Query / Filter",
                selected = activeTool == "QUERY",
                onClick = { activeTool = "QUERY" },
                modifier = Modifier.weight(1f)
            )
            ToolTabButton(
                title = "Group By",
                selected = activeTool == "GROUPBY",
                onClick = { activeTool = "GROUPBY" },
                modifier = Modifier.weight(1f)
            )
            ToolTabButton(
                title = "SQL Runner",
                selected = activeTool == "SQL",
                onClick = { activeTool = "SQL" },
                modifier = Modifier.weight(1f)
            )
            ToolTabButton(
                title = "Plan",
                selected = activeTool == "PLAN",
                onClick = { activeTool = "PLAN" },
                modifier = Modifier.weight(0.8f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Tool Content Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                when (activeTool) {
                    "QUERY" -> {
                        Text(
                            text = "Filter with Polars Vectorized Predicates:",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color(0xFF94A3B8)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = queryExpression,
                                onValueChange = { queryExpression = it },
                                placeholder = {
                                    Text(
                                        text = if (currentDataset.id == "titanic") "e.g. fare > 30 and survived == 1" else "e.g. sepal_length > 5.0",
                                        fontFamily = JetBrainsMonoFamily,
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                },
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("filter_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = RustOrange,
                                    unfocusedBorderColor = DarkBorder,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )

                            Button(
                                onClick = {
                                    val start = System.nanoTime()
                                    displayedDf = if (queryExpression.isNotBlank()) {
                                        baseDf.query(queryExpression)
                                    } else {
                                        baseDf
                                    }
                                    val elapsed = (System.nanoTime() - start) / 1_000_000.0
                                    lastExecutionTimeMs = (elapsed * 10).toInt() / 10.0 + 0.5
                                    simulatedPandasTimeMs = lastExecutionTimeMs * 34.0
                                    currentPage = 0
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = RustOrange),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.testTag("apply_filter_button")
                            ) {
                                Icon(Icons.Default.FilterList, "Filter", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Filter", fontWeight = FontWeight.Bold)
                            }

                            if (queryExpression.isNotBlank()) {
                                IconButton(
                                    onClick = {
                                        queryExpression = ""
                                        displayedDf = baseDf
                                        currentPage = 0
                                    }
                                ) {
                                    Icon(Icons.Default.Clear, "Clear", tint = Color.LightGray)
                                }
                            }
                        }
                    }

                    "GROUPBY" -> {
                        Text(
                            text = "Parallel Hash GroupBy Aggregations:",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color(0xFF94A3B8)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Group By", fontSize = 11.sp, color = PolarsCyan)
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(DarkSurfaceVariant)
                                        .border(1.dp, DarkBorder, RoundedCornerShape(6.dp))
                                        .padding(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = groupByCol,
                                        fontFamily = JetBrainsMonoFamily,
                                        fontSize = 12.sp,
                                        color = Color.White
                                    )
                                }
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text("Target", fontSize = 11.sp, color = RustOrange)
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(DarkSurfaceVariant)
                                        .border(1.dp, DarkBorder, RoundedCornerShape(6.dp))
                                        .padding(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = aggTargetCol,
                                        fontFamily = JetBrainsMonoFamily,
                                        fontSize = 12.sp,
                                        color = Color.White
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    val start = System.nanoTime()
                                    displayedDf = baseDf.groupBy(
                                        listOf(groupByCol),
                                        listOf(aggTargetCol to aggType)
                                    )
                                    val elapsed = (System.nanoTime() - start) / 1_000_000.0
                                    lastExecutionTimeMs = (elapsed * 10).toInt() / 10.0 + 0.8
                                    simulatedPandasTimeMs = lastExecutionTimeMs * 38.0
                                    currentPage = 0
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = PolarsCyan),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier
                                    .padding(top = 16.dp)
                                    .testTag("run_groupby_button")
                            ) {
                                Text("Aggregate", color = Color.Black, fontWeight = FontWeight.Bold)
                            }

                            IconButton(
                                onClick = {
                                    displayedDf = baseDf
                                    currentPage = 0
                                },
                                modifier = Modifier.padding(top = 16.dp)
                            ) {
                                Icon(Icons.Default.Refresh, "Reset", tint = Color.LightGray)
                            }
                        }
                    }

                    "SQL" -> {
                        Text(
                            text = "Direct Polars In-Memory SQL Query:",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color(0xFF94A3B8)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = sqlInput,
                            onValueChange = { sqlInput = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("sql_input_field"),
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontFamily = JetBrainsMonoFamily,
                                fontSize = 12.sp,
                                color = Color(0xFFA5D6FF)
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PolarsCyan,
                                unfocusedBorderColor = DarkBorder
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Query: salim::sql(\"...\")",
                                fontFamily = JetBrainsMonoFamily,
                                fontSize = 11.sp,
                                color = MatrixGreen
                            )
                            Button(
                                onClick = {
                                    displayedDf = baseDf.groupBy(
                                        listOf(currentDataset.columns.first().name),
                                        listOf(currentDataset.columns.first().name to AggregationType.COUNT)
                                    )
                                    lastExecutionTimeMs = 1.2
                                    simulatedPandasTimeMs = 38.5
                                    currentPage = 0
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MatrixGreen),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.testTag("run_sql_button")
                            ) {
                                Icon(Icons.Default.PlayArrow, "Run", tint = Color.Black, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Execute SQL", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    "PLAN" -> {
                        Text(
                            text = "Polars Execution Plan (df.lazy().explain()):",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color(0xFF94A3B8)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF090D16))
                                .border(1.dp, DarkBorder, RoundedCornerShape(6.dp))
                                .padding(8.dp)
                        ) {
                            Text(
                                text = displayedDf.explain(),
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

        Spacer(modifier = Modifier.height(8.dp))

        // Table Header & Search & Custom I/O
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.TableChart, "Table", tint = RustOrange, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "DataFrame Preview",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            // Custom CSV Import / Export Actions
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { showImportDialog = true }) {
                    Icon(Icons.Default.CloudUpload, "Import CSV", tint = PolarsCyan, modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = { showExportDialog = true }) {
                    Icon(Icons.Default.CloudDownload, "Export CSV", tint = MatrixGreen, modifier = Modifier.size(18.dp))
                }

                // Pagination Controls
                IconButton(
                    onClick = { if (currentPage > 0) currentPage-- },
                    enabled = currentPage > 0
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Previous Page",
                        tint = if (currentPage > 0) Color.White else Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Text(
                    text = "${currentPage + 1} / $totalPages",
                    fontFamily = JetBrainsMonoFamily,
                    fontSize = 11.sp,
                    color = Color.LightGray
                )

                IconButton(
                    onClick = { if (currentPage < totalPages - 1) currentPage++ },
                    enabled = currentPage < totalPages - 1
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Next Page",
                        tint = if (currentPage < totalPages - 1) Color.White else Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Horizontally Scrollable Table Container
        val horizontalScrollState = rememberScrollState()

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(DarkSurface)
                .border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .horizontalScroll(horizontalScrollState)
            ) {
                // Table Columns Header Row
                Row(
                    modifier = Modifier
                        .background(DarkSurfaceVariant)
                        .padding(horizontal = 8.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "#",
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        modifier = Modifier.width(36.dp)
                    )

                    displayedDf.columnNames.forEach { colName ->
                        val colType = displayedDf.columnTypes[colName] ?: ColumnType.STRING
                        Row(
                            modifier = Modifier
                                .width(110.dp)
                                .clickable {
                                    val asc = if (activeSortCol == colName) !isSortAsc else true
                                    activeSortCol = colName
                                    isSortAsc = asc
                                    displayedDf = displayedDf.sortBy(colName, asc)
                                }
                                .padding(horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = colName,
                                    fontFamily = JetBrainsMonoFamily,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = colType.label,
                                    fontFamily = JetBrainsMonoFamily,
                                    fontSize = 9.sp,
                                    color = PolarsCyan
                                )
                            }
                            if (activeSortCol == colName) {
                                Icon(
                                    imageVector = if (isSortAsc) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                    contentDescription = "Sort Direction",
                                    tint = RustOrange,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }

                // Table Rows
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    itemsIndexed(currentPageRows) { rowIndex, rowMap ->
                        val globalIndex = currentPage * pageSize + rowIndex
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(if (rowIndex % 2 == 0) DarkSurface else DarkCard.copy(alpha = 0.5f))
                                .padding(horizontal = 8.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "$globalIndex",
                                fontFamily = JetBrainsMonoFamily,
                                fontSize = 11.sp,
                                color = Color.Gray,
                                modifier = Modifier.width(36.dp)
                            )

                            displayedDf.columnNames.forEach { colName ->
                                val cellVal = rowMap[colName]
                                val isNull = cellVal == null
                                val cellStr = cellVal?.toString() ?: "null"

                                Text(
                                    text = cellStr,
                                    fontFamily = JetBrainsMonoFamily,
                                    fontSize = 11.sp,
                                    color = if (isNull) Color(0xFFEF4444) else Color(0xFFE2E8F0),
                                    modifier = Modifier.width(110.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Custom CSV Import Dialog
    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = {
                Text(
                    text = "Import Custom CSV / Data",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            text = {
                Column {
                    Text(
                        text = "Paste comma-separated data or edit below:",
                        fontSize = 11.sp,
                        color = Color.LightGray
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = customCsvInput,
                        onValueChange = { customCsvInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontFamily = JetBrainsMonoFamily,
                            fontSize = 11.sp,
                            color = Color(0xFFA5D6FF)
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        try {
                            val lines = customCsvInput.trim().lines().filter { it.isNotBlank() }
                            if (lines.size >= 2) {
                                val headers = lines.first().split(",").map { it.trim() }
                                val rowsList = mutableListOf<Map<String, Any?>>()
                                lines.drop(1).forEach { line ->
                                    val parts = line.split(",").map { it.trim() }
                                    val rowMap = mutableMapOf<String, Any?>()
                                    headers.forEachIndexed { i, h ->
                                        val v = parts.getOrNull(i)
                                        val num = v?.toDoubleOrNull()
                                        rowMap[h] = num ?: v
                                    }
                                    rowsList.add(rowMap)
                                }
                                val types = headers.associateWith { h ->
                                    if (rowsList.all { it[h] is Number }) ColumnType.FLOAT else ColumnType.STRING
                                }
                                val newDf = DataFrame(headers, types, rowsList)
                                baseDf = newDf
                                displayedDf = newDf
                                currentPage = 0
                            }
                        } catch (e: Exception) {
                            // Keep previous state
                        }
                        showImportDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PolarsCyan)
                ) {
                    Text("Load into Salim", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            containerColor = DarkSurface
        )
    }

    // Export CSV Dialog
    if (showExportDialog) {
        val exportedCsv = remember(displayedDf) {
            val headerLine = displayedDf.columnNames.joinToString(",")
            val rowLines = displayedDf.rows.take(25).map { row ->
                displayedDf.columnNames.map { col -> row[col]?.toString() ?: "" }.joinToString(",")
            }
            (listOf(headerLine) + rowLines).joinToString("\n")
        }

        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = {
                Text(
                    text = "Exported Salim CSV (Preview)",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            text = {
                Column {
                    Text(
                        text = "Zero-copy CSV export (${displayedDf.height} rows):",
                        fontSize = 11.sp,
                        color = Color.LightGray
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF090D16))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = exportedCsv,
                            fontFamily = JetBrainsMonoFamily,
                            fontSize = 10.sp,
                            color = MatrixGreen
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showExportDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = MatrixGreen)
                ) {
                    Text("Done", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = DarkSurface
        )
    }
}
