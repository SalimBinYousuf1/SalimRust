//! # salim-ml: Machine learning pipelines and algorithms.

#[derive(Clone, Debug, serde::Serialize)]
pub struct ModelMetrics {
    pub accuracy: f64,
    pub precision: f64,
    pub recall: f64,
    pub f1: f64,
    pub mse: f64,
    pub r2: f64,
}

#[derive(Clone, Debug)]
pub struct TrainTestSplit<T> {
    pub x_train: Vec<Vec<T>>,
    pub x_test: Vec<Vec<T>>,
    pub y_train: Vec<f64>,
    pub y_test: Vec<f64>,
}

pub fn train_test_split(x: &[Vec<f64>], y: &[f64], test_ratio: f64) -> TrainTestSplit<f64> {
    let split_idx = (x.len() as f64 * (1.0 - test_ratio)) as usize;
    TrainTestSplit {
        x_train: x[..split_idx].to_vec(),
        x_test: x[split_idx..].to_vec(),
        y_train: y[..split_idx].to_vec(),
        y_test: y[split_idx..].to_vec(),
    }
}
