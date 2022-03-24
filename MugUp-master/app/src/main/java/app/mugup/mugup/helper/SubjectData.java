package app.mugup.mugup.helper;

public class SubjectData
{
    private String name;
    private String id;
    private String course_id;
    private String semester_id;
    private String book_cover_url;
    private String author_name;
    private String book_name;
    private String book_url;
    private String book_sample_url;
    private String book_summary;
    private String price;
    private String views;
    public SubjectData(){}
    public SubjectData(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }
    public String getId() {
        return id;
    }
    public String getAuthorName() {
        return author_name;
    }
    public String getCourseId() {
        return course_id;
    }
    public String getBookCover() {
        return book_cover_url;
    }
    public String getBookName() {
        return book_name;
    }
    public String getBookSampleUrl() {
        return book_sample_url;
    }
    public String getBookSummary() {
        return book_summary;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getPrice() {
        return price;
    }
    public String getSemesterId() {
        return semester_id;
    }
}