package com.example.goy;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import org.eazegraph.lib.charts.PieChart;
import org.eazegraph.lib.models.PieModel;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class StatsFragment extends Fragment {

    private DataBaseHelper dataBaseHelper;
    private PieChart pieChart;
    private Button btn_total, btn_week, btn_month, btn_year;
    public StatsFragment(){}

    @RequiresApi(api = Build.VERSION_CODES.O)
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.stats_window, container, false);
        pieChart = view.findViewById(R.id.piechart);
        btn_total = view.findViewById(R.id.stats_total_btn);
        btn_week = view.findViewById(R.id.stats_week_btn);
        btn_month = view.findViewById(R.id.stats_month_btn);
        btn_year = view.findViewById(R.id.stats_year_btn);
        dataBaseHelper = new DataBaseHelper(requireContext());
        setUpPieChart(view, null, null);

        btn_total.setOnClickListener(view1 -> setUpPieChart(view, null,null));
        btn_week.setOnClickListener(view1 -> {
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minus(1, ChronoUnit.WEEKS);
            setUpPieChart(view, startDate, endDate);
        });
        btn_month.setOnClickListener(view1 -> {
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minus(1, ChronoUnit.MONTHS);
            setUpPieChart(view, startDate, endDate);
        });
        btn_year.setOnClickListener(view1 -> {
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minus(1, ChronoUnit.YEARS);
            setUpPieChart(view, startDate, endDate);
        });

        return  view;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setUpPieChart(View view, @Nullable LocalDate startDate, @Nullable LocalDate endDate){
        pieChart.clearChart();
        List<Pair<Course, Double>> courseTimes = getCourseTimes(startDate, endDate);
        double totalTime = Utilities.sum(courseTimes);
        LinearLayout legendLayout = view.findViewById(R.id.legendLayout);
        legendLayout.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        for(Pair<Course, Double> courseTime : courseTimes){
            int dotColor = generateRandomColor();
            pieChart.addPieSlice(new PieModel(
                    courseTime.getFirst().getGroup(),
                    (float) (courseTime.getSecond() / totalTime * 100),
                   dotColor
            ));

            View legendItemView = inflater.inflate(R.layout.legend_item, null);
            TextView legendLabel = legendItemView.findViewById(R.id.legendLabel);
            // Set the colored dot as drawableStart of the TextView
            Drawable coloredDot = generateColoredDot(dotColor);
            legendLabel.setCompoundDrawablesRelativeWithIntrinsicBounds(coloredDot, null, null, null);
            legendLabel.setText(courseTime.getFirst().getGroup());
            legendLayout.addView(legendItemView);
        }
        pieChart.startAnimation();
    }

    private Drawable generateColoredDot(int color) {
        ShapeDrawable shapeDrawable = new ShapeDrawable(new OvalShape());
        shapeDrawable.setIntrinsicWidth(20); // Adjust the width as needed
        shapeDrawable.setIntrinsicHeight(20); // Adjust the height as needed
        shapeDrawable.getPaint().setColor(color);
        return shapeDrawable;
    }


    private int generateRandomColor() {
        // Generate random values for red, green, and blue (RGB) channels
        int red = (int) (Math.random() * 256);
        int green = (int) (Math.random() * 256);
        int blue = (int) (Math.random() * 256);

        // Combine RGB channels into a single color value
        int color = Color.rgb(red, green, blue);

        return color;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private List<Pair<Course, Double>> getCourseTimes(@Nullable LocalDate start, @Nullable LocalDate end){
        List<Course> courseList = dataBaseHelper.getCourses();
        List<Pair<Course, Double>> rtrn = new ArrayList<>();
        for(Course course : courseList){
            rtrn.add(new Pair<>(course, course.getTotalTime(dataBaseHelper, start, end)));
        }
        return rtrn;
    }
}
