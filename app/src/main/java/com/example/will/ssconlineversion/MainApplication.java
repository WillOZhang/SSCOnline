package com.example.will.ssconlineversion;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.example.will.ssconlineversion.CourseScheduleManager.Course;
import com.example.will.ssconlineversion.CourseScheduleManager.CourseManager;
import com.example.will.ssconlineversion.CourseScheduleManager.Department;
import com.example.will.ssconlineversion.CourseScheduleManager.Section;
import com.example.will.ssconlineversion.HandleData.ReadJson;
import com.example.will.ssconlineversion.HandleData.RequestData;
import com.example.will.ssconlineversion.HandleData.StoreJson;
import com.example.will.ssconlineversion.SupportingUI.DCListAdapter;

import org.json.JSONObject;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainApplication extends AppCompatActivity {
    public static final String APPLICATION_NAME = "UBC Course Manager";
    public static final String TAG = "Tag";
    public static final String JSON = ".json";
    public static final String TXT = ".txt";
    private static final int COURSE_ID = 1000;
    private static final String FACULTY = "f";
    private static final String DEPARTMENT = "d";
    private static final String COURSE = "c";
    private static final String COURSE_LIST = "CourseList";

    private ActionBar actionBar;
    private ListView departmentAndCourseList;
    private DCListAdapter dcListAdapter;
    private Button sauder;
    private Button science;
    private Button engineering;

    private List<String> dcDataList;
    private String facultyChosen;
    private Department departmentChosen;
    private Course courseChosen;
    private RequestFacultyData requestFacultyData;
    private RequestDepartmentData requestDepartmentData;
    private RequestCourseData requestCourseData;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.navigation, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO: 1. search course/instructor/buildings 2. refresh data list
        switch (item.getItemId()) {
            case R.id.search:
                Toast.makeText(this, "clicked", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // TODO: for BottomNavigationView future update
//        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
//        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        actionBar = getActionBar();

        sauder = (Button) findViewById(R.id.sauder);
        science = (Button) findViewById(R.id.science);
        engineering = (Button) findViewById(R.id.engineering);

        dcDataList = new ArrayList<>();
        dcDataList.add("");
        dcListAdapter = new DCListAdapter(this, dcDataList);
        departmentAndCourseList = (ListView) findViewById(R.id.dclist);
        departmentAndCourseList.setAdapter(dcListAdapter);
        departmentAndCourseList.setVisibility(View.INVISIBLE);
        departmentAndCourseList.setEmptyView(findViewById(R.id.empty_list_view));
        assignOnItemClick();

        hideDataList();
        Log.i(TAG, "The screen has been created...");
    }

    @Override
    protected void onResume() {
        if(getRequestedOrientation()!= ActivityInfo.SCREEN_ORIENTATION_PORTRAIT){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        // TODO: handle pressing Back while downloading data
        if (isListDisplay()) {
            try {
                String departmentShortName = dcDataList.get(0).split("@")[2];
                facultyChosen = null;
                hideDataList();
                displayAllButtons();
                setTitle(APPLICATION_NAME);
            } catch (IndexOutOfBoundsException i) {
                departmentChosen = null;
                changeFacultyDataList(facultyChosen);
            }
        }
    }

    // Handle click each faculty button
    public void clickSauder(View view) throws IOException {
        String faculty = "sauder";
        requestFacultyData(faculty);
    }

    public void clickScience(View view) throws IOException {
        String faculty = "science";
        requestFacultyData(faculty);
    }

    private void requestFacultyData(String faculty) throws IOException {
        facultyChosen = faculty;

        if (isNetworkConnected()) {
            if (CourseManager.getInstance().hasFacultyData(faculty)) {
                changeFacultyDataList(faculty);
            } else {
                String facultyData = readFacultyJson(faculty + JSON);
                List<String> departments = ReadJson.facultyReader(facultyData);
                CourseManager.getInstance().addFDPair(faculty, departments);
                departments.add(0, faculty);
                requestFacultyData = new RequestFacultyData();
                requestFacultyData.execute(departments);
            }
        } else {
            String facultyData = readFacultyJson(faculty + JSON);
            List<String> departments = ReadJson.facultyReader(facultyData);
            CourseManager.getInstance().addFDPair(faculty, departments);
            readJson(FACULTY);
            changeFacultyDataList(faculty);
        }
        Log.i(TAG, "User clicked on " + faculty);
        hideAllButtons();
        displayDataList();
        setTitle(faculty); // ActionBar Title
    }

    private void changeFacultyDataList(String faculty) {
        changeDataList(CourseManager.getInstance().getDepartments(faculty));
        dcListAdapter.notifyDataSetChanged();
    }

    // Handle displaying or hiding items
    private void hideAllButtons() {
        sauder.setVisibility(View.INVISIBLE);
        science.setVisibility(View.INVISIBLE);
        engineering.setVisibility(View.INVISIBLE);
    }

    private void displayAllButtons() {
        sauder.setVisibility(View.VISIBLE);
        science.setVisibility(View.VISIBLE);
        engineering.setVisibility(View.VISIBLE);
    }

    private boolean isButtonDisplay() {
        return sauder.getVisibility() == View.VISIBLE;
    }

    private void displayDataList() {
        departmentAndCourseList.setVisibility(View.VISIBLE);
    }

    private void hideDataList() {
        departmentAndCourseList.setVisibility(View.INVISIBLE);
    }

    private boolean isListDisplay() {
        return departmentAndCourseList.getVisibility() == View.VISIBLE;
    }

    // Handle faculty json data
    private String readFacultyJson(String faculty) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        InputStreamReader inputStreamReader = new InputStreamReader(getAssets().open(faculty), "UTF-8");
        BufferedReader br = new BufferedReader(inputStreamReader);
        String line = br.readLine();
        while (line != null) {
            stringBuilder.append(line);
            line = br.readLine();
        }
        br.close();
        inputStreamReader.close();
        return stringBuilder.toString();
    }

    // Request faculty data set
    private class RequestFacultyData extends AsyncTask<List<String>, String, String> {
        Toast toast;
        @Override
        protected String doInBackground(List<String>... params) {
            List<String> temp = params[0];
            String faculty = temp.get(0);
            temp.remove(0);
            for (String department : temp) {
                RequestData.getInfoFromDepartment(faculty, department);
                publishProgress(faculty);
            }
            return faculty;
        }

        @Override
        protected void onPostExecute(String response) {
            if (dcDataList.size() < 1)
                Toast.makeText(getApplicationContext(), "Sorry, department data is not available at this point", Toast.LENGTH_LONG).show();
            makeSureHasDepartmentData();
            toast.cancel();
            storeDataInJson(FACULTY);
        }

        @Override
        protected void onPreExecute () {
            toast = Toast.makeText(getApplicationContext(), "Loading data... Please wait", Toast.LENGTH_LONG);
            toast.show();
        }

        // TODO: handle a case when the user clicked a department and yet the downloading data has not finished

        @Override
        protected void onProgressUpdate(String... value) {
            String faculty = value[0];
            changeFacultyDataList(faculty);
            hideAllButtons();
            displayDataList();
            setTitle(faculty); // ActionBar Title
        }

        public void onUserClickBeforeFinishLoadingData() {

        }
    }

    private void makeSureHasDepartmentData() {
        if (dcDataList.size() < 1) {
            if (!isButtonDisplay())
                displayAllButtons();
            if (isListDisplay())
                hideDataList();
        }
    }

    // Handle data list change
    private void changeDataList(List<String> departments) {
        dcDataList.clear();
        for (String string : departments)
            dcDataList.add(string);
    }

    // Handle click on a course or a department
    private void assignOnItemClick() {
        departmentAndCourseList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    String temp = (String) parent.getItemAtPosition(position);
                    String departmentShortName = temp.split("@")[2];
                    if (isNetworkConnected()) {
                        if (!CourseManager.getInstance().hasDepartmentData(departmentShortName)) {
                            requestDepartmentData = new RequestDepartmentData();
                            requestDepartmentData.execute(departmentShortName);
                        } else {
                            changeDataList(CourseManager.getInstance().getDepartment(departmentShortName).getCoursesForDisplay());
                            dcListAdapter.notifyDataSetChanged();
                        }
                        departmentChosen = CourseManager.getInstance().getDepartment(departmentShortName);
                    } else {
                        departmentChosen = new Department(departmentShortName);
                        readJson(DEPARTMENT);
                        List<String> tempList = CourseManager.getInstance().getDepartment(departmentShortName).getCoursesForDisplay();
                        if (tempList.size() > 0) {
                            changeDataList(tempList);
                            dcListAdapter.notifyDataSetChanged();
                        }
                    }
                } catch (IndexOutOfBoundsException i) { //dealing with a course list, not a department list
                    String temp = (String) parent.getItemAtPosition(position);
                    courseChosen = departmentChosen.getCourse(temp.split("&")[2]);
                    if (isNetworkConnected()) {
                        requestCourseData = new RequestCourseData();
                        requestCourseData.execute(courseChosen);
                    } else {
                        try {
                            readJson(COURSE);
                        } catch (IOException e) {
                            Toast.makeText(getApplicationContext(), "Section info is not available because of network connection", Toast.LENGTH_LONG).show();
                        }
                    }
                } catch (IOException e) {
                    Toast.makeText(getApplicationContext(), "Not connected", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_CANCELED) {
            courseChosen = null;
        }
    }

    // Request department data set
    private class RequestDepartmentData extends AsyncTask<String, String, String> {
        Toast toast;
        @Override
        protected String doInBackground(String... params) {
            RequestData.handleCoursesList(params[0]);
            return params[0];
        }

        @Override
        protected void onPostExecute(String response) {
            List<String> temp = CourseManager.getInstance().getDepartment(response).getCoursesForDisplay();
            changeDataList(temp);
            dcListAdapter.notifyDataSetChanged();
            toast.cancel();
            storeDataInJson(DEPARTMENT);
        }

        @Override
        protected void onPreExecute () {
            toast = Toast.makeText(getApplicationContext(), "Loading data... Please wait", Toast.LENGTH_LONG);
            toast.show();
        }
    }

    // Request Course Data
    private class RequestCourseData extends AsyncTask<Course, Course, Elements> {
        Course course;
        Toast toast;
        @Override
        protected Elements doInBackground(Course... params) {
            course = params[0];
            Elements sectionElements = RequestData.getCourseContent(course);
            return sectionElements;
        }

        @Override
        protected void onPreExecute() {
            toast = Toast.makeText(getApplicationContext(), "Loading data... Please wait", Toast.LENGTH_LONG);
            toast.show();
        }

        @Override
        protected void onPostExecute(Elements response) {
            toast.cancel();
            Intent displayCourse = new Intent(getApplication(), CourseView.class);
            displayCourse.putExtra("course", course);
            startActivityForResult(displayCourse, COURSE_ID);
            storeDataInJson(COURSE);
        }

//        private class DetailInfoOfSection
    }

    // store Json file for further use
    private void storeDataInJson(String cond) {
        String fileName = "";
        JSONObject jsonObject;

        switch (cond) {
            case (FACULTY):
                for (Department department : CourseManager.getInstance().getDepartments()) {
                    jsonObject = StoreJson.storeDepartmentJson(department); // TODO: reduce creating new objects
                    fileName = department.getShortName() + JSON;
                    writeJsonFiles(fileName, jsonObject);
                }
                break;
            case (DEPARTMENT):
                String courseList = "";
                for (Course course : departmentChosen) {
                    jsonObject = StoreJson.storeCourseJson(course);
                    fileName = course.getDepartment().getShortName() + course.getCourseNumber() + JSON;
                    writeJsonFiles(fileName, jsonObject);

                    courseList += course.getCourseNumber() + ";";
                }
                writeSublist(departmentChosen.getShortName(), courseList);
                break;
            case (COURSE):
                for (Section section : courseChosen) {
                    jsonObject = StoreJson.storeSectionJson(section);
                    fileName = courseChosen.getDepartment().getShortName() + courseChosen.getCourseNumber() + section.getSection() + JSON;
                    writeJsonFiles(fileName, jsonObject);
                }
                break;
            default:
                break;
        }
    }

    private void writeSublist(String name, String courseList) {
        getFilesDir(); //TODO: Delete the previous file before adding new files
        FileOutputStream fos = null;
        try {
            fos = openFileOutput(name + COURSE_LIST + TXT, Context.MODE_PRIVATE);
            fos.write(courseList.getBytes());
            fos.close();
        } catch (FileNotFoundException e) {
            Log.i("ERROR", "FileNotFoundException when refreshEachSection");
        } catch (IOException e) {
            Log.i("ERROR", "IOException when refreshEachSection");
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

    private void readJson(String cond) throws IOException {
        String fileName = "";
        String filePath = "";
        String infoString = "";

        switch (cond) {
            case (FACULTY):
                List<String> temp = CourseManager.getInstance().getDepartmentsByFaculty(facultyChosen);
                for (String department : temp) {
                    fileName = department + JSON;
                    filePath = getFilesDir().getPath();
                    infoString = convertToJsonString(filePath, fileName);
                    ReadJson.departmentReader(infoString);
                }
                makeSureHasDepartmentData();
                break;
            case (DEPARTMENT):
                String courseList = departmentChosen.getShortName() + COURSE_LIST;
                StringBuilder stringBuilder = new StringBuilder();
                BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(courseList))));
                String line = br.readLine();
                while (line != null) {
                    stringBuilder.append(line);
                    line = br.readLine();
                }
                br.close();
                String listTemp = stringBuilder.toString();
                String[] list = listTemp.split(";");
                for (int i = 0; i < list.length; i++) {
                    fileName = departmentChosen.getShortName() + TXT;
                    filePath = getFilesDir().getPath();
                    infoString = convertToJsonString(filePath, fileName);
                    ReadJson.courseReader(departmentChosen, infoString);
                }
                break;
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

    // TODO: for BottomNavigationView future update
    //    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
//            = new BottomNavigationView.OnNavigationItemSelectedListener() {
//
//        @Override
//        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
//            switch (item.getItemId()) {
//                case R.id.navigation_home:
//                    // if ...
//                    sauder.setVisibility(View.VISIBLE);
//
//                    departmentAndCourseList.setVisibility(View.INVISIBLE);
//
//
//                    return true;
//                case R.id.navigation_dashboard:
//                    departmentAndCourseList.setVisibility(View.INVISIBLE);
//                    return true;
//                case R.id.navigation_notifications:
//                    departmentAndCourseList.setVisibility(View.INVISIBLE);
//                    return true;
//            }
//            return false;
//        }
//
//    };
}
