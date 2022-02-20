package com.raju.disney.ui.activity

import android.R
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.browser.customtabs.CustomTabsIntent
import com.raju.disney.base.BaseActivity
import com.raju.disney.databinding.ActivityDahboardBinding
import com.raju.disney.opentelemetry.DisneyOTel
import dagger.hilt.android.AndroidEntryPoint
import io.opentelemetry.api.trace.Span
import io.opentelemetry.api.trace.StatusCode
import okhttp3.*
import org.apache.http.conn.ssl.AllowAllHostnameVerifier
import org.apache.http.conn.ssl.SSLSocketFactory
import java.io.IOException
import java.security.KeyManagementException
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.*
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager


@AndroidEntryPoint
class DashboardActivity : BaseActivity() {

    private lateinit var okHttpClient: Call.Factory
    lateinit var binding: ActivityDahboardBinding

    private var customChromeTabTimer: Span? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDahboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        okHttpClient = buildOkHttpClient(oTel)

        binding.btnTraceFlight.setOnClickListener {
            val span = oTel.startWorkflow("open:instrumented:api")
            startActivity(Intent(this, FlightActivity::class.java))
            span.end()
        }

        binding.btnTraceMarvelApi.setOnClickListener {
            val span = oTel.startWorkflow("open:not:instrumented:api")
            startActivity(Intent(this, MainActivity::class.java))
            span.end()
        }

        binding.httpMe.setOnClickListener {
            val workflow: Span = oTel.startWorkflow("User Login")
            //not really a login, but it does make an http call
            makeCall("https://pmrum.o11ystore.com?user=me&pass=secret123secret", workflow)
        }

        binding.crash.setOnClickListener { v -> throw IllegalStateException("Crashing due to a bug!") }

        binding.httpMeBad.setOnClickListener {
            val workflow: Span =
                oTel.startWorkflow("Workflow With Error")
            makeCall("https://asdlfkjasd.asdfkjasdf.ifi", workflow)
        }

        binding.httpMeNotFound.setOnClickListener {
            val workflow: Span =
                oTel.startWorkflow("Workflow with 404")
            makeCall("https://pmrum.o11ystore.com/foobarbaz", workflow)
        }

        binding.buttonToCustomTab.setOnClickListener {
            val url = "https://www.espn.com/"
            customChromeTabTimer = oTel.startWorkflow("Visit to Chrome Custom Tab")
            CustomTabsIntent.Builder()
                .setColorScheme(CustomTabsIntent.COLOR_SCHEME_DARK)
                .setStartAnimations(
                    this,
                    R.anim.slide_in_left,
                    R.anim.slide_out_right
                )
                .setExitAnimations(
                    this,
                    R.anim.slide_out_right,
                    R.anim.slide_in_left
                )
                .build()
                .launchUrl(this, Uri.parse(url))
        }

        binding.buttonFreeze.setOnClickListener {
            val appFreezer: Span = oTel.startWorkflow("app freezer")
            try {
                for (i in 0..19) {
                    Thread.sleep(1000)
                    appFreezer.addEvent("still sleeping")
                }
            } catch (e: InterruptedException) {
                e.printStackTrace()
            } finally {
                appFreezer.end()
            }
        }

        binding.buttonWork.setOnClickListener {
            val hardWorker: Span = oTel.startWorkflow("main thread working hard")
            val random = Random()
            val startTime = System.currentTimeMillis()
            while (true) {
                random.nextDouble()
                if (System.currentTimeMillis() - startTime > 20000) {
                    break
                }
            }
            hardWorker.end()
        }

        binding.sessionId.text = "SessionID: " + oTel.oTelSessionId

    }

    private fun makeCall(url: String, workflow: Span) {
        //make sure the span is in the current context so it can be propagated into the async call.
        workflow.makeCurrent().use {
            val call = okHttpClient.newCall(Request.Builder().url(url).get().build())
            call.enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    runOnUiThread { binding.httpResult.text = "error" }
                    workflow.setStatus(StatusCode.ERROR, "failure to communicate")
                    workflow.end()
                }

                override fun onResponse(call: Call, response: Response) {
                    response.body.run {
                        val responseCode = response.code
                        runOnUiThread { binding.httpResult.text = "Status Code: $responseCode" }
                        workflow.end()
                    }
                }
            })
        }
    }

    override fun onResume() {
        super.onResume()
        binding.sessionId.text = "SessionID: " + oTel.oTelSessionId

        customChromeTabTimer?.end()
        customChromeTabTimer = null
    }

    private fun buildOkHttpClient(disneyOTel: DisneyOTel): Call.Factory {
        //grab the default executor service that okhttp uses, and wrap it with one that will propagate the otel context.
        val builder = OkHttpClient.Builder()
        return try {
            // NOTE: This is really bad and dangerous. Don't ever do this in the real world.
            // it's only necessary because the demo endpoint uses a self-signed SSL cert.
            val sslContext = SSLContext.getInstance(SSLSocketFactory.SSL)
            sslContext.init(null, trustAllCerts, SecureRandom())
            val sslSocketFactory = sslContext.socketFactory
            disneyOTel.createRumOkHttpCallFactory(
                builder
                    .sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
                    .hostnameVerifier(AllowAllHostnameVerifier())
                    .build()
            )
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
            disneyOTel.createRumOkHttpCallFactory(builder.build())
        } catch (e: KeyManagementException) {
            e.printStackTrace()
            disneyOTel.createRumOkHttpCallFactory(builder.build())
        }
    }

    private val trustAllCerts = arrayOf<TrustManager>(
        object : X509TrustManager {
            override fun checkClientTrusted(
                chain: Array<X509Certificate>,
                authType: String
            ) {
            }

            override fun checkServerTrusted(
                chain: Array<X509Certificate>,
                authType: String
            ) {
            }

            override fun getAcceptedIssuers(): Array<X509Certificate> {
                return arrayOf()
            }
        }
    )
}