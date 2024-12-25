package model.entities;

import java.util.UUID;

public class Note {

    private String title;
    private String date;
    private String content;
    private String id;

    public Note(String title, String content, String date, String id) {
        this.title = title;
        this.id = id;
        this.date = date;
        this.content = content;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
