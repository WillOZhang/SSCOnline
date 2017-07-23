package com.example.will.ssconlineversion.CourseScheduleManager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Will on 2017/5/20.
 */
public class CourseManager {
    private Set<Department> departments;
    private static CourseManager instance;

    private Map<String, List<String>> facultyDepartmentPair = new HashMap<>();
    private Map<String, Set<Department>> facultyDepartmentObjectPair = new HashMap<>();

    private List<String> finishedParseFaculties = new ArrayList<>();

    private CourseManager() {
        departments = new HashSet<Department>();
    }

    public static CourseManager getInstance() {
        // Do not modify the implementation of this method!
        if(instance == null) {
            instance = new CourseManager();
        }
        return instance;
    }

    public void addDepartment(Department department) {
        for (Department department1 : departments){
            if (department.equals(department1)) {
                department1.changeToSameDepartment(department);
                return;
            }
        }
        departments.add(department);
        //facultyDepartmentObjectPair.get(department.getFaculty()).add(department);
    }

    public void addDepartmentForDownloadData(String faculty, Department department) {
        Set<Department> temp = facultyDepartmentObjectPair.get(faculty);
        if (temp == null)
            temp = new HashSet<>();
        for (Department department1 : departments){
            if (department.equals(department1)) {
                department1.changeToSameDepartment(department);
                temp.add(department1);
                return;
            }
        }
        departments.add(department);
        temp.add(department);
    }

    public void addCourse(String course, String number, String name) {
        Department temp = new Department(course);
        for (Department department : departments) {
            if (department.equals(temp)) {
                Course tempCourse = new Course(department, number, name);
                department.addCourse(tempCourse);
            }
        }
    }

    public Course getCourse(String course, String number) {
        Department temp = new Department(course);
        for (Department department : departments)
            if (department.equals(temp))
                return department.getCourse(number);
        return null;
    }

    public void addCourse(Department department, Course course) {
        for (Department next : departments)
            if (department.equals(next))
                next.addCourse(course);
    }

    public Set<Department> getDepartments() {
        return departments;
    }

    public void print() {
        for (Department department : departments)
            for (Course course : department.getCourses())
                course.print();
    }

    public Course hasTheCourse(String name, String number) {
        Department temp = new Department(name);
        Course tempCourse = new Course(temp, number, null);
        for (Department department : departments)
            if (department.equals(temp))
                for (Course course : department.getCourses())
                    if (course.sameCourse(tempCourse))
                        return course;
        return null;
    }

    public List<String> getDepartments(String faculty) {
        List<String> temp = new ArrayList<>();
        List<Department> tempDepartment = new ArrayList<>(facultyDepartmentObjectPair.get(faculty));
        Collections.sort(tempDepartment);
        for (Department department : tempDepartment) {
            temp.add(faculty + "@" + department.getName() + "@" + department.getShortName());
        }
        if (!finishedParseFaculties.contains(faculty))
            finishedParseFaculties.add(faculty);
        return temp;
    }

    public List<String> getFinishedParseFaculties() {
        return finishedParseFaculties;
    }

    public Department getDepartment(String shortName) {
        Department temp = null;
        try {
            for (Department department : departments) {
                if (department.getShortName().equals(shortName)) {
                    temp = department;
                    break;
                }
            }
        } catch (ConcurrentModificationException c) {
            getDepartment(shortName);
        }
        return temp;
    }

    public void addFDPair(String faculty, List<String> department) {
        facultyDepartmentPair.put(faculty, department);
        Set<Department> temp = facultyDepartmentObjectPair.get(faculty);
        if (temp == null || temp.size() > 0)
            facultyDepartmentObjectPair.put(faculty, new HashSet<Department>());
    }

    public List<String> getDepartmentsByFaculty(String faculty) {
        return facultyDepartmentPair.get(faculty);
    }

    public List<String> getCoursesByDepartment(Department department) {
        List<String> temp = new ArrayList<>();
        for (String number : department.getCourseNumbers()) {
            temp.add(department.getShortName() + number);
        }
        return temp;
    }

    public List<String> getSectionsByCourse(Course course) {
        List<String> temp = new ArrayList<>();
        for (String section : course.getSectionsList()) {
            temp.add(course.getDepartment().getShortName() + course.getCourseNumber() + section);
        }
        return temp;
    }

    public static void setInstance(CourseManager courseManager) {
        instance = courseManager;
    }

    public boolean hasFacultyData(String faculty) {
        return facultyDepartmentObjectPair.get(faculty) != null;
    }

    public boolean hasDepartmentData(String departmentShortName) {
        Department temp = getDepartment(departmentShortName);
        return temp == null;
    }
}
