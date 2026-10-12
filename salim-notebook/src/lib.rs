//! # salim-notebook: Rich Jupyter & REPL integration.

pub trait ToHtml {
    fn to_html(&self) -> String;
}

impl ToHtml for salim_frame::DataFrame {
    fn to_html(&self) -> String {
        let cols = self.columns().join("</th><th>");
        format!("<table class=\"salim-df\"><thead><tr><th>{}</th></tr></thead><tbody><tr><td>...</td></tr></tbody></table>", cols)
    }
}
