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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.google.android.material.color.MaterialColors;

import org.eazegraph.lib.charts.BarChart;
import org.eazegraph.lib.charts.PieChart;
import org.eazegraph.lib.models.BarModel;
import org.eazegraph.lib.models.PieModel;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;

public class StatsFragment extends Fragment {

    private DataBaseHelper dataBaseHelper;
    private PieChart pieChart;
    private BarChart barChart;
    private Button btn_total, btn_week, btn_month, btn_year;
    private Spinner department_spinner, year_spinner;

    public StatsFragment(){}

    @RequiresApi(api = Build.VERSION_CODES.O)
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.stats_window, container, false);
        String[] departments = new String[]{"Kurse", "Abteilungen"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item, departments);
        department_spinner = view.findViewById(R.id.stats_spinner_group);
        year_spinner = view.findViewById(R.id.stats_spinner_year);
        pieChart = view.findViewById(R.id.piechart);
        barChart = view.findViewById(R.id.barchart);
        btn_total = view.findViewById(R.id.stats_total_btn);
        btn_week = view.findViewById(R.id.stats_week_btn);
        btn_month = view.findViewById(R.id.stats_month_btn);
        btn_year = view.findViewById(R.id.stats_year_btn);
        department_spinner.setAdapter(spinnerAdapter);
        dataBaseHelper = new DataBaseHelper(requireContext());
        AtomicReference<LocalDate> start = new AtomicReference<>(),
                end = new AtomicReference<>();
        updatePieChart(null, null, view);
        setBtnHighlight(Period.OVERALL);
        Pair<LocalDate, LocalDate> minMax = dataBaseHelper.getMinMaxDate();
        int min = minMax.getFirst().getYear();
        int max = minMax.getSecond().getYear();
        int[] interpolated_years = IntStream.rangeClosed(min, max).toArray();
        Integer[] yearsArray = Arrays.stream(interpolated_years)
                .boxed()
                .toArray(Integer[]::new);
        ArrayAdapter<Integer> yearsAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, yearsArray);
        year_spinner.setAdapter(yearsAdapter);
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        int currentYearIndex = Arrays.binarySearch(yearsArray, currentYear);
        year_spinner.setSelection(currentYearIndex);
        setUpBarChart(view, (int)year_spinner.getSelectedItem());
        btn_total.setOnClickListener(view1 -> {
            setBtnHighlight(Period.OVERALL);
            start.set(null);
            end.set(null);
            updatePieChart(start.get(), end.get(), view);
        });
        btn_week.setOnClickListener(view1 -> {
            setBtnHighlight(Period.WEEK);
            end.set(LocalDate.now());
            start.set(end.get().minus(1, ChronoUnit.WEEKS));
            updatePieChart(start.get(), end.get(), view);
        });
        btn_month.setOnClickListener(view1 -> {
            setBtnHighlight(Period.MONTH);
            btn_month.setBackgroundResource(R.drawable.btn_highlight_oval);
            end.set(LocalDate.now());
            start.set(end.get().minus(1, ChronoUnit.MONTHS));
            updatePieChart(start.get(), end.get(), view);
        });
        btn_year.setOnClickListener(view1 -> {
            setBtnHighlight(Period.YEAR);
            end.set(LocalDate.now());
            start.set(end.get().minus(1, ChronoUnit.YEARS));
            updatePieChart(start.get(), end.get(), view);
        });

        department_spinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view2,
                                       int i, long l) {
                updatePieChart(start.get(), end.get(), view);
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        year_spinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view2,
                                       int i, long l) {
                int year  = (int) adapterView.getItemAtPosition(i);
                setUpBarChart(view, year);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        return  view;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setUpPieChart(View view, List<Pair<String, Double>> times)
    {
        pieChart.clearChart();
        double totalTime = Utilities.sum(times);
        LinearLayout legendLayout = view.findViewById(R.id.legendLayout_pie);
        legendLayout.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        for(Pair<String, Double> courseTime : times){
            double totalHours = courseTime.getSecond();
            if(totalHours == 0.f)
                continue;
            int dotColor = generateRandomColor();
            float coursePercentage = (float) (totalHours / totalTime * 100);
            pieChart.addPieSlice(new PieModel(
                    courseTime.getFirst(),
                    coursePercentage,
                    dotColor
            ));

            View legendItemView = inflater.inflate(R.layout.legend_item, null);
            TextView legendLabel = legendItemView.findViewById(R.id.legendLabel);
            int color = MaterialColors.getColor(view, com.google.android.material.R.attr.colorOnSurface);
            legendLabel.setTextColor(color);
            // Set the colored dot as drawableStart of the TextView
            Drawable coloredDot = generateColoredDot(dotColor);
            legendLabel.setCompoundDrawablesRelativeWithIntrinsicBounds(coloredDot, null, null, null);
            legendLabel.setText(String.format("%s, %.2fh (%.2f%%)",
                    courseTime.getFirst(), totalHours ,
                    coursePercentage));
            legendLayout.addView(legendItemView);
        }
        int innerColor = MaterialColors.getColor(view, com.google.android.material.R.attr.colorAccent);
        pieChart.setInnerPaddingColor(innerColor);
        pieChart.setUseInnerValue(true);
        pieChart.setInnerValueString(String.format("%.2fh", totalTime));
        pieChart.startAnimation();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void updatePieChart(@Nullable LocalDate start,
                                @Nullable LocalDate end, View view)
    {
        String selection = department_spinner.getSelectedItem().toString();
        List<Pair<String, Double>> times;
        if(selection.equals("Kurse"))
            times = getCourseTimes(start, end, false);
        else
            times = getCourseTimes(start, end, true);
        setUpPieChart(view, times);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setUpBarChart(View view, int year)
    {
        barChart.clearChart();
        int currentYear = year;
        int color = MaterialColors.getColor(view,
                com.google.android.material.R.attr.colorPrimary);
        for(int i = 1; i<=12; ++i) {
            LocalDate startDate = LocalDate.of(currentYear, i, 1);
            LocalDate endDate = LocalDate.of(currentYear, i,
                    startDate.lengthOfMonth());
            List<Pair<String, Double>> monthTimes = getCourseTimes(startDate,
                    endDate, false);
            double monthTime = Utilities.sum(monthTimes);
            barChart.addBar(new BarModel(
                    String.valueOf(endDate.getMonthValue()),
                    (float) monthTime,
                    color
            ));
        }
        barChart.startAnimation();
    }

    private void setBtnHighlight(Period period)
    {
        btn_total.setBackground(null);
        btn_year.setBackground(null);
        btn_month.setBackground(null);
        btn_week.setBackground(null);

        switch (period){
            case OVERALL:
                btn_total.setBackgroundResource(R.drawable.btn_highlight_oval);
                break;
            case YEAR:
                btn_year.setBackgroundResource(R.drawable.btn_highlight_oval);
                break;
            case MONTH:
                btn_month.setBackgroundResource(R.drawable.btn_highlight_oval);
                break;
            case WEEK:
                btn_week.setBackgroundResource(R.drawable.btn_highlight_oval);
                break;
        }
    }

    private Drawable generateColoredDot(int color)
    {
        ShapeDrawable shapeDrawable = new ShapeDrawable(new OvalShape());
        shapeDrawable.setIntrinsicWidth(20); // Adjust the width as needed
        shapeDrawable.setIntrinsicHeight(20); // Adjust the height as needed
        shapeDrawable.getPaint().setColor(color);
        return shapeDrawable;
    }


    private int generateRandomColor()
    {
        // Generate random values for red, green, and blue (RGB) channels
        int red = (int) (Math.random() * 256);
        int green = (int) (Math.random() * 256);
        int blue = (int) (Math.random() * 256);
        return Color.rgb(red, green, blue);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private List<Pair<String, Double>> getCourseTimes(
            @Nullable LocalDate start, @Nullable LocalDate end,
            boolean departments)
    {
        List<Pair<String, Double>> rtrn = new ArrayList<>();
        if(departments) {
            double tmpSum = .0;
            String[] allDepartments = getResources().getStringArray(R.array.departments);
            for (String department : allDepartments) {
                List<Course> courses = dataBaseHelper.getCourses(department);
                for(Course course : courses){
                    tmpSum += course.getTotalTime(dataBaseHelper, start, end);
                }
                rtrn.add(new Pair<>(department, tmpSum));
                tmpSum = .0;
            }
            return rtrn;
        }

        List<Course> courseList = dataBaseHelper.getCourses();
        for(Course course : courseList){
            rtrn.add(new Pair<>(course.getGroup(),
                    course.getTotalTime(dataBaseHelper, start, end)));
        }
        return rtrn;
    }
}
