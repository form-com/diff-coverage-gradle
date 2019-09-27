package com.worldapp.coverage.http

import java.net.URL

fun requestGet(requestUrl: String): String = URL(requestUrl).readText()
