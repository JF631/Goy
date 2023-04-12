package com.example.goy;

import android.app.DatePickerDialog;
import android.os.Build;
import android.os.Bundle;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.TranslateAnimation;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Calendar;
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
        setUpView(view, course);
        CardView cardView = view.findViewById(R.id.expanded_item);
        RecyclerView dateView = view.findViewById(R.id.show_course_dates);
        dateView.setLayoutManager(new LinearLayoutManager(getActivity()));
        DateAdapter dateAdapter = new DateAdapter(dateList, course);
        dateView.setAdapter(dateAdapter);

        FloatingActionButton addButton = view.findViewById(R.id.add_date);
        addButton.setTransitionName("add_button");

        Transition sharedElementTransition = TransitionInflater.from(getContext())
                .inflateTransition(android.R.transition.move);
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

        dateAdapter.setOnItemLongClickListener(pos -> {
            dateAdapter.deleteItem(pos, getContext());

        });
        return view;
    }

    private void setUpView(View view, Course course){
        TextView titleView = view.findViewById(R.id.expanded_course_title);
        TextView dayView = view.findViewById(R.id.expand_course_days);
        TextView locationView = view.findViewById(R.id.expand_course_locations);
        TextView listTitleView = view.findViewById(R.id.expand_recycler_title);
        TextView departmentView = view.findViewById(R.id.expand_course_department);

        String times = "Kurszeiten: \n";
        List<Triple<String, LocalTime, LocalTime>> weekTimes = course.getCourseTimes();
        for(Triple<String, LocalTime, LocalTime> weekTime : weekTimes){
            times += weekTime.getFirst() + " von " +
                    weekTime.getSecond() +
                    " bis " + weekTime.getThird() + "\n";
        }
        String locations = "Kursorte: \n" +
                course.getLocationsFlattened().replaceAll(",", " und ") + "\n";
        String department = "Abteilung: \n" +
                course.getDepartment() + "\n";
        titleView.setText(course.getGroup());
        dayView.setText(times);
        locationView.setText(locations);
        departmentView.setText(department);
        listTitleView.setText(course.getGroup() + " wurde an folgenden Terminen gehalten: \n");
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onDestroy() {
        super.onDestroy();
        ((MainActivity) requireActivity()).showNavBar();
    }

}
