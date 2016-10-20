package ssu.sel.smartdiary.model;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;

/**
 * Created by hanter on 16. 10. 7..
 */
public class Diary {
    private int diaryID = -1;
    private String title;
    private Calendar date;
    private String content;
    private ArrayList<DiaryContext> diaryContexts = null;

    public Diary(int diaryID, String title, Calendar date, String content) {
        this.diaryID = diaryID;
        this.title = title;
        this.date = date;
        this.content = content;
        this.diaryContexts = new ArrayList<>();
    }

    public Diary(int diaryID, String title, Calendar date, String content,
                 Collection<DiaryContext> diaryContexts) {
        this.diaryID = diaryID;
        this.title = title;
        this.date = date;
        this.content = content;
        this.diaryContexts = new ArrayList<>();
        this.diaryContexts.addAll(diaryContexts);
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

    public ArrayList<DiaryContext> getDiaryContexts() {
        return diaryContexts;
    }

    public DiaryContext getDiaryContext(int index) {
        return diaryContexts.get(index);
    }

    public void addDiaryContext(DiaryContext diaryContext) {
        diaryContexts.add(diaryContext);
    }

    public void addDiaryContexts(Collection<DiaryContext> diaryContexts) {
        this.diaryContexts.addAll(diaryContexts);
    }
}
