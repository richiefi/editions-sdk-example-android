package fi.richie.editions.testapp

import android.app.Application
import com.squareup.picasso.OkHttp3Downloader
import com.squareup.picasso.Picasso
import fi.richie.Richie
import fi.richie.common.Log
import fi.richie.common.StorageOption
import fi.richie.common.shared.TokenCompletion
import fi.richie.common.shared.TokenProvider
import fi.richie.editions.AnalyticsEvent
import fi.richie.editions.AnalyticsListener
import fi.richie.editions.Editions
import fi.richie.editions.EditionsConfiguration
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import okhttp3.OkHttpClient
import okhttp3.Protocol
import java.util.Collections

/**
 * Created by Luis Ángel San Martín on 2019-08-28.
 */
class EditionsTestApplication : Application() {
    private val deferredEditions = CompletableDeferred<Editions>()
    val editions: Deferred<Editions> = this.deferredEditions

    override fun onCreate() {
        super.onCreate()

        Log.level = Log.Level.VERBOSE

        val client = OkHttpClient.Builder()
            .protocols(Collections.singletonList(Protocol.HTTP_1_1))
            .build()

        val picasso = Picasso.Builder(this)
            .downloader(OkHttp3Downloader(client))
            .build()

        picasso.isLoggingEnabled = true

        Picasso.setSingletonInstance(picasso)

        val tokenProvider = object : TokenProvider {
            override val hasToken: Boolean
                get() = true

            override fun token(
                reason: TokenProvider.RequestReason,
                trigger: TokenProvider.TokenRequestTrigger,
                completion: TokenCompletion,
            ) {
                completion("eyJhbGciOiJFUzM4NCIsImtpZCI6Im5JYVJ5d1RXNlg1WndPaXllWFNmeDhnYWVWV1d6Z2g4YkRVbUJSeVRseVUifQ.eyJlbnQiOlsiZWRpdGlvbnNfZGVtb19jb250ZW50Il0sImV4cCI6MTkyMTQ3NDgwMCwiaWF0IjoxNzAwNTUwMDAwfQ.zIoJ1htS-xpnHsOR_8ju-gVp9iNmY63424xly4fDaWgtsagoosf2vNW7DDY0gnIV_cJT8SnU0F5GrCO_CVPYT6omsG5qV4KwaGuZrL72j-g_KvO48MYmHlH8OV7oEK3w")
            }
        }

        val analyticsListener = object : AnalyticsListener {
            override fun onAnalyticsEvent(event: AnalyticsEvent) {
                Log.info("Event : ${event.name}")
            }
        }

        val configuration = EditionsConfiguration(storageLocation = StorageOption.INTERNAL)

        Richie.start("fi.richie.editionsTestApp", this)
        Richie.editions(tokenProvider, analyticsListener, configuration) { editions ->
            if (editions != null) {
                this.deferredEditions.complete(editions)
            } else {
                this.deferredEditions.completeExceptionally(Exception("Could not create Editions"))
            }
        }
    }
}
