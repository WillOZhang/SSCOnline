package com.example.will.ssconlineversion.SupportingUI;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
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

public class SListViewAdapter extends RecyclerView.Adapter<SListViewAdapter.ViewHolder> {
    private static final String TAG = "Tag";
    private static final String ERROR = "Error";
    private static final String NO_SECTION = "There is no section info available for registration";

    private final List<String> values;
    private String[] displayString;
    private String firstLineName;
    private List<String> firstLineTextList;
    private List<String> secondLineTextList;
    private List<Integer> total, current, general, restrict;

    private boolean hasSection = true;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView1;
        TextView textView2;
        ProgressBar prograssBar;

        public ViewHolder(View itemView) {
            super(itemView);
            textView1 = (TextView) itemView.findViewById(R.id.firstLine);
            textView2 = (TextView) itemView.findViewById(R.id.secondLine);
            prograssBar = (ProgressBar) itemView.findViewById(R.id.progressBar);
        }
    }

    public SListViewAdapter (ArrayList<String> data) {
        this.values = data;

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
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.s_list, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        setDisplayData(holder, position);
    }

    @Override
    public int getItemCount() {
        return values == null ? 0 : values.size();
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

    public void updateData() {
        preHandleS();
        notifyDataSetChanged();
        Log.i(TAG, "updated sections info");
    }

    private void preHandleS() {
        reset();
        if (values.size() == 0)
            values.add(NO_SECTION);
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
            } else if (string.contains(NO_SECTION)) {
                firstLineName = NO_SECTION;
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
