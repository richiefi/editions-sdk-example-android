package fi.richie.editions.testapp

import android.app.Application
import com.squareup.picasso.OkHttp3Downloader
import com.squareup.picasso.Picasso
import fi.richie.Richie
import fi.richie.common.Log
import fi.richie.common.Scopes
import fi.richie.common.coroutines.launchWithOuterScope
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
                completion("eyJhbGciOiJFUzM4NCIsInR5cCI6IkpXVCIsImtpZCI6InJpY2hpZS1ib29rcy1kZXYifQ.eyJlbnQiOlsiZGV2LWFsbC1hY2Nlc3MiXSwiZXhwIjoxNzE3NDExMjk5LCJpc3MiOiJyaWNoaWUtYm9va3MtZGV2Iiwic3ViIjoicmljaGllLWJvb2tzLWRldiIsImlhdCI6MTU1OTU1ODU2NH0.mC8jbluWgSMa6f0b4fGesZElNr74S36tMAQFPjSyGwINjBNnbf-NG9DXgO6qwyhKk1RgnkpfyiRxkzfrYjkHhgDCPMlcNsA_MvWWdCEehOn3DE5HsvxS2Ev21fotXDXb")
            }
        }

        val analyticsListener = object : AnalyticsListener {
            override fun onAnalyticsEvent(event: AnalyticsEvent) {
                Log.info("Event : ${event.name}")
            }
        }

        val configuration = EditionsConfiguration(2.0f)

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
