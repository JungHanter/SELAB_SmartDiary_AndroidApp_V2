package ssu.sel.smartdiary.view;

import android.app.DatePickerDialog;
import android.content.Context;
import android.support.v7.widget.SwitchCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Calendar;

import ssu.sel.smartdiary.GlobalUtils;
import ssu.sel.smartdiary.R;

/**
 * Created by hanter on 16. 10. 5..
 */
public class DiarySearchToolbar extends LinearLayout {
    private LinearLayout layoutSearchTime = null;
    private LinearLayout layoutSearchText = null;
    private TextView tvSearchStartDate = null;
    private TextView tvSearchEndDate = null;
    private EditText edtSearchText = null;

    private Calendar startDate = null, endDate = null;
    private DatePickerDialog dlgStartDatePicker = null;
    private DatePickerDialog dlgEndDatePicker = null;

//    private OnSearchDateClickListener searchDateClickListener = null;
    private OnSearchListener searchListener = null;

    public DiarySearchToolbar(Context context) {
        super(context);
        initView();
    }

    public DiarySearchToolbar(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public DiarySearchToolbar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.view_search_toolbar, this, true);

        layoutSearchTime = (LinearLayout)findViewById(R.id.layoutSearchTime);
        layoutSearchText = (LinearLayout)findViewById(R.id.layoutSearchText);

        layoutSearchTime.setVisibility(View.VISIBLE);
        layoutSearchText.setVisibility(View.GONE);

        SwitchCompat switchSearchType = (SwitchCompat)findViewById(R.id.switchSearchType);
        switchSearchType.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                if (checked) {
                    layoutSearchTime.setVisibility(View.GONE);
                    layoutSearchText.setVisibility(View.VISIBLE);
                } else {
                    layoutSearchTime.setVisibility(View.VISIBLE);
                    layoutSearchText.setVisibility(View.GONE);
                }
            }
        });

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.tvSearchStartDate:
//                        searchDateClickListener.onStartDateClick();
                        dlgStartDatePicker.show();
                        return;
                    case R.id.tvSearchEndDate:
//                        searchDateClickListener.onEndDateClick();
                        dlgEndDatePicker.show();
                        return;
                    case R.id.btnSearchTime:
                        searchListener.onSearchTime(startDate, endDate);
                        return;
                    case R.id.btnSearchText:
                        searchListener.onSearchText(edtSearchText.getText().toString());
                        return;
                }
            }
        };

        edtSearchText = (EditText)findViewById(R.id.edtSearchText);
        tvSearchStartDate = (TextView)findViewById(R.id.tvSearchStartDate);
        tvSearchEndDate = (TextView)findViewById(R.id.tvSearchEndDate);
        tvSearchStartDate.setOnClickListener(onClickListener);
        tvSearchEndDate.setOnClickListener(onClickListener);
        findViewById(R.id.btnSearchTime).setOnClickListener(onClickListener);
        findViewById(R.id.btnSearchText).setOnClickListener(onClickListener);

        setSearchDateModals();
        this.setVisibility(View.GONE);
    }

    private void setSearchDateModals() {
        startDate = Calendar.getInstance();
        startDate.add(Calendar.DAY_OF_MONTH, -6);
        startDate.set(Calendar.HOUR_OF_DAY, 0);
        startDate.set(Calendar.MINUTE, 0);
        startDate.set(Calendar.SECOND, 0);
        startDate.set(Calendar.MILLISECOND, 0);
        tvSearchStartDate.setText(GlobalUtils.DIARY_DATE_FORMAT.format(startDate.getTime()));

        endDate = Calendar.getInstance();
        endDate.set(Calendar.HOUR_OF_DAY, 23);
        endDate.set(Calendar.MINUTE, 59);
        endDate.set(Calendar.SECOND, 59);
        endDate.set(Calendar.MILLISECOND, 999);
        tvSearchEndDate.setText(GlobalUtils.DIARY_DATE_FORMAT.format(endDate.getTime()));

        dlgStartDatePicker = new DatePickerDialog(getContext(),
                new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
                startDate.set(year, month, dayOfMonth);
                tvSearchStartDate.setText(GlobalUtils.DIARY_DATE_FORMAT.format(startDate.getTime()));

                if (startDate.after(endDate)) {
                    endDate.set(year, month, dayOfMonth);
                    tvSearchEndDate.setText(GlobalUtils.DIARY_DATE_FORMAT.format(endDate.getTime()));
                }
            }
        }, startDate.get(Calendar.YEAR), startDate.get(Calendar.MONTH),
                startDate.get(Calendar.DAY_OF_MONTH));

        dlgEndDatePicker = new DatePickerDialog(getContext(),
                new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
                endDate.set(year, month, dayOfMonth);
                tvSearchEndDate.setText(GlobalUtils.DIARY_DATE_FORMAT.format(endDate.getTime()));

                if (endDate.before(startDate)) {
                    startDate.set(year, month, dayOfMonth);
                    tvSearchStartDate.setText(GlobalUtils.DIARY_DATE_FORMAT.format(endDate.getTime()));
                }
            }
        }, endDate.get(Calendar.YEAR), endDate.get(Calendar.MONTH),
                endDate.get(Calendar.DAY_OF_MONTH));
    }

//    public void setOnSearchDateClickListener(OnSearchDateClickListener l) {
//        searchDateClickListener = l;
//    }

    public void setOnSearchListener(OnSearchListener l) {
        searchListener = l;
    }

//    public interface OnSearchDateClickListener {
//        void onStartDateClick(final Calendar startDate, final Calendar endDate);
//        void onEndDateClick();
//    }

    public interface OnSearchListener {
        void onSearchTime(final Calendar startDate, final Calendar endDate);
        void onSearchText(String text);
    }
}
