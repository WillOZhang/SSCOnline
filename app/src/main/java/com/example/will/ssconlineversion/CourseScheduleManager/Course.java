package com.example.will.ssconlineversion.CourseScheduleManager;

import android.support.annotation.NonNull;

import com.example.will.ssconlineversion.CourseScheduleManager.Exceptions.InstructorTBAException;
import com.example.will.ssconlineversion.CourseScheduleManager.Exceptions.NoScheduledMeetingException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Will on 2017/5/20.
 */
public class Course implements Serializable, Iterable<Section>, Comparable<Course>, Searchable {
    private Department department;
    private String courseNumber;
    private String courseName;
    private String description;
    private String credits;
    private String reqs;
    private Set<Section> sections;
    private List<String> sectionsList;

    private Set<Instructor> instructorsWhoOfferThisCourse;
    private Set<Classroom> classrooms;

    public Course(Department department, String courseNumber, String courseName) {
        this.department = department;
        this.courseNumber = courseNumber;
        this.courseName = courseName;
        this.description = "";
        this.credits = "Credits: 3";

        sections = new HashSet<Section>();
        sectionsList = new ArrayList<>();
        instructorsWhoOfferThisCourse = new HashSet<Instructor>();
        classrooms = new HashSet<Classroom>();

        department.addCourse(this);
    }

    public List<String> getSectionsList() {
        return sectionsList;
    }

    public void addSection(String section) {
        sectionsList.add(section);
    }

    public void setCredits(String credits) {
        this.credits = credits;
    }

    public void setReqs(String reqs) {
        this.reqs = reqs;
    }

    public void setDescription(String description) {
        // handle duplicated description
//        String regex = "\\n\\n\\n";
//        String[] tempArray = description.split(regex);
//        String result = "";
//        for (int i = 0; i < tempArray.length; i++) {
//            for (int j = i + 1; j < tempArray.length; j++) {
//                if (tempArray[i] == null)
//                    break;
//                result += tempArray[i] + regex;
//                if (tempArray[i].length() == tempArray[j].length()) {
//                    int index = j;
//                    for (int t = tempArray.length - 1; j < t; t--) {
//                        if (tempArray[t] != null)
//                            index = t;
//                    }
//                    tempArray[j] = tempArray[index];
//                    tempArray[index] = null;
//                }
//            }
//        }
        this.description = description;
    }

    public void addSection(Section section) {
        for (Section section1 : sections) {
            if (section1.equals(section)) {
                section1.changeToSameSection(section);
                addInstructorAndClassroom(section);
                return;
            }
        }
        sections.add(section);
        addInstructorAndClassroom(section);
    }

    private void addInstructorAndClassroom(Section section) {
        try {
            instructorsWhoOfferThisCourse.add(section.getInstructor());
        } catch (InstructorTBAException ignored) {

        }
        try {
            classrooms.add(section.getClassroom());
        } catch (NoScheduledMeetingException ignored) {

        }
    }

    public Section getSection(Course course, String name) {
        Section temp = new Section(this, name, null, null, null, null, null);
        for (Section section : sections)
            if (section.equals(temp))
                return section;
        throw new NoSuchElementException();
    }

    public List<String> getSectionsForDisplay() {
        List<String> temp = new ArrayList<>();
        List<Section> tempSection = new ArrayList<>(sections);
        Collections.sort(tempSection);
        for (Section section : tempSection) {
            String a = "section!" + section.getSection() + "@"
                    + section.getActivity() + "@" +
                    section.getTerm() + "@";
            try {
                a += section.getInstructor().getName();
            } catch (InstructorTBAException e) {
                a += "Instructor TBA";
            }
            a = a + "@" + Integer.toString(section.getTotalSeats()) + "@" +
                    Integer.toString(section.getCurrentRegistered()) + "@" +
                    Integer.toString(section.getGeneralSeats()) + "@" +
                    Integer.toString(section.getRestrictSeats()) + "@";
            temp.add(a);
        }
        return temp;
    }

    public boolean sameCourse(Course course) {
        if (course.getDepartment() == this.department
                && course.getCourseNumber().equals(this.courseNumber))
            return true;
        return false;
    }

    public void changeToSameCourse(Course course) {
        this.credits = course.credits;
        this.description = course.description;
        this.courseName = course.courseName;

        try {
            for (Section section : course.sections)
                this.sections.add(section);
            for (Instructor instructor : course.instructorsWhoOfferThisCourse)
                this.instructorsWhoOfferThisCourse.add(instructor);
            for (Classroom classroom : course.classrooms)
                this.classrooms.add(classroom);
        } catch (ConcurrentModificationException e) {
            changeToSameCourse(course);
        }
    }

