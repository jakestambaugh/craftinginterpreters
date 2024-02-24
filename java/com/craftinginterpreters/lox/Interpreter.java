package com.craftinginterpreters.lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class Interpreter {
  final Environment globals = new Environment();
  private Environment environment = globals;
  private final Map<Expr, Integer> locals = new HashMap<>();

  Interpreter() {
    globals.define(
        "clock",
        new LoxCallable() {
          @Override
          public int arity() {
            return 0;
          }

          @Override
          public Object call(Interpreter interpreter, List<Object> arguments) {
            return (double) System.currentTimeMillis() / 1000.0;
          }

          @Override
          public String toString() {
            return "<native fn>";
          }
        });
  }

  void interpret(List<Stmt> statements) {
    try {
      for (Stmt statement : statements) {
        execute(statement);
      }
    } catch (RuntimeError error) {
      Lox.runtimeError(error);
    }
  }

  private void execute(Stmt stmt) {
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

  void resolve(Expr expr, int depth) {
    locals.put(expr, depth);
  }

  void executeBlock(List<Stmt> statements, Environment environment) {
    Environment previous = this.environment;
    try {
      this.environment = environment;

      for (Stmt statement : statements) {
        execute(statement);
      }
    } finally {
      this.environment = previous;
    }
  }

  public Void visitBlockStmt(Stmt.Block stmt) {
    executeBlock(stmt.statements(), new Environment(environment));
    return null;
  }

  private Object evaluate(Expr expr) {
    return switch (expr) {
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
    };
  }

  public Void visitClassStmt(Stmt.Class stmt) {
    Object superclass = null;
    if (stmt.superclass() != null) {
      superclass = evaluate(stmt.superclass());
      if (!(superclass instanceof LoxClass)) {
        throw new RuntimeError(stmt.superclass().name(), "Superclass must be a class.");
      }
    }

    environment.define(stmt.name().lexeme(), null);

    if (stmt.superclass() != null) {
      environment = new Environment(environment);
      environment.define("super", superclass);
    }

    Map<String, LoxFunction> methods = new HashMap<>();
    for (Stmt.Function method : stmt.methods()) {
      LoxFunction function = new LoxFunction(method, environment, method.name().lexeme().equals("init"));
      methods.put(method.name().lexeme(), function);
    }

    LoxClass klass = new LoxClass(stmt.name().lexeme(), (LoxClass) superclass, methods);

    if (superclass != null) {
      environment = environment.enclosing;
    }

    environment.assign(stmt.name(), klass);
    return null;
  }

  public Void visitExpressionStmt(Stmt.Expression stmt) {
    evaluate(stmt.expression());
    return null;
  }

  public Void visitFunctionStmt(Stmt.Function stmt) {
    LoxFunction function = new LoxFunction(stmt, environment, false);
    environment.define(stmt.name().lexeme(), function);
    return null;
  }

  public Void visitIfStmt(Stmt.If stmt) {
    if (isTruthy(evaluate(stmt.condition()))) {
      execute(stmt.thenBranch());
    } else if (stmt.elseBranch() != null) {
      execute(stmt.elseBranch());
    }
    return null;
  }

  public Void visitPrintStmt(Stmt.Print stmt) {
    Object value = evaluate(stmt.expression());
    System.out.println(stringify(value));
    return null;
  }

  public Void visitReturnStmt(Stmt.Return stmt) {
    Object value = null;
    if (stmt.value() != null)
      value = evaluate(stmt.value());

    throw new Return(value);
  }

  public Void visitVarStmt(Stmt.Var stmt) {
    Object value = null;
    if (stmt.initializer() != null) {
      value = evaluate(stmt.initializer());
    }

    environment.define(stmt.name().lexeme(), value);
    return null;
  }

  public Void visitWhileStmt(Stmt.While stmt) {
    while (isTruthy(evaluate(stmt.condition()))) {
      execute(stmt.body());
    }
    return null;
  }

  public Object visitAssignExpr(Expr.Assign expr) {
    Object value = evaluate(expr.value());

    Integer distance = locals.get(expr);
    if (distance != null) {
      environment.assignAt(distance, expr.name(), value);
    } else {
      globals.assign(expr.name(), value);
    }
    return value;
  }

  public Object visitBinaryExpr(Expr.Binary expr) {
    Object left = evaluate(expr.left());
    Object right = evaluate(expr.right());

    switch (expr.operator().type()) {
      case BANG_EQUAL:
        return !isEqual(left, right);
      case EQUAL_EQUAL:
        return isEqual(left, right);
      case GREATER:
        checkNumberOperands(expr.operator(), left, right);
        return (double) left > (double) right;
      case GREATER_EQUAL:
        checkNumberOperands(expr.operator(), left, right);
        return (double) left >= (double) right;
      case LESS:
        checkNumberOperands(expr.operator(), left, right);
        return (double) left < (double) right;
      case LESS_EQUAL:
        checkNumberOperands(expr.operator(), left, right);
        return (double) left <= (double) right;
      case MINUS:
        checkNumberOperands(expr.operator(), left, right);
        return (double) left - (double) right;
      case PLUS:
        if (left instanceof Double && right instanceof Double) {
          return (double) left + (double) right;
        }

        if (left instanceof String && right instanceof String) {
          return (String) left + (String) right;
        }
        throw new RuntimeError(expr.operator(), "Operands must be two numbers or two strings.");
      case SLASH:
        checkNumberOperands(expr.operator(), left, right);
        return (double) left / (double) right;
      case STAR:
        checkNumberOperands(expr.operator(), left, right);
        return (double) left * (double) right;
      default:
    }

    // Unreachable
    return null;
  }

  public Object visitCallExpr(Expr.Call expr) {
    Object callee = evaluate(expr.callee());

    List<Object> arguments = new ArrayList<>();
    for (Expr argument : expr.arguments()) {
      arguments.add(evaluate(argument));
    }

    if (!(callee instanceof LoxCallable)) {
      throw new RuntimeError(expr.paren(), "Can only call functions and classes.");
    }

    LoxCallable function = (LoxCallable) callee;
    if (arguments.size() != function.arity()) {
      throw new RuntimeError(
          expr.paren(),
          "Expected " + function.arity() + " arguments but got " + arguments.size() + ".");
    }

    return function.call(this, arguments);
  }

  public Object visitGetExpr(Expr.Get expr) {
    Object object = evaluate(expr.object());
    if (object instanceof LoxInstance) {
      return ((LoxInstance) object).get(expr.name());
    }

    throw new RuntimeError(expr.name(),
        "Only instances have properties.");
  }

  public Object visitGroupingExpr(Expr.Grouping expr) {
    return evaluate(expr.expression());
  }

  public Object visitLiteralExpr(Expr.Literal expr) {
    return expr.value();
  }

  public Object visitLogicalExpr(Expr.Logical expr) {
    Object left = evaluate(expr.left());

    if (expr.operator().type() == TokenType.OR) {
      if (isTruthy(left))
        return left;
    } else {
      if (!isTruthy(left))
        return left;
    }

    return evaluate(expr.right());
  }

  public Object visitSetExpr(Expr.Set expr) {
    Object object = evaluate(expr.object());

    if (!(object instanceof LoxInstance)) {
      throw new RuntimeError(expr.name(), "Only instances have fields.");
    }

    Object value = evaluate(expr.value());
    ((LoxInstance) object).set(expr.name(), value);
    return value;
  }

  public Object visitSuperExpr(Expr.Super expr) {
    int distance = locals.get(expr);
    LoxClass superclass = (LoxClass) environment.getAt(distance, "super");

    // "this" is always one level nearer than "super"'s environment.
    LoxInstance object = (LoxInstance) environment.getAt(distance - 1, "this");

    LoxFunction method = superclass.findMethod(expr.method().lexeme());

    if (method == null) {
      throw new RuntimeError(expr.method(),
          "Undefined property '" + expr.method().lexeme() + "'.");
    }

    return method.bind(object);
  }

  public Object visitThisExpr(Expr.This expr) {
    return lookUpVariable(expr.keyword(), expr);
  }

  public Object visitUnaryExpr(Expr.Unary expr) {
    Object right = evaluate(expr.right());

    switch (expr.operator().type()) {
      case BANG:
        return !isTruthy(right);
      case MINUS:
        checkNumberOperand(expr.operator(), right);
        return -(double) right;
      default:
    }

    // Unreachable.
    return null;
  }

  public Object visitVariableExpr(Expr.Variable expr) {
    return lookUpVariable(expr.name(), expr);
  }

  private Object lookUpVariable(Token name, Expr expr) {
    Integer distance = locals.get(expr);
    if (distance != null) {
      return environment.getAt(distance, name.lexeme());
    } else {
      return globals.get(name);
    }
  }

  private void checkNumberOperand(Token operator, Object operand) {
    if (operand instanceof Double)
      return;
    throw new RuntimeError(operator, "Operand must be a number.");
  }

  private void checkNumberOperands(Token operator, Object left, Object right) {
    if (left instanceof Double && right instanceof Double)
      return;
    throw new RuntimeError(operator, "Operands must be numbers.");
  }

  private boolean isTruthy(Object object) {
    if (object == null)
      return false;
    if (object instanceof Boolean)
      return (boolean) object;
    return true;
  }

  private boolean isEqual(Object a, Object b) {
    // nil is only equal to nil.
    if (a == null && b == null)
      return true;
    if (a == null)
      return false;

    return a.equals(b);
  }

  private String stringify(Object object) {
    if (object == null)
      return "nil";

    // Hack. Work around Java adding ".0" to integer-valued doubles.
    if (object instanceof Double) {
      String text = object.toString();
      if (text.endsWith(".0")) {
        text = text.substring(0, text.length() - 2);
      }
      return text;
    }

    return object.toString();
  }
}
