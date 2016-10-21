package ssu.sel.smartdiary.model;

/**
 * Created by hanter on 2016. 10. 20..
 */

public class DiaryWeather extends DiaryEnvironment {
    public DiaryWeather(long contextId, String contextType, String subType, String value) {
        super(contextId, contextType, subType, value);
    }

    public DiaryWeather(long contextId, String subType, String value) {
        super(contextId, subType, value);
    }

    public DiaryWeather(long contextId, String value) {
        super(contextId, SUB_TYPE_ENV_WEATHER, value);
    }
}