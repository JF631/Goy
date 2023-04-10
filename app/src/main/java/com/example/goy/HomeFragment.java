package com.example.goy;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment implements CreateFragment.OnCreateCourseClickedListener {
    private CourseAdapter courseAdapter;
    private DataBaseHelper dataBaseHelper;

    public HomeFragment(){}

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.home_view, container, false);
        RecyclerView homeView = view.findViewById(R.id.home_course_view);
        FloatingActionButton addBtn = view.findViewById(R.id.add_course);
        dataBaseHelper = new DataBaseHelper(getContext());
        List<Course> courseList = dataBaseHelper.getCourses();
        courseAdapter = new CourseAdapter(courseList);
        homeView.setLayoutManager(new LinearLayoutManager(getContext()));
        homeView.setAdapter(courseAdapter);

        courseAdapter.setOnItemClickListener((position, sharedView) -> {
            CourseFragment courseFragment = new CourseFragment();
            Bundle bundle = new Bundle();
            bundle.putParcelable("course", courseList.get(position));
            courseFragment.setArguments(bundle);
            FragmentManager fragmentManager = getParentFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.anim_scale_cardview);
            sharedView.startAnimation(animation);
            fragmentTransaction.replace(R.id.fragment_container_view, courseFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();

            /**
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.setReorderingAllowed(true);
            ft.addSharedElement(view, "card_transition");
            createFragment.show(ft, "show_dates");**/

            List<LocalDate> dates = dataBaseHelper.getDates(courseList.get(position));
            List<String> locs = dataBaseHelper.getLocations(courseList.get(position));
            String times = dataBaseHelper.getTimes(courseList.get(position)).toString();
            //Toast.makeText(getContext(), "Dates: " + dates, Toast.LENGTH_SHORT).show();
        });


        courseAdapter.setOnItemLongClickListener(pos -> {
            if(pos != RecyclerView.NO_POSITION) {
                courseAdapter.deleteItem(pos, getContext());
            }

        });

        addBtn.setOnClickListener(view1 -> showCreate());

        return view;
    }

    private void showCreate(){
        CreateFragment createFragment = new CreateFragment();
        createFragment.show(getChildFragmentManager(), "create_course");
        createFragment.setOnCreateCourseClickedListener(this);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreateCourseClicked(List<Triple<String, LocalTime, LocalTime>> times, String department, String group, ArrayList<String> locations) {
        Course course = new Course(department, group, times, locations);
        DataBaseHelper dataBaseHelper = new DataBaseHelper(getContext());
        long id = dataBaseHelper.insertCourse(course);
        if(id == -1) Log.e("FATAL", "couldn't add course");
        Log.d("id: ", Long.toString(id));
        course.setId(id);
        courseAdapter.insertItem(course);
    }
}
