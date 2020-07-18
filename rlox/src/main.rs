extern crate clap;
#[macro_use]
extern crate lazy_static;


mod lexer;

use clap::{App, Arg};
use lexer::Scanner;
use std::fs::File;
use std::io::prelude::*;
use std::io::{self, Read};

fn main() {
    let matches = App::new("Lox")
        .version("0.1")
        .about("A programming language")
        .arg(
            Arg::with_name("script")
                .help("A lox script to be executed")
                .index(1),
        )
        .get_matches();

    if let Some(s) = matches.value_of("script") {
        match run_file(s) {
            Ok(()) => println!(),
            Err(e) => println!("Error while running script \"{}\": {}", s, e),
        }
    } else {
        match run_prompt() {
            Ok(()) => (),
            Err(e) => println!("Error in interpreter: {}", e),
        }
    }
}

fn run_file(path: &str) -> std::io::Result<()> {
    let mut file = File::open(path)?;
    let mut contents = String::new();
    file.read_to_string(&mut contents)?;

    run(&contents)
}

fn run_prompt() -> std::io::Result<()> {
    print!("> ");
    io::stdout().flush()?;
    let stdin = io::stdin();
    let iterator = stdin.lock().lines();

    for line in iterator {
        match line {
            Ok(l) => run(&l)?,
            Err(e) => {
                return Err(e);
            }
        }
        print!("> ");
        io::stdout().flush()?;
    }

    Ok(())
}

fn run(program: &str) -> std::io::Result<()> {
    println!("-- {}", program);

    // Scanner scans program into tokens
    let mut scanner = Scanner::from_source(program);
    let tokens = scanner.scan_tokens();
    // For now, print the tokens
    for token in tokens.iter() {
        println!("{:?}", token);
    }
    Ok(())
}
