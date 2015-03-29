#!/usr/bin/env Rscript
options(echo = FALSE)
args <- commandArgs(trailingOnly = TRUE)

v1 <- scan(args[1], sep = "\n", quiet = TRUE)
v2 <- scan(args[2], sep = "\n", quiet = TRUE)

pTwo <- stats::wilcox.test(v1, v2, alternative="two.sided")$p.value
pLess <- stats::wilcox.test(v1, v2, alternative="less")$p.value
pGreater <- stats::wilcox.test(v1, v2, alternative="greater")$p.value

fsymb <- ifelse(pTwo < 0.05, ifelse(pLess < 0.05, "[<]", "[>]"), "[?]")

cat(paste(args[1], "vs", args[2], ":", fsymb, " { <", pLess, "} { =", pTwo, "} { >", pGreater, "}\n"))
