package com.example.track_my_money.ViewModel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.track_my_money.Models.MonthlySummary;
import com.example.track_my_money.Models.Transaction;
import com.example.track_my_money.Utils.Constants;

import java.util.Calendar;
import java.util.Date;

import io.realm.Realm;
import io.realm.RealmResults;

public class MainViewModel extends AndroidViewModel {

    public MutableLiveData<RealmResults<Transaction>> transactions = new MutableLiveData<>();
    public MutableLiveData<Double> totalIncome = new MutableLiveData<>();
    public MutableLiveData<Double> totalExpense = new MutableLiveData<>();
    public MutableLiveData<Double> totalAmount = new MutableLiveData<>();

    // Stats tab: Monthly Income vs Expense (PieChart)
    public MutableLiveData<MonthlySummary> monthlySummary = new MutableLiveData<>();

    private Realm realm;
    private Calendar calendar;

    public MainViewModel(@NonNull Application application) {
        super(application);
        Realm.init(application);
        setUpDatabase();
    }

    // -----------------------------
    // Safe getter for edit-mode UI
    // -----------------------------
    public Transaction getTransactionById(long id) {
        if (realm == null || realm.isClosed()) {
            realm = Realm.getDefaultInstance();
        }
        return realm.where(Transaction.class)
                .equalTo("id", id)
                .findFirst();
    }

    // -----------------------------
    // FIX 1: Add via primitive args (no managed object mutations in Fragment)
    // Runs on background thread (fixes "UI thread writes disabled")
    // -----------------------------
    public void addTransaction(long id,
                               double amount,
                               String note,
                               String category,
                               String account,
                               Date date,
                               String type) {

        Realm bgRealm = Realm.getDefaultInstance();
        bgRealm.executeTransactionAsync(r -> {
            // If your Transaction model uses @PrimaryKey on "id", this works great.
            Transaction t = r.createObject(Transaction.class, id);
            t.setAmount(amount);
            t.setNote(note);
            t.setCategory(category);
            t.setAccount(account);
            t.setDate(date);
            t.setType(type);
        }, () -> {
            bgRealm.close();
            if (calendar != null) getTransaction(calendar);
        }, error -> {
            bgRealm.close();
            error.printStackTrace();
        });
    }

    // -----------------------------
    // FIX 2: Update on background thread
    // (fixes both crashes: "outside write transaction" + "writes on UI thread disabled")
    // -----------------------------
    public void updateTransaction(long id,
                                  double amount,
                                  String note,
                                  String category,
                                  String account,
                                  Date date,
                                  String type) {

        Realm bgRealm = Realm.getDefaultInstance();
        bgRealm.executeTransactionAsync(r -> {
            Transaction t = r.where(Transaction.class)
                    .equalTo("id", id)
                    .findFirst();

            if (t != null) {
                t.setAmount(amount);
                t.setNote(note);
                t.setCategory(category);
                t.setAccount(account);
                t.setDate(date);
                t.setType(type);
            }
        }, () -> {
            bgRealm.close();
            if (calendar != null) getTransaction(calendar);
        }, error -> {
            bgRealm.close();
            error.printStackTrace();
        });
    }

    // -----------------------------
    // Existing method (kept) - but make it safe (bg thread)
    // If you still call addTransaction(Transaction) anywhere, it won't crash now.
    // -----------------------------
    public void addTransaction(Transaction transaction) {
        if (transaction == null) return;

        Realm bgRealm = Realm.getDefaultInstance();
        bgRealm.executeTransactionAsync(r -> r.copyToRealmOrUpdate(transaction),
                bgRealm::close,
                error -> {
                    bgRealm.close();
                    error.printStackTrace();
                });
    }

    // -----------------------------
    // FIX 3: Delete by id (do NOT pass a managed object across threads)
    // -----------------------------
    public void deleteTransaction(Transaction transaction) {
        if (transaction == null) return;

        long id = transaction.getId();

        Realm bgRealm = Realm.getDefaultInstance();
        bgRealm.executeTransactionAsync(r -> {
            Transaction t = r.where(Transaction.class).equalTo("id", id).findFirst();
            if (t != null) t.deleteFromRealm();
        }, () -> {
            bgRealm.close();
            if (calendar != null) getTransaction(calendar);
        }, error -> {
            bgRealm.close();
            error.printStackTrace();
        });
    }

