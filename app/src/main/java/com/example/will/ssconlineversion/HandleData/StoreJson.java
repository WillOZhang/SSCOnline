package com.example.will.ssconlineversion.HandleData;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.Time;
import java.util.List;
import java.util.Set;
import com.example.will.ssconlineversion.CourseScheduleManager.*;
import com.example.will.ssconlineversion.CourseScheduleManager.Exceptions.InstructorTBAException;
import com.example.will.ssconlineversion.CourseScheduleManager.Exceptions.NoScheduledMeetingException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Will on 2017/7/11.
 */

public class StoreJson {
    public static final String JSON = ".json";

    public StoreJson() {
        Storing();
        System.out.println("Done!");
    }

    private void Storing() {
            for (Department department : CourseManager.getInstance().getDepartments()) {

                storeDepartmentJson(department);

            }
    }


    public static JSONObject storeDepartmentJson(Department department){
        JSONObject departmentObject = new JSONObject();
        try {
            departmentObject.put("shortName", department.getShortName());
            departmentObject.put("name", department.getName());
//            JSONArray coursesList = new JSONArray();
//            for (Course course : department) {
//                JSONObject temp = new JSONObject();
//                temp.put("course", course.getCourseNumber());
//                coursesList.put(temp);
//            }
        } catch (JSONException ignored) {

        }

        // file naming
        //department.getShortName();

        return departmentObject;
    }

    public static JSONObject storeCourseJson(Course course) {
        JSONObject courseJson = new JSONObject();
        try {
            courseJson.put("courseNumber", course.getCourseNumber());
            courseJson.put("courseName", course.getCourseName());
            courseJson.put("description", course.getDescription());
            courseJson.put("credits", course.getCredits());
            courseJson.put("reqs", course.getReqs());
        } catch (JSONException ignored) {

        }
        //course.getDepartment().getShortName() + course.getCourseNumber();

        return courseJson;
    }

    public static JSONObject storeSectionJson(Section section) {
        JSONObject sectionJson = new JSONObject();

        try {
            sectionJson.put("section", section.getSection());
            sectionJson.put("status", section.getStatus());
            sectionJson.put("activity", section.getActivity());
            sectionJson.put("term", section.getTerm());
            // TODO: new part, do refactor accordingly
            sectionJson.put("total", section.getTotalSeats());
            sectionJson.put("current", section.getCurrentRegistered());
            sectionJson.put("general", section.getGeneralSeats());
            sectionJson.put("restrict", section.getRestrictSeats());
            sectionJson.put("restrictedTo", section.getRestrictTo());
            sectionJson.put("withrawDay", section.getLastWithdraw());

            try {
                Classroom classroom = section.getClassroom();
                sectionJson.put("classroom", classroom.getName());
                sectionJson.put("building", classroom.getBuildingThatThisClassroomAt().getName());

                Set<String> days = section.getDays();
                JSONArray daysJsonArray = new JSONArray();
                for (String day : days) {
                    List<Time> times = section.getTimeMap().get(day);
                    JSONObject timePairs = new JSONObject();
                    for (Time time : times)
                        timePairs.put(day, time);
                    daysJsonArray.put(timePairs);
                }
                if (daysJsonArray.length() != 0)
                    sectionJson.put("days", daysJsonArray);
            } catch (NoScheduledMeetingException ignored) {
            }

            try {
                Instructor instructor = section.getInstructor();
                JSONObject instructorInfo = new JSONObject();
                instructorInfo.put("name", instructor.getName());
                instructorInfo.put("website", instructor.getWebsite());
                sectionJson.put("instructor", instructorInfo);
            } catch (InstructorTBAException ignored) {
            }
        } catch (JSONException ignored) {

        }

        // file naming
        //course.getDepartment().getShortName() + course.getCourseNumber() + section.getSection()
        return sectionJson;
    }
}
