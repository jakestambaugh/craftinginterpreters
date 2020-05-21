load("@rules_java//java:defs.bzl", "java_binary")

java_library(
    name = "lox_lib",
    srcs = glob(["*.java"]),
    deps = [
        "//java/com/craftinginterpreters/tool:ast",
    ],
)

java_binary(
    name = "lox",
    main_class = "com.craftinginterpreters.lox.Lox",
    runtime_deps = [
        ":lox_lib",
    ],
)
