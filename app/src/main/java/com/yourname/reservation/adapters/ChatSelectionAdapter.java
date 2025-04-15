package com.yourname.reservation.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.yourname.reservation.R;
import com.yourname.reservation.models.ChatContact;

import java.util.List;

public class ChatSelectionAdapter extends RecyclerView.Adapter<ChatSelectionAdapter.ContactViewHolder> {
    
    private final Context context;
    private final List<ChatContact> contacts;
    private final OnChatContactClickListener listener;
    
    public interface OnChatContactClickListener {
        void onChatContactClick(ChatContact contact);
    }
    
    public ChatSelectionAdapter(Context context, List<ChatContact> contacts, OnChatContactClickListener listener) {
        this.context = context;
        this.contacts = contacts;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_chat_contact, parent, false);
        return new ContactViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        ChatContact contact = contacts.get(position);
        
        holder.nameTextView.setText(contact.getName());
        holder.propertyTextView.setText(contact.getPropertyTitle());
        
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onChatContactClick(contact);
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return contacts.size();
    }
    
    static class ContactViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        TextView propertyTextView;
        
        public ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.contactNameText);
            propertyTextView = itemView.findViewById(R.id.propertyTitleText);
        }
    }
} 