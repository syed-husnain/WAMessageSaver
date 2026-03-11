package com.wamessagesaver;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "wa_messages.db";
    private static final int DB_VERSION = 1;

    private static final String TABLE_MESSAGES = "messages";
    private static final String COL_ID = "id";
    private static final String COL_SENDER = "sender";
    private static final String COL_TEXT = "text";
    private static final String COL_TIMESTAMP = "timestamp";
    private static final String COL_DELETED = "is_deleted";
    private static final String COL_MEDIA_TYPE = "media_type";

    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_MESSAGES + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_SENDER + " TEXT NOT NULL, "
                + COL_TEXT + " TEXT, "
                + COL_TIMESTAMP + " INTEGER NOT NULL, "
                + COL_DELETED + " INTEGER DEFAULT 0, "
                + COL_MEDIA_TYPE + " TEXT DEFAULT 'text'"
                + ")";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGES);
        onCreate(db);
    }

    // Save a new message
    public long saveMessage(Message message) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_SENDER, message.getSender());
        values.put(COL_TEXT, message.getText());
        values.put(COL_TIMESTAMP, message.getTimestamp());
        values.put(COL_DELETED, message.isDeleted() ? 1 : 0);
        values.put(COL_MEDIA_TYPE, message.getMediaType());
        return db.insert(TABLE_MESSAGES, null, values);
    }

    // Check if duplicate message exists (same sender + text within 2 seconds)
    public boolean isDuplicate(String sender, String text, long timestamp) {
        SQLiteDatabase db = getReadableDatabase();
        String query = "SELECT COUNT(*) FROM " + TABLE_MESSAGES
                + " WHERE " + COL_SENDER + "=? AND " + COL_TEXT + "=?"
                + " AND ABS(" + COL_TIMESTAMP + " - " + timestamp + ") < 2000";
        Cursor cursor = db.rawQuery(query, new String[]{sender, text});
        boolean exists = false;
        if (cursor.moveToFirst()) {
            exists = cursor.getInt(0) > 0;
        }
        cursor.close();
        return exists;
    }

    // Get all unique senders (for contacts list)
    public List<String> getAllSenders() {
        List<String> senders = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        String query = "SELECT DISTINCT " + COL_SENDER + ", MAX(" + COL_TIMESTAMP + ") as last_time"
                + " FROM " + TABLE_MESSAGES
                + " GROUP BY " + COL_SENDER
                + " ORDER BY last_time DESC";
        Cursor cursor = db.rawQuery(query, null);
        while (cursor.moveToNext()) {
            senders.add(cursor.getString(0));
        }
        cursor.close();
        return senders;
    }

    // Get message count per sender
    public int getMessageCount(String sender) {
        SQLiteDatabase db = getReadableDatabase();
        String query = "SELECT COUNT(*) FROM " + TABLE_MESSAGES + " WHERE " + COL_SENDER + "=?";
        Cursor cursor = db.rawQuery(query, new String[]{sender});
        int count = 0;
        if (cursor.moveToFirst()) count = cursor.getInt(0);
        cursor.close();
        return count;
    }

    // Get deleted message count per sender
    public int getDeletedCount(String sender) {
        SQLiteDatabase db = getReadableDatabase();
        String query = "SELECT COUNT(*) FROM " + TABLE_MESSAGES
                + " WHERE " + COL_SENDER + "=? AND " + COL_DELETED + "=1";
        Cursor cursor = db.rawQuery(query, new String[]{sender});
        int count = 0;
        if (cursor.moveToFirst()) count = cursor.getInt(0);
        cursor.close();
        return count;
    }

    // Get all messages for a specific sender
    public List<Message> getMessagesForSender(String sender) {
        List<Message> messages = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_MESSAGES,
                null,
                COL_SENDER + "=?",
                new String[]{sender},
                null, null,
                COL_TIMESTAMP + " ASC");
        while (cursor.moveToNext()) {
            Message msg = new Message();
            msg.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COL_ID)));
            msg.setSender(cursor.getString(cursor.getColumnIndexOrThrow(COL_SENDER)));
            msg.setText(cursor.getString(cursor.getColumnIndexOrThrow(COL_TEXT)));
            msg.setTimestamp(cursor.getLong(cursor.getColumnIndexOrThrow(COL_TIMESTAMP)));
            msg.setDeleted(cursor.getInt(cursor.getColumnIndexOrThrow(COL_DELETED)) == 1);
            msg.setMediaType(cursor.getString(cursor.getColumnIndexOrThrow(COL_MEDIA_TYPE)));
            messages.add(msg);
        }
        cursor.close();
        return messages;
    }

    // Get last message for sender (for preview)
    public Message getLastMessage(String sender) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_MESSAGES,
                null,
                COL_SENDER + "=?",
                new String[]{sender},
                null, null,
                COL_TIMESTAMP + " DESC",
                "1");
        Message msg = null;
        if (cursor.moveToFirst()) {
            msg = new Message();
            msg.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COL_ID)));
            msg.setSender(cursor.getString(cursor.getColumnIndexOrThrow(COL_SENDER)));
            msg.setText(cursor.getString(cursor.getColumnIndexOrThrow(COL_TEXT)));
            msg.setTimestamp(cursor.getLong(cursor.getColumnIndexOrThrow(COL_TIMESTAMP)));
            msg.setDeleted(cursor.getInt(cursor.getColumnIndexOrThrow(COL_DELETED)) == 1);
            msg.setMediaType(cursor.getString(cursor.getColumnIndexOrThrow(COL_MEDIA_TYPE)));
        }
        cursor.close();
        return msg;
    }

    // Delete all messages for a sender
    public void deleteMessagesForSender(String sender) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_MESSAGES, COL_SENDER + "=?", new String[]{sender});
    }

    // Delete all messages
    public void deleteAllMessages() {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_MESSAGES, null, null);
    }
}
