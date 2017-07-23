package com.example.will.ssconlineversion.SupportingUI;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.will.ssconlineversion.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Will on 2017/6/24.
 */

public class SListViewAdapter extends ArrayAdapter<String> {
    private static final String TAG = "Tag";
    private static final String ERROR = "Error";

    private final Context context;
    private final List<String> values;
    private String[] displayString;
    private String firstLineName;
    private List<String> firstLineTextList;
    private List<String> secondLineTextList;
    private List<Integer> total, current, general, restrict;

    private boolean hasSection = true;

    private static class ViewHolder {
        TextView textView1;
        TextView textView2;
        ProgressBar prograssBar;
    }

    public SListViewAdapter(Context context, List<String> values) {
        super(context, -1, values);

        this.context = context;
        this.values = values;

        if (firstLineTextList == null && secondLineTextList == null) {
            firstLineTextList = new ArrayList<>();
            secondLineTextList = new ArrayList<>();
            total = new ArrayList<>();
            current = new ArrayList<>();
            general = new ArrayList<>();
            restrict = new ArrayList<>();
        }

        updateData();
        Log.i(TAG, "Section List adapter has been created");
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        updateData();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            convertView = inflater.inflate(R.layout.s_list, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.textView1 = (TextView) convertView.findViewById(R.id.firstLine);
            viewHolder.textView2 = (TextView) convertView.findViewById(R.id.secondLine);
            viewHolder.prograssBar = (ProgressBar) convertView.findViewById(R.id.progressBar);

            convertView.setTag(viewHolder);
        } else
            viewHolder = (ViewHolder) convertView.getTag();

        setDisplayData(viewHolder, position);

        return convertView;
    }

    private void setDisplayData(ViewHolder viewHolder, int position) {
        TextView textView1 = viewHolder.textView1;
        TextView textView2 = viewHolder.textView2;
        ProgressBar progressBar = viewHolder.prograssBar;

        setVisiblity(progressBar);

        try {
            String firstLine = firstLineTextList.get(position);
            textView1.setText(firstLine);

            int totalSeats = total.get(position);
            int currentRegistered = current.get(position);
            if (currentRegistered < totalSeats) {
                progressBar.setMax(totalSeats);
                progressBar.setProgress(currentRegistered);
            } else {
                progressBar.setMax(currentRegistered);
                progressBar.setProgress(currentRegistered);
            }

            if (!hasSection)
                progressBar.setVisibility(View.INVISIBLE);

            String secondLine = secondLineTextList.get(position);
            textView2.setText(secondLine);
        } catch (IndexOutOfBoundsException e) {
            textView2.setText("");
        }
    }

    private void updateData() {
        preHandleS();
        Log.i(TAG, "updated sections info");
    }

    private void preHandleS() {
        reset();
        if (values.size() == 0)
            values.add("No section for the selected course");
        for (int i = 0; i < values.size(); i++) {
            String string = values.get(i);
            if (string.contains("section!")) {
                try {
                    hasSection = true;
                    displayString = string.split("section!")[1].split("@");
                    firstLineName = "Section: " + displayString[0] + "   " + displayString[1];
                    firstLineTextList.add(firstLineName);
                    String secondLineName = "Term: " + displayString[2] + "   " + displayString[3];
                    secondLineTextList.add(secondLineName);

                    total.add(Integer.parseInt(displayString[4]));
                    current.add(Integer.parseInt(displayString[5]));
                    general.add(Integer.parseInt(displayString[6]));
                    restrict.add(Integer.parseInt(displayString[7]));
                } catch (IndexOutOfBoundsException e) {
                    Log.i(ERROR, string);
                }
            } else if (string.contains("No section for the selected course")) {
                firstLineName = "No section for the selected course";
                firstLineTextList.add(firstLineName);
                hasSection = false;
            }
        }
    }

    private void setVisiblity(ProgressBar progressBar) {
        if (hasSection)
            progressBar.setVisibility(View.VISIBLE);
        else
            progressBar.setVisibility(View.INVISIBLE);
    }

    private void reset() {
        firstLineTextList.clear();
        secondLineTextList.clear();
        total.clear();
        current.clear();
        general.clear();
        restrict.clear();
    }
}
