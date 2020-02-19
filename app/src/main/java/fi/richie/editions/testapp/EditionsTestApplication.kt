package fi.richie.editions.testapp

import android.app.Application
import com.squareup.picasso.OkHttp3Downloader
import com.squareup.picasso.Picasso
import fi.richie.editions.Editions
import okhttp3.OkHttpClient
import okhttp3.Protocol
import java.util.Collections
import android.util.Log
import fi.richie.common.public.TokenCompletion
import fi.richie.common.public.TokenProvider
import fi.richie.editions.AnalyticsEvent
import fi.richie.editions.AnalyticsListener

/**
 * Created by Luis Ángel San Martín on 2019-08-28.
 */
class EditionsTestApplication : Application() {
    lateinit var editions: Editions

    override fun onCreate() {
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
            override val hasToken: Boolean
                get() = true //true if the user is logged in, false otherwise

            override fun token(
                reason: TokenProvider.RequestReason,
                trigger: TokenProvider.TokenRequestTrigger,
                completion: TokenCompletion
            ) {
                when(reason) {
                    is TokenProvider.RequestReason.NoToken -> completion("eyJhbGciOiJFUzM4NCIsImtpZCI6Im5JYVJ5d1RXNlg1WndPaXllWFNmeDhnYWVWV1d6Z2g4YkRVbUJSeVRseVUifQ.eyJpc3MiOiJodHRwczovL2FwcGRhdGEucmljaGllLmZpIiwiZW50IjpbImVkaXRpb25zX2RlbW9fY29udGVudCJdLCJleHAiOjE4OTA4ODkyMDAsImlhdCI6MTU3NTI3MDAwMH0.TWFZ6T8PqPwTB5Icv8BjxXiAVeZatoJxxSvTJcd31QXMnE-6m1_0XHELqv5Zr91Hg0XGyElo9HGhG7scTOlf17-40d35HnFn6cLoKrCJhGcrTNrUw1mJ7_W8X6XBeOZS")
                    is TokenProvider.RequestReason.NoAccess -> completion(null)
                    is TokenProvider.RequestReason.NoEntitlements -> completion(null)
                }
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
    }
}
