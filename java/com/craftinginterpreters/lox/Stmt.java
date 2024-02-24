package com.craftinginterpreters.lox;

import java.util.List;

sealed interface Stmt permits Stmt.Block, Stmt.Class, Stmt.Expression, Stmt.Function, Stmt.If, Stmt.Print, Stmt.Return,
    Stmt.Var, Stmt.While {
  record Block(List<Stmt> statements) implements Stmt {
  }

  record Class(Token name, Expr.Variable superclass, List<Stmt.Function> methods) implements Stmt {
  }

  record Expression(Expr expression) implements Stmt {
  }

  record Function(Token name, List<Token> params, List<Stmt> body) implements Stmt {
  }

  record If(Expr condition, Stmt thenBranch, Stmt elseBranch) implements Stmt {
  }

  record Print(Expr expression) implements Stmt {
  }

  record Return(Token keyword, Expr value) implements Stmt {
  }

  record Var(Token name, Expr initializer) implements Stmt {
  }

  record While(Expr condition, Stmt body) implements Stmt {
  }
}
