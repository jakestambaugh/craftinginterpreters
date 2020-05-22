load("@rules_java//java:defs.bzl", "java_binary", "java_library")

java_library(
    name = "token",
    srcs = [
        "Token.java",
        "TokenType.java",
    ],
    visibility = ["//visibility:public"],
)

java_library(
    name = "lox_lib",
    srcs = glob(
        ["*.java"],
        exclude = [
            "Token.java",
            "TokenType.java",
        ],
    ),
    deps = [
        ":token",
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
