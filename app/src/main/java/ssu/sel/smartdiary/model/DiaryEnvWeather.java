package ssu.sel.smartdiary.model;

/**
 * Created by hanter on 2016. 10. 20..
 */

public class DiaryEnvWeather extends DiaryEnvContext {
    public DiaryEnvWeather(long contextId, String type, String value) {
        super(contextId, type, value);
    }

    public DiaryEnvWeather(long contextId, String value) {
        super(contextId, TYPE_ENV_WEATHER, value);
    }
}
