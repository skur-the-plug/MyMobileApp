package com.example.track_my_money;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.track_my_money.Models.MonthlySummary;
import com.example.track_my_money.ViewModel.MainViewModel;
import com.example.track_my_money.databinding.FragmentStatsBinding;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class StatsFragment extends Fragment {

    private FragmentStatsBinding binding;
    private MainViewModel viewModel;
    private final Calendar monthCalendar = Calendar.getInstance();

    public StatsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentStatsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        monthCalendar.set(Calendar.DAY_OF_MONTH, 1);
        monthCalendar.set(Calendar.HOUR_OF_DAY, 0);
        monthCalendar.set(Calendar.MINUTE, 0);
        monthCalendar.set(Calendar.SECOND, 0);
        monthCalendar.set(Calendar.MILLISECOND, 0);

        setupPieChart();

        binding.btnPrevMonth.setOnClickListener(v -> {
            monthCalendar.add(Calendar.MONTH, -1);
            refreshMonth();
        });

        binding.btnNextMonth.setOnClickListener(v -> {
            monthCalendar.add(Calendar.MONTH, 1);
            refreshMonth();
        });

        viewModel.monthlySummary.observe(getViewLifecycleOwner(), this::renderChart);

        refreshMonth();
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshMonth();
    }

    private void refreshMonth() {
        if (binding == null || viewModel == null) return;
        binding.tvMonthTitle.setText(formatMonthTitle(monthCalendar));
        viewModel.getMonthlySummary(monthCalendar);
    }

    private String formatMonthTitle(Calendar cal) {
        return new SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(cal.getTime());
    }

    private void setupPieChart() {
        binding.pieChart.getDescription().setEnabled(false);
        binding.pieChart.setDrawHoleEnabled(true);
        binding.pieChart.setHoleRadius(58f);
        binding.pieChart.setTransparentCircleRadius(62f);
        binding.pieChart.setNoDataText("No data");
        binding.pieChart.setEntryLabelTextSize(12f);
        binding.pieChart.setDrawEntryLabels(true);
        binding.pieChart.setRotationEnabled(false);
        binding.pieChart.getLegend().setEnabled(true);
    }

    private void renderChart(MonthlySummary summary) {
        if (summary == null || binding == null) return;

        double income = summary.getIncome();
        double expense = summary.getExpense();

        List<PieEntry> entries = new ArrayList<>();
        if (income > 0) entries.add(new PieEntry((float) income, "Income"));
        if (expense > 0) entries.add(new PieEntry((float) expense, "Expense"));

        if (entries.isEmpty()) {
            binding.pieChart.clear();
            binding.pieChart.setCenterText("No data");
            binding.pieChart.invalidate();
            return;
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setSliceSpace(2f);
        dataSet.setValueTextSize(12f);
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);

        PieData data = new PieData(dataSet);
        binding.pieChart.setData(data);
        binding.pieChart.setCenterText("Income: " + trim(income) + "\nExpense: " + trim(expense));
        binding.pieChart.invalidate();
    }

    private String trim(double value) {
        if (value == (long) value) {
            return String.valueOf((long) value);
        }
        return String.valueOf(value);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
