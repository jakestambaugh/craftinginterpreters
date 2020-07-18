use crate::lexer::literal::Literal;

#[derive(Debug)]
pub enum TokenType {
    // Single-character tokens
    LeftParen,
    RightParen,
    LeftBrace,
    RightBrace,
    Comma,
    Dot,
    Minus,
    Plus,
    Semicolon,
    Slash,
    Star,

    // One or two character tokens.
    Bang,
    BangEqual,
    Equal,
    EqualEqual,
    Greater,
    GreaterEqual,
    Less,
    LessEqual,

    // Literals.
    Identifier(Literal),
    String(Literal),
    Number(Literal),

    // Keywords.
    And,
    Class,
    Else,
    False,
    Fun,
    For,
    If,
    Nil,
    Or,
    Print,
    Return,
    Super,
    This,
    True,
    Var,
    While,

    EOF,

    // TODO: this skip should probably be removed in favor of an Option<TokenType> since it represents a line of source that should be skipped by the parser
    Skip,
}

#[derive(Debug)]
pub struct Token {
    r#type: TokenType,
    lexeme: String,
    line: u32,
}

impl Token {
    pub fn new(r#type: TokenType, lexeme: String, line: u32) -> Token {
        Token {
            r#type,
            lexeme,
            line,
        }
    }
}
