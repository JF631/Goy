package com.example.goy;

import android.os.Build;
import android.os.Bundle;
import android.transition.Fade;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.time.LocalTime;
import java.util.List;
import java.util.Set;

public class HomeFragment extends Fragment implements CreateFragment.OnCreateCourseClickedListener {
    private CourseAdapter courseAdapter;

    public HomeFragment(){}

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.home_view, container, false);
        RecyclerView homeView = view.findViewById(R.id.home_course_view);
        ExtendedFloatingActionButton addButton = view.findViewById(R.id.add_course);

        Transition sharedElementTransition = TransitionInflater.from(getContext())
                .inflateTransition(android.R.transition.slide_bottom);
        sharedElementTransition.addTarget(addButton);

        requireActivity().getWindow().setSharedElementEnterTransition(sharedElementTransition);

        DataBaseHelper dataBaseHelper = new DataBaseHelper(getContext());
        List<Course> courseList = dataBaseHelper.getCourses();
        courseAdapter = new CourseAdapter(courseList);
        homeView.setLayoutManager(new LinearLayoutManager(getContext()));
        homeView.setAdapter(courseAdapter);

        SwipeToDeleteCallback swipeToDeleteCallback = new SwipeToDeleteCallback(courseAdapter, requireContext());
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeToDeleteCallback);
        itemTouchHelper.attachToRecyclerView(homeView);

        courseAdapter.setOnItemClickListener((position, sharedView) -> {
            CourseFragment courseFragment = new CourseFragment();
            Bundle bundle = new Bundle();
            bundle.putParcelable("course", courseList.get(position));
            courseFragment.setArguments(bundle);
            FragmentManager fragmentManager = getParentFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            courseFragment.setSharedElementEnterTransition(TransitionInflater.from(getContext())
                    .inflateTransition(android.R.transition.move));
            courseFragment.setEnterTransition(new Fade());
            setExitTransition(new Fade());
            courseFragment.setSharedElementReturnTransition(TransitionInflater.from(getContext())
                    .inflateTransition(android.R.transition.move));
            fragmentTransaction.addSharedElement(addButton, "add_button");
            fragmentTransaction.replace(R.id.fragment_container_view, courseFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
            ((MainActivity) requireActivity()).hideNavBar();
        });


        courseAdapter.setOnItemLongClickListener(pos -> {
            if(pos != RecyclerView.NO_POSITION) {
                courseAdapter.deleteItem(pos, getContext());
            }

        });

        addButton.setOnClickListener(view1 -> showCreate());

        return view;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void showCreate(){
        CreateFragment createFragment = new CreateFragment(null);
        createFragment.show(getChildFragmentManager(), "create_course");
        createFragment.setOnCreateCourseClickedListener(this);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreateCourseClicked(List<Triple<String, LocalTime, LocalTime>> times, String department, String group, Set<String> locations) {
        Course course = new Course(department, group, times, locations);
        DataBaseHelper dataBaseHelper = new DataBaseHelper(getContext());
        long id = dataBaseHelper.insertCourse(course);
        if(id == -1) Log.e("FATAL", "couldn't add course");
        Log.d("id: ", Long.toString(id));
        course.setId(id);
        courseAdapter.insertItem(course);
    }
}
