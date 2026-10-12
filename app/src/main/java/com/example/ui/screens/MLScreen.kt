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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BubbleChart
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ModelTraining
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ColumnType
import com.example.data.DatasetInfo
import com.example.ml.MLEngine
import com.example.ml.MLModelEvaluation
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.MatrixGreen
import com.example.ui.theme.PolarsCyan
import com.example.ui.theme.RustOrange
import com.example.ui.components.ModelChip
import com.example.ui.components.ScoreCard

val ML_MODELS = listOf(
    "Logistic Regression",
    "Random Forest",
    "Decision Tree",
    "K-Means Clustering",
    "K-Nearest Neighbors"
)

@Composable
fun MLScreen(
    currentDataset: DatasetInfo,
    modifier: Modifier = Modifier
) {
    var selectedModel by remember { mutableStateOf(ML_MODELS[0]) }

    val numericCols = remember(currentDataset) {
        currentDataset.columns.filter { it.type == ColumnType.FLOAT || it.type == ColumnType.INT }.map { it.name }
    }

    var targetCol by remember(currentDataset) {
        val target = if (currentDataset.id == "titanic") "survived" else if (currentDataset.id == "iris") "species" else currentDataset.columns.first().name
        mutableStateOf(target)
    }

    val selectedFeatures = remember(currentDataset) {
        mutableStateListOf<String>().apply {
            addAll(numericCols.filter { it != targetCol }.take(4))
        }
    }

    var testRatio by remember { mutableDoubleStateOf(0.2) }
    var kClusters by remember { mutableIntStateOf(3) }
    var evaluationResult by remember { mutableStateOf<MLModelEvaluation?>(null) }

    // Live Prediction Form inputs
    val predictionInputs = remember(selectedFeatures) {
        mutableStateMapOf<String, Double>().apply {
            selectedFeatures.forEach { feat ->
                val sampleVal = (currentDataset.rows.firstOrNull()?.get(feat) as? Number)?.toDouble() ?: 10.0
                put(feat, sampleVal)
            }
        }
    }

    var livePredictionOutcome by remember { mutableStateOf<Pair<String, Double>?>(null) }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(12.dp)
    ) {
        Text(
            text = "SELECT SCALABLE ML MODEL",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = PolarsCyan
        )

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            ML_MODELS.take(3).forEach { model ->
                ModelChip(
                    name = model,
                    selected = selectedModel == model,
                    onClick = { selectedModel = model },
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            ML_MODELS.drop(3).forEach { model ->
                ModelChip(
                    name = model,
                    selected = selectedModel == model,
                    onClick = { selectedModel = model },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Configuration Card
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
                        text = "Features & Target Setup",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "Target: $targetCol",
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 11.sp,
                        color = RustOrange
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Feature Columns (Click to Toggle):",
                    fontSize = 10.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    numericCols.filter { it != targetCol }.forEach { col ->
                        val isSelected = selectedFeatures.contains(col)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isSelected) PolarsCyan.copy(alpha = 0.2f) else DarkSurfaceVariant)
                                .border(1.dp, if (isSelected) PolarsCyan else DarkBorder, RoundedCornerShape(4.dp))
                                .clickable {
                                    if (isSelected) selectedFeatures.remove(col) else selectedFeatures.add(col)
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = col,
                                fontFamily = JetBrainsMonoFamily,
                                fontSize = 10.sp,
                                color = if (isSelected) PolarsCyan else Color.LightGray
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (selectedModel == "K-Means Clustering") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Clusters (k):", fontSize = 11.sp, color = Color.White)
                        Text("$kClusters Clusters", fontFamily = JetBrainsMonoFamily, fontSize = 11.sp, color = MatrixGreen)
                    }
                    Slider(
                        value = kClusters.toFloat(),
                        onValueChange = { kClusters = it.toInt() },
                        valueRange = 2f..5f,
                        steps = 2,
                        colors = SliderDefaults.colors(thumbColor = MatrixGreen, activeTrackColor = MatrixGreen)
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Train/Test Split:", fontSize = 11.sp, color = Color.White)
                        Text(
                            text = "${((1.0 - testRatio) * 100).toInt()}% Train / ${(testRatio * 100).toInt()}% Test",
                            fontFamily = JetBrainsMonoFamily,
                            fontSize = 11.sp,
                            color = MatrixGreen
                        )
                    }

                    Slider(
                        value = testRatio.toFloat(),
                        onValueChange = { testRatio = it.toDouble() },
                        valueRange = 0.1f..0.4f,
                        colors = SliderDefaults.colors(thumbColor = MatrixGreen, activeTrackColor = MatrixGreen)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        val eval = MLEngine.trainClassifier(
                            modelName = selectedModel,
                            rows = currentDataset.rows,
                            featureCols = selectedFeatures,
                            targetCol = targetCol,
                            testRatio = testRatio
                        )
                        evaluationResult = eval
                        if (eval != null) {
                            val map = selectedFeatures.associateWith { feat ->
                                predictionInputs[feat] ?: 10.0
                            }
                            livePredictionOutcome = eval.predictFn(map)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("train_model_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = RustOrange),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Icon(Icons.Default.ModelTraining, "Train", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Fit & Evaluate $selectedModel", fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Evaluation Results Dashboard
        if (evaluationResult != null) {
            val eval = evaluationResult!!
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "EVALUATION METRICS (${eval.modelName})",
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MatrixGreen
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ScoreCard("ACCURACY", "${"%.1f".format(eval.accuracy * 100)}%", MatrixGreen, Modifier.weight(1f))
                        ScoreCard("PRECISION", "${"%.1f".format(eval.precision * 100)}%", PolarsCyan, Modifier.weight(1f))
                        ScoreCard("RECALL", "${"%.1f".format(eval.recall * 100)}%", RustOrange, Modifier.weight(1f))
                        ScoreCard("F1-SCORE", "${"%.2f".format(eval.f1Score)}", Color(0xFFA855F7), Modifier.weight(1f))
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Confusion Matrix Card
                    Text(
                        text = "Confusion Matrix (${eval.classLabels.joinToString(" vs ")})",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(DarkCard)
                            .padding(8.dp)
                    ) {
                        eval.confusionMatrix.forEachIndexed { rIdx, rowVals ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                rowVals.forEachIndexed { cIdx, count ->
                                    val isDiagonal = rIdx == cIdx
                                    Box(
                                        modifier = Modifier
                                            .size(54.dp, 36.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(if (isDiagonal) MatrixGreen.copy(alpha = 0.25f) else Color.Red.copy(alpha = 0.15f))
                                            .border(1.dp, if (isDiagonal) MatrixGreen else Color.Transparent, RoundedCornerShape(4.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "$count",
                                            fontFamily = JetBrainsMonoFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = if (isDiagonal) MatrixGreen else Color.Red
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 2D K-Means Clustering Canvas (if K-Means or multi-feature)
            if (selectedModel == "K-Means Clustering") {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PolarsCyan),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "K-Means 2D Clusters & Centroids (k = $kClusters)",
                            fontFamily = JetBrainsMonoFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PolarsCyan
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height
                            val clusterColors = listOf(MatrixGreen, RustOrange, PolarsCyan, Color(0xFFA855F7), Color(0xFFFFD166))

                            // Draw simulated 2D points clustered around centroids
                            for (i in 0 until kClusters) {
                                val cX = w * (0.25f + 0.5f * (i / (kClusters.toFloat())))
                                val cY = h * (0.3f + 0.4f * ((i % 2).toFloat()))

                                // Centroid marker
                                drawCircle(Color.White, 8.dp.toPx(), Offset(cX, cY))
                                drawCircle(clusterColors[i % clusterColors.size], 5.dp.toPx(), Offset(cX, cY))

                                // Scatter points around centroid
                                for (p in 0..12) {
                                    val offsetX = (kotlin.random.Random(p + i * 20).nextFloat() - 0.5f) * (w * 0.22f)
                                    val offsetY = (kotlin.random.Random(p * 2 + i * 20).nextFloat() - 0.5f) * (h * 0.35f)
                                    drawCircle(
                                        clusterColors[i % clusterColors.size].copy(alpha = 0.75f),
                                        4.dp.toPx(),
                                        Offset((cX + offsetX).coerceIn(10f, w - 10f), (cY + offsetY).coerceIn(10f, h - 10f))
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            // Interactive Live Prediction Sandbox
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.dp, PolarsCyan),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, "Predict", tint = PolarsCyan, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Live Interactive Predictor",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 13.sp
                            )
                        }

                        if (livePredictionOutcome != null) {
                            val (predictedClass, prob) = livePredictionOutcome!!
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(MatrixGreen)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Pred: $predictedClass (${"%.0f".format(prob * 100)}%)",
                                    fontFamily = JetBrainsMonoFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = Color.Black
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    selectedFeatures.forEach { feat ->
                        val currentVal = predictionInputs[feat] ?: 10.0
                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(feat, fontFamily = JetBrainsMonoFamily, fontSize = 11.sp, color = Color.LightGray)
                                Text("${"%.1f".format(currentVal)}", fontFamily = JetBrainsMonoFamily, fontSize = 11.sp, color = PolarsCyan)
                            }
                            Slider(
                                value = currentVal.toFloat(),
                                onValueChange = {
                                    predictionInputs[feat] = it.toDouble()
                                    livePredictionOutcome = eval.predictFn(predictionInputs.toMap())
                                },
                                valueRange = 0f..100f,
                                colors = SliderDefaults.colors(thumbColor = PolarsCyan, activeTrackColor = PolarsCyan)
                            )
                        }
                    }
                }
            }
        }
    }
}
