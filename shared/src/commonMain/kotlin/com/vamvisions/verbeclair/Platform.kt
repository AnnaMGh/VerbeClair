package com.vamvisions.verbeclair

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform