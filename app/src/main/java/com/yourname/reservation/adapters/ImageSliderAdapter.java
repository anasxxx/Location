package com.yourname.reservation.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.yourname.reservation.R;

import java.util.ArrayList;
import java.util.List;

public class ImageSliderAdapter extends RecyclerView.Adapter<ImageSliderAdapter.ImageViewHolder> {
    private static final String TAG = "ImageSliderAdapter";
    
    private final Context context;
    private List<String> images;
    private final RequestOptions requestOptions;

    public ImageSliderAdapter(Context context, List<String> images) {
        this.context = context;
        this.images = images != null ? new ArrayList<>(images) : new ArrayList<>();
        this.requestOptions = new RequestOptions()
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .placeholder(R.drawable.placeholder_property)
            .error(R.drawable.error_property)
            .centerCrop();
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_image_slider, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        String imageUrl = images.get(position);
        Log.d(TAG, "Loading image at position " + position + ": " + imageUrl);
        
        if (imageUrl == null || imageUrl.isEmpty() || "placeholder".equals(imageUrl)) {
            Log.d(TAG, "Using placeholder image");
            holder.imageView.setImageResource(R.drawable.placeholder_property);
            return;
        }

        try {
            Log.d(TAG, "Loading image with Glide: " + imageUrl);
            Glide.with(context)
                .load(imageUrl)
                .apply(requestOptions)
                .into(holder.imageView);
        } catch (Exception e) {
            Log.e(TAG, "Error loading image: " + e.getMessage(), e);
            holder.imageView.setImageResource(R.drawable.error_property);
        }
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    public void updateImages(List<String> newImages) {
        Log.d(TAG, "Updating images. New size: " + (newImages != null ? newImages.size() : 0));
        this.images = newImages != null ? new ArrayList<>(newImages) : new ArrayList<>();
        notifyDataSetChanged();
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        final ImageView imageView;

        ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
        }
    }
} 