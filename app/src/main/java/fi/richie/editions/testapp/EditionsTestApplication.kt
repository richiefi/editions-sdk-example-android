package fi.richie.editions.testapp

import android.app.Application
import android.os.StrictMode
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import com.squareup.picasso.OkHttp3Downloader
import com.squareup.picasso.Picasso
import fi.richie.editions.Editions
import fi.richie.editions.TokenCompletion
import fi.richie.editions.TokenProvider
import okhttp3.OkHttpClient
import okhttp3.Protocol
import java.util.Collections
import android.os.StrictMode.setThreadPolicy
import android.util.Log
import fi.richie.editions.AnalyticsEvent
import fi.richie.editions.AnalyticsListener

/**
 * Created by Luis Ángel San Martín on 2019-08-28.
 */
class EditionsTestApplication : Application() {
    lateinit var editions: Editions

    override fun onCreate() {

        setThreadPolicy(
            StrictMode.ThreadPolicy.Builder().detectAll()
                .penaltyLog()
                .build()
        )

        super.onCreate()

        val client = OkHttpClient.Builder()
            .protocols(Collections.singletonList(Protocol.HTTP_1_1))
            .build()

        val picasso = Picasso.Builder(this)
            .downloader(OkHttp3Downloader(client))
            .build()

        picasso.isLoggingEnabled = true

        Picasso.setSingletonInstance(picasso)

        val tokenProvider = object : TokenProvider {
            override fun token(
                reason: TokenProvider.RequestReason,
                completion: TokenCompletion
            ) {
                // TODO add here a valid token
                completion(null)
            }
        }

        val analyticsListener = object : AnalyticsListener {
            override fun onAnalyticsEvent(event: AnalyticsEvent) {
                Log.i("INFO", "Event : ${event.name}")
            }

        }

        this.editions = Editions(
            appId = "fi.richie.editionsTestApp",
            tokenProvider = tokenProvider,
            analyticsListener = analyticsListener,
            application = this
        )

        this.editions.initialize { success ->
            if (!success)
            Toast.makeText(this, "ERROR LOADING APPCONFIG", LENGTH_LONG).show()
        }
    }
}
