package com.yourname.reservation.adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.yourname.reservation.R;
import java.util.List;

public class PropertyImageAdapter extends RecyclerView.Adapter<PropertyImageAdapter.ImageViewHolder> {
    private Context context;
    private List<Object> images; // Can be String (URL) or Uri

    public PropertyImageAdapter(Context context, List<Object> images) {
        this.context = context;
        this.images = images;
    }

    public void updateImages(List<Object> newImages) {
        this.images = newImages;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_property_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        Object image = images.get(position);
        
        if (image instanceof String) {
            // Load image URL
            Glide.with(context)
                    .load((String) image)
                    .placeholder(R.drawable.placeholder_property)
                    .error(R.drawable.placeholder_property)
                    .centerCrop()
                    .into(holder.imageView);
        } else if (image instanceof Uri) {
            // Load local Uri
            Glide.with(context)
                    .load((Uri) image)
                    .placeholder(R.drawable.placeholder_property)
                    .error(R.drawable.placeholder_property)
                    .centerCrop()
                    .into(holder.imageView);
        }
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        ImageViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.propertyImage);
        }
    }
} 