package ssu.sel.smartdiary.model;

import java.util.Calendar;

/**
 * Created by hanter on 16. 9. 22..
 */
public class UserProfile {
    private static UserProfile globalInstance = null;

    private String userID;
    private String password;
    private String userName;
    private Calendar birthday;
    private String gender;

    private UserProfile(){}

    public static void setUserProfile(String userID, String password, String userName,
                                      Calendar birthday, String gender) {
        if (globalInstance == null) {
            globalInstance = new UserProfile();
        }
        globalInstance.userID = userID;
        globalInstance.password = password;
        globalInstance.userName = userName;
        globalInstance.birthday = birthday;
        globalInstance.gender = gender;
    }

    public static void setUserProfile(String userID, String password, String userName,
                                      long birthday, String gender) {
        Calendar birthdayCalendar = Calendar.getInstance();
        birthdayCalendar.setTimeInMillis(birthday);
        setUserProfile(userID, password, userName, birthdayCalendar, gender);
    }

    public static UserProfile getUserProfile() {
        return globalInstance;
    }

    public String getUserID() {
        return userID;
    }

    public String getPassword() {
        return password;
    }

    public String getUserName() {
        return userName;
    }

    public Calendar getBirthday() {
        return birthday;
    }

    public String getGender() {
        return gender;
    }
}
