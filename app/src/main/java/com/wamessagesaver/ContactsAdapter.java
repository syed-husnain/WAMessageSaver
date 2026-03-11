package com.wamessagesaver;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ViewHolder> {

    private final Context context;
    private final List<String> senders;
    private final DatabaseHelper db;
    private final OnContactClickListener listener;

    public interface OnContactClickListener {
        void onContactClick(String sender);
        void onContactLongClick(String sender);
    }

    public ContactsAdapter(Context context, List<String> senders, DatabaseHelper db, OnContactClickListener listener) {
        this.context = context;
        this.senders = senders;
        this.db = db;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_contact, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String sender = senders.get(position);
        holder.tvName.setText(sender);

        // Avatar initials
        if (!sender.isEmpty()) {
            holder.tvAvatar.setText(String.valueOf(sender.charAt(0)).toUpperCase());
        }

        // Last message preview
        Message lastMsg = db.getLastMessage(sender);
        if (lastMsg != null) {
            String preview = lastMsg.getText();
            if (lastMsg.isDeleted()) {
                preview = "🚫 " + preview;
            }
            holder.tvPreview.setText(preview);

            // Time
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            holder.tvTime.setText(sdf.format(new Date(lastMsg.getTimestamp())));
        }

        // Message count badge
        int total = db.getMessageCount(sender);
        int deleted = db.getDeletedCount(sender);
        String badge = total + " msgs";
        if (deleted > 0) {
            badge += " • " + deleted + " deleted";
        }
        holder.tvCount.setText(badge);

        holder.itemView.setOnClickListener(v -> listener.onContactClick(sender));
        holder.itemView.setOnLongClickListener(v -> {
            listener.onContactLongClick(sender);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return senders.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvAvatar, tvName, tvPreview, tvTime, tvCount;

        ViewHolder(View view) {
            super(view);
            tvAvatar = view.findViewById(R.id.tvAvatar);
            tvName = view.findViewById(R.id.tvName);
            tvPreview = view.findViewById(R.id.tvPreview);
            tvTime = view.findViewById(R.id.tvTime);
            tvCount = view.findViewById(R.id.tvCount);
        }
    }
}
