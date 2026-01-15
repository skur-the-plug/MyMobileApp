package com.example.track_my_money.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.example.track_my_money.Models.Category;
import com.example.track_my_money.Models.Transaction;
import com.example.track_my_money.R;
import com.example.track_my_money.Utils.Constants;
import com.example.track_my_money.Utils.Helper;
import com.example.track_my_money.databinding.RowTransactionBinding;
import com.example.track_my_money.views.Activities.MainActivity;

import io.realm.RealmResults;

public class TransactionsAdapter extends RecyclerView.Adapter<TransactionsAdapter.TransactioViewHolder> {

    public interface OnTransactionClickListener {
        void onTransactionClick(Transaction transaction);
    }

    private OnTransactionClickListener clickListener;

    public void setOnTransactionClickListener(OnTransactionClickListener listener) {
        this.clickListener = listener;
    }

    Context context;
    RealmResults<Transaction> transactions;

    public TransactionsAdapter(Context context, RealmResults<Transaction> transactions) {
        this.context = context;
        this.transactions = transactions;
    }

    @NonNull
    @Override
    public TransactioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new TransactioViewHolder(LayoutInflater.from(context).inflate(R.layout.row_transaction, parent, false));
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onBindViewHolder(@NonNull TransactioViewHolder holder, int position) {

        Transaction transaction = transactions.get(position);

        holder.binding.transctionAmount.setText(String.valueOf(transaction.getAmount()));
        holder.binding.accountLabel.setText(transaction.getAccount());
        holder.binding.transactionDate.setText(Helper.formatdate(transaction.getDate()));
        holder.binding.TranasactionCatogary.setText(transaction.getCategory());

        Category transactionCategory = Constants.getCaategoryDetails(transaction.getCategory());
        holder.binding.catogaryIcon.setImageResource(transactionCategory.getCategoryImage());
        holder.binding.catogaryIcon.setBackgroundTintList(context.getColorStateList(transactionCategory.getCatColor()));

        holder.binding.accountLabel.setBackgroundTintList(
                ColorStateList.valueOf(context.getColor(Constants.getAccountsColor(transaction.getAccount())))
        );

        if (transaction.getType().equals(Constants.INCOME)) {
            holder.binding.transctionAmount.setTextColor(context.getColor(R.color.greenColor));
        } else {
            holder.binding.transctionAmount.setTextColor(context.getColor(R.color.redColor));
        }

        holder.itemView.setOnLongClickListener(v -> {
            AlertDialog deleteDialog = new AlertDialog.Builder(context).create();
            deleteDialog.setTitle("Delete Transaction");
            deleteDialog.setMessage("Are you sure you want to delete the transaction?");

            deleteDialog.setButton(DialogInterface.BUTTON_POSITIVE, "YES", (dialog, which) -> {
                ((MainActivity) context).viewModel.deleteTransaction(transaction);
                notifyDataSetChanged(); // ✅ refresh list
            });

            deleteDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "NO", (dialog, which) -> deleteDialog.dismiss());

            deleteDialog.show();

            return true; // ✅ IMPORTANT: consume long click (prevents weird behavior)
        });

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onTransactionClick(transaction);
            }
        });
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    public static class TransactioViewHolder extends RecyclerView.ViewHolder {
        RowTransactionBinding binding;

        public TransactioViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = RowTransactionBinding.bind(itemView);
        }
    }
}
