package info.ascetx.stockstalker.helper;

/**
 * Created by JAYESH on 13-12-2018.
 */

public class News {
    private String planetName;
    private int distanceFromSun;
    private int gravity;
    private int diameter;

    private String datetime;
    private String headline;
    private String source;
    private String url;
    private String summary;
    private String image;

    public News(String datetime, String headline, String source, String url, String summary, String image) {
        this.datetime = datetime;
        this.headline = headline;
        this.source = source;
        this.url = url;
        this.summary = summary;
        this.image = image;
    }

    public String getDatetime() {
        return datetime;
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }

    public String getHeadline() {
        return headline;
    }

    public void setHeadline(String headline) {
        this.headline = headline;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

}
