package com.example.will.ssconlineversion.CourseScheduleManager;

/**
 * Created by Will on 2017/7/30.
 */

public class Item implements Searchable {
    private String tag;
    private String name;

    public Item(String tag, String name) {
        this.tag = tag;
        this.name = name;
    }

    public boolean match(String tag, String pattern) {
        if (!tag.equals(this.tag))
            return false;
        return match(pattern);
    }

    @Override
    public boolean match(String pattern) {
        return false;
    }
}
