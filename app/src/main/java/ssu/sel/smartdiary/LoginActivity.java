package ssu.sel.smartdiary;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import ssu.sel.smartdiary.model.UserProfile;
import ssu.sel.smartdiary.network.JsonRestConnector;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world"
    };

    private JsonRestConnector loginConnector = null;

    // UI references.
    private EditText edtID;
    private EditText edtPassword;
    private Button btnSignin;
    private Button btnSignup;
    private View viewLoginProgress;
    private View viewLoginForm;

    private String reqUserID = null;
    private String reqUserPW = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
        Locale.setDefault(new Locale("ko", "KR"));

        // Set up the login form.
        edtID = (EditText) findViewById(R.id.edtLoginID);
        edtPassword = (EditText) findViewById(R.id.edtLoginPassword);

        btnSignin = (Button) findViewById(R.id.btnSignin);
        btnSignin.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        btnSignup = (Button) findViewById(R.id.btnSignup);
        btnSignup.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(intent);
            }
        });

        viewLoginForm = findViewById(R.id.viewLoginForm);
        viewLoginProgress = findViewById(R.id.login_progress);

        //for testing
        edtID.setText("lhs");
        edtPassword.setText("1234");

        loginConnector = new JsonRestConnector("user/login", "POST",
                new JsonRestConnector.OnConnectListener() {
            @Override
            public void onDone(JSONObject resJson) {
                showProgress(false);
                if (resJson != null) {
                    Log.d("Login - JSON", resJson.toString());

                    try {
                        Boolean success = resJson.getBoolean("login");
                        if (success) {
                            String gender = resJson.getString("gender");
                            String name = resJson.getString("name");
                            long birthday = resJson.getLong("timestamp");
                            UserProfile.setUserProfile(reqUserID, reqUserPW, name,
                                    0l, gender);
                            Log.d("Login Success", UserProfile.getUserProfile().toString());

                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            //intent.putExtra(EXTRA_MESSsAGE, message);
                            startActivity(intent);
                            LoginActivity.this.finish();
                        } else {
                            edtID.setError("ID or password is incorrect");
                            edtPassword.setError("ID or password is incorrect");
                            edtID.requestFocus();
                            reqUserID = reqUserPW = null;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        edtID.setError("Login response error");
                        edtID.requestFocus();
                        reqUserID = reqUserPW = null;
                    }
                } else {
                    Log.d("Login - Json", "No response");
                    edtID.setError("There is no response...");
                    edtID.requestFocus();
                    reqUserID = reqUserPW = null;
                }
            }

            @Override
            public void onCancelled() {
                showProgress(false);
                reqUserID = reqUserPW = null;
            }
        });
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        // Reset errors.
        edtID.setError(null);
        edtPassword.setError(null);

        // Store values at the time of the login attempt.
        String userId = edtID.getText().toString();
        String password = edtPassword.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password)) {
            edtPassword.setError("Password is empty");
            focusView = edtPassword;
            cancel = true;
        } else if(!isPasswordValid(password)) {
            edtPassword.setError(getString(R.string.error_invalid_password));
            focusView = edtPassword;
            cancel = true;
        }

        // Check for a valid user id.
        if (TextUtils.isEmpty(userId)) {
            edtID.setError(getString(R.string.error_field_required));
            focusView = edtID;
            cancel = true;
        } else if (!isUserIdValid(userId)) {
            edtID.setError(getString(R.string.error_invalid_id));
            focusView = edtID;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);

            JSONObject json = new JSONObject();
            try {
                json.put("user_id", userId);
                json.put("password", password);
            } catch (Exception e) {
                e.printStackTrace();
            }
            reqUserID = userId;
            reqUserPW = password;
            loginConnector.request(json);
        }
    }

    private boolean isUserIdValid(String userId) {
        return userId.length() >= 3;
    }

    private boolean isPasswordValid(String password) {
        return password.length() >= 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            viewLoginForm.setVisibility(show ? View.GONE : View.VISIBLE);
            viewLoginForm.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    viewLoginForm.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            viewLoginProgress.setVisibility(show ? View.VISIBLE : View.GONE);
            viewLoginProgress.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    viewLoginProgress.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            viewLoginProgress.setVisibility(show ? View.VISIBLE : View.GONE);
            viewLoginForm.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
}

