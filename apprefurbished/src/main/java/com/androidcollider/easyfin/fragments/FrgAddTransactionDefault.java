package com.androidcollider.easyfin.fragments;

import android.app.DatePickerDialog;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import com.androidcollider.easyfin.R;
import com.androidcollider.easyfin.adapters.SpinAccountForTransHeadIconAdapter;
import com.androidcollider.easyfin.adapters.SpinIconTextHeadAdapter;
import com.androidcollider.easyfin.common.app.App;
import com.androidcollider.easyfin.common.events.UpdateFrgAccounts;
import com.androidcollider.easyfin.common.events.UpdateFrgHome;
import com.androidcollider.easyfin.common.events.UpdateFrgTransactions;
import com.androidcollider.easyfin.managers.format.date.DateFormatManager;
import com.androidcollider.easyfin.managers.format.number.NumberFormatManager;
import com.androidcollider.easyfin.managers.ui.toast.ToastManager;
import com.androidcollider.easyfin.models.Account;
import com.androidcollider.easyfin.models.Transaction;
import com.androidcollider.easyfin.repository.Repository;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import rx.Subscriber;

public class FrgAddTransactionDefault extends CommonFragmentAddEdit implements FrgNumericDialog.OnCommitAmountListener {

    private TextView tvDate, tvAmount;
    private DatePickerDialog datePickerDialog;
    private Spinner spinCategory, spinAccount;
    private final String DATEFORMAT = "dd MMMM yyyy";
    private View view;
    private ArrayList<Account> accountList = null;
    private int mode, transType;
    private Transaction transFromIntent;
    //private final String prefixExpense = "-", prefixIncome = "+";

    @Inject
    Repository repository;

    @Inject
    ToastManager toastManager;

    @Inject
    DateFormatManager dateFormatManager;

