""" Creates a rule to generate the AST """
generate_ast_srcs = rule(
    implementation = _impl,
    attrs = {
        "generator": attr.label(),
    }
)