package com.example.will.ssconlineversion.CourseScheduleManager;

import java.io.Serializable;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Will on 2017/5/20.
 */
public class InstructorManager implements Serializable {
    private Set<Instructor> instructors;
    private static InstructorManager instance;

    private InstructorManager() {
        instructors = new HashSet<Instructor>();
    }

    public static InstructorManager getInstance() {
        if(instance == null) {
            instance = new InstructorManager();
        }
        return instance;
    }

    public Instructor getInstructor(Instructor instructor) {
        try {
            for (Instructor instructor1 : instructors) {
                if (instructor1.equals(instructor)) {
                    instructor1.changeToSameInstructor(instructor);
                    return instructor1;
                }
            }
        } catch (ConcurrentModificationException e) {
            // avoiding doing foreach loop while other threads editing instructors variable
            getInstructor(instructor);
        }
        instructors.add(instructor);
        return instructor;
    }

    public static void setInstance(InstructorManager instance) {
        InstructorManager.instance = instance;
    }
}
