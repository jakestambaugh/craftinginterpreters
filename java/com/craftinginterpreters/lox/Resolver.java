package com.craftinginterpreters.lox;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

class Resolver {
  private final Interpreter interpreter;
  private final Stack<Map<String, Boolean>> scopes = new Stack<>();
  private FunctionType currentFunction = FunctionType.NONE;

  Resolver(Interpreter interpreter) {
    this.interpreter = interpreter;
  }

  private enum FunctionType {
    NONE,
    FUNCTION,
    INITIALIZER,
    METHOD
  }

  private enum ClassType {
    NONE,
    CLASS,
    SUBCLASS
  }

  private ClassType currentClass = ClassType.NONE;

  public void visitBlockStmt(Stmt.Block stmt) {
    beginScope();
    resolve(stmt.statements());
    endScope();
  }

  public void visitClassStmt(Stmt.Class stmt) {
    ClassType enclosingClass = currentClass;
    currentClass = ClassType.CLASS;

    declare(stmt.name());
    define(stmt.name());

    if (stmt.superclass() != null && stmt.name().lexeme().equals(stmt.superclass().name().lexeme())) {
      Lox.error(stmt.superclass().name(), "A class cannot inherit from itself.");
    }

    if (stmt.superclass() != null) {
      currentClass = ClassType.SUBCLASS;
      resolve(stmt.superclass());
    }

    if (stmt.superclass() != null) {
      beginScope();
      scopes.peek().put("super", true);
    }

    beginScope();
    scopes.peek().put("this", true);

    for (Stmt.Function method : stmt.methods()) {
      FunctionType declaration = FunctionType.METHOD;
      if (method.name().lexeme().equals("init")) {
        declaration = FunctionType.INITIALIZER;
      }
      resolveFunction(method, declaration);
    }

    endScope();

    if (stmt.superclass() != null)
      endScope();

    currentClass = enclosingClass;
  }

  public void visitExpressionStmt(Stmt.Expression stmt) {
    resolve(stmt.expression());
  }

  public void visitIfStmt(Stmt.If stmt) {
    resolve(stmt.condition());
    resolve(stmt.thenBranch());
    if (stmt.elseBranch() != null) {
      resolve(stmt.elseBranch());
    }
  }

  public void visitPrintStmt(Stmt.Print stmt) {
    resolve(stmt.expression());
  }

  public void visitReturnStmt(Stmt.Return stmt) {
    if (currentFunction == FunctionType.NONE) {
      Lox.error(stmt.keyword(), "Cannot return from top-level code.");
    }

    if (stmt.value() != null) {
      if (currentFunction == FunctionType.INITIALIZER) {
        Lox.error(stmt.keyword(),
            "Cannot return a value from an initializer.");
      }
      resolve(stmt.value());
    }
  }

  public void visitWhileStmt(Stmt.While stmt) {
    resolve(stmt.condition());
    resolve(stmt.body());
  }

  public void visitVarStmt(Stmt.Var stmt) {
    declare(stmt.name());
    if (stmt.initializer() != null) {
      resolve(stmt.initializer());
    }
    define(stmt.name());
  }

  public void visitFunctionStmt(Stmt.Function stmt) {
    declare(stmt.name());
    define(stmt.name());

    resolveFunction(stmt, FunctionType.FUNCTION);
  }

  public void visitAssignExpr(Expr.Assign expr) {
    resolve(expr.value());
    resolveLocal(expr, expr.name());
  }

  public void visitBinaryExpr(Expr.Binary expr) {
    resolve(expr.left());
    resolve(expr.right());
  }

  public void visitCallExpr(Expr.Call expr) {
    resolve(expr.callee());
    for (Expr argument : expr.arguments()) {
      resolve(argument);
    }
  }

  public void visitGetExpr(Expr.Get expr) {
    resolve(expr.object());
  }

  public void visitGroupingExpr(Expr.Grouping expr) {
    resolve(expr.expression());
  }

  public void visitLiteralExpr(Expr.Literal expr) {
  }