    public void print() {
        System.out.println("!Course Name: " + department.getShortName() + courseNumber + courseName + "!");
        // TODO
        // when parse info from string, one way is string.split(" ")[i]
        // when i = 0, we get department name
        // when i = 1, we get number
        // ! is used to indicate the end of course name pair
        System.out.println("\t" + "@Credit: " + credits + "@");
        System.out.println("\t" + "#Description: " + description + "#");
        System.out.println("\t" + "$Reqs: " + reqs + "$");
        for (Section section : sections)
            section.print();
    }

    public Department getDepartment() {
        return department;
    }

    public String getCourseNumber() {
        return courseNumber;
    }

    public String getCourseName() {
        return courseName;
    }

    public String getDescription() {
        return description;
    }

    public String getCredits() {
        return credits;
    }

    public String getReqs() {
        // handle duplicated description
//        String regex = "\n";
//        String[] tempArray = reqs.split(regex);
//        String result = "";
//        for (int i = 0; i < tempArray.length; i++) {
//            for (int j = i + 1; j < tempArray.length; j++) {
//                if (tempArray[i] == null || tempArray[j] == null)
//                    break;
//                result += tempArray[i] + regex;
//                if (tempArray[i].length() == tempArray[j].length()) {
//                    int index = j;
//                    for (int t = tempArray.length - 1; j < t; t--) {
//                        if (tempArray[t] != null)
//                            index = t;
//                    }
//                    tempArray[j] = tempArray[index];
//                    tempArray[index] = null;
//                }
//            }
//        }
        
//        String regex = "\n";
//        String[] tempArray = reqs.split(regex);
//        String result = "";
//        for (int i = 0; i < tempArray.length - 1; i++) {
//                if (tempArray[i] == null || tempArray[i + 1] == null)
//                    break;
//                result += tempArray[i] + regex;
//                if (tempArray[i].length() == tempArray[i + 1].length()) {
//                    int index = i + 1;
//                    for (int t = tempArray.length - 1; i + 1 < t; t--) {
//                        if (tempArray[t] != null)
//                            index = t;
//                    }
//                    tempArray[i + 1] = tempArray[index];
//                    tempArray[index] = null;
//                }
//        }
        return reqs;
    }

    public Set<Section> getSections() {
        return sections;
    }

    public Set<Instructor> getInstructorsWhoOfferThisCourse() {
        return instructorsWhoOfferThisCourse;
    }

    public Set<Classroom> getClassrooms() {
        return classrooms;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Course)) return false;

        Course course = (Course) o;

        if (department != null ? !department.equals(course.department) : course.department != null) return false;
        return courseNumber != null ? courseNumber.equals(course.courseNumber) : course.courseNumber == null;
    }

    @Override
    public int hashCode() {
        int result = department != null ? department.hashCode() : 0;
        result = 31 * result + (courseNumber != null ? courseNumber.hashCode() : 0);
        return result;
    }

    @Override
    public Iterator<Section> iterator() {
        return sections.iterator();
    }

    @Override
    public int compareTo(@NonNull Course o) {
        Pattern pattern = Pattern.compile("[a-zA-Z]*");
        Pattern number = Pattern.compile("\\d*");

        Matcher thisCourseNumLetter = pattern.matcher(this.courseNumber);
        Matcher thisCourseNumber = number.matcher(this.courseNumber);

        Matcher oCourseNumLetter = pattern.matcher(o.courseNumber);
        Matcher oCourseNumber = number.matcher(o.courseNumber);

        String s1 = null;
        String s2 = null;

        while (thisCourseNumLetter.find())
            s1 = thisCourseNumLetter.group();
        while (oCourseNumLetter.find())
            s2 = oCourseNumLetter.group();

        int i1 = 0;
        int i2 = 0;

        while (thisCourseNumber.find()) {
            String temp = thisCourseNumber.group();
            if (!temp.isEmpty())
                i1 = Integer.parseInt(temp);
        }
        while (oCourseNumber.find()) {
            String temp = oCourseNumber.group();
            if (!temp.isEmpty())
                i2 = Integer.parseInt(temp);
        }

        if (s1 == null && s2 == null)
            return i1 - i2;
        else if (s1 != null && s2 != null) {
            if (i1 - i2 == 0)
                return s1.compareTo(s2);
            else
                return i1 - i2;
        } else {
            if (i1 - i2 == 0) {
                if (s1 == null)
                    return "Z".compareTo(s2);
                else
                    return s1.compareTo("Z");
            } else
                return i1 - i2;
        }
    }

    @Override
    public boolean match(String pattern) {
        String name = this.department.getShortName().toLowerCase().replaceAll(" ", "") + this.courseNumber.replaceAll(" ", "");
        return name.contains(pattern);
    }
}
