package com.example.goy;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CreateFragment extends DialogFragment {
    private Spinner departmentSpinner;
    private EditText etGroup;
    private CheckBox cbHall, cbTrack;
    private static final List<String> days = Arrays.asList("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday");
    private final ArrayList<String> locations = new ArrayList<>();
    private final List<Triple<String, LocalTime, LocalTime>> timeList = new ArrayList<>();

    private OnCreateCourseClickedListener onCreateCourseClickedListener;

    public interface OnCreateCourseClickedListener{
        void onCreateCourseClicked(List<Triple<String,LocalTime, LocalTime>> times, String department, String group, ArrayList<String> locations);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.create_course_window, container, false);
        RecyclerView weekdayView = view.findViewById(R.id.create_weekday_list);
        Button saveBtn = view.findViewById(R.id.create_save_course);
        Button exitBtn = view.findViewById(R.id.create_exit);
        departmentSpinner = view.findViewById(R.id.create_spinner_departments);
        etGroup = view.findViewById(R.id.create_et_group);
        cbHall = view.findViewById(R.id.create_checkbox_halle);
        cbTrack = view.findViewById(R.id.create_checkbox_sportplatz);

        weekdayView.setLayoutManager(new LinearLayoutManager(getActivity()));
        WeekdayAdapter adapter = new WeekdayAdapter(days);
        weekdayView.setAdapter(adapter);

        weekdayView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                View childView = rv.findChildViewUnder(e.getX(), e.getY());
                if(childView != null){
                    CheckedTextView checkedTextView = childView.findViewById(R.id.create_weekday_item);
                    TextView startTextView = childView.findViewById(R.id.create_start_item);
                    TextView endTextView = childView.findViewById(R.id.create_end_item);
                    boolean isChecked = checkedTextView.isChecked();
                    if(!isChecked && startTextView.getText().toString().equals("start") ||
                            endTextView.getText().toString().equals("end")){
                        return false;
                    }
                    checkedTextView.setChecked(!isChecked);
                    String day = adapter.getItem(rv.getChildAdapterPosition(childView)).toUpperCase();
                    LocalTime start = LocalTime.parse(startTextView.getText().toString());
                    LocalTime end = LocalTime.parse(endTextView.getText().toString());
                    Triple<String, LocalTime, LocalTime> time = new Triple<>(day, start, end);
                    return isChecked ? timeList.remove(time) : timeList.add(time);
                }
                return false;
            }

            @Override
            public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {

            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

            }
        });

        cbHall.setOnCheckedChangeListener((compoundButton, b) -> {
            boolean c = b ? locations.add(cbHall.getText().toString()) : locations.remove(cbHall.getText().toString());

        });

        cbTrack.setOnCheckedChangeListener((compoundButton, b) -> {
            boolean c = b ? locations.add(cbTrack.getText().toString()) : locations.remove(cbTrack.getText().toString());

        });

        saveBtn.setOnClickListener(view1 -> {
            if(timeList.size() <= 0){
                Toast.makeText(getActivity(), "Select at least one day", Toast.LENGTH_SHORT).show();
                return;
            }

            String group = etGroup.getText().toString();
            if(group.isEmpty()){
                Toast.makeText(getActivity(), "Provide a group name", Toast.LENGTH_SHORT).show();
                return;
            }

            for(Triple<String, LocalTime, LocalTime> time : timeList){
                if(time.getSecond().isAfter(time.getThird()) || time.getSecond().equals(time.getThird())){
                    Toast.makeText(getActivity(), "Check your entered times", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            if (locations.size() <= 0){
                Toast.makeText(getActivity(), "please select at least one location", Toast.LENGTH_SHORT).show();
                return;
            }
            onCreateCourseClickedListener.onCreateCourseClicked(
                    timeList,
                    departmentSpinner.getSelectedItem().toString(),
                    group,
                    locations);
            dismiss();
        });

        exitBtn.setOnClickListener(view12 -> dismiss());

        return view;
    }

    public void setOnCreateCourseClickedListener(OnCreateCourseClickedListener listener){
        onCreateCourseClickedListener = listener;
    }
}
