package com.wamessagesaver;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import androidx.core.app.NotificationCompat;

public class NotificationService extends NotificationListenerService {

    private static final String TAG = "WAMessageSaver";
    private static final String WHATSAPP_PACKAGE = "com.whatsapp";
    private static final String WHATSAPP_BUSINESS_PACKAGE = "com.whatsapp.w4b";
    private static final String CHANNEL_ID = "wa_saver_channel";
    private static final int NOTIF_ID = 1001;

    private DatabaseHelper db;

    @Override
    public void onCreate() {
        super.onCreate();
        db = DatabaseHelper.getInstance(this);
        createNotificationChannel();
        startForegroundNotification();
        Log.d(TAG, "NotificationService started");
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        if (sbn == null) return;

        String packageName = sbn.getPackageName();
        if (!WHATSAPP_PACKAGE.equals(packageName) && !WHATSAPP_BUSINESS_PACKAGE.equals(packageName)) {
            return;
        }

        Notification notification = sbn.getNotification();
        if (notification == null) return;

        Bundle extras = notification.extras;
        if (extras == null) return;

        // Extract sender name
        CharSequence titleSeq = extras.getCharSequence(Notification.EXTRA_TITLE);
        if (titleSeq == null) return;
        String sender = titleSeq.toString().trim();

        // Skip group summaries and system notifications
        if (sender.isEmpty() || sender.equals("WhatsApp") || sender.equals("WhatsApp Business")) return;

        // Extract message text
        CharSequence textSeq = extras.getCharSequence(Notification.EXTRA_TEXT);
        String messageText = "";

        if (textSeq != null) {
            messageText = textSeq.toString().trim();
        }

        // Check for missed call or media notifications
        String mediaType = "text";
        if (messageText.contains("📷") || messageText.equalsIgnoreCase("Photo") || messageText.equalsIgnoreCase("image")) {
            mediaType = "image";
            if (messageText.isEmpty()) messageText = "📷 Photo";
        } else if (messageText.contains("🎥") || messageText.equalsIgnoreCase("Video")) {
            mediaType = "video";
            if (messageText.isEmpty()) messageText = "🎥 Video";
        } else if (messageText.contains("🎵") || messageText.equalsIgnoreCase("Audio")) {
            mediaType = "audio";
            if (messageText.isEmpty()) messageText = "🎵 Audio";
        } else if (messageText.contains("📄") || messageText.equalsIgnoreCase("Document")) {
            mediaType = "document";
            if (messageText.isEmpty()) messageText = "📄 Document";
        } else if (messageText.equalsIgnoreCase("Sticker")) {
            mediaType = "sticker";
            messageText = "🎭 Sticker";
        } else if (messageText.contains("Missed voice call") || messageText.contains("Missed video call")) {
            mediaType = "call";
        }

        // Skip empty messages
        if (messageText.isEmpty()) return;

        // Skip duplicates
        long now = System.currentTimeMillis();
        if (db.isDuplicate(sender, messageText, now)) return;

        // Save to database
        Message message = new Message(sender, messageText, now);
        message.setMediaType(mediaType);
        db.saveMessage(message);

        Log.d(TAG, "Saved message from: " + sender + " | " + messageText);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        // We don't need to do anything here
        // Messages are already saved when notification was posted
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "WA Message Saver",
                    NotificationManager.IMPORTANCE_MIN
            );
            channel.setDescription("Running in background to save messages");
            channel.setShowBadge(false);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private void startForegroundNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_email)
                .setContentTitle("WA Message Saver")
                .setContentText("Monitoring WhatsApp messages...")
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .setOngoing(true)
                .setSilent(true);

        startForeground(NOTIF_ID, builder.build());
    }
}
