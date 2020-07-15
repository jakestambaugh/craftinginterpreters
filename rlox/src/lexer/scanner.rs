use super::token::{Token, TokenType};

pub struct Scanner<'a> {
    source: &'a str,
    tokens: Vec<Token>,

    start: usize,
    current: usize,
    line: u32,
}

impl Scanner<'a> {
    pub fn from_source<'a>(source: &'a str) -> Scanner<'a> {
        Scanner {
            source,
            tokens: Vec::new(),

            start: 0,
            current: 0,
            line: 1,
        }
    }

    pub fn scan_tokens(&mut self) -> Vec<Token> {
        while !self.is_at_end() {
            // We are at the beginning of the next lexeme.
            self.start = self.current;
            self.scan_token();
        }

        self.tokens.push(Token::new(
            TokenType::EOF,
            String::from(""),
            None,
            self.line,
        ));
        self.tokens
    }

    fn scan_token(&mut self) {
        let c = self.advance();
        let token_type = match c {
            '{' => TokenType::LeftBrace,
            '}' => TokenType::RightBrace,
            _ => TokenType::False,
        };

        let text: String = String::from(
            self.source
                .get(self.start..self.current)
                .unwrap_or("ERROR EVALUATING TOKEN"),
        );

        self.tokens.push(Token::new(token_type, text, ))
    }

    fn is_at_end(&self) -> bool {
        return self.current >= self.source.len();
    }

    fn advance(&mut self) -> char {
        // https://users.rust-lang.org/t/accessing-the-char-at-a-byte-index/15398
        self.current += 1;
        self.source.chars().nth(self.current).unwrap()
    }
}
