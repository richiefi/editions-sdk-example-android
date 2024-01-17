package fi.richie.editions.testapp

import android.annotation.SuppressLint
import android.graphics.drawable.BitmapDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import fi.richie.editions.EditionCoverBitmapProvider
import fi.richie.editions.EditionsDiskUsageProvider
import fi.richie.common.IntSize
import fi.richie.common.interfaces.Cancelable
import fi.richie.editions.Edition

/**
 * Created by Luis Ángel San Martín on 2019-08-26.
 */

data class IssueViewModel(
    val isDownloading: Boolean,
    val progressDownload: Int,
    val isProcessing: Boolean
)

class IssuesAdapter(
    private var issues: List<Edition>,
    private var onIssueSelected: (Edition, position: Int) -> Unit,
    private var onDeleteIssue: (Edition, position: Int) -> Unit,
    private var editionIsDownloaded: (Edition) -> Boolean,
    private var issueStatusProvider: (Edition) -> IssueViewModel?,
    private var coverProvider: EditionCoverBitmapProvider,
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

    @SuppressLint("NotifyDataSetChanged")
    fun setEditions(editions: List<Edition>) {
        this.issues = editions

        this.notifyDataSetChanged()
    }

    override fun getItemCount(): Int = this.issues.count()

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: IssueViewHolder, position: Int) {
        val edition = this.issues[position]

        holder.coverView.setImageDrawable(null)

        holder.coverCancelable?.cancel()

        holder.coverCancelable = this.coverProvider.coverBitmapForEdition(
            this.issues[position],
            IntSize(800, 400)
        ) { bitmap, _ ->
            bitmap?.let {
                holder.coverView.setImageDrawable(BitmapDrawable(holder.coverView.resources, it)) }
        }

        val issueStatus = this.issueStatusProvider(edition)

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

        holder.title.text = edition.title

        holder.view.setOnClickListener {
            this.onIssueSelected(edition, position)
        }

        holder.view.setOnLongClickListener {
            this.onDeleteIssue(edition, position)

            true
        }

        if (this.editionIsDownloaded(edition)) {
            holder.dowloadedIndicator.visibility = View.VISIBLE
            this.diskUsageProvider.diskUsageByDownloadedEdition(edition.id) { size ->
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
    val dowloadedIndicator: TextView,
    var coverCancelable: Cancelable? = null
) : RecyclerView.ViewHolder(view)