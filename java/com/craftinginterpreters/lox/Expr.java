package com.craftinginterpreters.lox;

import java.util.List;

sealed interface Expr
    permits Expr.Assign, Expr.Binary, Expr.Call, Expr.Get, Expr.Grouping, Expr.Literal, Expr.Logical, Expr.Set,
    Expr.Super, Expr.This, Expr.Unary, Expr.Variable {

  record Assign(Token name, Expr value) implements Expr {
  }

  record Binary(Expr left, Token operator, Expr right) implements Expr {
  }

  record Call(Expr callee, Token paren, List<Expr> arguments) implements Expr {
  }

  record Get(Expr object, Token name) implements Expr {
  }

  record Grouping(Expr expression) implements Expr {
  }

  record Literal(Object value) implements Expr {
  }

  record Logical(Expr left, Token operator, Expr right) implements Expr {
  }

  record Set(Expr object, Token name, Expr value) implements Expr {
  }

  record Super(Token keyword, Token method) implements Expr {
  }

  record This(Token keyword) implements Expr {
  }

  record Unary(Token operator, Expr right) implements Expr {
  }

  record Variable(Token name) implements Expr {
  }
}
