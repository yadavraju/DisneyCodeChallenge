package com.raju.disney.api.repository

import com.raju.disney.BuildConfig.API_KEY
import com.raju.disney.BuildConfig.P_API_KEY
import com.raju.disney.util.Md5HashGenerator

object QueryParams {

    // ts - a timestamp (or other long string which can change on a request-by-request basis)
    // hash - a md5 digest of the ts parameter, your private key and your public key (e.g.
    // md5(ts+privateKey+publicKey)
    fun getQueryParams(): MutableMap<String, String> {
        val currentTimeStamp =
            "1627355040" // Currently I am passing these two as default for now, but we need to pass // current time stamp
        val data: MutableMap<String, String> = HashMap()
        data["ts"] = currentTimeStamp
        data["apikey"] = API_KEY
        data["hash"] = Md5HashGenerator.getMd5(currentTimeStamp + P_API_KEY + API_KEY)
        return data
    }

    //traceparent: 00-d0d6c66f58574a2518ad3f28af6204f4-cfb23945dc0d5847-01
    fun getTraceParent(): MutableMap<String, String> {
        val data: MutableMap<String, String> = HashMap()
        data["traceparent"] = "00-d0d6c66f58574a2518ad3f28af6204f4-cfb23945dc0d5847-01"
        return data
    }
}
