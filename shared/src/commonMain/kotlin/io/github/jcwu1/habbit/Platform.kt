package io.github.jcwu1.habbit

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform