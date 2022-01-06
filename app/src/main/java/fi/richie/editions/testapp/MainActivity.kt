package fi.richie.editions.testapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import fi.richie.common.interfaces.Cancelable
import fi.richie.editions.DownloadProgressListener
import fi.richie.editions.Editions
import kotlinx.android.synthetic.main.main_activity.recyclerView
import java.util.UUID
import kotlin.collections.HashMap

class MainActivity : AppCompatActivity() {
    private lateinit var editions: Editions

    var adapter: IssuesAdapter? = null

    val progressTracker = HashMap<UUID, IssueViewModel>()
    val downloads = HashMap<UUID, Cancelable>()

    private var latestIssueTapped: UUID? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.editions = (this.application as EditionsTestApplication).editions

        setContentView(R.layout.main_activity)

        recyclerView.layoutManager = GridLayoutManager(this, 2)

        this.adapter = IssuesAdapter(
            emptyArray(),
            { issue, position -> onIssueSelected(issue, position) },
            { issue, position -> onDeleteIssue(issue, position) },
            { issue ->
                this.editions.downloadedEditionsProvider.downloadedEditions().contains(issue)
            },
            { this.progressTracker[it] },
            this.editions.editionCoverUrlProvider,
            this.editions.editionDisplayInfoProvider,
            this.editions.editionsDiskUsageProvider
        )

        recyclerView.adapter = this.adapter

        this.editions.editionProvider.allEditions { editions ->
            this.adapter?.setEditions(editions)
        }

        this.editions.updateFeed {
            this.editions.editionProvider.allEditions { editions ->
                this.adapter?.setEditions(editions)
            }
        }
    }

    private fun onIssueSelected(editionId: UUID, position: Int) {
        if (this.editions.downloadedEditionsProvider.downloadedEditions().contains(editionId)) {
            openIssue(editionId, position)

            // if the user explicitly wants to open an already downloaded issue we should always open it
            this.latestIssueTapped = editionId
        } else {
            if (this.downloads.containsKey(editionId)) { //cancel
                this.downloads[editionId]?.cancel()
                this.downloads.remove(editionId)

                this@MainActivity.progressTracker[editionId] = IssueViewModel(
                    isDownloading = false,
                    progressDownload = 0,
                    isProcessing = false
                )
                this@MainActivity.adapter?.refresh(position)

                purgeTracker()
            } else {
                downloadIssue(editionId, position)
            }
        }
    }

    private fun onDeleteIssue(editionId: UUID, position: Int) {
        if (this.editions.downloadedEditionsProvider.downloadedEditions().contains(editionId)) {
            this.editions.downloadedEditionsManager.deleteEdition(editionId, completion = {
                this@MainActivity.adapter?.refresh(position)
            })
        }
    }

    private fun openIssue(editionId: UUID, position: Int) {
        this.editions.editionPresenter.openEdition(editionId, this) { openError ->
            this@MainActivity.progressTracker.remove(editionId)
            this@MainActivity.adapter?.refresh(position)

            purgeTracker()

            if (openError != null) {
                Toast.makeText(
                    this@MainActivity,
                    "Error opening edition ${openError.name}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun downloadIssue(editionId: UUID, position: Int) {
        val download = this.editions.editionPresenter.downloadEdition(editionId, object :
            DownloadProgressListener {
            override fun editionDidFailDownload(editionId: UUID, exception: Exception?) {
                Toast.makeText(
                    this@MainActivity,
                    exception?.message ?: "unknown error downloading issue",
                    Toast.LENGTH_LONG
                ).show()

                this@MainActivity.progressTracker[editionId] = IssueViewModel(
                    isDownloading = false,
                    progressDownload = -1,
                    isProcessing = false
                )
                this@MainActivity.adapter?.refresh(position)
            }

            override fun editionDidFailWithNoEntitlements(editionId: UUID?) {

            }

            override fun editionWillStartDownload(editionId: UUID) {
                this@MainActivity.progressTracker[editionId] = IssueViewModel(
                    isDownloading = true,
                    progressDownload = 0,
                    isProcessing = false
                )
                this@MainActivity.adapter?.refresh(position)
            }

            override fun editionDownloadProgress(
                editionId: UUID,
                progress: Float,
                isBeingPreparedForPresentation: Boolean,
                downloadedBytes: Long,
                expectedTotalBytes: Long
            ) {
                this@MainActivity.progressTracker[editionId] = IssueViewModel(
                    isDownloading = progress < 1,
                    progressDownload = (progress * 100).toInt(),
                    isProcessing = isBeingPreparedForPresentation
                )
                this@MainActivity.adapter?.refresh(position)
            }

            override fun editionDidDownload(editionId: UUID) {
                this@MainActivity.progressTracker[editionId] = IssueViewModel(
                    isDownloading = false,
                    progressDownload = -1,
                    isProcessing = false
                )
                this@MainActivity.adapter?.refresh(position)

                val numOfItemsBeingDownloaded = this@MainActivity.progressTracker.size

                if (numOfItemsBeingDownloaded == 1) {
                    this@MainActivity.openIssue(editionId, position)
                }

                this@MainActivity.downloads.remove(editionId)

                purgeTracker()
            }
        })


        if (download != null) {
            this.downloads[editionId] = download

            this@MainActivity.progressTracker[editionId] = IssueViewModel(
                isDownloading = true,
                progressDownload = 0,
                isProcessing = false
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
