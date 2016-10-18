package ssu.sel.smartdiary;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import ssu.sel.smartdiary.model.UserProfile;

public class ProfileActivity extends SignupActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ((TextView)actionBarView.findViewById(R.id.tvActionBarTitle)).setText("Profile");
        btnConfirm.setText("Update");
        edtUserId.setInputType(InputType.TYPE_NULL);
        edtUserId.setClickable(false);
        edtUserId.setFocusable(false);

        UserProfile profile = UserProfile.getUserProfile();
        edtUserId.setText(profile.getUserID());
        edtPassword.setText(profile.getPassword());
        edtPasswordConfirm.setText(profile.getPassword());
        edtUserName.setText(profile.getUserName());
        edtBirthday.setText(GlobalUtils.DIARY_DATE_FORMAT.format(profile.getBirthday().getTime()));
        radioGender.clearCheck();
        if(profile.getGender().equals("female")) {
            ((RadioButton)findViewById(R.id.radioGenderFemale)).setChecked(true);
        } else {
            ((RadioButton)findViewById(R.id.radioGenderMale)).setChecked(true);
        }
    }

    @Override
    protected void setModals() {
        super.setModals();
        birthdayCal = (Calendar)UserProfile.getUserProfile().getBirthday().clone();
    }

    @Override
    public void onConfirmButtonClick(View v) {
        //to update profile
    }
}
