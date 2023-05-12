package com.example.healthcare.adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.healthcare.R
import com.example.healthcare.databinding.ArticlesItemBinding
import com.example.healthcare.funcation.firebase
import com.example.healthcare.model.articles
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso


class DoctorArticles(var context: Context , var data : ArrayList<articles>) : RecyclerView.Adapter<DoctorArticles.ViewHolder>() {
    class ViewHolder(var binding: ArticlesItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layout = ArticlesItemBinding.inflate(LayoutInflater.from(parent.context), parent , false)
        return ViewHolder(layout)
    }

    private var player: SimpleExoPlayer? = null
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.ArticleTitle.text = data[position].title
        holder.binding.dcotorName.text = "د.${data[position].doctorName}"
        holder.binding.ArticleTitle.setLineSpacing(10f , 1f)
        holder.binding.date.text = "تاريخ النشر : ${data[position].date}"
        holder.binding.parefraf.text = data[position].paragraph
        if (data[position].imageURI != null){
            holder.binding.imagePost.visibility = View.VISIBLE
            Picasso.get()
                .load(data[position].imageURI)
                .placeholder(R.drawable.baseline_image_search_24) // optional placeholder image
                .error(R.drawable.baseline_error_24) // optional error image
                .into(holder.binding.imagePost)
        }
        if (data[position].videoURI != null){
            holder.binding.videoPost.visibility = View.VISIBLE
            val dataSourceFactory = DefaultDataSourceFactory(
                context,
                Util.getUserAgent(context, "MyVideo")
            )

            val videoItem: MediaItem = MediaItem.fromUri(Uri.parse(data[position].videoURI))
            val videoSource: MediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(videoItem)

            val player = SimpleExoPlayer.Builder(context).build()
            player.playWhenReady = true
            player.setMediaSource(videoSource)
            player.prepare()

            holder.binding.videoPost.player = player
            holder.binding.videoPost.keepScreenOn = true

            holder.itemView.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
                override fun onViewAttachedToWindow(p0: View) {

                }

                override fun onViewDetachedFromWindow(p0: View) {
                    player.release()
                    holder.binding.videoPost.player = null
                }
            })
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }
}