package ssu.sel.smartdiary.view;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;

import ssu.sel.smartdiary.R;

/**
 * Created by hanter on 16. 10. 5..
 */

//dynamic list
//http://theeye.pe.kr/archives/1287

public class DiaryListViewAdapter extends BaseAdapter {
    private ArrayList<DiaryListViewItem> listViewItemList = new ArrayList<DiaryListViewItem>();
    private OnDiaryViewItemClickListener listener = null;

    public void addItem(DiaryListViewItem item) {
        listViewItemList.add(item);
    }

    public void removeItem(int index) {
        listViewItemList.remove(index);
    }

    public void removeItem(DiaryListViewItem item) {
        listViewItemList.remove(item);
    }

    public void removeAll(Collection<DiaryListViewItem> items) {
        listViewItemList.removeAll(items);
    }

    public void clear() {
        listViewItemList.clear();
    }

    @Override
    public int getCount() {
        return listViewItemList.size();
    }

    @Override
    public Object getItem(int index) {
        return listViewItemList.get(index);
    }

    @Override
    public long getItemId(int index) {
        return index;
    }

    @Override
    public View getView(int index, View convertView, ViewGroup parent) {
        final int idx = index;
        final Context context = parent.getContext();

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.layout_diary_list_item, parent, false);
        }

        TextView tvTitle = (TextView)convertView.findViewById(R.id.tvDiaryListElemTitle);
        TextView tvDate = (TextView)convertView.findViewById(R.id.tvDiaryListElemDate);
        TextView tvContent = (TextView)convertView.findViewById(R.id.tvDiaryListElemContent);

        DiaryListViewItem listViewItem = listViewItemList.get(index);

        tvTitle.setText(listViewItem.getTitle());
        tvDate.setText(listViewItem.getDate());
        tvContent.setText(listViewItem.getContent());

        convertView.findViewById(R.id.viewDiaryListElem).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("DiaryListAdapter", idx+" item");
                listener.onClick(listViewItemList.get(idx));
            }
        });

        return convertView;
    }

    public void setOnDiaryViewItemClickListener(OnDiaryViewItemClickListener l) {
        this.listener = l;
    }

    public interface OnDiaryViewItemClickListener {
        void onClick(DiaryListViewItem diary);
    }
}