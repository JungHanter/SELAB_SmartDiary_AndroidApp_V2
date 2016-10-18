package ssu.sel.smartdiary.model;

import java.util.Calendar;

/**
 * Created by hanter on 16. 10. 7..
 */
public class Diary {
    private int diaryID = -1;
    private String title;
    private Calendar date;
    private String content;
    private String annotation;
    private String location;

    public Diary(String title, Calendar date, String content) {
        this.title = title;
        this.date = date;
        this.content = content;
        this.annotation = "";
        this.location = "";
    }

    public Diary(String title, Calendar date, String content, String annotation) {
        this.title = title;
        this.date = date;
        this.content = content;
        this.annotation = annotation;
        this.location = "";
    }

    public Diary(String title, Calendar date, String content, String annotation, String location) {
        this.title = title;
        this.date = date;
        this.content = content;
        this.annotation = annotation;
        this.location = location;
    }

    public Diary(int diaryID, String title, Calendar date, String content, String annotation, String location) {
        this.diaryID = diaryID;
        this.title = title;
        this.date = date;
        this.content = content;
        this.annotation = annotation;
        this.location = location;
    }

    public int getDiaryID() {
        return diaryID;
    }

    public void setDiaryID(int diaryID) {
        this.diaryID = diaryID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Calendar getDate() {
        return date;
    }

    public void setDate(Calendar date) {
        this.date = date;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAnnotation() {
        return annotation;
    }

    public void setAnnotation(String annotation) {
        this.annotation = annotation;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
