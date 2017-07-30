package com.example.will.ssconlineversion;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.example.will.ssconlineversion.CourseScheduleManager.*;
import com.example.will.ssconlineversion.HandleData.*;
import com.example.will.ssconlineversion.SupportingUI.SListViewAdapter;

import static com.example.will.ssconlineversion.MainApplication.JSON;
import static com.example.will.ssconlineversion.MainApplication.TAG;

public class CourseView extends AppCompatActivity {
    private static final String COURSE = "Course";
    private ArrayList<String> dataList;
    private Course course;

    private RecyclerView mRecyclerView;
    private SListViewAdapter sListViewAdapter;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private TextView subtitle;
    private FloatingActionButton refresh;
    private FloatingActionButton details;
    private FloatingActionButton cancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_view_refactor);

        initData();
        initView();

        // load section data
        if (isNetworkConnected())
            new RefreshSectionList().execute(course);
        else
            readJson(COURSE);
    }

    private void initData() {
        // handle the data that was passed from the intent
        Intent intent = this.getIntent();
        course = (Course) intent.getSerializableExtra("course");

        mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        dataList = new ArrayList<>();
        sListViewAdapter = new SListViewAdapter(dataList);
        mAdapter = sListViewAdapter;
    }

    private void initView() {
        // Update on the UI
        // set toolbar to display the course number and the course name
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(course.getDepartment().getShortName() + " " + course.getCourseNumber());
        setSupportActionBar(toolbar);

        subtitle = (TextView) findViewById(R.id.subtitle);
        String subtitleString = getString(R.string.subtitle_placeholder) + course.getCourseName();
        subtitle.setText(subtitleString);

        refresh = (FloatingActionButton) findViewById(R.id.refresh);
        assignRefreshOnClickListener();
        details = (FloatingActionButton) findViewById(R.id.details);
        String descriptionText = course.getReqs() + "\n" + course.getDescription();
        assignDetailsOnClickListener(descriptionText);
        cancel = (FloatingActionButton) findViewById(R.id.cancel);
        assignCancelOnClickListener();

        // handle data list
        mRecyclerView = (RecyclerView) findViewById(R.id.sections);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
    }

    private void assignRefreshOnClickListener() {
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new RefreshSectionList().execute(course);
                Log.i("Successfully", "dataList has been updated");
            }
        });
    }

    private void assignDetailsOnClickListener(final String descriptionText) {

        // TODO: BUG !!!!!!!!!!!!!

        details.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Use the Builder class for convenient dialog construction
                AlertDialog.Builder builder = new AlertDialog.Builder(CourseView.this);
                String temp;
                if (descriptionText.length() > 1)
                    temp = descriptionText;
                else
                    temp = getString(R.string.no_description);
                builder.setMessage(temp)
                        .setNegativeButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // don't really need to do something here
                            }
                        });
                // Create the AlertDialog object
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });
    }

    private void assignCancelOnClickListener() {
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setResult(RESULT_CANCELED);
                Log.i(TAG, "User closed " + course.getDepartment().getCourseNumbers() + course.getCourseNumber() + " window");
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        Log.i(TAG, "User closed " + course.getDepartment().getCourseNumbers() + course.getCourseNumber() + " window");
        finish();
    }

    private void changeDataList(List<String> departments) {
        dataList.clear();
        for (String string : departments)
            dataList.add(string);
    }

    private class RefreshSectionList extends AsyncTask<Course, Course, Course> {
        Toast toast;
        @Override
        protected Course doInBackground(Course... params) {
            Elements temp = RequestData.getCourseContent(params[0]);
            for (Element section : temp) {
                RequestData.refreshEachSection(course, section);
                publishProgress(course);
            }
            return course;
        }

        @Override
        protected void onPreExecute() {
            toast = Toast.makeText(getApplicationContext(), "Loading data... Please wait", Toast.LENGTH_LONG);
            toast.show();
        }

        @Override
        protected void onPostExecute(Course response) {
            toast.cancel();
            ArrayList listForIntent = (ArrayList) response.getSectionsForDisplay();
            if (listForIntent.size() < 1) {
                dataList.add("No section for the selected course");
            }
            changeDataList(course.getSectionsForDisplay());
            sListViewAdapter.updateData();
            storeDataInJson(COURSE);
        }

        @Override
        protected void onProgressUpdate(Course... value) {
            changeDataList(course.getSectionsForDisplay());
            sListViewAdapter.updateData();
        }
    }

    private void storeDataInJson(String cond) {
        String fileName = "";
        JSONObject jsonObject;

        switch (cond) {
            case (COURSE):
                for (Section section : course) {
                    jsonObject = StoreJson.storeSectionJson(section);
                    fileName = course.getDepartment().getShortName() + course.getCourseNumber() + section.getSection() + JSON;
                    writeJsonFiles(fileName, jsonObject);
                }
                break;
            default:
                break;
        }
    }

    private void writeJsonFiles(String fileName, JSONObject jsonObject) {
        getFilesDir(); //TODO: Delete the previous file before adding new files
        FileOutputStream fos = null;
        try {
            fos = openFileOutput(fileName, Context.MODE_PRIVATE);
            fos.write(jsonObject.toString().getBytes());
            fos.close();
            Log.i(TAG, "Stored data " + getFilesDir() + "/" + fileName + " readable " + new File(getFilesDir() + "/" + fileName).canRead());
        } catch (FileNotFoundException e) {
            Log.i("ERROR", "FileNotFoundException when refreshEachSection");
        } catch (IOException e) {
            Log.i("ERROR", "IOException when refreshEachSection");
        }
    }

    private void readJson(String cond) {
        String fileName = "";
        String filePath = "";
        String infoString = "";

        // TODO: add offline reading section.json function
        switch (cond) {
            case (COURSE):
                Toast.makeText(this, "Section info is not available because of network connection", Toast.LENGTH_LONG).show();
                break;
            default:
                break;
        }
    }

    private String convertToJsonString(String jsonFilePath, String fileName) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        File temp = new File(jsonFilePath, fileName);
        if (temp.exists()) {
            InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(temp), "UTF-8");
            BufferedReader br = new BufferedReader(inputStreamReader);
            String line = br.readLine();
            while (line != null) {
                stringBuilder.append(line);
                line = br.readLine();
            }
            br.close();
            inputStreamReader.close();
            Log.i(TAG, "Convert " + jsonFilePath + "to json string");
        }
        return stringBuilder.toString();
    }


    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
}
