package com.example.client.bottom_nav.practice;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.client.R;
import com.example.client.bottom_nav.BaseFragment;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.example.client.adapters.PracticeBaseAdapter;
import com.example.client.databinding.FragmentPracticeBinding;
import com.example.client.models.Course;
import com.example.client.models.Group;
import com.example.client.models.PracticeBase;
import com.example.client.models.User;
import com.example.client.models.Year;
import com.example.client.network.PracticeRequests;
import com.example.client.view_models.UserViewModel;

import java.util.List;
import java.util.ArrayList;

public class PracticeFragment extends BaseFragment<FragmentPracticeBinding> {
    private static final String TAG = PracticeFragment.class.getSimpleName();

    private PracticeBaseAdapter practiceBaseAdapter;
    private ArrayAdapter<Year> yearAdapter;
    private ArrayAdapter<Course> courseAdapter;
    private ArrayAdapter<Group> groupAdapter;

    private List<Year> years = new ArrayList<>();
    private List<Course> allCourses = new ArrayList<>();
    private List<Course> displayCourses = new ArrayList<>();
    private List<Group> allGroups = new ArrayList<>();
    private List<Group> displayGroups = new ArrayList<>();

    private int selectedYearId = -1;
    private int selectedCourseId = -1;
    private int selectedGroupId = -1;
    private int currentUserId;

    @Override
    protected FragmentPracticeBinding inflateBinding(LayoutInflater inflater, ViewGroup container) {
        return FragmentPracticeBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        currentUserId = userViewModel.getCurrentUser().getValue().getId();

        setupUI();
        loadInitialData();
    }

    private void setupUI() {
        setupSpinners();
        setupPracticeListView();
        setupListeners();
    }

    private void setupSpinners() {
        yearAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, years);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.yearSpinner.setAdapter(yearAdapter);

