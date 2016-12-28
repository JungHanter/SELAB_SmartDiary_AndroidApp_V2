package ssu.sel.smartdiary.model;

/**
 * Created by hanter on 2016. 10. 20..
 */

public class DiaryEnvEvent extends DiaryEnvContext {
    public DiaryEnvEvent(long contextId, String type, String value) {
        super(contextId, type, value);
    }

    public DiaryEnvEvent(long contextId, String value) {
        super(contextId, TYPE_ENV_EVENT, value);
    }
}
