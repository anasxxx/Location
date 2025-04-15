package com.yourname.reservation.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.yourname.reservation.R;
import com.yourname.reservation.models.Chat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MessageViewHolder> {
    private static final String TAG = "ChatAdapter";
    private static final int VIEW_TYPE_MESSAGE_SENT = 1;
    private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;

    private final List<Chat> messages;
    private final String currentUserId;
    private final SimpleDateFormat timeFormat;

    public ChatAdapter(List<Chat> messages, String currentUserId) {
        this.messages = messages;
        this.currentUserId = currentUserId;
        this.timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == VIEW_TYPE_MESSAGE_SENT) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_sent, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_received, parent, false);
        }
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        try {
            Chat message = messages.get(position);
            
            // Set message text
            String messageText = message.getMessage();
            if (messageText == null || messageText.isEmpty()) {
                messageText = "(Message vide)";
            }
            holder.messageText.setText(messageText);
            
            // Format and set time
            try {
                String time = timeFormat.format(new Date(message.getTimestamp()));
                holder.timeText.setText(time);
            } catch (Exception e) {
                Log.e(TAG, "Error formatting time", e);
                holder.timeText.setText("--:--");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error binding message", e);
        }
    }

    @Override
    public int getItemCount() {
        return messages != null ? messages.size() : 0;
    }

    @Override
    public int getItemViewType(int position) {
        Chat message = messages.get(position);
        if (message.getSenderId() != null && message.getSenderId().equals(currentUserId)) {
            return VIEW_TYPE_MESSAGE_SENT;
        } else {
            return VIEW_TYPE_MESSAGE_RECEIVED;
        }
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;
        TextView timeText;

        MessageViewHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.messageText);
            timeText = itemView.findViewById(R.id.timeText);
        }
    }
}
