package com.example.will.ssconlineversion.HandleData;

import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.sql.Time;

import com.example.will.ssconlineversion.CourseScheduleManager.*;
import com.example.will.ssconlineversion.CourseScheduleManager.Exceptions.NoScheduledMeetingException;

/**
 * Created by Will on 2017/6/25.
 */

public class RequestData {
    public static final String OBJECT_PATH = "/Users/ZC/Documents/CourseScheduleBackground/src/com/CourseSchedule/CourseScheduleManager/";
    public static final String PREFIX = "https://courses.students.ubc.ca";
    public static final String DEPARTMENT_URL = "https://courses.students.ubc.ca/cs/main?pname=subjarea&tname=subjareas&req=1&dept=";
    public static final String SECTION_URL = "https://courses.students.ubc.ca/cs/main?pname=subjarea&tname=subjareas&req=3&dept=";
    public static final String SECTION_URL_EXTRA = "&course=";
    private static Section lastSection;

    public static Department getInfoFromDepartment(String faculty, String department) {
        try {
            String departmentInfo = reading(DEPARTMENT_URL + department);
            Document departmentDoc = Jsoup.parse(departmentInfo);
            String shortName = department;
            Elements temp = departmentDoc.getElementsByClass("content expand");//.tagName("h5").text();//.split("Subject Code - " + department);
            Element temp2 = temp.first().getElementsByTag("h5").first();
            String departmentName = temp2.text().split("Subject Code - " + department + " ")[1].replaceAll("\\p{P}","");;

            Element temp3 = temp.first();
            if (temp3.getAllElements().size() < 17)
                return null;

            Department tempDepartment = new Department(shortName);
            tempDepartment.setName(departmentName);
            CourseManager.getInstance().addDepartmentForDownloadData(faculty, tempDepartment);
            return tempDepartment;
        } catch (IOException e) {
            Log.i("Error", "Something went wrong with " + department + " info");
        } catch (IndexOutOfBoundsException i) {
            Log.i("Error", "Website is unusual when download " + department + " info");
        }
        return null;
    }

    public static void handleCoursesList(String department) {
        String coursesListText = null;
        try {
            coursesListText = reading(DEPARTMENT_URL + department);
            Document coursesListDoc = Jsoup.parse(coursesListText);
            Elements tbodyElements = coursesListDoc.getElementsByTag("tbody");
            Element coursesListElement = tbodyElements.first();
            Elements coursesElements = coursesListElement.getElementsByTag("tr");
            for (Element courseElement : coursesElements) {
                String course = courseElement.getElementsByTag("a").first().text();
                String[] parts = course.split(" ");
                String part1 = parts[0]; // Department
                String part2 = parts[1]; // Number
                String courseTitle = courseElement.getElementsByTag("td").get(1).text();
                CourseManager.getInstance().addCourse(part1, part2, courseTitle);
            }
        } catch (IOException ignored) {

        }
    }

    public static synchronized Elements getCourseContent(Course editingCourse) {
        try {
            String sectionListText = reading(SECTION_URL + editingCourse.getDepartment().getShortName() + SECTION_URL_EXTRA + editingCourse.getCourseNumber());
            Document sectionListDoc = Jsoup.parse(sectionListText);
            Element mainContentElement = sectionListDoc.getElementsByClass("content expand").first();

            Log.i("", sectionListText);

            Elements creditsAndDescription = mainContentElement.getElementsByTag("p");
            Element description = creditsAndDescription.get(0);
            Element credits = creditsAndDescription.get(1);
            editingCourse.setCredits(credits.text());
            editingCourse.setDescription(description.text());

            // dealing with reqs
            String reqs = "";
            for (int i = 2; i < creditsAndDescription.size(); i++)
                reqs += creditsAndDescription.get(i).text() + "\n";
            editingCourse.setReqs(reqs);

            // dealing with sections
            Element sectionTableElement = mainContentElement.getElementsByTag("tbody").first();
            Elements sectionElements = sectionTableElement.getElementsByTag("tr");
//            for (Element section : sectionElements)
//                simpleVersionOfRefreshEachSection(editingCourse, section);

            Log.i("", "finished refresh");
            return sectionElements;
        } catch (IOException e) {
            return null;
        }
    }

