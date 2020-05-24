load("@rules_cc//cc:defs.bzl", "cc_library", "cc_binary")

cc_library(
    name = "clox_lib",
    srcs = glob(["*.c"]),
    hdrs = glob(["*.h"]),
)

cc_binary(
    name = "clox",
    deps = [":clox_lib"],
)