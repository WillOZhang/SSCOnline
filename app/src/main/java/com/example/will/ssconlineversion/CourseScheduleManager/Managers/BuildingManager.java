package com.example.will.ssconlineversion.CourseScheduleManager.Managers;

import com.example.will.ssconlineversion.CourseScheduleManager.Building;
import com.example.will.ssconlineversion.CourseScheduleManager.Classroom;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Will on 2017/5/20.
 */
public class BuildingManager implements Serializable {
    private Set<Building> buildings;
    private static BuildingManager instance;

    private BuildingManager() {
        this.buildings = new HashSet<Building>();
    }

    public static void setInstance(BuildingManager instance) {
        BuildingManager.instance = instance;
    }

    public static BuildingManager getInstance() {
        // Do not modify the implementation of this method!
        if(instance == null) {
            instance = new BuildingManager();
        }
        return instance;
    }

    public Building getBuilding(Building building) {
        for (Building building1 : buildings) {
            if (building.equals(building1)) {
                for (Classroom classroom : building) {
                    building1.addClassroom(classroom);
                    return building1;
                }
            }
        }
        buildings.add(building);
        return building;
    }

    public void addClassroom(Classroom classroom) {

    }
}
