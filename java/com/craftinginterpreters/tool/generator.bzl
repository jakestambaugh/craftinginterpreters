""" Creates a rule to generate the AST """

def _generated_ast_srcs_impl(ctx):
    expr = ctx.actions.declare_file("Expr.java")
    stmt = ctx.actions.declare_file("Stmt.java")
    ctx.actions.run(
        outputs = [expr, stmt],
        executable = ctx.executable.generator,
        arguments = [expr.dirname],
        progress_message = "Generating AST sources",
    )
    return DefaultInfo(files = depset([expr, stmt]))

generated_ast_srcs = rule(
    implementation = _generated_ast_srcs_impl,
    attrs = {
        "generator": attr.label(
            executable = True,
            cfg = "host",
            default = Label("//java/com/craftinginterpreters/tool:tool"),
        ),
    },
)
