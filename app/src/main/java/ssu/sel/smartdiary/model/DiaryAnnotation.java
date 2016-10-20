package ssu.sel.smartdiary.model;

/**
 * Created by hanter on 2016. 10. 20..
 */

public class DiaryAnnotation extends DiaryContext {
    public DiaryAnnotation(long contextId, String contextType, String subType, String value) {
        super(contextId, contextType, subType, value);
    }

    public DiaryAnnotation(long contextId, String value) {
        super(contextId, CONTEXT_TYPE_ANNOTATION, "", value);
    }
}