    @Inject
    NumberFormatManager numberFormatManager;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.frg_add_trans_def, container, false);
        ((App) getActivity().getApplication()).getComponent().inject(this);
        setToolbar();

        //accountList = InMemoryRepository.getInstance().getAccountList();
        accountList = new ArrayList<>();
        repository.getAllAccounts()
                .subscribe(new Subscriber<List<Account>>() {

                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(List<Account> accountList) {
                        FrgAddTransactionDefault.this.accountList.clear();
                        FrgAddTransactionDefault.this.accountList.addAll(accountList);

                        ScrollView scrollView = (ScrollView) view.findViewById(R.id.scrollAddTransDef);

                        if (accountList.isEmpty()) {
                            scrollView.setVisibility(View.GONE);
                            showDialogNoAccount(getString(R.string.dialog_text_transaction_no_account), false);
                        } else {
                            scrollView.setVisibility(View.VISIBLE);
                            mode = getArguments().getInt("mode", 0);
                            tvAmount = (TextView) view.findViewById(R.id.tvAddTransDefAmount);
                            tvAmount.setOnClickListener(v -> openNumericDialog(tvAmount.getText().toString()));

                            switch (mode) {
                                case 0: {
                                    transType = getArguments().getInt("type", 0);
                                    setAmountValue("0,00");
                                    openNumericDialog(tvAmount.getText().toString());
                                    break;
                                }
                                case 1: {
                                    transFromIntent = (Transaction) getArguments().getSerializable("transaction");
                                    final int PRECISE = 100;
                                    final String FORMAT = "###,##0.00";

                                    if (transFromIntent != null) {
                                        double amount = transFromIntent.getAmount();
                                        transType = numberFormatManager.isDoubleNegative(amount) ? 0 : 1;
                                        String amountS = numberFormatManager.doubleToStringFormatterForEdit(
                                                transType == 1 ? amount : Math.abs(amount), FORMAT, PRECISE);
                                        setAmountValue(amountS);
                                    }
                                    break;
                                }
                            }

                            tvDate = (TextView) view.findViewById(R.id.tvTransactionDate);
                            setDateTimeField();
                            setSpinner();

                            tvAmount.setTextColor(ContextCompat.getColor(getActivity(), transType == 1 ? R.color.custom_green : R.color.custom_red));
                        }
                    }
                });

        return view;
    }

    private void setSpinner() {
        spinCategory = (Spinner) view.findViewById(R.id.spinAddTransCategory);
        spinAccount = (Spinner) view.findViewById(R.id.spinAddTransDefAccount);

        String[] categoryArray = getResources().getStringArray(
                transType == 1 ?
                        R.array.transaction_category_income_array :
                        R.array.transaction_category_expense_array);
        TypedArray categoryIcons = getResources().obtainTypedArray(
                transType == 1 ?
                        R.array.transaction_category_income_icons :
                        R.array.transaction_category_expense_icons);

        spinCategory.setAdapter(new SpinIconTextHeadAdapter(
                getActivity(),
                R.layout.spin_head_icon_text,
                R.id.tvSpinHeadIconText,
                R.id.ivSpinHeadIconText,
                R.layout.spin_drop_icon_text,
                R.id.tvSpinDropIconText,
                R.id.ivSpinDropIconText,
                categoryArray,
                categoryIcons));

        spinCategory.setSelection(categoryArray.length - 1);

        spinAccount.setAdapter(new SpinAccountForTransHeadIconAdapter(
                getActivity(),
                R.layout.spin_head_icon_text,
                accountList,
                numberFormatManager));

        if (mode == 1) {
            String accountName = transFromIntent.getAccountName();
            for (int i = 0; i < accountList.size(); i++) {
                if (accountList.get(i).getName().equals(accountName)) {
                    spinAccount.setSelection(i);
                    break;
                }
            }

            spinCategory.setSelection(transFromIntent.getCategory());
        }
    }

    public void addTransaction() {
        String sum = numberFormatManager.prepareStringToParse(tvAmount.getText().toString());
        if (checkSumField(sum)) {
            double amount = Double.parseDouble(sum);
            boolean isExpense = transType == 0;
            if (isExpense) amount *= -1;

            Account account = (Account) spinAccount.getSelectedItem();

            double accountAmount = account.getAmount();

            if (checkIsEnoughCosts(isExpense, amount, accountAmount)) {
                accountAmount += amount;

                int category = spinCategory.getSelectedItemPosition();
                Long date = dateFormatManager.stringToDate(tvDate.getText().toString(), DATEFORMAT).getTime();
                int idAccount = account.getId();

                Transaction transaction = Transaction.builder()
                        .date(date)
                        .amount(amount)
                        .category(category)
                        .idAccount(idAccount)
                        .accountAmount(accountAmount)
                        .accountName(account.getName())
                        .accountType(account.getType())
                        .currency(account.getCurrency())
                        .build();
                //InMemoryRepository.getInstance().getDataSource().insertNewTransaction(transaction);
                repository.addNewTransaction(transaction)
                        .subscribe(new Subscriber<Transaction>() {

                            @Override
                            public void onCompleted() {

                            }

                            @Override
                            public void onError(Throwable e) {

                            }

                            @Override
                            public void onNext(Transaction transaction) {
                                lastActions();
                            }
                        });
            }
        }
    }

    private void editTransaction() {
        String sum = numberFormatManager.prepareStringToParse(tvAmount.getText().toString());
        if (checkSumField(sum)) {
            double amount = Double.parseDouble(sum);
            boolean isExpense = transType == 0;
            if (isExpense) amount *= -1;

            Account account = (Account) spinAccount.getSelectedItem();
            double accountAmount = account.getAmount();
            int accountId = account.getId();

            int oldAccountId = transFromIntent.getIdAccount();
            boolean isAccountTheSame = accountId == oldAccountId;
            double oldAmount = transFromIntent.getAmount();
            double oldAccountAmount = 0;

            if (isAccountTheSame) accountAmount -= oldAmount;
            else {
                for (Account account1 : accountList) {
                    if (oldAccountId == account1.getId()) {
                        oldAccountAmount = account1.getAmount() - oldAmount;
                        break;
                    }
                }
            }

            if (checkIsEnoughCosts(isExpense, amount, accountAmount)) {
                accountAmount += amount;

                int category = spinCategory.getSelectedItemPosition();
                Long date = dateFormatManager.stringToDate(tvDate.getText().toString(), DATEFORMAT).getTime();
                int idAccount = account.getId();

                int idTrans = transFromIntent.getId();

                Transaction transaction = Transaction.builder()
                        .date(date)
                        .amount(amount)
                        .category(category)
                        .idAccount(idAccount)
                        .accountAmount(accountAmount)
                        .id(idTrans)
                        .currency(account.getCurrency())
                        .accountType(account.getType())
                        .accountName(account.getName())
                        .build();

                if (isAccountTheSame) {
                    //InMemoryRepository.getInstance().getDataSource().editTransaction(transaction);
                    repository.updateTransaction(transaction)
                            .subscribe(new Subscriber<Transaction>() {

                                @Override
                                public void onCompleted() {

                                }

                                @Override
                                public void onError(Throwable e) {

                                }

                                @Override
                                public void onNext(Transaction transaction) {
                                    lastActions();
                                }
                            });
                } else {
                    //InMemoryRepository.getInstance().getDataSource().editTransactionDifferentAccounts(transaction, oldAccountAmount, oldAccountId);
                    repository.updateTransactionDifferentAccounts(transaction, oldAccountAmount, oldAccountId)
                            .subscribe(new Subscriber<Boolean>() {

                                @Override
                                public void onCompleted() {

                                }

                                @Override
                                public void onError(Throwable e) {

                                }

                                @Override
                                public void onNext(Boolean aBoolean) {
                                    lastActions();
                                }
                            });
                }
            }
        }
    }

    private boolean checkSumField(String sum) {
        if (!sum.matches(".*\\d.*") || Double.parseDouble(sum) == 0) {
            toastManager.showClosableToast(getActivity(), getString(R.string.empty_amount_field), ToastManager.SHORT);
            return false;
        }
        return true;
    }

    private boolean checkIsEnoughCosts(boolean isExpense, double amount, double accountAmount) {
        if (isExpense && Math.abs(amount) > accountAmount) {
            toastManager.showClosableToast(getActivity(), getString(R.string.not_enough_costs), ToastManager.SHORT);
            return false;
        }
        return true;
    }

    private void lastActions() {
        //InMemoryRepository.getInstance().updateAccountList();
        pushBroadcast();
        finish();
    }

    private void setDateTimeField() {
        tvDate.setOnClickListener(v -> datePickerDialog.show());
        Calendar newCalendar = Calendar.getInstance();
        if (mode == 1) {
            newCalendar.setTime(new Date(transFromIntent.getDate()));
        }
        tvDate.setText(dateFormatManager.dateToString(newCalendar.getTime(), DATEFORMAT));

        datePickerDialog = new DatePickerDialog(getActivity(), (view1, year, monthOfYear, dayOfMonth) -> {
            Calendar newDate = Calendar.getInstance();
            newDate.set(year, monthOfYear, dayOfMonth);

            if (newDate.getTimeInMillis() > System.currentTimeMillis()) {
                toastManager.showClosableToast(getActivity(), getString(R.string.transaction_date_future), ToastManager.SHORT);
            } else {
                tvDate.setText(dateFormatManager.dateToString(newDate.getTime(), DATEFORMAT));
            }
        }, newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));
    }

    private void pushBroadcast() {
        EventBus.getDefault().post(new UpdateFrgHome());
        EventBus.getDefault().post(new UpdateFrgTransactions());
        EventBus.getDefault().post(new UpdateFrgAccounts());
    }

    @Override
    public void onCommitAmountSubmit(String amount) {
        setAmountValue(amount);
    }

    @Override
    void handleSaveAction() {
        switch (mode) {
            case 0:
                addTransaction();
                break;
            case 1:
                editTransaction();
                break;
        }
    }

    private void setAmountValue(String amount) {
        setTVTextSize(tvAmount, amount, 9, 14);
        tvAmount.setText(String.format("%1$s %2$s", transType == 1 ? "+" : "-", amount));
    }
}