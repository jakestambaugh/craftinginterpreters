load("@rules_java//java:defs.bzl", "java_binary")
load(":generator.bzl", "generated_ast_srcs")

java_binary(
    name = "tool",
    srcs = ["GenerateAst.java"],
    main_class = "com.craftinginterpreters.tool.GenerateAst",
)

generated_ast_srcs(
    name = "ast_srcs",
    generator = ":tool",
)

java_library(
    name = "ast",
    srcs = [":ast_srcs"],
    visibility = ["//visibility:public"],
)
