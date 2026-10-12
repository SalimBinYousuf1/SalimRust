package com.example.data

data class DatasetInfo(
    val id: String,
    val name: String,
    val description: String,
    val columns: List<ColumnInfo>,
    val rows: List<Map<String, Any?>>
)

data class ColumnInfo(
    val name: String,
    val type: ColumnType
)

enum class ColumnType(val label: String) {
    FLOAT("f64"),
    INT("i64"),
    STRING("str"),
    BOOLEAN("bool")
}

object SampleDatasets {
    val TITANIC = DatasetInfo(
        id = "titanic",
        name = "Titanic Passengers",
        description = "Demographics and survival data of 891 passengers on the Titanic.",
        columns = listOf(
            ColumnInfo("survived", ColumnType.INT),
            ColumnInfo("pclass", ColumnType.INT),
            ColumnInfo("sex", ColumnType.STRING),
            ColumnInfo("age", ColumnType.FLOAT),
            ColumnInfo("sibsp", ColumnType.INT),
            ColumnInfo("parch", ColumnType.INT),
            ColumnInfo("fare", ColumnType.FLOAT),
            ColumnInfo("embarked", ColumnType.STRING)
        ),
        rows = listOf(
            mapOf("survived" to 0, "pclass" to 3, "sex" to "male", "age" to 22.0, "sibsp" to 1, "parch" to 0, "fare" to 7.25, "embarked" to "S"),
            mapOf("survived" to 1, "pclass" to 1, "sex" to "female", "age" to 38.0, "sibsp" to 1, "parch" to 0, "fare" to 71.28, "embarked" to "C"),
            mapOf("survived" to 1, "pclass" to 3, "sex" to "female", "age" to 26.0, "sibsp" to 0, "parch" to 0, "fare" to 7.92, "embarked" to "S"),
            mapOf("survived" to 1, "pclass" to 1, "sex" to "female", "age" to 35.0, "sibsp" to 1, "parch" to 0, "fare" to 53.10, "embarked" to "S"),
            mapOf("survived" to 0, "pclass" to 3, "sex" to "male", "age" to 35.0, "sibsp" to 0, "parch" to 0, "fare" to 8.05, "embarked" to "S"),
            mapOf("survived" to 0, "pclass" to 3, "sex" to "male", "age" to null, "sibsp" to 0, "parch" to 0, "fare" to 8.46, "embarked" to "Q"),
            mapOf("survived" to 0, "pclass" to 1, "sex" to "male", "age" to 54.0, "sibsp" to 0, "parch" to 0, "fare" to 51.86, "embarked" to "S"),
            mapOf("survived" to 0, "pclass" to 3, "sex" to "male", "age" to 2.0, "sibsp" to 3, "parch" to 1, "fare" to 21.07, "embarked" to "S"),
            mapOf("survived" to 1, "pclass" to 3, "sex" to "female", "age" to 27.0, "sibsp" to 0, "parch" to 2, "fare" to 11.13, "embarked" to "S"),
            mapOf("survived" to 1, "pclass" to 2, "sex" to "female", "age" to 14.0, "sibsp" to 1, "parch" to 0, "fare" to 30.07, "embarked" to "C"),
            mapOf("survived" to 1, "pclass" to 3, "sex" to "female", "age" to 4.0, "sibsp" to 1, "parch" to 1, "fare" to 16.70, "embarked" to "S"),
            mapOf("survived" to 1, "pclass" to 1, "sex" to "female", "age" to 58.0, "sibsp" to 0, "parch" to 0, "fare" to 26.55, "embarked" to "S"),
            mapOf("survived" to 0, "pclass" to 3, "sex" to "male", "age" to 20.0, "sibsp" to 0, "parch" to 0, "fare" to 8.05, "embarked" to "S"),
            mapOf("survived" to 0, "pclass" to 3, "sex" to "male", "age" to 39.0, "sibsp" to 1, "parch" to 5, "fare" to 31.27, "embarked" to "S"),
            mapOf("survived" to 0, "pclass" to 3, "sex" to "female", "age" to 14.0, "sibsp" to 0, "parch" to 0, "fare" to 7.85, "embarked" to "S"),
            mapOf("survived" to 1, "pclass" to 2, "sex" to "female", "age" to 55.0, "sibsp" to 0, "parch" to 0, "fare" to 16.00, "embarked" to "S"),
            mapOf("survived" to 0, "pclass" to 3, "sex" to "male", "age" to 2.0, "sibsp" to 4, "parch" to 1, "fare" to 29.12, "embarked" to "Q"),
            mapOf("survived" to 1, "pclass" to 2, "sex" to "male", "age" to 31.0, "sibsp" to 0, "parch" to 0, "fare" to 13.00, "embarked" to "S"),
            mapOf("survived" to 0, "pclass" to 3, "sex" to "female", "age" to 18.0, "sibsp" to 1, "parch" to 0, "fare" to 18.00, "embarked" to "S"),
            mapOf("survived" to 1, "pclass" to 3, "sex" to "female", "age" to 22.0, "sibsp" to 0, "parch" to 0, "fare" to 7.22, "embarked" to "C"),
            mapOf("survived" to 0, "pclass" to 2, "sex" to "male", "age" to 35.0, "sibsp" to 0, "parch" to 0, "fare" to 26.00, "embarked" to "S"),
            mapOf("survived" to 1, "pclass" to 2, "sex" to "male", "age" to 34.0, "sibsp" to 0, "parch" to 0, "fare" to 13.00, "embarked" to "S"),
            mapOf("survived" to 1, "pclass" to 3, "sex" to "female", "age" to 15.0, "sibsp" to 0, "parch" to 0, "fare" to 8.02, "embarked" to "Q"),
            mapOf("survived" to 1, "pclass" to 1, "sex" to "male", "age" to 28.0, "sibsp" to 0, "parch" to 0, "fare" to 35.50, "embarked" to "S"),
            mapOf("survived" to 0, "pclass" to 3, "sex" to "female", "age" to 8.0, "sibsp" to 3, "parch" to 1, "fare" to 21.07, "embarked" to "S"),
            mapOf("survived" to 1, "pclass" to 3, "sex" to "female", "age" to 38.0, "sibsp" to 1, "parch" to 5, "fare" to 31.38, "embarked" to "S"),
            mapOf("survived" to 0, "pclass" to 3, "sex" to "male", "age" to null, "sibsp" to 0, "parch" to 0, "fare" to 7.22, "embarked" to "C"),
            mapOf("survived" to 0, "pclass" to 1, "sex" to "male", "age" to 19.0, "sibsp" to 3, "parch" to 2, "fare" to 263.00, "embarked" to "S"),
            mapOf("survived" to 1, "pclass" to 3, "sex" to "female", "age" to null, "sibsp" to 0, "parch" to 0, "fare" to 7.87, "embarked" to "Q"),
            mapOf("survived" to 0, "pclass" to 3, "sex" to "male", "age" to 40.0, "sibsp" to 0, "parch" to 0, "fare" to 7.89, "embarked" to "S")
        )
    )

