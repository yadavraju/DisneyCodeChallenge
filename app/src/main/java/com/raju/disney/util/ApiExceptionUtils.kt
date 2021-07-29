package com.raju.disney.util

import retrofit2.HttpException
import java.net.HttpURLConnection

const val SOMETHING_WENT_WRONG = "Something Went Wrong"
const val NO_INTERNET = "No Internet Connected"
const val NOT_FOUND = "Not Found"
const val UN_AUTHORIZED_ACCESS = "Unauthorized Access"

object ApiExceptionUtils {

  fun getExceptionMessage(e: Throwable): String {
    return when (e) {
      is HttpException -> {
        val response = e.response()
        when (response?.code()) {
          HttpURLConnection.HTTP_UNAUTHORIZED -> UN_AUTHORIZED_ACCESS
          HttpURLConnection.HTTP_NOT_FOUND -> NOT_FOUND
          else -> SOMETHING_WENT_WRONG
        }
      }
      is java.net.UnknownHostException -> NO_INTERNET
      else -> SOMETHING_WENT_WRONG
    }
  }
}