package com.jeff.animeapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.jeff.animeapp.R;
import com.jeff.animeapp.models.ReleaseItem;

import java.util.List;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder> {

    private List<ReleaseItem> releaseList;

    public CalendarAdapter(List<ReleaseItem> releaseList) {
        this.releaseList = releaseList;
    }

    @NonNull
    @Override
    public CalendarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_release_calendar, parent, false);
        return new CalendarViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CalendarViewHolder holder, int position) {
        ReleaseItem item = releaseList.get(position);

        holder.title.setText(item.getTitle());
        holder.date.setText(item.getReleaseDate());
        holder.type.setText(item.getType());
        holder.tags.setText(item.getTags());

        // Load thumbnail image with Glide
        Glide.with(holder.thumbnail.getContext())
                .load(item.getImageUrl())
                .placeholder(R.drawable.placeholder_image) // shown while loading
                .error(R.drawable.placeholder_image)       // shown if load fails
                .into(holder.thumbnail);
    }

    @Override
    public int getItemCount() {
        return releaseList.size();
    }

    static class CalendarViewHolder extends RecyclerView.ViewHolder {
        TextView title, date, type, tags;
        ImageView thumbnail;

        CalendarViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tvTitle);
            date = itemView.findViewById(R.id.tvDate);
            type = itemView.findViewById(R.id.tvType);
            tags = itemView.findViewById(R.id.tvTags);
            thumbnail = itemView.findViewById(R.id.ivThumbnail);
        }
    }
}
