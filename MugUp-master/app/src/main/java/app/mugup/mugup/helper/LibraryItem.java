package app.mugup.mugup.helper;

public class LibraryItem {
    String semester, bookName, authorName, bookCoverUrl, bookUrl, expiry;

    public LibraryItem(String semester, String bookName, String authorName, String bookCoverUrl, String bookUrl, String expiry) {
        this.semester = semester;
        this.bookName = bookName;
        this.authorName = authorName;
        this.bookCoverUrl = bookCoverUrl;
        this.bookUrl = bookUrl;
        this.expiry = expiry;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public String getBookName() {
        return bookName;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getBookCoverUrl() {
        return bookCoverUrl;
    }

    public void setBookCoverUrl(String bookCoverUrl) {
        this.bookCoverUrl = bookCoverUrl;
    }

    public String getBookUrl() {
        return bookUrl;
    }

    public void setBookUrl(String bookUrl) {
        this.bookUrl = bookUrl;
    }

    public String getExpiry() {
        return expiry;
    }

    public void setExpiry(String expiry) {
        this.expiry = expiry;
    }
}
