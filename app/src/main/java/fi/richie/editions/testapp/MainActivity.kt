package fi.richie.editions.testapp

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import fi.richie.common.coroutines.launchWithOuterScope
import fi.richie.common.interfaces.Cancelable
import fi.richie.editions.DownloadProgressListener
import fi.richie.editions.Edition
import fi.richie.editions.Editions
import fi.richie.editions.testapp.databinding.MainActivityBinding
import kotlin.collections.HashMap

class MainActivity : AppCompatActivity() {
    private lateinit var editions: Editions

    var adapter: IssuesAdapter? = null

    val progressTracker = HashMap<Edition, IssueViewModel>()
    val downloads = HashMap<Edition, Cancelable>()

    private var latestIssueTapped: Edition? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        init()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        this.menuInflater.inflate(R.menu.top_bar_menu, menu)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.refresh_button -> refresh()
        }

        return super.onOptionsItemSelected(item)
    }

    private fun init() = this.lifecycleScope.launchWithOuterScope {
        this.editions = (this.application as EditionsTestApplication).editions.await()

        val binding = MainActivityBinding.inflate(this.layoutInflater)

        setContentView(binding.root)

        binding.recyclerView.layoutManager = GridLayoutManager(this, 2)

        this.adapter = IssuesAdapter(
            issues = emptyList(),
            onIssueSelected = { edition, position -> onEditionSelected(edition, position) },
            onDeleteIssue = { edition, position -> onDeleteEdition(edition, position) },
            editionIsDownloaded = { edition ->
                this.editions.downloadedEditionsProvider.isEditionDownloaded(edition.id)
            },
            issueStatusProvider = { this.progressTracker[it] },
            coverProvider = this.editions.editionCoverProvider,
            diskUsageProvider = this.editions.editionsDiskUsageProvider,
        )

        binding.recyclerView.adapter = this.adapter

        // you can query the available editions before updating
        this.editions.editionProvider?.editions()?.next { result ->
            if (result.isFailure) {
                Toast.makeText(
                    this@MainActivity,
                    "Error getting editions at launch",
                    Toast.LENGTH_LONG,
                ).show()
            }

            result.getOrNull()?.let { page ->
                val editions = page.editions
                this.adapter?.setEditions(editions)

                // call update, and then update the content again
                refresh()
            }
        }
    }

    private fun refresh() {
        this.editions.updateFeed {
            this.editions.editionProvider?.editions()?.next { result ->
                result.getOrNull()?.let { page ->
                    val newEditions = page.editions
                    this.adapter?.setEditions(newEditions)
                }
            }
        }
    }

    private fun onEditionSelected(edition: Edition, position: Int) {
        if (this.editions.downloadedEditionsProvider.isEditionDownloaded(edition.id)) {
            openEdition(edition, position)

            // if the user explicitly wants to open an already downloaded issue we should always open it
            this.latestIssueTapped = edition
        } else {
            if (this.downloads.containsKey(edition)) { // cancel
                this.downloads[edition]?.cancel()
                this.downloads.remove(edition)

                this@MainActivity.progressTracker[edition] = IssueViewModel(
                    isDownloading = false,
                    progressDownload = 0,
                    isProcessing = false,
                )
                this@MainActivity.adapter?.refresh(position)

                purgeTracker()
            } else {
                downloadEdition(edition, position)
            }
        }
    }

    private fun onDeleteEdition(edition: Edition, position: Int) {
        if (this.editions.downloadedEditionsProvider.isEditionDownloaded(edition.id)) {
            this.editions.downloadedEditionsManager.deleteEdition(edition.id, completion = {
                this@MainActivity.adapter?.refresh(position)
            })
        }
    }

    private fun openEdition(edition: Edition, position: Int) {
        this.editions.editionPresenter.openEdition(edition, this) { openError ->
            this@MainActivity.progressTracker.remove(edition)
            this@MainActivity.adapter?.refresh(position)

            purgeTracker()

            if (openError != null) {
                Toast.makeText(
                    this@MainActivity,
                    "Error opening edition ${openError.name}",
                    Toast.LENGTH_LONG,
                ).show()
            }
        }
    }

    private fun downloadEdition(edition: Edition, position: Int) {
        val download = this.editions.editionPresenter.downloadEdition(
            edition = edition,
            downloadProgressListener = object : DownloadProgressListener {
                override fun editionDidFailDownload(edition: Edition, exception: Throwable?) {
                    Toast.makeText(
                        this@MainActivity,
                        exception?.message ?: "unknown error downloading issue",
                        Toast.LENGTH_LONG,
                    ).show()

                    this@MainActivity.progressTracker[edition] = IssueViewModel(
                        isDownloading = false,
                        progressDownload = -1,
                        isProcessing = false,
                    )
                    this@MainActivity.downloads.remove(edition)
                    this@MainActivity.adapter?.refresh(position)
                }

                override fun editionWillStartDownload(edition: Edition) {
                    this@MainActivity.progressTracker[edition] = IssueViewModel(
                        isDownloading = true,
                        progressDownload = 0,
                        isProcessing = false,
                    )
                    this@MainActivity.adapter?.refresh(position)
                }

                override fun editionDownloadProgress(
                    edition: Edition,
                    progress: Float,
                    isBeingPreparedForPresentation: Boolean,
                    downloadedBytes: Long,
                    expectedTotalBytes: Long,
                ) {
                    this@MainActivity.progressTracker[edition] = IssueViewModel(
                        isDownloading = progress < 1,
                        progressDownload = (progress * 100).toInt(),
                        isProcessing = isBeingPreparedForPresentation,
                    )
                    this@MainActivity.adapter?.refresh(position)
                }

                override fun editionDidDownload(edition: Edition) {
                    this@MainActivity.progressTracker[edition] = IssueViewModel(
                        isDownloading = false,
                        progressDownload = -1,
                        isProcessing = false,
                    )
                    this@MainActivity.adapter?.refresh(position)

                    val numOfItemsBeingDownloaded = this@MainActivity.progressTracker.size

                    if (numOfItemsBeingDownloaded == 1) {
                        this@MainActivity.openEdition(edition, position)
                    }

                    this@MainActivity.downloads.remove(edition)

                    purgeTracker()
                }

                override fun editionDownloadCanceled(edition: Edition) {
                    this@MainActivity.downloads.remove(edition)
                }
            },
        )

        if (download != null) {
            this.downloads[edition] = download

            this@MainActivity.progressTracker[edition] = IssueViewModel(
                isDownloading = true,
                progressDownload = 0,
                isProcessing = false,
            )
            this@MainActivity.adapter?.refresh(position)
        }
    }

    private fun purgeTracker() {
        var allDone = true
        this.progressTracker.forEach { (_, model) ->
            if (model.isProcessing || model.isDownloading) {
                allDone = false
            }
        }

        if (allDone) {
            this.progressTracker.clear()
        }
    }
}
