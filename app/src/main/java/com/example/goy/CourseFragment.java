package com.example.goy;

import android.app.DatePickerDialog;
import android.os.Build;
import android.os.Bundle;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CourseFragment extends Fragment implements CreateFragment.OnCreateCourseClickedListener{

    private Course course;
    private TextView courseDepartment, courseGroup, courseTimes, courseLocations, listTitleView;

    public CourseFragment(){}
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        course = requireArguments().getParcelable("course");
        DataBaseHelper dataBaseHelper = new DataBaseHelper(getContext());
        List<LocalDate> dateList = dataBaseHelper.getDates(course, true);
        View view = inflater.inflate(R.layout.expanded_course, container, false);
        setUpView(view, course);
        ImageView imageView = view.findViewById(R.id.expanded_edit);
        RecyclerView dateView = view.findViewById(R.id.show_course_dates);
        dateView.setLayoutManager(new LinearLayoutManager(getActivity()));
        DateAdapter dateAdapter = new DateAdapter(dateList, course);
        dateView.setAdapter(dateAdapter);

        FloatingActionButton addButton = view.findViewById(R.id.add_date);
        addButton.setTransitionName("add_button");

        Transition sharedElementTransition = TransitionInflater.from(getContext())
                .inflateTransition(android.R.transition.slide_bottom);
        sharedElementTransition.addTarget(addButton);

        getActivity().getWindow().setSharedElementEnterTransition(sharedElementTransition);

        FloatingActionButton fabDate = view.findViewById(R.id.add_date);
        fabDate.setOnClickListener(view1 -> {
            List<String> courseDays = dataBaseHelper.getWeekDays(course);
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
            DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), (view2, year1, month1, dayOfMonth1) -> {
                LocalDate selectedDate = LocalDate.of(year1, month1 + 1, dayOfMonth1);
                if(!courseDays.contains(selectedDate.getDayOfWeek().toString()))
                    return;
                dataBaseHelper.insertDate(course, selectedDate);
                dateAdapter.insertItem(selectedDate);
            }, year, month, dayOfMonth);

            datePickerDialog.show();
        });

        dateAdapter.setOnItemLongClickListener(pos -> dateAdapter.deleteItem(pos, getContext()));


        imageView.setOnClickListener(view1 -> {
            showCreate();
        });
        return view;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void setUpView(View view, Course course){
        courseGroup = view.findViewById(R.id.expanded_course_title);
        courseTimes = view.findViewById(R.id.expand_course_days);
        courseLocations = view.findViewById(R.id.expand_course_locations);
        listTitleView = view.findViewById(R.id.expand_recycler_title);
        courseDepartment = view.findViewById(R.id.expand_course_department);

        String times = "Kurszeiten: \n";
        HashMap<String, Pair<LocalTime, LocalTime>> hashMap = course.getCourseTimesMap();
        for(Map.Entry<String, Pair<LocalTime, LocalTime>> timeEntry : hashMap.entrySet()){
            times += timeEntry.getKey() + " von " +
                    timeEntry.getValue().getFirst() +
                    " bis " + timeEntry.getValue().getSecond() + "\n";
        }
        String locations = "Kursorte: \n" +
                course.getLocationsFlattened().replaceAll(",", " und ") + "\n";
        String department = "Abteilung: \n" +
                course.getDepartment() + "\n";
        setTitle(course.getGroup());
        setCourseTimes(times);
        setCourseLocations(locations);
        setCourseDepartment(department);
        setListViewTitle(course.getGroup() + " wurde an folgenden Terminen gehalten: \n");
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void showCreate(){
        CreateFragment createFragment = new CreateFragment(course);
        createFragment.show(getChildFragmentManager(), "create_course");
        createFragment.setOnCreateCourseClickedListener(this);
    }

    private void setTitle(String group){
        courseGroup.setText(group);
    }

    private void setCourseTimes(String times){
        courseTimes.setText(times);
    }

    private void setCourseLocations(String locations){
        courseLocations.setText(locations);
    }

    private void setCourseDepartment(String department){
        courseDepartment.setText(department);
    }

    private void setListViewTitle(String listViewTitle){
        listTitleView.setText(listViewTitle);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onDestroy() {
        super.onDestroy();
        ((MainActivity) requireActivity()).showNavBar();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreateCourseClicked(List<Triple<String, LocalTime, LocalTime>> times, String department, String group, Set<String> locations) {
        DataBaseHelper dbHelper = new DataBaseHelper(requireContext());
        course.setCourseTimes(times);
        course.setLocations(locations);
        course.setDepartment(department);
        course.setGroup(group);

        String timeString = "Kurszeiten: \n";
        HashMap<String, Pair<LocalTime, LocalTime>> hashMap = course.getCourseTimesMap();
        for(Map.Entry<String, Pair<LocalTime, LocalTime>> timeEntry : hashMap.entrySet()){
            timeString += timeEntry.getKey() + " von " +
                    timeEntry.getValue().getFirst() +
                    " bis " + timeEntry.getValue().getSecond() + "\n";
        }
        String locationString = "Kursorte: \n" +
                course.getLocationsFlattened().replaceAll(",", " und ") + "\n";
        String departmentString = "Abteilung: \n" +
                course.getDepartment() + "\n";
        setCourseDepartment(departmentString);
        setTitle(group);
        setCourseTimes(timeString);
        setCourseLocations(locationString);
        setListViewTitle(group + " wurde an folgenden Terminen gehalten: \n");

        dbHelper.updateLocations(course, locations);
        dbHelper.updateBasicGroupInfo(course, "department_name", department);
        dbHelper.updateBasicGroupInfo(course, "group_name", group);
        dbHelper.updateCourseTimes(course, times);
        dbHelper.close();
    }
}
