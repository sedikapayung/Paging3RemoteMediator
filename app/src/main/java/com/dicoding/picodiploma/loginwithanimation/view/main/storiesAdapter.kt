package com.dicoding.picodiploma.loginwithanimation.view.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.dicoding.picodiploma.loginwithanimation.data.response.ListStoryItem
import com.dicoding.picodiploma.loginwithanimation.databinding.StoriesitemBinding
class storiesAdapter(private val onClick: (ListStoryItem) -> Unit) :
    PagingDataAdapter<ListStoryItem, storiesAdapter.StoryViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryViewHolder {
        val binding = StoriesitemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StoryViewHolder, position: Int) {
        val story = getItem(position)
        if (story != null) {
            holder.bind(story, onClick)
        }
    }

    class StoryViewHolder(private val binding: StoriesitemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(story: ListStoryItem, onClick: (ListStoryItem) -> Unit) {
            binding.apply {
                itemTitle.text = story.name
                itemDescription.text = story.description
                Glide.with(itemPhoto.context)
                    .load(story.photoUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(itemPhoto)
                root.setOnClickListener {
                    onClick(story)
                }
            }
        }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ListStoryItem>() {
            override fun areItemsTheSame(oldItem: ListStoryItem, newItem: ListStoryItem): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: ListStoryItem, newItem: ListStoryItem): Boolean {
                return oldItem == newItem
            }
        }
    }
}