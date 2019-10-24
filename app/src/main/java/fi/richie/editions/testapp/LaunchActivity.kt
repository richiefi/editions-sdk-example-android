package fi.richie.editions.testapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import fi.richie.editions.Editions
import kotlinx.android.synthetic.main.launch_activity.*

/**
 * Created by Luis Ángel San Martín on 2019-10-24.
 */
class LaunchActivity : AppCompatActivity() {
    private lateinit var editions: Editions

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.editions = (this.application as EditionsTestApplication).editions

        setContentView(R.layout.launch_activity)

        this.editions.initialize { success ->
            if (success) {
                val intent = Intent(this@LaunchActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                notification_text.text = getString(R.string.error_init)
            }
        }
    }
}