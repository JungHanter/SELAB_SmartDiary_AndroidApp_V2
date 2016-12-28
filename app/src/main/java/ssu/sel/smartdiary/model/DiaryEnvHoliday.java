package ssu.sel.smartdiary.model;

/**
 * Created by hanter on 2016. 10. 20..
 */

public class DiaryEnvHoliday extends DiaryEnvContext {
    public DiaryEnvHoliday(long contextId, String type, String value) {
        super(contextId, type, value);
    }

    public DiaryEnvHoliday(long contextId, String value) {
        super(contextId, TYPE_ENV_HOLIDAY, value);
    }
}
