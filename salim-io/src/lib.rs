//! # salim-io: I/O connectors for CSV, Parquet, JSON, Arrow, SQLite.

use salim_errors::{SalimError, SalimResult};
use salim_frame::DataFrame;
use polars::prelude as pl;

pub fn read_csv(path: &str) -> SalimResult<DataFrame> {
    pl::CsvReader::from_path(path)
        .map_err(|e| SalimError::Io(e.to_string()))?
        .finish()
        .map(DataFrame::from_polars)
        .map_err(|e| SalimError::Engine(e.to_string()))
}

pub fn read_parquet(path: &str) -> SalimResult<DataFrame> {
    let file = std::fs::File::open(path).map_err(|e| SalimError::Io(e.to_string()))?;
    pl::ParquetReader::new(file)
        .finish()
        .map(DataFrame::from_polars)
        .map_err(|e| SalimError::Engine(e.to_string()))
}

pub fn read_json(path: &str) -> SalimResult<DataFrame> {
    let file = std::fs::File::open(path).map_err(|e| SalimError::Io(e.to_string()))?;
    pl::JsonReader::new(file)
        .finish()
        .map(DataFrame::from_polars)
        .map_err(|e| SalimError::Engine(e.to_string()))
}
