package ssu.sel.smartdiary.model;

import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
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

    public static Diary fromJSON(JSONObject jsonDiary, JSONArray jsonContexts) {
        Diary diary = null;
        try {
            long time = jsonDiary.getLong("created_date");
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(time);

            diary = new Diary(jsonDiary.getInt("audio_diary_id"), jsonDiary.getString("title"),
                    calendar, jsonDiary.getString("content"));

            if (jsonContexts != null) {
                for (int i = 0; i < jsonContexts.length(); i++) {
                    JSONObject jsonContext = jsonContexts.getJSONObject(i);
                    DiaryContext diaryContext = DiaryContext.fromJSON(jsonContext);
                    if (diaryContext != null) {
                        diary.addDiaryContext(diaryContext);
                    }
                }
            }
        } catch (JSONException je) {
            je.printStackTrace();
        }
        return diary;
    }

    public JSONObject toJSON() {
        return null;
    }

    public DiaryContext getAnnotation() {
        for (DiaryContext context : diaryContexts) {
            if(context.getClass() == DiaryAnnotation.class ||
                    context.getContextType().equals(DiaryContext.CONTEXT_TYPE_ANNOTATION)) {
                return context;
            }
        }
        return null;
    }

    public ArrayList<DiaryContext> getDiaryContexts(String type) {
        return getDiaryContexts(type, null);
    }

    public ArrayList<DiaryContext> getDiaryContexts(String type, String subType) {
        ArrayList<DiaryContext> searchContexts = new ArrayList<>();
        for (DiaryContext context : diaryContexts) {
            if(context.getContextType().equals(type)) {
                if (subType == null || TextUtils.isEmpty(subType)) {
                    searchContexts.add(context);
                } else if (context.getSubType().equals(subType)) {
                    searchContexts.add(context);
                }
            }
        }
        return searchContexts;
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
