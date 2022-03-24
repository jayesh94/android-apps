package app.mugup.mugup.database.model;

public class Notifications {
    public static final String TABLE_NAME = "notifications";

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NOTIFICATION_TITLE = "notification_title";
    public static final String COLUMN_NOTIFICATION_TEXT = "notification_text";
    public static final String COLUMN_TIMESTAMP = "timestamp";
    public static final String COLUMN_VIEWED = "viewed";

    private int id;
    private String notification_title;
    private String notification_text;
    private String timestamp;
    private String viewed;



    // Create table SQL query
    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + "("
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_NOTIFICATION_TITLE + " TEXT,"
                    + COLUMN_NOTIFICATION_TEXT + " TEXT NOT NULL UNIQUE,"
                    + COLUMN_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP"
                    + COLUMN_VIEWED + " INTEGER DEFAULT 0"
                    + ")";

    public Notifications() {
    }

    public Notifications(int id, String notification_title, String notification_text, String timestamp) {
        this.id = id;
        this.notification_title = notification_title;
        this.notification_text = notification_text;
        this.timestamp = timestamp;
    }

    public String getViewed() {
        return viewed;
    }

    public void setViewed(String viewed) {
        this.viewed = viewed;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNotification_title() {
        return notification_title;
    }

    public void setNotification_title(String notification_title) {
        this.notification_title = notification_title;
    }

    public String getNotification_text() {
        return notification_text;
    }

    public void setNotification_text(String notification_text) {
        this.notification_text = notification_text;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
