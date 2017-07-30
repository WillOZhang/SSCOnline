package com.example.will.ssconlineversion.CourseScheduleManager.Managers;

import com.example.will.ssconlineversion.CourseScheduleManager.Building;
import com.example.will.ssconlineversion.CourseScheduleManager.Item;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Will on 2017/7/29.
 */

public class SearchManager {
    private static SearchManager instance;
    private Set<Item> items;

    private SearchManager() {
        this.items = new HashSet<Item>();
    }

    public static SearchManager getInstance() {
        // Do not modify the implementation of this method!
        if(instance == null) {
            instance = new SearchManager();
        }
        return instance;
    }

}