  public void visitLogicalExpr(Expr.Logical expr) {
    resolve(expr.left());
    resolve(expr.right());
  }

  public void visitSetExpr(Expr.Set expr) {
    resolve(expr.value());
    resolve(expr.object());
  }

  public void visitSuperExpr(Expr.Super expr) {
    if (currentClass == ClassType.NONE) {
      Lox.error(expr.keyword(),
          "Cannot use 'super' outside of a class.");
    } else if (currentClass != ClassType.SUBCLASS) {
      Lox.error(expr.keyword(),
          "Cannot use 'super' in a class with no superclass.");
    }

    resolveLocal(expr, expr.keyword());
  }

  public void visitThisExpr(Expr.This expr) {
    if (currentClass == ClassType.NONE) {
      Lox.error(expr.keyword(),
          "Cannot use 'this' outside of a class.");
      return;
    }

    resolveLocal(expr, expr.keyword());
  }

  public void visitUnaryExpr(Expr.Unary expr) {
    resolve(expr.right());
  }

  public void visitVariableExpr(Expr.Variable expr) {
    if (!scopes.isEmpty() && scopes.peek().get(expr.name().lexeme()) == Boolean.FALSE) {
      Lox.error(expr.name(), "Cannot read local variable in its own initializer.");
    }

    resolveLocal(expr, expr.name());
  }

  private void resolve(Stmt stmt) {
    switch (stmt) {
      case Stmt.Block s -> visitBlockStmt(s);
      case Stmt.Class s -> visitClassStmt(s);
      case Stmt.Expression s -> visitExpressionStmt(s);
      case Stmt.Function s -> visitFunctionStmt(s);
      case Stmt.If s -> visitIfStmt(s);
      case Stmt.Print s -> visitPrintStmt(s);
      case Stmt.Return s -> visitReturnStmt(s);
      case Stmt.Var s -> visitVarStmt(s);
      case Stmt.While s -> visitWhileStmt(s);
    }
  }

  private void resolve(Expr expr) {
    switch (expr) {
      case Expr.Assign e -> visitAssignExpr(e);
      case Expr.Binary e -> visitBinaryExpr(e);
      case Expr.Call e -> visitCallExpr(e);
      case Expr.Get e -> visitGetExpr(e);
      case Expr.Grouping e -> visitGroupingExpr(e);
      case Expr.Literal e -> visitLiteralExpr(e);
      case Expr.Logical e -> visitLogicalExpr(e);
      case Expr.Set e -> visitSetExpr(e);
      case Expr.Super e -> visitSuperExpr(e);
      case Expr.This e -> visitThisExpr(e);
      case Expr.Unary e -> visitUnaryExpr(e);
      case Expr.Variable e -> visitVariableExpr(e);
    }
  }

  void resolve(List<Stmt> statements) {
    for (Stmt statement : statements) {
      resolve(statement);
    }
  }

  private void resolveFunction(Stmt.Function function, FunctionType type) {
    FunctionType enclosingFunction = currentFunction;
    currentFunction = type;

    beginScope();
    for (Token param : function.params()) {
      declare(param);
      define(param);
    }
    resolve(function.body());
    endScope();
    currentFunction = enclosingFunction;
  }

  private void beginScope() {
    scopes.push(new HashMap<String, Boolean>());
  }

  private void endScope() {
    scopes.pop();
  }

  private void declare(Token name) {
    if (scopes.isEmpty())
      return;

    Map<String, Boolean> scope = scopes.peek();
    if (scope.containsKey(name.lexeme())) {
      Lox.error(name,
          "Variable with this name already declared in this scope.");
    }
    scope.put(name.lexeme(), false);
  }

  private void define(Token name) {
    if (scopes.isEmpty())
      return;
    scopes.peek().put(name.lexeme(), true);
  }

  private void resolveLocal(Expr expr, Token name) {
    for (int i = scopes.size() - 1; i >= 0; i--) {
      if (scopes.get(i).containsKey(name.lexeme())) {
        interpreter.resolve(expr, scopes.size() - 1 - i);
        return;
      }
    }

    // Not found. Assume it is global.
  }
}