    // -----------------------------
    // Your existing query logic (unchanged)
    // -----------------------------
    public void getTransaction(Calendar calendar) {

        this.calendar = calendar;
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        double income = 0;
        double expense = 0;
        double amount = 0;

        RealmResults<Transaction> NewTransactions = null;

        if (Constants.SELECTED_TAB == Constants.DAILY) {

            NewTransactions = realm.where(Transaction.class)
                    .greaterThanOrEqualTo("date", calendar.getTime())
                    .lessThan("date", new Date(calendar.getTime().getTime() + (24 * 60 * 60 * 1000L)))
                    .findAll();

            income = realm.where(Transaction.class)
                    .greaterThanOrEqualTo("date", calendar.getTime())
                    .lessThan("date", new Date(calendar.getTime().getTime() + (24 * 60 * 60 * 1000L)))
                    .equalTo("type", Constants.INCOME)
                    .sum("amount").doubleValue();

            expense = realm.where(Transaction.class)
                    .greaterThanOrEqualTo("date", calendar.getTime())
                    .lessThan("date", new Date(calendar.getTime().getTime() + (24 * 60 * 60 * 1000L)))
                    .equalTo("type", Constants.EXPENSE)
                    .sum("amount").doubleValue();

            amount = realm.where(Transaction.class)
                    .greaterThanOrEqualTo("date", calendar.getTime())
                    .lessThan("date", new Date(calendar.getTime().getTime() + (24 * 60 * 60 * 1000L)))
                    .sum("amount").doubleValue();

        } else if (Constants.SELECTED_TAB == Constants.MONTHLY) {

            calendar.set(Calendar.DAY_OF_MONTH, 0);

            Date startTime = calendar.getTime();
            calendar.add(Calendar.MONTH, 1);
            Date endTime = calendar.getTime();

            NewTransactions = realm.where(Transaction.class)
                    .greaterThanOrEqualTo("date", startTime)
                    .lessThan("date", endTime)
                    .findAll();

            income = realm.where(Transaction.class)
                    .greaterThanOrEqualTo("date", startTime)
                    .lessThan("date", endTime)
                    .equalTo("type", Constants.INCOME)
                    .sum("amount").doubleValue();

            expense = realm.where(Transaction.class)
                    .greaterThanOrEqualTo("date", startTime)
                    .lessThan("date", endTime)
                    .equalTo("type", Constants.EXPENSE)
                    .sum("amount").doubleValue();

            amount = realm.where(Transaction.class)
                    .greaterThanOrEqualTo("date", startTime)
                    .lessThan("date", endTime)
                    .sum("amount").doubleValue();
        }

        totalIncome.setValue(income);
        totalExpense.setValue(expense);
        totalAmount.setValue(amount);
        transactions.setValue(NewTransactions);
    }

    // -----------------------------
    // Stats tab: Monthly Income vs Expense
    // Expense is returned as POSITIVE number (absolute value) since expenses are stored negative.
    // Does NOT rely on Constants.SELECTED_TAB.
    // -----------------------------
    public void getMonthlySummary(Calendar monthCalendar) {
        if (monthCalendar == null) return;
        if (realm == null || realm.isClosed()) {
            realm = Realm.getDefaultInstance();
        }

        Calendar cal = (Calendar) monthCalendar.clone();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        Date startTime = cal.getTime();
        cal.add(Calendar.MONTH, 1);
        Date endTime = cal.getTime();

        double income = realm.where(Transaction.class)
                .greaterThanOrEqualTo("date", startTime)
                .lessThan("date", endTime)
                .equalTo("type", Constants.INCOME)
                .sum("amount").doubleValue();

        double expenseRaw = realm.where(Transaction.class)
                .greaterThanOrEqualTo("date", startTime)
                .lessThan("date", endTime)
                .equalTo("type", Constants.EXPENSE)
                .sum("amount").doubleValue();

        monthlySummary.setValue(new MonthlySummary(income, Math.abs(expenseRaw)));
    }

    // Optional sample data function (kept) - but this is still UI-thread if called from UI.
    // If you actually use it, better to convert to executeTransactionAsync as well.
    public void addTransactions() {
        Realm bgRealm = Realm.getDefaultInstance();
        bgRealm.executeTransactionAsync(r -> {
            r.copyToRealmOrUpdate(new Transaction(Constants.INCOME, "Business", "GooglePay", "Some note here", new Date(), 500, new Date().getTime()));
            r.copyToRealmOrUpdate(new Transaction(Constants.INCOME, "Business", "GooglePay", "Some note here", new Date(), 500, new Date().getTime()));
            r.copyToRealmOrUpdate(new Transaction(Constants.EXPENSE, "Investment", "PAYTM", "Some note here", new Date(), -500, new Date().getTime()));
            r.copyToRealmOrUpdate(new Transaction(Constants.INCOME, "Loan", "Cash", "Some note here", new Date(), 500, new Date().getTime()));
            r.copyToRealmOrUpdate(new Transaction(Constants.EXPENSE, "Rent", "Bank", "Some note here", new Date(), -500, new Date().getTime()));
            r.copyToRealmOrUpdate(new Transaction(Constants.INCOME, "Salary", "Other", "Some note here", new Date(), 500, new Date().getTime()));
        }, bgRealm::close, error -> {
            bgRealm.close();
            error.printStackTrace();
        });
    }

    private void setUpDatabase() {
        realm = Realm.getDefaultInstance();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (realm != null && !realm.isClosed()) {
            realm.close();
        }
    }
}
