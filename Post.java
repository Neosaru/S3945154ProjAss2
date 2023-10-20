package application;

public class Post{
    private int id;
    private String content;
    private String author;
    private int likes;
    private int shares;
    private String dateTime;

    public Post(){
    }
    
    public Post(int id, String content, String author, int likes, int shares, String dateTime) {
        this.id = id;
        this.content = content;
        this.author = author;
        this.likes = likes;
        this.shares = shares;
        this.dateTime = dateTime;
    }
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    
    public int getShares() {
		return shares;
	}

	public int getLikes() {
		return likes;
	}

    
    @Override
    public String toString() {
        return String.format("%d | %s | %s | %d | %d | %s", id, content, author, likes, shares, dateTime);
    }

	
}