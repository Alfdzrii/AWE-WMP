package com.example.awe;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.MusicViewHolder> {

    private final List<MusicItem> musicList;
    private final OnItemInteractionListener listener;

    public interface OnItemInteractionListener {
        void onFavoriteClicked(MusicItem item, int position);
        void onPlayPauseClicked(MusicItem item, int position);
        void onDeleteClicked(MusicItem item, int position);
    }

    public MusicAdapter(List<MusicItem> musicList, OnItemInteractionListener listener) {
        this.musicList = musicList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MusicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_music, parent, false);
        return new MusicViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MusicViewHolder holder, int position) {
        MusicItem currentItem = musicList.get(position);
        holder.songTitle.setText(currentItem.getTitle());

        holder.playPauseButton.setImageResource(
                currentItem.isPlaying() ? R.drawable.ic_pause : R.drawable.ic_play_arrow);

        // --- PERUBAHAN UNTUK WARNA FAVORIT ---
        if (currentItem.isFavorite()) {
            holder.favoriteButton.setImageResource(R.drawable.ic_favorite); // Ikon terisi
            holder.favoriteButton.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(holder.itemView.getContext(), R.color.merah))); // Warna merah
        } else {
            holder.favoriteButton.setImageResource(R.drawable.ic_favorite_border); // Ikon kosong
            holder.favoriteButton.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(holder.itemView.getContext(), R.color.white))); // Warna putih
        }

        holder.playPauseButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPlayPauseClicked(currentItem, holder.getAdapterPosition());
            }
        });

        holder.favoriteButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onFavoriteClicked(currentItem, holder.getAdapterPosition());
            }
        });

        holder.deleteButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClicked(currentItem, holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return musicList.size();
    }

    public static class MusicViewHolder extends RecyclerView.ViewHolder {
        TextView songTitle;
        ImageButton playPauseButton;
        ImageButton favoriteButton;
        ImageButton deleteButton;

        public MusicViewHolder(@NonNull View itemView) {
            super(itemView);
            songTitle = itemView.findViewById(R.id.text_song_title);
            playPauseButton = itemView.findViewById(R.id.button_play_pause);
            favoriteButton = itemView.findViewById(R.id.button_favorite);
            deleteButton = itemView.findViewById(R.id.button_delete);
        }
    }
}
