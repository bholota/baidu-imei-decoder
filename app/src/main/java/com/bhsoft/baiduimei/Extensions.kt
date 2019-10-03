package com.bhsoft.baiduimei

@ExperimentalUnsignedTypes // just to make it clear that the experimental unsigned types are used
fun ByteArray.toHexString() = asUByteArray().map { it.toString(16) }.joinToString("")