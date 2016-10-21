package ssu.sel.smartdiary;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import ssu.sel.smartdiary.model.UserProfile;
import ssu.sel.smartdiary.network.JsonRestConnector;

import static ssu.sel.smartdiary.MainActivity.rootMainActivity;

public class ProfileActivity extends SignupActivity {
    private Button btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        btnLogout = (Button)findViewById(R.id.btnProfileCancel);
        btnLogout.setVisibility(View.VISIBLE);
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserProfile.removeUserProfile();
                Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                ProfileActivity.this.finish();
                if (rootMainActivity!=null) rootMainActivity.finish();
            }
        });

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
        edtEmail.setText(profile.getEmail());
        edtPhone.setText(profile.getPhone());
        radioGender.clearCheck();

        selGender = profile.getGender();
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
        dlgDatePicker = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                birthdayCal.set(year, month, dayOfMonth);
                edtBirthday.setText(GlobalUtils.DIARY_DATE_FORMAT.format(birthdayCal.getTime()));
            }
        }, birthdayCal.get(Calendar.YEAR), birthdayCal.get(Calendar.MONTH),
                birthdayCal.get(Calendar.DAY_OF_MONTH));
    }

    @Override
    protected void setConnector() {
        profileConnector = new JsonRestConnector("user", "PUT",
                new JsonRestConnector.OnConnectListener() {
                    @Override
                    public void onDone(JSONObject resJson) {
                        showProgress(false);
                        if (resJson != null) {
                            Log.d("Profile - JSON", resJson.toString());

                            try {
                                Boolean success = resJson.getBoolean("update_user");
                                if (success) {
                                    //update user profile
                                    UserProfile.setUserProfile(edtUserId.getText().toString(),
                                            edtPassword.getText().toString(),
                                            edtUserName.getText().toString(),
                                            birthdayCal, selGender,
                                            edtEmail.getText().toString(),
                                            edtPhone.getText().toString());
                                    openAlertModal("Profile is successfully updated.");
                                } else {
                                    openAlertModal("Profile is not updated.");
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                openAlertModal("Profile update error");
                            }
                        } else {
                            Log.d("Signup - Json", "No response");
                            openAlertModal("There is no repsonse...");
                        }
                    }

                    @Override
                    public void onCancelled() {
                        showProgress(false);
                    }
                });
    }
}
