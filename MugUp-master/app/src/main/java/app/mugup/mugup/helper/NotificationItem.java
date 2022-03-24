package app.mugup.mugup.helper;

public class NotificationItem {
    private String topic, title, message, url, ts;

    public NotificationItem(String topic, String title, String message, String url, String ts) {
        this.topic = topic;
        this.title = title;
        this.message = message;
        this.url = url;
        this.ts = ts;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTs() {
        return ts;
    }

    public void setTs(String ts) {
        this.ts = ts;
    }
}
