package com.example.shop.util

import kotlinx.serialization.json.Json

val AppJson = Json {
    prettyPrint = true
    ignoreUnknownKeys = true
}
