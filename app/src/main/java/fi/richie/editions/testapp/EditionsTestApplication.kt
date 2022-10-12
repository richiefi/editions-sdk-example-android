package fi.richie.editions.testapp

import android.app.Application
import fi.richie.editions.Editions
import fi.richie.common.Log
import fi.richie.common.shared.TokenCompletion
import fi.richie.common.shared.TokenProvider
import fi.richie.editions.AnalyticsEvent
import fi.richie.editions.AnalyticsListener
import fi.richie.editions.EditionsConfiguration

/**
 * Created by Luis Ángel San Martín on 2019-08-28.
 */
class EditionsTestApplication : Application() {
    lateinit var editions: Editions

    override fun onCreate() {
        super.onCreate()

        Log.level = Log.Level.VERBOSE

        val tokenProvider = object : TokenProvider {
            override val hasToken: Boolean
                get() = true

            override fun token(
                reason: TokenProvider.RequestReason,
                trigger: TokenProvider.TokenRequestTrigger,
                completion: TokenCompletion
            ) {
                when (reason) {
                    is TokenProvider.RequestReason.NoToken -> completion("eyJhbGciOiJFUzM4NCIsInR5cCI6IkpXVCIsImtpZCI6InJpY2hpZS1ib29rcy1kZXYifQ.eyJlbnQiOlsiZGV2LWFsbC1hY2Nlc3MiXSwiZXhwIjoxNzE3NDExMjk5LCJpc3MiOiJyaWNoaWUtYm9va3MtZGV2Iiwic3ViIjoicmljaGllLWJvb2tzLWRldiIsImlhdCI6MTU1OTU1ODU2NH0.mC8jbluWgSMa6f0b4fGesZElNr74S36tMAQFPjSyGwINjBNnbf-NG9DXgO6qwyhKk1RgnkpfyiRxkzfrYjkHhgDCPMlcNsA_MvWWdCEehOn3DE5HsvxS2Ev21fotXDXb")
                    is TokenProvider.RequestReason.NoAccess -> completion(null)
                    is TokenProvider.RequestReason.NoEntitlements -> completion(null)
                }
            }
        }

        val analyticsListener = object : AnalyticsListener {
            override fun onAnalyticsEvent(event: AnalyticsEvent) {
                Log.info("Event : ${event.name}")
            }
        }

        val configuration = EditionsConfiguration(2.0f)

        this.editions = Editions(
            appId = "fi.richie.editionsTestApp",
            tokenProvider = tokenProvider,
            analyticsListener = analyticsListener,
            application = this,
            configuration = configuration
        )
    }
}
