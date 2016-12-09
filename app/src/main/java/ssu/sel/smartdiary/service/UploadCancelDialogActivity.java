package ssu.sel.smartdiary.service;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import ssu.sel.smartdiary.R;

/**
 * Created by hanter on 2016. 12. 9..
 */

public class UploadCancelDialogActivity extends Activity {
    private int serviceId = -1;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setFinishOnTouchOutside(true);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_dlg_diary_upload_cancel);

        serviceId = getIntent().getIntExtra("SERVICE_ID", -1);
        String diaryTitle = getIntent().getStringExtra(DiaryUploadService.EXTRA_NAME_DIARY_TITLE);
        ((TextView)findViewById(R.id.tvDiaryUploadCancel)).setText(
                "Are you sure to cancel the diary \"" + diaryTitle + "\"?"
        );
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnDiaryUploadCancelYes:
                UploadCancelDialogActivity.this.finish();
                Intent intent = new Intent("ssu.sel.smartdiary.UPLOAD.CANCEL");
                intent.putExtra("STOP", true);
                intent.putExtra("SERVICE_ID", serviceId);
                sendBroadcast(intent);
                break;
            case R.id.btnDiaryUploadCancelNo:
                UploadCancelDialogActivity.this.finish();
                break;
        }
    }
}
