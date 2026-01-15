package com.example.track_my_money;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.track_my_money.Adapters.TransactionsAdapter;
import com.example.track_my_money.Models.Transaction;
import com.example.track_my_money.Utils.Constants;
import com.example.track_my_money.Utils.Helper;
import com.example.track_my_money.ViewModel.MainViewModel;
import com.example.track_my_money.databinding.FragmentHomeBinding;
import com.example.track_my_money.views.Fragments.AddTransactionFragment;

import java.util.Calendar;

import io.realm.RealmResults;

public class HomeFragment extends Fragment {

    FragmentHomeBinding binding;
    Calendar calendar;
    public MainViewModel viewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater);

        // ✅ FIX: Use Activity ViewModel (same as MainActivity)
        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        Constants.setCatogeries();

        calendar = Calendar.getInstance();
        updateDate();

        binding.floatingActionBtn.setOnClickListener(c -> {
            new AddTransactionFragment().show(getParentFragmentManager(), null);
        });

        binding.transactionsList.setLayoutManager(new LinearLayoutManager(getContext()));

        viewModel.transactions.observe(getViewLifecycleOwner(), new Observer<RealmResults<Transaction>>() {
            @Override
            public void onChanged(RealmResults<Transaction> transactions) {

                TransactionsAdapter transactionsAdapter = new TransactionsAdapter(getContext(), transactions);

                transactionsAdapter.setOnTransactionClickListener(transaction -> {
                    AddTransactionFragment fragment = new AddTransactionFragment();
                    Bundle bundle = new Bundle();
                    bundle.putLong("transactionId", transaction.getId());
                    fragment.setArguments(bundle);
                    fragment.show(getParentFragmentManager(), fragment.getTag());
                });

                binding.transactionsList.setAdapter(transactionsAdapter);

                if (transactions.size() > 0) {
                    binding.emptyState.setVisibility(View.GONE);
                } else {
                    binding.emptyState.setVisibility(View.VISIBLE);
                }
            }
        });

        viewModel.totalAmount.observe(getViewLifecycleOwner(), aDouble ->
                binding.totalAmountLbl.setText(String.valueOf(aDouble)));

        viewModel.totalIncome.observe(getViewLifecycleOwner(), aDouble ->
                binding.incomeLbl.setText(String.valueOf(aDouble)));

        viewModel.totalExpense.observe(getViewLifecycleOwner(), aDouble ->
                binding.expenseLbl.setText(String.valueOf(aDouble)));

        viewModel.getTransaction(calendar);

        return binding.getRoot();
    }

    public void getTransaction() {
        viewModel.getTransaction(calendar);
    }

    void updateDate() {
        if (Constants.SELECTED_TAB == Constants.DAILY) {
            binding.currentDate.setText(Helper.formatdate(calendar.getTime()));
        } else if (Constants.SELECTED_TAB == Constants.MONTHLY) {
            binding.currentDate.setText(Helper.formatdateByMonth(calendar.getTime()));
        }

        viewModel.getTransaction(calendar);
    }
}
