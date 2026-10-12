//! # salim-errors: Friendly, actionable errors for Python developers switching to Rust.

use thiserror::Error;

pub type SalimResult<T> = Result<T, SalimError>;

#[derive(Error, Debug)]
pub enum SalimError {
    #[error("Column '{requested}' not found in DataFrame. Available columns: {available:?}. {suggestion}")]
    ColumnNotFound {
        requested: String,
        available: Vec<String>,
        suggestion: String,
    },

    #[error("Type mismatch for column '{col}': expected {expected}, found {found}. Hint: use .cast({expected})")]
    TypeMismatch {
        col: String,
        expected: String,
        found: String,
    },

    #[error("Python habit detected: Rust DataFrames are immutable by default. Instead of inplace=True, write: let df = df.{operation}(...)")]
    InplaceMutationAttempt {
        operation: String,
    },

    #[error("Index out of bounds: length is {len}, but index {idx} was requested.")]
    IndexOutOfBounds {
        idx: i64,
        len: usize,
    },

    #[error("I/O error: {0}")]
    Io(String),

    #[error("Engine error: {0}")]
    Engine(String),
}

impl SalimError {
    pub fn column_not_found(requested: &str, available: &[String]) -> Self {
        let suggestion = available
            .iter()
            .map(|col| (col, strsim::jaro_winkler(requested, col)))
            .filter(|(_, sim)| *sim > 0.6)
            .max_by(|a, b| a.1.partial_cmp(&b.1).unwrap_or(std::cmp::Ordering::Equal))
            .map(|(best, _)| format!("Did you mean '{}'?", best))
            .unwrap_or_else(|| "Please check your column names.".to_string());

        Self::ColumnNotFound {
            requested: requested.to_string(),
            available: available.to_vec(),
            suggestion,
        }
    }
}
