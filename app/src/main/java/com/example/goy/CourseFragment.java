package com.example.goy;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDate;
import java.util.List;

public class CourseFragment extends Fragment {

    public CourseFragment(){}
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Course course = (Course) requireArguments().getParcelable("course");
        DataBaseHelper dataBaseHelper = new DataBaseHelper(getContext());
        List<LocalDate> dateList = dataBaseHelper.getDates(course);

        View view = inflater.inflate(R.layout.expanded_course, container, false);
        CardView cardView = view.findViewById(R.id.expanded_item);
        RecyclerView dateView = view.findViewById(R.id.show_course_dates);
        TextView titleView = view.findViewById(R.id.expanded_course_title);

        titleView.setText(course.getGroup());
        dateView.setLayoutManager(new LinearLayoutManager(getActivity()));
        DateAdapter dateAdapter = new DateAdapter(dateList);
        dateView.setAdapter(dateAdapter);
        return view;
    }
}
