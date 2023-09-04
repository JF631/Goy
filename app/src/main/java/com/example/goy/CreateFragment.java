package com.example.goy;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
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

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class CreateFragment extends BottomSheetDialogFragment {
    private Spinner departmentSpinner;
    private EditText etGroup;
    private CheckBox cbHall, cbTrack;
    private static final List<String> days = Arrays.asList("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday");
    private Set<String> locations = new HashSet<>();
    private List<Triple<String, LocalTime, LocalTime>> timeList = new ArrayList<>();
    private final Course course;
    private static final String[] departments = {"Leichtathletik", "Turnen", "Fitness"};


    private OnCreateCourseClickedListener onCreateCourseClickedListener;

    public interface OnCreateCourseClickedListener{
        void onCreateCourseClicked(List<Triple<String,LocalTime, LocalTime>> times, String department, String group, Set<String> locations);
    }

    public CreateFragment(@Nullable Course course){
        this.course = course;
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View view = inflater.inflate(R.layout.create_course_window, container, false);
        RecyclerView weekdayView = view.findViewById(R.id.create_weekday_list);
        Button saveBtn = view.findViewById(R.id.btn_save_course);
        Button exitBtn = view.findViewById(R.id.create_exit);
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, departments);
        departmentSpinner = view.findViewById(R.id.create_spinner_departments);
        departmentSpinner.setAdapter(spinnerAdapter);
        etGroup = view.findViewById(R.id.create_et_group);
        cbHall = view.findViewById(R.id.create_checkbox_halle);
        cbTrack = view.findViewById(R.id.create_checkbox_sportplatz);
        locations = Course.getLocations(course);
        departmentSpinner.setSelection(spinnerAdapter.getPosition(Course.getDepartment(course)));
        etGroup.setText(Course.getGroup(course));
        cbHall.setChecked(locations != null && locations.contains(cbHall.getText()));
        cbTrack.setChecked(locations != null && locations.contains(cbTrack.getText()));


        HashMap<String, Pair<LocalTime, LocalTime>> timeMap = Course.getCourseTimesMap(course);
        if(timeMap != null){
            timeList = timeMap.entrySet()
                    .stream()
                    .map(entry -> new Triple<>(entry.getKey(), entry.getValue().getFirst(), entry.getValue().getSecond()))
                    .collect(Collectors.toList());
            saveBtn.setText("übernehmen");
        }
        weekdayView.setLayoutManager(new LinearLayoutManager(getActivity()));
        WeekdayAdapter adapter = new WeekdayAdapter(days, timeMap, getChildFragmentManager());
        weekdayView.setAdapter(adapter);


        adapter.setOnItemClickListener((pos, itemView) -> {
            CheckedTextView checkedTextView = itemView.findViewById(R.id.create_weekday_item);
            TextView startTextView = itemView.findViewById(R.id.create_start_item);
            TextView endTextView = itemView.findViewById(R.id.create_end_item);
            boolean isChecked = checkedTextView.isChecked();
            LocalTime start = Utilities.tryParseTime(startTextView.getText().toString());
            LocalTime end = Utilities.tryParseTime(endTextView.getText().toString());
            if(!isChecked && start == null || end  == null){
                return;
            }
            String day = adapter.getItem(pos).toUpperCase();
            Triple<String, LocalTime, LocalTime> time = new Triple<>(day, start, end);
            Iterator<Triple<String, LocalTime, LocalTime>> iterator = timeList.iterator();
            while (iterator.hasNext()) {
                Triple<String, LocalTime, LocalTime> currentlySaved = iterator.next();
                if (currentlySaved.getFirst().equals(time.getFirst())) {
                    iterator.remove();
                }
            }
            timeList.add(time);
            checkedTextView.setChecked(!isChecked);
        });

        cbHall.setOnCheckedChangeListener((compoundButton, b) -> {
            boolean c = b ? locations.add(cbHall.getText().toString()) : locations.remove(cbHall.getText().toString());

        });

        cbTrack.setOnCheckedChangeListener((compoundButton, b) -> {
            boolean c = b ? locations.add(cbTrack.getText().toString()) : locations.remove(cbTrack.getText().toString());

        });

        saveBtn.setOnClickListener(view1 -> {
            if(timeList.size() == 0){
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

            if (locations.size() == 0){
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
