package fi.richie.editions.testapp

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import fi.richie.editions.EditionCoverUrlProvider
import fi.richie.editions.EditionDisplayInfoProvider
import fi.richie.editions.EditionsDiskUsageProvider
import fi.richie.richiekit.IntSize
import java.util.UUID

/**
 * Created by Luis Ángel San Martín on 2019-08-26.
 */

data class IssueViewModel(
    val isDownloading: Boolean,
    val progressDownload: Int,
    val isProcessing: Boolean
)

class IssuesAdapter(
    private var issues: Array<UUID>,
    private var onIssueSelected: (UUID, position: Int) -> Unit,
    private var onDeleteIssue: (UUID, position: Int) -> Unit,
    private var editionIsDownloaded: (UUID) -> Boolean,
    private var issueStatusProvider: (UUID) -> IssueViewModel?,
    private var coverUrlProvider: EditionCoverUrlProvider,
    private var displayInfoProvider: EditionDisplayInfoProvider,
    private var diskUsageProvider: EditionsDiskUsageProvider
) : RecyclerView.Adapter<IssueViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IssueViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.issue, parent, false)

        return IssueViewHolder(
            view,
            view.findViewById(R.id.titleTextView),
            view.findViewById(R.id.coverImageView),
            view.findViewById(R.id.downloadingProgressBar),
            view.findViewById(R.id.preparingProgressBar),
            view.findViewById(R.id.downloadedIndicator)
        )
    }

    fun setEditions(editions: Array<UUID>) {
        this.issues = editions

        this.notifyDataSetChanged()
    }

    override fun getItemCount(): Int = this.issues.count()

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: IssueViewHolder, position: Int) {
        val issueId = this.issues[position]

        val issueDisplayInfo = this.displayInfoProvider.displayInfoForEdition(issueId) ?: return

        val url = this.coverUrlProvider.coverUrlForEdition(
            this.issues[position],
            IntSize(issueDisplayInfo.coverSize.width, issueDisplayInfo.coverSize.height)
        ).toString()

        holder.coverView.setImageDrawable(null)

        val issueStatus = this.issueStatusProvider(issueId)

        if (issueStatus?.isDownloading == true) {
            holder.downloadProgressBar.visibility = View.VISIBLE
            holder.downloadProgressBar.progress = issueStatus.progressDownload
        } else {
            holder.downloadProgressBar.visibility = View.GONE
        }

        if (issueStatus?.isProcessing == true) {
            holder.prepareProgressBar.visibility = View.VISIBLE
        } else {
            holder.prepareProgressBar.visibility = View.GONE
        }

        Picasso.get().load(url).fit().centerInside().into(holder.coverView)

        holder.title.text = issueDisplayInfo.title

        holder.view.setOnClickListener {
            this.onIssueSelected(issueId, position)
        }

        holder.view.setOnLongClickListener {
            this.onDeleteIssue(issueId, position)

            true
        }

        if (this.editionIsDownloaded(issueId)) {
            holder.dowloadedIndicator.visibility = View.VISIBLE
            this.diskUsageProvider.diskUsageByDownloadedEdition(issueId) { size ->
                val spaceOnDisk = size / 1024 / 1024
                holder.dowloadedIndicator.text = "downloaded - ${spaceOnDisk}MB"
            }
        } else {
            holder.dowloadedIndicator.visibility = View.GONE
        }
    }

    fun refresh(position: Int) {
        notifyItemChanged(position)
    }
}

class IssueViewHolder(
    val view: View,
    val title: TextView,
    val coverView: ImageView,
    val downloadProgressBar: ProgressBar,
    val prepareProgressBar: ProgressBar,
    val dowloadedIndicator: TextView
) : RecyclerView.ViewHolder(view)