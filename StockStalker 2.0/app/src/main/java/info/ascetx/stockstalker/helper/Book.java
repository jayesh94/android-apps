package info.ascetx.stockstalker.helper;

/**
 * Created by JAYESH on 24-03-2017.
 */

public class Book {
    private String title;
    private String author;
    private String name;
    public Book(String s, String s1, String n) {
        this.title=s;
        this.author=s1;
        this.name = n;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }
}