    private static synchronized void simpleVersionOfRefreshEachSection(Course editingCourse, Elements sectionElements) throws IOException {
        for (Element section : sectionElements) {
            Elements sectionInfo = section.getElementsByTag("td");
            String status = sectionInfo.get(0).text();
            String Section = sectionInfo.get(1).text().split(" ")[2];
            String activity = sectionInfo.get(2).text();
            String term = sectionInfo.get(3).text();

            String urlForSectionInfo = sectionInfo.get(1).getElementsByTag("a").attr("href");
            String sectionInfoText = reading(PREFIX + urlForSectionInfo);
            Document sectionInfoDoc = Jsoup.parse(sectionInfoText);
            Element sectionPage = sectionInfoDoc.getElementsByClass("content expand").first();


            // dealing with seats
            Element seatsSummaryElement = sectionPage.getElementsByAttribute("table-nonfluid&#39;").first();
            int total = 0;
            int current = 0;
            int general = 0;
            int restricted = 0;
            String restrictedTo = "";
            if (seatsSummaryElement != null) {
                Element seatsInfoElement = seatsSummaryElement.getElementsByTag("tbody").first();
                String seatsInfoCombine = getSeatsInfo(seatsInfoElement);
                try {
                    String[] seatsInfoArray = seatsInfoCombine.split("@");
                    total = Integer.parseInt(seatsInfoArray[0]);
                    current = Integer.parseInt(seatsInfoArray[1]);
                    general = Integer.parseInt(seatsInfoArray[2]);
                    restricted = Integer.parseInt(seatsInfoArray[3]);
                    if (seatsInfoArray.length > 4)
                        restrictedTo = seatsInfoArray[4];
                } catch (IndexOutOfBoundsException | NumberFormatException e) {
                    System.out.println("IndexOutOfBoundsException | NumberFormatException is found in " +
                            editingCourse.getDepartment().getShortName() + editingCourse.getCourseNumber()
                            + sectionInfoText);
                }
            }

            // dealing with instructor info
            Elements info = sectionPage.getElementsByTag("table");
            Elements instructorInfoElements = findingInstructorInfo(info);
            String instructorInfo;
            if (instructorInfoElements != null)
                instructorInfo = instructorInfoElements.text().split(": ")[1];
            else
                instructorInfo = null;
            Instructor instructor;
            if (instructorInfo == null || instructorInfo.contains("TBA"))
                instructor = null;
            else {
                Instructor temp = new Instructor(instructorInfo);
                instructor = InstructorManager.getInstance().getInstructor(temp);
                instructor.addCourse(editingCourse);
                Element tempWeb = instructorInfoElements.tagName("a").first().getElementsByTag("a").get(1);
                String website = tempWeb.attr("href");
                instructor.setWebsite(website);
            }

            // ignore all waiting list courses and the sections that are blocked
            if (activity.equals("Waiting List"))
                return;
            if (status.equals("Blocked"))
                return;

            // Set the section
            Section currentSection = new Section(editingCourse, Section, status, activity, instructor, null, term);
            currentSection.setSeatsInfo(total, current, restricted, general);
            currentSection.setRestrictTo(restrictedTo);
            editingCourse.addSection(currentSection);
            if (instructor != null)
                instructor.addSection(currentSection);
        }
    }