    val IRIS = DatasetInfo(
        id = "iris",
        name = "Iris Flowers",
        description = "Fisher's classic dataset of 150 iris flowers with 4 morphological measurements.",
        columns = listOf(
            ColumnInfo("sepal_length", ColumnType.FLOAT),
            ColumnInfo("sepal_width", ColumnType.FLOAT),
            ColumnInfo("petal_length", ColumnType.FLOAT),
            ColumnInfo("petal_width", ColumnType.FLOAT),
            ColumnInfo("species", ColumnType.STRING)
        ),
        rows = listOf(
            mapOf("sepal_length" to 5.1, "sepal_width" to 3.5, "petal_length" to 1.4, "petal_width" to 0.2, "species" to "setosa"),
            mapOf("sepal_length" to 4.9, "sepal_width" to 3.0, "petal_length" to 1.4, "petal_width" to 0.2, "species" to "setosa"),
            mapOf("sepal_length" to 4.7, "sepal_width" to 3.2, "petal_length" to 1.3, "petal_width" to 0.2, "species" to "setosa"),
            mapOf("sepal_length" to 4.6, "sepal_width" to 3.1, "petal_length" to 1.5, "petal_width" to 0.2, "species" to "setosa"),
            mapOf("sepal_length" to 5.0, "sepal_width" to 3.6, "petal_length" to 1.4, "petal_width" to 0.2, "species" to "setosa"),
            mapOf("sepal_length" to 5.4, "sepal_width" to 3.9, "petal_length" to 1.7, "petal_width" to 0.4, "species" to "setosa"),
            mapOf("sepal_length" to 4.6, "sepal_width" to 3.4, "petal_length" to 1.4, "petal_width" to 0.3, "species" to "setosa"),
            mapOf("sepal_length" to 5.0, "sepal_width" to 3.4, "petal_length" to 1.5, "petal_width" to 0.2, "species" to "setosa"),
            mapOf("sepal_length" to 7.0, "sepal_width" to 3.2, "petal_length" to 4.7, "petal_width" to 1.4, "species" to "versicolor"),
            mapOf("sepal_length" to 6.4, "sepal_width" to 3.2, "petal_length" to 4.5, "petal_width" to 1.5, "species" to "versicolor"),
            mapOf("sepal_length" to 6.9, "sepal_width" to 3.1, "petal_length" to 4.9, "petal_width" to 1.5, "species" to "versicolor"),
            mapOf("sepal_length" to 5.5, "sepal_width" to 2.3, "petal_length" to 4.0, "petal_width" to 1.3, "species" to "versicolor"),
            mapOf("sepal_length" to 6.5, "sepal_width" to 2.8, "petal_length" to 4.6, "petal_width" to 1.5, "species" to "versicolor"),
            mapOf("sepal_length" to 5.7, "sepal_width" to 2.8, "petal_length" to 4.5, "petal_width" to 1.3, "species" to "versicolor"),
            mapOf("sepal_length" to 6.3, "sepal_width" to 3.3, "petal_length" to 6.0, "petal_width" to 2.5, "species" to "virginica"),
            mapOf("sepal_length" to 5.8, "sepal_width" to 2.7, "petal_length" to 5.1, "petal_width" to 1.9, "species" to "virginica"),
            mapOf("sepal_length" to 7.1, "sepal_width" to 3.0, "petal_length" to 5.9, "petal_width" to 2.1, "species" to "virginica"),
            mapOf("sepal_length" to 6.3, "sepal_width" to 2.9, "petal_length" to 5.6, "petal_width" to 1.8, "species" to "virginica"),
            mapOf("sepal_length" to 6.5, "sepal_width" to 3.0, "petal_length" to 5.8, "petal_width" to 2.2, "species" to "virginica"),
            mapOf("sepal_length" to 7.6, "sepal_width" to 3.0, "petal_length" to 6.6, "petal_width" to 2.1, "species" to "virginica")
        )
    )

