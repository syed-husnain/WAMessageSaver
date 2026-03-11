package com.wamessagesaver;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MainActivity extends AppCompatActivity implements ContactsAdapter.OnContactClickListener {

    private RecyclerView recyclerView;
    private ContactsAdapter adapter;
    private TextView emptyView;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = DatabaseHelper.getInstance(this);

        recyclerView = findViewById(R.id.recyclerView);
        emptyView = findViewById(R.id.emptyView);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Check notification permission on first launch
        if (!isNotificationServiceEnabled()) {
            showPermissionDialog();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadContacts();
    }

    private void loadContacts() {
        List<String> senders = db.getAllSenders();

        if (senders.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
            adapter = new ContactsAdapter(this, senders, db, this);
            recyclerView.setAdapter(adapter);
        }
    }

    @Override
    public void onContactClick(String sender) {
        Intent intent = new Intent(this, ChatDetailActivity.class);
        intent.putExtra("sender", sender);
        startActivity(intent);
    }

    @Override
    public void onContactLongClick(String sender) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Chat")
                .setMessage("Delete all saved messages from " + sender + "?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    db.deleteMessagesForSender(sender);
                    loadContacts();
                    Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 1, 0, "Clear All").setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        menu.add(0, 2, 0, "Permission Settings").setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == 1) {
            new AlertDialog.Builder(this)
                    .setTitle("Clear All Messages")
                    .setMessage("Are you sure you want to delete ALL saved messages?")
                    .setPositiveButton("Delete All", (dialog, which) -> {
                        db.deleteAllMessages();
                        loadContacts();
                        Toast.makeText(this, "All messages cleared", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            return true;
        } else if (item.getItemId() == 2) {
            openNotificationSettings();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean isNotificationServiceEnabled() {
        String flat = Settings.Secure.getString(getContentResolver(), "enabled_notification_listeners");
        if (!TextUtils.isEmpty(flat)) {
            String[] names = flat.split(":");
            for (String name : names) {
                ComponentName cn = ComponentName.unflattenFromString(name);
                if (cn != null && getPackageName().equals(cn.getPackageName())) {
                    return true;
                }
            }
        }
        return false;
    }

    private void showPermissionDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Permission Required")
                .setMessage("WA Message Saver needs 'Notification Access' permission to save WhatsApp messages.\n\nClick OK to open settings, then find 'WA Message Saver' and enable it.")
                .setPositiveButton("Open Settings", (dialog, which) -> openNotificationSettings())
                .setNegativeButton("Later", null)
                .setCancelable(false)
                .show();
    }

    private void openNotificationSettings() {
        startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));
    }
}
