//! # salim-array: NumPy-like arrays and vector math built on ndarray.

use ndarray::{Array1, Array2};

#[derive(Clone, Debug)]
pub struct Array<T> {
    data: Vec<T>,
    shape: Vec<usize>,
}

pub fn zeros(n: usize) -> Vec<f64> {
    vec![0.0; n]
}

pub fn ones(n: usize) -> Vec<f64> {
    vec![1.0; n]
}

pub fn linspace(start: f64, end: f64, num: usize) -> Vec<f64> {
    if num <= 1 {
        return vec![start];
    }
    let step = (end - start) / (num - 1) as f64;
    (0..num).map(|i| start + i as f64 * step).collect()
}

pub fn arange(start: f64, end: f64, step: f64) -> Vec<f64> {
    let mut current = start;
    let mut res = Vec::new();
    while current < end {
        res.push(current);
        current += step;
    }
    res
}

pub fn eye(dim: usize) -> Vec<Vec<f64>> {
    let mut mat = vec![vec![0.0; dim]; dim];
    for i in 0..dim {
        mat[i][i] = 1.0;
    }
    mat
}