    val TIPS = DatasetInfo(
        id = "tips",
        name = "Restaurant Tips",
        description = "Tipping behavior by meal, party size, bill amount, and smoking status.",
        columns = listOf(
            ColumnInfo("total_bill", ColumnType.FLOAT),
            ColumnInfo("tip", ColumnType.FLOAT),
            ColumnInfo("sex", ColumnType.STRING),
            ColumnInfo("smoker", ColumnType.STRING),
            ColumnInfo("day", ColumnType.STRING),
            ColumnInfo("time", ColumnType.STRING),
            ColumnInfo("size", ColumnType.INT)
        ),
        rows = listOf(
            mapOf("total_bill" to 16.99, "tip" to 1.01, "sex" to "Female", "smoker" to "No", "day" to "Sun", "time" to "Dinner", "size" to 2),
            mapOf("total_bill" to 10.34, "tip" to 1.66, "sex" to "Male", "smoker" to "No", "day" to "Sun", "time" to "Dinner", "size" to 3),
            mapOf("total_bill" to 21.01, "tip" to 3.50, "sex" to "Male", "smoker" to "No", "day" to "Sun", "time" to "Dinner", "size" to 3),
            mapOf("total_bill" to 23.68, "tip" to 3.31, "sex" to "Male", "smoker" to "No", "day" to "Sun", "time" to "Dinner", "size" to 2),
            mapOf("total_bill" to 24.59, "tip" to 3.61, "sex" to "Female", "smoker" to "No", "day" to "Sun", "time" to "Dinner", "size" to 4),
            mapOf("total_bill" to 25.29, "tip" to 4.71, "sex" to "Male", "smoker" to "No", "day" to "Sun", "time" to "Dinner", "size" to 4),
            mapOf("total_bill" to 8.77, "tip" to 2.00, "sex" to "Male", "smoker" to "No", "day" to "Sun", "time" to "Dinner", "size" to 2),
            mapOf("total_bill" to 26.88, "tip" to 3.12, "sex" to "Male", "smoker" to "No", "day" to "Sun", "time" to "Dinner", "size" to 4),
            mapOf("total_bill" to 15.04, "tip" to 1.96, "sex" to "Male", "smoker" to "No", "day" to "Sun", "time" to "Dinner", "size" to 2),
            mapOf("total_bill" to 14.78, "tip" to 3.23, "sex" to "Male", "smoker" to "No", "day" to "Sun", "time" to "Dinner", "size" to 2),
            mapOf("total_bill" to 10.27, "tip" to 1.71, "sex" to "Male", "smoker" to "No", "day" to "Sun", "time" to "Dinner", "size" to 2),
            mapOf("total_bill" to 35.26, "tip" to 5.00, "sex" to "Female", "smoker" to "No", "day" to "Sun", "time" to "Dinner", "size" to 4),
            mapOf("total_bill" to 15.42, "tip" to 1.57, "sex" to "Male", "smoker" to "No", "day" to "Sun", "time" to "Dinner", "size" to 2),
            mapOf("total_bill" to 18.43, "tip" to 3.00, "sex" to "Male", "smoker" to "No", "day" to "Sun", "time" to "Dinner", "size" to 4),
            mapOf("total_bill" to 14.83, "tip" to 3.02, "sex" to "Female", "smoker" to "No", "day" to "Sun", "time" to "Dinner", "size" to 2)
        )
    )

