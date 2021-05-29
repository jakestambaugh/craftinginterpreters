# Lox Programming Language

Two implementations of the Lox Programming Language from the book _[Crafting Interpreters](http://craftinginterpreters.com/)_ by Bob Nystrom. This repo is my implementation as I worked through the textbook. The reference implementations (as well as the source for the book) can be found at [munificent/craftinginterpreters](github.com/munificent/craftinginterpreters).

## Dependencies

This repo uses `bazel` to build both the Java implementation and the C implementation. Bazel can be installed from http://bazel.io.

## Bazel targets

* `//java/com/craftinginterpreters/lox`: The binary for thr Java tree-walk interpreter
* `//java/com/craftinginterpreters/tool`: The binary for the helper program to generate Java sources for our AST node types
* `//clox`: The binary for the C implementation of the stack-based virtual machine

The target `//java/com/craftinginterpreters/lox` automatically regenerates the sources from `//java/com/craftinginterpreters/tool`. The Starlark scripts for this are in `java/com/craftininterpreters/tool/generator.bzl`.

## Running a Lox script

Test scripts are in `clox/scripts/` and can be run from the Workspace directory with these commands:
```
$ bazel run //java/com/craftinginterpreters/lox -- $(pwd)/scripts/test.lox
$ bazel run //clox -- $(pwd)/scripts/test.lox
```

Both the C and the Java implementations also support a REPL, which starts when the binary is run with no arguments:
```
$ bazel run //java/com/craftinginterpreters/lox
$ bazel run //clox
```
