package app.mugup.mugup.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

import app.mugup.mugup.database.model.Notifications;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "notifications_db";


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {

        // create notifications table
        db.execSQL(Notifications.CREATE_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + Notifications.TABLE_NAME);

        // Create tables again
        onCreate(db);
    }

    public long insertNotification(String notification_title, String notification_text) {
        // get writable database as we want to write data
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        // `id` and `timestamp` will be inserted automatically.
        // no need to add them
        values.put(Notifications.COLUMN_NOTIFICATION_TITLE, notification_title);
        values.put(Notifications.COLUMN_NOTIFICATION_TEXT, notification_text);

        // insert row
        long id = db.insert(Notifications.TABLE_NAME, null, values);

        // close db connection
        db.close();

        // return newly inserted row id
        return id;
    }

    public Notifications getNotifications(long id) {
        // get readable database as we are not inserting anything
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(Notifications.TABLE_NAME,
                new String[]{Notifications.COLUMN_ID, Notifications.COLUMN_NOTIFICATION_TITLE, Notifications.COLUMN_NOTIFICATION_TEXT, Notifications.COLUMN_TIMESTAMP},
                Notifications.COLUMN_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);

        if (cursor != null)
            cursor.moveToFirst();

        // prepare notification object
        Notifications notification = new Notifications(
                cursor.getInt(cursor.getColumnIndex(Notifications.COLUMN_ID)),
                cursor.getString(cursor.getColumnIndex(Notifications.COLUMN_NOTIFICATION_TITLE)),
                cursor.getString(cursor.getColumnIndex(Notifications.COLUMN_NOTIFICATION_TEXT)),
                cursor.getString(cursor.getColumnIndex(Notifications.COLUMN_TIMESTAMP)));

        // close the db connection
        cursor.close();

        return notification;
    }

    public List<Notifications> getAllNotifications() {
        List<Notifications> notifications = new ArrayList<>();

        // Select All Query
        String selectQuery = "SELECT  * FROM " + Notifications.TABLE_NAME + " ORDER BY " +
                Notifications.COLUMN_TIMESTAMP + " DESC";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Notifications notification = new Notifications();
                notification.setId(cursor.getInt(cursor.getColumnIndex(Notifications.COLUMN_ID)));
                notification.setNotification_title(cursor.getString(cursor.getColumnIndex(Notifications.COLUMN_NOTIFICATION_TITLE)));
                notification.setNotification_text(cursor.getString(cursor.getColumnIndex(Notifications.COLUMN_NOTIFICATION_TEXT)));
                notification.setTimestamp(cursor.getString(cursor.getColumnIndex(Notifications.COLUMN_TIMESTAMP)));

                notifications.add(notification);
            } while (cursor.moveToNext());
        }

        // close db connection
        db.close();

        // return notifications list
        return notifications;
    }

    public int getNotificationsCount() {
        String countQuery = "SELECT  * FROM " + Notifications.TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);

        int count = cursor.getCount();
        cursor.close();


        // return count
        return count;
    }

    public void deleteNotification(Notifications note) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(Notifications.TABLE_NAME, Notifications.COLUMN_ID + " = ?",
                new String[]{String.valueOf(note.getId())});
        db.close();
    }
}
