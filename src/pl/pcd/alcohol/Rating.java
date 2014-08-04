package pl.pcd.alcohol;

public class Rating {
    public String author;
    public String content;
    public String date;
    public int rating;

    public Rating(String author, String content, String date, int rating) {
        this.author = author;
        this.content = content;
        this.date = date;
        this.rating = rating;
    }
}