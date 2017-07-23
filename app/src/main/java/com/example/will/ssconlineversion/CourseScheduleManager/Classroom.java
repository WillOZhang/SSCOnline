package com.example.will.ssconlineversion.CourseScheduleManager;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Will on 2017/5/20.
 */
public class Classroom implements Serializable {
    private String name;

    private Building buildingThatThisClassroomAt;
    private Set<Section> coursesHoldInThisClassroom;

    public Classroom(String name, Building building) {
        this.name = name;
        buildingThatThisClassroomAt = building;
        coursesHoldInThisClassroom = new HashSet<Section>();
    }

    public void addBuilding(Building building) {
        buildingThatThisClassroomAt = building;
    }

    public void changeToSameClassroom(Classroom classroom) {
        boolean contains = false;
        for (Section section : classroom.getCoursesHoldInThisClassroom()) {
            for (Section section1 : coursesHoldInThisClassroom) {
                if (section1.equals(section)) {
                    section1.changeToSameSection(section);
                    contains = true;
                    break;
                }
            }
            if (!contains) {
                coursesHoldInThisClassroom.add(section);
                contains = false;
            }
        }
    }

    public String getName() {
        return name;
    }

    public Building getBuildingThatThisClassroomAt() {
        return buildingThatThisClassroomAt;
    }

    public Set<Section> getCoursesHoldInThisClassroom() {
        return coursesHoldInThisClassroom;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Classroom)) return false;

        Classroom classroom = (Classroom) o;

        if (name != null ? !name.equals(classroom.name) : classroom.name != null) return false;
        return buildingThatThisClassroomAt != null ? buildingThatThisClassroomAt.equals(classroom.buildingThatThisClassroomAt) : classroom.buildingThatThisClassroomAt == null;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (buildingThatThisClassroomAt != null ? buildingThatThisClassroomAt.hashCode() : 0);
        return result;
    }
}
