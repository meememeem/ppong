package com.donxux.ppong

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform