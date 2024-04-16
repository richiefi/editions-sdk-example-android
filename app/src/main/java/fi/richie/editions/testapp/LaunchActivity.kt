package fi.richie.editions.testapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import fi.richie.common.coroutines.launchWithOuterScope
import fi.richie.common.tryCatch
import fi.richie.editions.Editions
import fi.richie.editions.testapp.databinding.LaunchActivityBinding

/**
 * Created by Luis Ángel San Martín on 2019-10-24.
 */
class LaunchActivity : AppCompatActivity() {
    private lateinit var editions: Editions

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.lifecycleScope.launchWithOuterScope {
            val editions =
                tryCatch { (this.application as EditionsTestApplication).editions.await() }

            val binding = LaunchActivityBinding.inflate(this.layoutInflater)

            setContentView(binding.root)

            if (editions != null) {
                this.editions = editions

                val intent = Intent(this@LaunchActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                binding.notificationText.text = getString(R.string.error_init)
            }
        }
    }
}