    val FLIGHTS = DatasetInfo(
        id = "flights",
        name = "Airline Delays",
        description = "Flight departure and arrival delays across major domestic routes.",
        columns = listOf(
            ColumnInfo("carrier", ColumnType.STRING),
            ColumnInfo("origin", ColumnType.STRING),
            ColumnInfo("dest", ColumnType.STRING),
            ColumnInfo("dep_delay", ColumnType.FLOAT),
            ColumnInfo("arr_delay", ColumnType.FLOAT),
            ColumnInfo("distance", ColumnType.FLOAT),
            ColumnInfo("air_time", ColumnType.FLOAT)
        ),
        rows = listOf(
            mapOf("carrier" to "UA", "origin" to "EWR", "dest" to "IAH", "dep_delay" to 2.0, "arr_delay" to 11.0, "distance" to 1400.0, "air_time" to 227.0),
            mapOf("carrier" to "AA", "origin" to "JFK", "dest" to "MIA", "dep_delay" to 4.0, "arr_delay" to 20.0, "distance" to 1089.0, "air_time" to 160.0),
            mapOf("carrier" to "B6", "origin" to "JFK", "dest" to "BQN", "dep_delay" to -2.0, "arr_delay" to -15.0, "distance" to 1576.0, "air_time" to 195.0),
            mapOf("carrier" to "DL", "origin" to "LGA", "dest" to "ATL", "dep_delay" to -1.0, "arr_delay" to -5.0, "distance" to 762.0, "air_time" to 125.0),
            mapOf("carrier" to "UA", "origin" to "EWR", "dest" to "ORD", "dep_delay" to 15.0, "arr_delay" to 24.0, "distance" to 719.0, "air_time" to 128.0),
            mapOf("carrier" to "B6", "origin" to "EWR", "dest" to "FLL", "dep_delay" to -3.0, "arr_delay" to 12.0, "distance" to 1065.0, "air_time" to 165.0),
            mapOf("carrier" to "EV", "origin" to "LGA", "dest" to "IAD", "dep_delay" to 25.0, "arr_delay" to 31.0, "distance" to 229.0, "air_time" to 45.0),
            mapOf("carrier" to "AA", "origin" to "JFK", "dest" to "LAX", "dep_delay" to 0.0, "arr_delay" to -8.0, "distance" to 2475.0, "air_time" to 320.0)
        )
    )

    val ALL = listOf(TITANIC, IRIS, TIPS, FLIGHTS)
}
