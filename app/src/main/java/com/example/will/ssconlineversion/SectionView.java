package com.example.will.ssconlineversion;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class SectionView extends AppCompatActivity {
    // TODO: section view should be able to let user
    //       1. see the instructor info
    //       2. see the classroom info
    //       3. track the remaining seats info of the current section and inform user when a seat become available
    //       4. open web browser to register the current section

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_section_view);
    }
}
