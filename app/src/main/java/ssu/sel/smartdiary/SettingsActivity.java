package ssu.sel.smartdiary;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
        View mActionBarView = getLayoutInflater().inflate(R.layout.action_bar_center_with_back_button, null);
        ((TextView)mActionBarView.findViewById(R.id.tvActionBarTitle)).setText("Settings");
        actionBar.setCustomView(mActionBarView,
                new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT,
                        ActionBar.LayoutParams.MATCH_PARENT, Gravity.CENTER));
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        setContentView(R.layout.activity_settings);
    }
}
