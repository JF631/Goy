package com.example.goy;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
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
        DateAdapter dateAdapter = new DateAdapter(dateList);
        dateView.setAdapter(dateAdapter);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

        FloatingActionButton fabDate = view.findViewById(R.id.add_date);
        fabDate.setOnClickListener(view1 -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
            DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                    LocalDate selectedDate = LocalDate.of(year, month + 1, dayOfMonth);
                    dataBaseHelper.insertDate(course, selectedDate.format(formatter));
                    dateAdapter.insertItem(selectedDate);
                }
            }, year, month, dayOfMonth);

            datePickerDialog.show();
        });

        dateAdapter.setOnItemLongClickListener(pos -> {
            String date = dateList.get(pos).format(formatter);
            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(getContext())
                    .setTitle("Datum löschen?")
                    .setMessage("Möchten Sie den " + date + " aus der Liste entfernen?")
                    .setCancelable(false)
                    .setPositiveButton("Löschen", (dialogInterface, i) -> {
                        if(!dataBaseHelper.deleteDate(course, date)){
                            Toast.makeText(getContext(), "Es ist ein Fehler aufgetreten", Toast.LENGTH_SHORT).show();
                        }else {
                            dateAdapter.deleteItem(pos);
                        }
                        dialogInterface.dismiss();

                    })
                    .setNegativeButton("Abbrechen", (dialogInterface, i) -> {
                        dialogInterface.dismiss();
                    });
            AlertDialog dialog = alertBuilder.create();
            dialog.show();

        });
        return view;
    }

    private void setUpView(View view, Course course){
        TextView titleView = view.findViewById(R.id.expanded_course_title);
        TextView dayView = view.findViewById(R.id.expand_course_days);
        TextView locationView = view.findViewById(R.id.expand_course_locations);
        TextView listTitleView = view.findViewById(R.id.expand_recycler_title);

        String times = "Kurszeiten: \n";
        List<Triple<String, LocalTime, LocalTime>> weekTimes = course.getCourseTimes();
        for(Triple<String, LocalTime, LocalTime> weekTime : weekTimes){
            times += weekTime.getFirst() + " von " +
                    weekTime.getSecond() +
                    " bis " + weekTime.getThird() + "\n";
        }
        String locations = "Kursorte: \n" +
                course.getLocationsFlattened().replaceAll(",", " und ");
        titleView.setText(course.getGroup());
        dayView.setText(times);
        locationView.setText(locations);
        listTitleView.setText("Kurs wurde an folgenden Terminen gehalten: \n");
    }
}
