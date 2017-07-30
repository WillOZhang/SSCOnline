package com.example.will.ssconlineversion.CourseScheduleManager;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by Will on 2017/5/20.
 */
public class Building implements Serializable, Iterable<Classroom>, Searchable {
    private String name;

    private Set<Classroom> classrooms;

    public Building(String name) {
        this.name = name;
        classrooms = new HashSet<Classroom>();
    }

    public void addClassroom(Classroom classroom) {
        for (Classroom classroom1 : classrooms) {
            if (classroom.equals(classroom1)) {
                classroom1.addBuilding(this);
                classroom1.changeToSameClassroom(classroom);
                return;
            }
        }
        classrooms.add(classroom);
    }

    public String getName() {
        return name;
    }

    public Set<Classroom> getClassrooms() {
        return classrooms;
    }

    @Override
    public Iterator<Classroom> iterator() {
        return classrooms.iterator();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Building)) return false;

        Building that = (Building) o;

        return name != null ? name.equals(that.name) : that.name == null;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    @Override
    public boolean match(String pattern) {
        return name.toLowerCase().contains(pattern);
    }
}
