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
import androidx.cardview.widget.CardView;
import androidx.compose.material3.ColorScheme;
import androidx.compose.material3.MaterialTheme;
import androidx.fragment.app.Fragment;

import com.google.android.material.color.MaterialColors;

import org.eazegraph.lib.charts.BarChart;
import org.eazegraph.lib.charts.PieChart;
import org.eazegraph.lib.models.BarModel;
import org.eazegraph.lib.models.PieModel;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class StatsFragment extends Fragment {

    private DataBaseHelper dataBaseHelper;
    private PieChart pieChart;
    private BarChart barChart;
    private Button btn_total, btn_week, btn_month, btn_year;
    public StatsFragment(){}

    @RequiresApi(api = Build.VERSION_CODES.O)
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.stats_window, container, false);
        pieChart = view.findViewById(R.id.piechart);
        barChart = view.findViewById(R.id.barchart);
        btn_total = view.findViewById(R.id.stats_total_btn);
        btn_week = view.findViewById(R.id.stats_week_btn);
        btn_month = view.findViewById(R.id.stats_month_btn);
        btn_year = view.findViewById(R.id.stats_year_btn);
        CardView cardView = view.findViewById(R.id.stats_card_pie);

        dataBaseHelper = new DataBaseHelper(requireContext());
        setUpPieChart(view, null, null);
        setBtnHighlight(Period.OVERALL);
        setUpBarChart(view);

        btn_total.setOnClickListener(view1 -> {
            setBtnHighlight(Period.OVERALL);
            setUpPieChart(view, null, null);
        });
        btn_week.setOnClickListener(view1 -> {
            setBtnHighlight(Period.WEEK);
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minus(1, ChronoUnit.WEEKS);
            setUpPieChart(view, startDate, endDate);
        });
        btn_month.setOnClickListener(view1 -> {
            setBtnHighlight(Period.MONTH);
            btn_month.setBackgroundResource(R.drawable.btn_highlight_oval);
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minus(1, ChronoUnit.MONTHS);
            setUpPieChart(view, startDate, endDate);
        });
        btn_year.setOnClickListener(view1 -> {
            setBtnHighlight(Period.YEAR);
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
        LinearLayout legendLayout = view.findViewById(R.id.legendLayout_pie);
        legendLayout.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        for(Pair<Course, Double> courseTime : courseTimes){
            double totalHours = courseTime.getSecond();
            if(totalHours == 0.f)
                continue;
            int dotColor = generateRandomColor();
            float coursePercentage = (float) (totalHours / totalTime * 100);
            pieChart.addPieSlice(new PieModel(
                    courseTime.getFirst().getGroup(),
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
                    courseTime.getFirst().getGroup(), totalHours ,
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
    private void setUpBarChart(View view){
        barChart.clearChart();
        int currentYear = LocalDate.now().getYear();
        int color = MaterialColors.getColor(view, com.google.android.material.R.attr.colorPrimary);
        for(int i = 1; i<=12; ++i) {
            LocalDate startDate = LocalDate.of(currentYear, i, 1);
            LocalDate endDate = LocalDate.of(currentYear, i, startDate.lengthOfMonth());
            List<Pair<Course, Double>> monthTimes = getCourseTimes(startDate, endDate);
            double monthTime = Utilities.sum(monthTimes);
            barChart.addBar(new BarModel(
                    String.valueOf(endDate.getMonthValue()),
                    (float) monthTime,
                    color
            ));
        }
        barChart.startAnimation();
    }

    private void setBtnHighlight(Period period){
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
        return Color.rgb(red, green, blue);
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