    public static synchronized void refreshEachSection(Course editingCourse, Element section) {
        try {
            Elements sectionInfo = section.getElementsByTag("td");
            String status = sectionInfo.get(0).text();
            String courseFullInfo = sectionInfo.get(1).text();
            if (!courseFullInfo.equals("") && !courseFullInfo.equals(" ")) {
                String Section = sectionInfo.get(1).text().split(" ")[2];
                String activity = sectionInfo.get(2).text();
                String term = sectionInfo.get(3).text();

                String urlForSectionInfo = sectionInfo.get(1).getElementsByTag("a").attr("href");
                String sectionInfoText = reading(PREFIX + urlForSectionInfo);
                Document sectionInfoDoc = Jsoup.parse(sectionInfoText);
                Element sectionPage = sectionInfoDoc.getElementsByClass("content expand").first();

                // dealing with last day to withdraw
                Element withdrawDay = sectionPage.getElementsByClass("table table-nonfluid").first();
                Element withdrawInfoElement = withdrawDay.getElementsByTag("tbody").first();
                String withdrawInfo = withdrawInfoElement.text();

                // dealing with seats
                Element seatsSummaryElement = sectionPage.getElementsByAttribute("table-nonfluid&#39;").first();
                int total = 0;
                int current = 0;
                int general = 0;
                int restricted = 0;
                String restrictedTo = "";
                if (seatsSummaryElement != null) {
                    Element seatsInfoElement = seatsSummaryElement.getElementsByTag("tbody").first();
                    String seatsInfoCombine = getSeatsInfo(seatsInfoElement);
                    try {
                        String[] seatsInfoArray = seatsInfoCombine.split("@");
                        total = Integer.parseInt(seatsInfoArray[0]);
                        current = Integer.parseInt(seatsInfoArray[1]);
                        general = Integer.parseInt(seatsInfoArray[2]);
                        restricted = Integer.parseInt(seatsInfoArray[3]);
                        if (seatsInfoArray.length > 4)
                            restrictedTo = seatsInfoArray[4];
                    } catch (IndexOutOfBoundsException | NumberFormatException e) {
                        System.out.println("IndexOutOfBoundsException | NumberFormatException is found in " +
                                editingCourse.getDepartment().getShortName() + editingCourse.getCourseNumber()
                                + sectionInfoText);
                    }
                }

                // dealing with classroom info
                Element classroomInfo = sectionPage.getElementsByClass("table  table-striped").first();
                Classroom classroom1;
                if (classroomInfo != null) {
                    Elements buildingInfoTable = classroomInfo.getElementsByTag("td");
                    String building = buildingInfoTable.get(4).text();
                    String classroom = buildingInfoTable.get(5).text();
                    Building building1;
                    if (building.equals("No Scheduled Meeting"))
                        classroom1 = null;
                    else {
                        building1 = new Building(building);
                        classroom1 = new Classroom(classroom, building1);
                        Building temp = BuildingManager.getInstance().getBuilding(building1);
                        temp.addClassroom(classroom1);
                    }
                } else
                    classroom1 = null;

                // dealing with instructor info
                Elements info = sectionPage.getElementsByTag("table");
                Elements instructorInfoElements = findingInstructorInfo(info);
                String instructorInfo;
                if (instructorInfoElements != null)
                    instructorInfo = instructorInfoElements.text().split(": ")[1];
                else
                    instructorInfo = null;
                Instructor instructor;
                if (instructorInfo == null || instructorInfo.contains("TBA"))
                    instructor = null;
                else {
                    Instructor temp = new Instructor(instructorInfo);
                    instructor = InstructorManager.getInstance().getInstructor(temp);
                    instructor.addCourse(editingCourse);
                    Element tempWeb = instructorInfoElements.tagName("a").first().getElementsByTag("a").get(1);
                    String website = tempWeb.attr("href");
                    instructor.setWebsite(website);
                }

                // ignore all waiting list courses and the sections that are blocked
                if (activity.equals("Waiting List"))
                    return;
                if (status.equals("Blocked"))
                    return;

                // Set the section
                Section currentSection = new Section(editingCourse, Section, status, activity, instructor, classroom1, term);
                currentSection.setLastWithdraw(withdrawInfo);
                currentSection.setSeatsInfo(total, current, restricted, general);
                currentSection.setRestrictTo(restrictedTo);
                editingCourse.addSection(currentSection);
                if (instructor != null)
                    instructor.addSection(currentSection);
                lastSection = currentSection;

                String days = sectionInfo.get(5).text();
                try {
                    if (!days.equals("") && !days.equals(" ")
                            && classroom1 != null) { // in some cases, days are available and yet time is not
                        String[] time1 = sectionInfo.get(6).text().split(":");
                        Time start = new Time(Integer.parseInt(time1[0]), Integer.parseInt(time1[1]), 0);
                        String[] time2 = sectionInfo.get(7).text().split(":");
                        Time end = new Time(Integer.parseInt(time2[0]), Integer.parseInt(time2[1]), 0);
                        currentSection.addTime(days, start, end);
                    }
                } catch (NumberFormatException e) {

                }
            } else { // if one section has separated colums storing info
                String days = sectionInfo.get(5).text();
                try {
                    lastSection.getClassroom();
                    if (!days.equals("") && !days.equals(" ")) { // add try catch to avoid NumberFormatException
                        String[] time1 = sectionInfo.get(6).text().split(":");
                        Time start = new Time(Integer.parseInt(time1[0]), Integer.parseInt(time1[1]), 0);
                        String[] time2 = sectionInfo.get(7).text().split(":");
                        Time end = new Time(Integer.parseInt(time2[0]), Integer.parseInt(time2[1]), 0);
                        lastSection.addTime(days, start, end);
                    }
                } catch (NoScheduledMeetingException e) {
                    return;
                } catch (NumberFormatException e) {

                }
            }
        } catch (IOException ignored) {

        }
    }

    private static String getSeatsInfo(Element seatsInfoElement) {
        String temp = "";
        String split = "@";
        String total = "";
        String current = "";
        String general = "";
        String restricted = "";
        String restrictedTo = "";
        Elements tempInfo = seatsInfoElement.getElementsByTag("tr");
        for (Element element : tempInfo) {
            if (element.text().contains("Total"))
                total = element.getElementsByTag("strong").first().text();
            else if (element.text().contains("Current"))
                current = element.getElementsByTag("strong").first().text();
            else if (element.text().contains("General"))
                general = element.getElementsByTag("strong").first().text();
            else if (element.text().contains("Restricted"))
                restricted = element.getElementsByTag("strong").first().text();
            else
                restrictedTo = element.text();
        }
        temp = total + split +
                current + split +
                general + split +
                restricted + split + restrictedTo;

        return temp;
    }

    private static Elements findingInstructorInfo(Elements info) {
        for (Element element : info) {
            Elements childNodes = element.children().tagName("td").tagName("td");
            if (childNodes.text().contains("Instructor"))
                return childNodes;
        }
        return null;
    }

    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    public static String reading(String url) throws IOException {
        InputStream is = new URL(url).openStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));

        return readAll(reader);
    }
}
