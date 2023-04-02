package com.example.goy;

import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class CreateFragment extends DialogFragment {
    private RecyclerView weekdayView;
    private Button saveBtn, exitBtn;
    private Spinner departmentSpinner;
    private EditText etGroup, etTimeStart, etTimeEnd;
    private LocalTime start, end;
    private List<String> days = Arrays.asList("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday");
    private List<String> selected_days = new ArrayList<>();

    private OnCreateCourseClickedListener onCreateCourseClickedListener;
    private DateTimeFormatter formatter;

    public interface OnCreateCourseClickedListener{
        void onCreateCourseClicked(List<String> selectedDays, String department, String group, LocalTime start, LocalTime end);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.create_course_window, container, false);
        weekdayView = (RecyclerView) view.findViewById(R.id.create_weekday_list);
        saveBtn = (Button) view.findViewById(R.id.create_save_course);
        exitBtn = (Button) view.findViewById(R.id.create_exit);
        departmentSpinner = (Spinner) view.findViewById(R.id.create_spinner_departments);
        etGroup = (EditText) view.findViewById(R.id.create_et_group);
        etTimeStart = (EditText) view.findViewById(R.id.create_et_time_start);
        etTimeEnd = (EditText) view.findViewById(R.id.create_et_time_end);

        weekdayView.setLayoutManager(new LinearLayoutManager(getActivity()));
        WeekdayAdapter adapter = new WeekdayAdapter(days);
        weekdayView.setAdapter(adapter);

        formatter = DateTimeFormatter.ofPattern("HH:mm");
        LocalTime currentTime = LocalTime.now();
        LocalTime nextHour = currentTime.plusHours(1);
        etTimeStart.setText(currentTime.format(formatter));
        etTimeEnd.setText(nextHour.format(formatter));

        weekdayView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                View childView = rv.findChildViewUnder(e.getX(), e.getY());
                if(childView != null && e.getAction() == MotionEvent.ACTION_UP){
                    CheckedTextView checkedTextView = (CheckedTextView) childView.findViewById(R.id.create_weekday_item);
                    boolean isChecked = checkedTextView.isChecked();
                    checkedTextView.setChecked(!isChecked);

                    String day = adapter.getItem(rv.getChildAdapterPosition(childView));
                    return isChecked ? selected_days.remove(day) : selected_days.add(day);
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

        etTimeStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTimePickerDialog(etTimeStart);
            }
        });

        etTimeEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTimePickerDialog(etTimeEnd);
            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                start = LocalTime.parse(etTimeStart.getText());
                end = LocalTime.parse(etTimeEnd.getText());
                onCreateCourseClickedListener.onCreateCourseClicked(selected_days, departmentSpinner.getSelectedItem().toString(), etGroup.getText().toString(), start, end);
                dismiss();
            }
        });

        exitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        return view;
    }

    private void showTimePickerDialog(EditText et) {
        final Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        final LocalTime[] selectedTime = new LocalTime[1];

        TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(),
                new TimePickerDialog.OnTimeSetListener() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay,
                                          int minute) {
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        calendar.set(Calendar.MINUTE, minute);
                        selectedTime[0] = LocalTime.of(hourOfDay, minute);
                        et.setText(selectedTime[0].toString());
                    }
                }, hour, minute, true);
        timePickerDialog.show();
    }


    public void setOnCreateCourseClickedListener(OnCreateCourseClickedListener listener){
        onCreateCourseClickedListener = listener;
    }
}
