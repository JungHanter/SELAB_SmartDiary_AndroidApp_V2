package ssu.sel.smartdiary.model;

/**
 * Created by hanter on 2016. 10. 20..
 */

public class DiaryEnvPlace extends DiaryEnvContext {

    public DiaryEnvPlace(long contextId, String type, String value) {
        super(contextId, type, value);
    }

    public DiaryEnvPlace(long contextId, String value) {
        super(contextId, TYPE_ENV_PLACE, value);
    }
}
