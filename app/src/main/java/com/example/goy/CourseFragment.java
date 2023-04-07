package com.example.goy;

import androidx.fragment.app.Fragment;

import java.util.Date;
import java.util.List;

public class CourseFragment extends Fragment {

    private List<Date> dateList;

    public CourseFragment(List<Date> dateList){
       this.dateList = dateList;
    }
}
