package com.example.track_my_money.views.Fragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.track_my_money.Adapters.AccountAdapter;
import com.example.track_my_money.Adapters.CategoryAdapter;
import com.example.track_my_money.Models.Account;
import com.example.track_my_money.Models.Category;
import com.example.track_my_money.Models.Transaction;
import com.example.track_my_money.R;
import com.example.track_my_money.Utils.Constants;
import com.example.track_my_money.Utils.Helper;
import com.example.track_my_money.databinding.FragmentAddTransactionBinding;
import com.example.track_my_money.databinding.ListDialogBinding;
import com.example.track_my_money.views.Activities.MainActivity;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class AddTransactionFragment extends BottomSheetDialogFragment {

    private boolean isEditMode = false;
    private long transactionId = -1;

    // for edit-mode prefill only
    private Transaction transaction;

    // UI state (NOT tied to realm object)
    private String selectedType = Constants.EXPENSE;
    private Date selectedDate = null;
    private String selectedCategory = "";
    private String selectedAccount = "";

    private FragmentAddTransactionBinding binding;

    public AddTransactionFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args != null && args.containsKey("transactionId")) {
            transactionId = args.getLong("transactionId");
            isEditMode = true;
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentAddTransactionBinding.inflate(inflater, container, false);

        // --------------------------
        // Setup defaults + prefill
        // --------------------------
        if (isEditMode) {
            transaction = ((MainActivity) requireActivity())
                    .viewModel
                    .getTransactionById(transactionId);

            if (transaction == null) {
                Toast.makeText(getContext(), "Transaction not found", Toast.LENGTH_SHORT).show();
                dismiss();
                return binding.getRoot();
            }

            // Copy values into local state (so we don't touch realm object later)
            selectedType = transaction.getType() != null ? transaction.getType() : Constants.EXPENSE;
            selectedDate = transaction.getDate();
            selectedCategory = transaction.getCategory() != null ? transaction.getCategory() : "";
            selectedAccount = transaction.getAccount() != null ? transaction.getAccount() : "";

            // Prefill UI
            binding.note.setText(transaction.getNote() != null ? transaction.getNote() : "");

            double absAmount = Math.abs(transaction.getAmount());
            binding.amount.setText(String.valueOf(absAmount));

            if (selectedDate != null) {
                binding.date.setText(Helper.formatdate(selectedDate));
            }
            if (!selectedCategory.isEmpty()) binding.catogary.setText(selectedCategory);
            if (!selectedAccount.isEmpty()) binding.account.setText(selectedAccount);

            // set type button UI
            if (Constants.INCOME.equals(selectedType)) {
                binding.incomeBtn.performClick();
            } else {
                binding.expenseBtn.performClick();
            }

        } else {
            // Add mode defaults
            selectedType = Constants.EXPENSE;
            selectedDate = new Date(); // optional default: today
            binding.date.setText(Helper.formatdate(selectedDate));
        }

        // --------------------------
        // Type buttons
        // --------------------------
        binding.incomeBtn.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                binding.incomeBtn.setBackground(requireContext().getDrawable(R.drawable.income_selector));
                binding.expenseBtn.setBackground(requireContext().getDrawable(R.drawable.dafault_selector));
                binding.incomeBtn.setTextColor(requireContext().getColor(R.color.greenColor));
                binding.expenseBtn.setTextColor(requireContext().getColor(R.color.defaultTxtColor));
            }
            selectedType = Constants.INCOME;
        });

        binding.expenseBtn.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                binding.expenseBtn.setBackground(requireContext().getDrawable(R.drawable.expense_selector));
                binding.incomeBtn.setBackground(requireContext().getDrawable(R.drawable.dafault_selector));
                binding.expenseBtn.setTextColor(requireContext().getColor(R.color.redColor));
                binding.incomeBtn.setTextColor(requireContext().getColor(R.color.defaultTxtColor));
            }
            selectedType = Constants.EXPENSE;
        });

        // --------------------------
        // Date picker
        // --------------------------
        binding.date.setOnClickListener(v -> {
            Calendar now = Calendar.getInstance();
            if (selectedDate != null) now.setTime(selectedDate);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    requireContext(),
                    new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                            Calendar calendar = Calendar.getInstance();
                            calendar.set(year, month, dayOfMonth, 0, 0, 0);
                            calendar.set(Calendar.MILLISECOND, 0);

                            selectedDate = calendar.getTime();
                            binding.date.setText(Helper.formatdate(selectedDate));
                        }
                    },
                    now.get(Calendar.YEAR),
                    now.get(Calendar.MONTH),
                    now.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        });

        // --------------------------
        // Category dialog
        // --------------------------
        binding.catogary.setOnClickListener(v -> {
            ListDialogBinding dialogBinding = ListDialogBinding.inflate(inflater);
            AlertDialog categoryDialog = new AlertDialog.Builder(requireContext()).create();
            categoryDialog.setView(dialogBinding.getRoot());

            CategoryAdapter categoryAdapter = new CategoryAdapter(requireContext(), Constants.categories,
                    new CategoryAdapter.CategoryClickListner() {
                        @Override
                        public void onCategoryClicked(Category category) {
                            binding.catogary.setText(category.getCategoryName());
                            selectedCategory = category.getCategoryName();
                            categoryDialog.dismiss();
                        }
                    });

            dialogBinding.recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 3));
            dialogBinding.recyclerView.setAdapter(categoryAdapter);
            categoryDialog.show();
        });

        // --------------------------
        // Account dialog
        // --------------------------
        binding.account.setOnClickListener(v -> {
            ListDialogBinding dialogBinding = ListDialogBinding.inflate(inflater);
            AlertDialog accountsDialog = new AlertDialog.Builder(requireContext()).create();
            accountsDialog.setView(dialogBinding.getRoot());

            ArrayList<Account> accounts = new ArrayList<>();
            accounts.add(new Account(0, "PAYTM"));
            accounts.add(new Account(0, "GooglePay"));
            accounts.add(new Account(0, "Cash"));
            accounts.add(new Account(0, "Bank"));
            accounts.add(new Account(0, "Other"));

            AccountAdapter accountAdapter = new AccountAdapter(requireContext(), accounts,
                    new AccountAdapter.AccountClickListner() {
                        @Override
                        public void onAccountSelected(Account account) {
                            binding.account.setText(account.getAccountName());
                            selectedAccount = account.getAccountName();
                            accountsDialog.dismiss();
                        }
                    });

            dialogBinding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
            dialogBinding.recyclerView.addItemDecoration(
                    new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));
            dialogBinding.recyclerView.setAdapter(accountAdapter);

            accountsDialog.show();
        });

        // --------------------------
        // Save
        // --------------------------
        binding.saveTransactionBtn.setOnClickListener(v -> {

            String amountStr = binding.amount.getText().toString().trim();
            if (amountStr.isEmpty()) {
                binding.amount.setError("Enter amount");
                return;
            }

            double amountVal;
            try {
                amountVal = Double.parseDouble(amountStr);
            } catch (Exception e) {
                binding.amount.setError("Invalid amount");
                return;
            }

            String note = binding.note.getText().toString();

            if (selectedDate == null) {
                Toast.makeText(getContext(), "Pick a date", Toast.LENGTH_SHORT).show();
                return;
            }

            if (Constants.EXPENSE.equals(selectedType)) {
                amountVal = -amountVal;
            }

            MainActivity activity = (MainActivity) requireActivity();

            if (isEditMode) {
                activity.viewModel.updateTransaction(
                        transactionId,
                        amountVal,
                        note,
                        selectedCategory,
                        selectedAccount,
                        selectedDate,
                        selectedType
                );
            } else {
                long newId = selectedDate.getTime(); // unique enough for your app
                activity.viewModel.addTransaction(
                        newId,
                        amountVal,
                        note,
                        selectedCategory,
                        selectedAccount,
                        selectedDate,
                        selectedType
                );
            }

            Toast.makeText(getContext(), "Saved", Toast.LENGTH_SHORT).show();
            dismiss();
        });

        return binding.getRoot();
    }
}
