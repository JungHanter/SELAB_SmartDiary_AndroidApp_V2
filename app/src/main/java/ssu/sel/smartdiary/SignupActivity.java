package ssu.sel.smartdiary;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.TintContextWrapper;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import ssu.sel.smartdiary.network.JsonRestConnector;

public class SignupActivity extends AppCompatActivity {
    protected AlertDialog dlgAlert = null;

    protected DatePickerDialog dlgDatePicker = null;
    protected Calendar birthdayCal = null;

    protected View actionBarView = null;
    protected EditText edtUserId = null;
    protected EditText edtPassword = null;
    protected EditText edtPasswordConfirm = null;
    protected EditText edtUserName = null;
    protected EditText edtEmail = null;
    protected EditText edtPhone = null;
    protected EditText edtBirthday = null;
    protected RadioGroup radioGender = null;
    protected Button btnConfirm = null;

    protected View viewProfileForm = null;
    protected View viewProfileProgress = null;

    protected JsonRestConnector profileConnector = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBarView = getLayoutInflater().inflate(R.layout.action_bar_center_with_back_button, null);
        ((TextView)actionBarView.findViewById(R.id.tvActionBarTitle)).setText("Sign Up");
        actionBar.setCustomView(actionBarView,
                new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT,
                        ActionBar.LayoutParams.MATCH_PARENT, Gravity.CENTER));
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        setContentView(R.layout.activity_signup);

        edtUserId = (EditText)findViewById(R.id.edtProfileUserId);
        edtPassword = (EditText)findViewById(R.id.edtProfilePassword);
        edtPasswordConfirm = (EditText)findViewById(R.id.edtProfilePasswordConfirm);
        edtUserName = (EditText)findViewById(R.id.edtProfileUserName);
        edtEmail = (EditText)findViewById(R.id.edtProfileEmail);
        edtPhone = (EditText)findViewById(R.id.edtProfilePhone);
        edtBirthday = (EditText)findViewById(R.id.edtProfileBirthday);
        radioGender = (RadioGroup)findViewById(R.id.radioGender);
        onRadioGenderClear(findViewById(R.id.radioGenderMale));
        btnConfirm = (Button)findViewById(R.id.btnProfileConfirm);
        viewProfileForm = findViewById(R.id.viewSignupForm);
        viewProfileProgress = findViewById(R.id.signup_progress);

        setModals();
        setConnector();
    }

    protected void setModals() {
        birthdayCal = Calendar.getInstance();
        dlgDatePicker = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                birthdayCal.set(year, month, dayOfMonth);
                edtBirthday.setText(GlobalUtils.DIARY_DATE_FORMAT.format(birthdayCal.getTime()));
            }
        }, birthdayCal.get(Calendar.YEAR), birthdayCal.get(Calendar.MONTH),
                birthdayCal.get(Calendar.DAY_OF_MONTH));
        edtBirthday.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) dlgDatePicker.show();
            }
        });
        edtBirthday.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dlgDatePicker.show();
            }
        });

        dlgAlert = new AlertDialog.Builder(this).setMessage("Alert!")
                .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dlgAlert.dismiss();
                    }
                }).create();
    }

    protected void openAlertModal(CharSequence msg) {
        dlgAlert.setMessage(msg);
        dlgAlert.show();
    }

    protected void setConnector() {
        profileConnector = new JsonRestConnector("user", "POST",
                new JsonRestConnector.OnConnectListener() {
                    @Override
                    public void onDone(JSONObject resJson) {
                        showProgress(false);
                        if (resJson != null) {
                            Log.d("Signup - JSON", resJson.toString());

                            try {
                                Boolean success = resJson.getBoolean("register");
                                if (success) {
                                    SignupActivity.this.finish();
                                } else {
                                    openAlertModal("Signup failed");
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                openAlertModal("Signup response error");
                            }
                        } else {
                            Log.d("Signup - Json", "No response");
                            openAlertModal("There is no reponse...");
                        }
                    }

                    @Override
                    public void onCancelled() {
                        showProgress(false);
                    }
                });
    }

    protected void attempt() {
        //reset errors
        edtUserId.setError(null);
        edtPassword.setError(null);
        edtPasswordConfirm.setError(null);
        edtUserName.setError(null);
        edtBirthday.setError(null);
        edtPhone.setError(null);
        edtEmail.setError(null);

        String userId = edtUserId.getText().toString();
        String password = edtPassword.getText().toString();
        String passwordConfirm = edtPasswordConfirm.getText().toString();
        String userName = edtUserName.getText().toString();
        long birthday = birthdayCal.getTimeInMillis();
        String phone = edtPhone.getText().toString();
        String email = edtEmail.getText().toString();

        boolean cancel = false;
        View focusView = btnConfirm;

        if (!isEmailValid(email)) {
            edtEmail.setError("This is not email forma");
            focusView = edtEmail;
            cancel = true;
        }

        if (!isPhoneValid(phone)) {
            edtPhone.setError("Phone number is too short");
            focusView = edtPhone;
            cancel = true;
        }

        if (!isBirthdayValid(birthday)) {
            edtBirthday.setError("Birthday is invalid");
            cancel = true;
        }

        if (TextUtils.isEmpty(userName)) {
            edtUserName.setError(getString(R.string.error_field_required));
            focusView = edtUserName;
            cancel = true;
        } else if (!isUserNameValid(userName)) {
            edtUserName.setError("The name is too short");
            focusView = edtUserName;
            cancel = true;
        }

        if (TextUtils.isEmpty(password)) {
            edtPassword.setError(getString(R.string.error_field_required));
            focusView = edtPassword;
            cancel = true;
        } else if (!isPasswordValid(password)) {
            edtPassword.setError(getString(R.string.error_invalid_password));
            focusView = edtPassword;
            cancel = true;
        }

        if (!password.equals(passwordConfirm)) {
            edtPasswordConfirm.setError("Password Confirm is not equal to password");
            focusView = edtPasswordConfirm;
            cancel = true;
        }

        if (TextUtils.isEmpty(userId)) {
            edtUserId.setError(getString(R.string.error_field_required));
            focusView = edtUserId;
            cancel = true;
        } else if (!isUserIdValid(userId)) {
            edtUserId.setError(getString(R.string.error_invalid_id));
            focusView = edtUserId;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            showProgress(true);

            JSONObject json = new JSONObject();
            try {
                json.put("user_id", userId);
                json.put("password", password);
                json.put("name", userName);
                json.put("gender", selGender);
                json.put("birthday", birthdayCal.getTimeInMillis());
                json.put("phone", phone);
                json.put("email", email);
            } catch (Exception e) {
                e.printStackTrace();
            }
            profileConnector.request(json);
        }
    }

//    protected void attemptDone(String userId, String password, String userName,
//                               String gender, long birthday, String phone, String email) {
//        JSONObject json = new JSONObject();
//        try {
//            json.put("user_id", userId);
//            json.put("password", password);
//            json.put("name", userName);
//            json.put("gender", selGender);
//            json.put("birthday", birthdayCal.getTimeInMillis());
//            json.put("phone", phone);
//            json.put("email", email);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        profileConnector.request(json);
//    }

    private boolean isUserIdValid(String userId) {
        return userId.length() >= 3;
    }

    private boolean isPasswordValid(String password) {
        return password.length() >= 4;
    }

    private boolean isUserNameValid(String userName) {
        return userName.length() >= 4;
    }

    private boolean isBirthdayValid(long birthday) {
        Calendar calendar = Calendar.getInstance();
        return calendar.getTimeInMillis() > birthday;
    }

    private boolean isPhoneValid(String phone) { return phone.length() > 8; }

    private boolean isEmailValid(String email) {
        if (email.length() < 3) return false;
        final char[] emailChar = email.toCharArray();
        return (emailChar[0] != '@' && emailChar[emailChar.length-1] != '@'
                && email.indexOf('@') > 0 && (email.indexOf('@')==email.lastIndexOf('@')));
    }

    public void onConfirmButtonClick(View v) {
        attempt();
    }

    protected String selGender = "";
    public void onRadioGenderClear(View v) {
        radioGender.clearCheck();
        ((RadioButton)v).setChecked(true);
        switch (v.getId()) {
            case R.id.radioGenderMale: selGender = "male"; break;
            case R.id.radioGenderFemale: selGender = "female"; break;
            default: selGender="";
        }
    }

    /**
     * Shows the progress UI and hides the profile/signup form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    protected void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            viewProfileForm.setVisibility(show ? View.GONE : View.VISIBLE);
            viewProfileForm.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    viewProfileForm.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            viewProfileProgress.setVisibility(show ? View.VISIBLE : View.GONE);
            viewProfileProgress.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    viewProfileProgress.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            viewProfileProgress.setVisibility(show ? View.VISIBLE : View.GONE);
            viewProfileForm.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
}
