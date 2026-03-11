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

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.ViewHolder> {

    private final Context context;
    private final List<Message> messages;

    public MessagesAdapter(Context context, List<Message> messages) {
        this.context = context;
        this.messages = messages;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_message, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Message msg = messages.get(position);

        // Format message text
        String displayText = msg.getText();

        if (msg.isDeleted()) {
            holder.tvMessage.setTextColor(context.getResources().getColor(android.R.color.holo_red_light));
            holder.tvDeleted.setVisibility(View.VISIBLE);
            holder.tvDeleted.setText("🚫 This message was deleted");
        } else {
            holder.tvMessage.setTextColor(context.getResources().getColor(android.R.color.black));
            holder.tvDeleted.setVisibility(View.GONE);
        }

        holder.tvMessage.setText(displayText);

        // Format time
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault());
        holder.tvTime.setText(sdf.format(new Date(msg.getTimestamp())));
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage, tvTime, tvDeleted;

        ViewHolder(View view) {
            super(view);
            tvMessage = view.findViewById(R.id.tvMessage);
            tvTime = view.findViewById(R.id.tvTime);
            tvDeleted = view.findViewById(R.id.tvDeleted);
        }
    }
}
