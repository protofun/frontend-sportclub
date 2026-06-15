package org.example.project.api

import io.ktor.client.HttpClient

expect fun createHttpClient(): HttpClient