        displayCourses.add(new Course(-1, getString(R.string.select_course)));
        courseAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, displayCourses);
        courseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.courseSpinner.setAdapter(courseAdapter);

        displayGroups.add(new Group(-1, getString(R.string.select_group), -1));
        groupAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, displayGroups);
        groupAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.groupSpinner.setAdapter(groupAdapter);
    }

    private void setupPracticeListView() {
        practiceBaseAdapter = new PracticeBaseAdapter(requireContext(), new ArrayList<>(), currentUserId);
        binding.practiceLv.setAdapter(practiceBaseAdapter);
        binding.practiceLv.setVisibility(View.GONE);

        practiceBaseAdapter.setOnRegisterClickListener(this::registerForPractice);
        practiceBaseAdapter.setOnUnregisterClickListener(this::unregisterFromPractice);
    }

    private void registerForPractice(PracticeBase practiceBase) {
        setLoadingState(true);
        PracticeRequests.registerForPractice(requireContext(), currentUserId, practiceBase.getId(),
                (success, message) -> requireActivity().runOnUiThread(() -> {
                    setLoadingState(false);
                    if (success) {
                        Toast.makeText(requireContext(), "Вы успешно записаны", Toast.LENGTH_SHORT).show();
                        refreshPracticeBases();
                    } else {
                        showError("Ошибка записи: " + message);
                        Log.e(TAG, message);
                    }
                }));
    }

    private void unregisterFromPractice(PracticeBase practiceBase) {
        setLoadingState(true);
        PracticeRequests.unregisterFromPractice(requireContext(), currentUserId, practiceBase.getId(),
                (success, message) -> requireActivity().runOnUiThread(() -> {
                    setLoadingState(false);
                    if (success) {
                        Toast.makeText(requireContext(), "Вы отписаны от практики", Toast.LENGTH_SHORT).show();
                        refreshPracticeBases();
                    } else {
                        showError("Ошибка при отписке: " + message);
                        Log.e(TAG, message);
                    }
                }));
    }

    private void refreshPracticeBases() {
        if (selectedYearId != -1 && selectedGroupId != -1) {
            loadPracticeBases(selectedYearId, selectedCourseId, selectedGroupId);
        }
    }

    private void setupListeners() {
        binding.yearSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && !years.isEmpty()) {
                    Year selectedYear = years.get(position);
                    selectedYearId = selectedYear.getId();
                    loadCourses(selectedYearId);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedYearId = -1;
            }
        });

        binding.courseSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0 && !allCourses.isEmpty()) {
                    Course selectedCourse = allCourses.get(position - 1);
                    selectedCourseId = selectedCourse.getId();
                    loadGroups(selectedYearId, selectedCourseId);
                } else {
                    selectedCourseId = -1;
                    showGroupHint();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedCourseId = -1;
                showGroupHint();
            }
        });

        binding.groupSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0 && !allGroups.isEmpty()) {
                    Group selectedGroup = allGroups.get(position - 1);
                    selectedGroupId = selectedGroup.getId();
                    loadPracticeBases(selectedYearId, selectedCourseId, selectedGroupId);
                } else {
                    selectedGroupId = -1;
                    showGroupHint();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedGroupId = -1;
                showGroupHint();
            }
        });
    }

    private void loadInitialData() {
        loadYears();
    }


    private void loadYears() {
        setLoadingState(true);
        PracticeRequests.getYears(requireContext(), (success, message, yearsList) -> requireActivity().runOnUiThread(() -> {
            setLoadingState(false);
            if (success && yearsList != null) {
                updateYearsList(yearsList);
            } else {
                showError("Ошибка загрузки годов обучения: " + message);
            }
        }));
    }
    private void updateYearsList(List<Year> yearsList) {
        years.clear();
        years.addAll(yearsList);
        yearAdapter.notifyDataSetChanged();

        if (!years.isEmpty()) {
            binding.yearSpinner.setSelection(0);
        }
    }


    private void loadCourses(int yearId) {
        setLoadingState(true);
        PracticeRequests.getCourses(requireContext(), yearId, (success, message, courses) ->
                requireActivity().runOnUiThread(() -> {
                    setLoadingState(false);
                    if (success && courses != null) {
                        updateCoursesList(courses);
                    } else {
                        showError("Ошибка загрузки курсов: " + message);
                    }
                }));
    }
    private void updateCoursesList(List<Course> coursesList) {
        allCourses.clear();
        allCourses.addAll(coursesList);

        displayCourses.clear();
        displayCourses.add(new Course(-1, getString(R.string.select_course)));
        displayCourses.addAll(allCourses);

        courseAdapter.notifyDataSetChanged();
        binding.courseSpinner.setSelection(0);
        showGroupHint();
    }


    private void loadGroups(int yearId, int courseId) {
        setLoadingState(true);
        PracticeRequests.getGroups(requireContext(), yearId, courseId,
                (success, message, groups) -> requireActivity().runOnUiThread(() -> {
                    setLoadingState(false);
                    if (success && groups != null) {
                        updateGroupsList(groups);
                    } else {
                        showError("Ошибка загрузки групп: " + message);
                    }
                }));
    }
    private void updateGroupsList(List<Group> groups) {
        allGroups.clear();
        allGroups.addAll(groups);

        displayGroups.clear();
        displayGroups.add(new Group(-1, getString(R.string.select_group), -1));
        displayGroups.addAll(allGroups);

        groupAdapter.notifyDataSetChanged();
        binding.groupSpinner.setSelection(0);
        showGroupHint();
    }


    private void loadPracticeBases(int yearId, int courseId, int groupId) {
        setLoadingState(true);
        PracticeRequests.getAvailablePracticeBases(requireContext(), yearId, courseId, groupId,
                (success, message, bases) -> requireActivity().runOnUiThread(() -> {
                    setLoadingState(false);
                    if (success && bases != null) {
                        showPracticeBases(bases);
                    } else {
                        showError("Ошибка загрузки баз практики: " + message);
                        showGroupHint();
                    }
                }));
    }
    private void showPracticeBases(List<PracticeBase> bases) {
        practiceBaseAdapter.updateList(bases);
        binding.selectGroupHint.setVisibility(View.GONE);
        binding.practiceLv.setVisibility(View.VISIBLE);
    }


    private void showGroupHint() {
        practiceBaseAdapter.updateList(new ArrayList<>());
        binding.selectGroupHint.setVisibility(View.VISIBLE);
        binding.practiceLv.setVisibility(View.GONE);
    }
    private void showError(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
    }
}