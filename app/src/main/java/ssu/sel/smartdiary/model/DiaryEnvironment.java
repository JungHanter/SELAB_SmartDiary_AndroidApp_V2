package ssu.sel.smartdiary.model;

/**
 * Created by hanter on 2016. 10. 20..
 */

public class DiaryEnvironment extends DiaryContext {
    public DiaryEnvironment(long contextId, String contextType, String subType, String value) {
        super(contextId, contextType, subType, value);
    }

    public DiaryEnvironment(long contextId, String subType, String value) {
        super(contextId, CONTEXT_TYPE_ENVIRONMENT, subType, value);
    }
}
