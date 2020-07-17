#[derive(Debug)]
pub enum Literal {
    LoxNumber(f64),
    LoxString(String),
    LoxIdentifier(String),
}
