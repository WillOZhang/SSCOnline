package com.example.will.ssconlineversion.CourseScheduleManager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Will on 2017/5/20.
 */
public class Instructor implements Serializable, Searchable {
    private String name;

    private List<Course> courses;
    private List<Section> sections;
    private String website;

    public Instructor(String name) {
        courses = new ArrayList<Course>();
        sections = new ArrayList<Section>();
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public List<Course> getCourses() {
        return courses;
    }

    public List<Section> getSections() {
        return sections;
    }

    public String getWebsite() {
        return website;
    }

    public void changeToSameInstructor(Instructor instructor) {
        this.website = instructor.getWebsite();
        for (Course course : instructor.getCourses())
            this.addCourse(course);
        for (Section section : instructor.getSections())
            this.addSection(section);
    }

    public void addCourse(Course course) {
        for (Course temp : this.courses) {
            if (temp.equals(course))
                temp.changeToSameCourse(course);
            return;
        }
        courses.add(course);
    }

    public void addSection(Section section) {
        for (Section temp : sections)
            if (temp.equals(section))
                return;
        sections.add(section);
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Instructor)) return false;

        Instructor that = (Instructor) o;

        return name != null ? name.equals(that.name) : that.name == null;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    @Override
    public boolean match(String pattern) {
        return name.toLowerCase().replaceAll(" ", "").contains(pattern);
    }
}
