package com.example.will.ssconlineversion.CourseScheduleManager;

/**
 * Created by Will on 2017/7/29.
 */

public interface Searchable {

    // match consumes a string that has no space or any wired char inside it
    // returns a boolean that indicates if the object matches the pattern
    boolean match(String pattern);
}
