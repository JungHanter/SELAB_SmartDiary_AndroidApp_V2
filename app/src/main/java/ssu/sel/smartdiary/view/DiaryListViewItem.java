package ssu.sel.smartdiary.view;

/**
 * Created by hanter on 16. 10. 5..
 */
public class DiaryListViewItem {
    private int diaryID;
    private String title;
    private String date;
    private String content;

    public DiaryListViewItem(int diaryID, String title, String date, String content) {
        this.diaryID = diaryID;
        this.title = title;
        this.date = date;
        this.content = content;
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
}
