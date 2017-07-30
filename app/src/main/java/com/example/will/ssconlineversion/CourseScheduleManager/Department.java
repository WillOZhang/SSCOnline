package com.example.will.ssconlineversion.CourseScheduleManager;

import android.support.annotation.NonNull;
import android.widget.ListView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Will on 2017/5/22.
 */
public class Department implements Serializable, Iterable<Course>, Comparable<Department>, Searchable {

    private String shortName;
    private String name;
    private Set<Course> courses;
    private Set<String> courseNumbers;

    public Department(String shortName) {
        this.shortName = shortName;
        this.name = "";
        courses = new HashSet<Course>();
        courseNumbers = new HashSet<>();
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addCourse(Course course) {
        courses.add(course);
    }

    public String getShortName() {
        return shortName;
    }

    public Course getCourse(String number) {
        Course temp = new Course(this, number, null);
        for (Course course : courses)
            if (course.sameCourse(temp))
                return course;
        throw new NoSuchElementException("this course does not exist");
    }

    public void changeToSameDepartment(Department department) {
        this.name = department.getName();
        Set<Course> temp = new HashSet<>();
        for (Course course : department.getCourses()) {
            for (Course course1 :this.courses) {
                if (course.equals(course1))
                    course1.changeToSameCourse(course);
                else
                    temp.add(course);
            }
        }
        for (Course course : temp)
            this.courses.add(course);
    }

    public Set<Course> getCourses() {
        return courses;
    }

    public List<String> getCoursesForDisplay() {
        List<String> temp = new ArrayList<>();

        List<Course> tempCourseList = new ArrayList<>(courses);
        Collections.sort(tempCourseList);
        for (Course course : tempCourseList) {
            temp.add("course#" + course.getCourseName() + "&" +
                    course.getDepartment().getShortName() + "&" +
                    course.getCourseNumber());
        }
        return temp;

    }

    public String getName() {
        return name;
    }

    public Set<String> getCourseNumbers() {
        return courseNumbers;
    }

    public void addCourseNumber(String number) {
        courseNumbers.add(number);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Department)) return false;

        Department that = (Department) o;

        return shortName != null ? shortName.equals(that.shortName) : that.shortName == null;
    }

    @Override
    public int hashCode() {
        return shortName != null ? shortName.hashCode() : 0;
    }

    @Override
    public Iterator<Course> iterator() {
        return courses.iterator();
    }

    @Override
    public int compareTo(@NonNull Department o) {
        Pattern l = Pattern.compile("[a-zA-Z]");
        Matcher m1 = l.matcher(this.shortName);
        Matcher m2 = l.matcher(o.shortName);

        String s1 = "a";
        String s2 = "a";

        while (s1.equals(s2)) {
            if (m1.find() && m2.find()) {
                s1 = m1.group();
                s2 = m2.group();
            } else
                break;
        }

        return s1.compareTo(s2);
    }

    @Override
    public boolean match(String pattern) {
        return name.toLowerCase().contains(pattern);
    }
}
