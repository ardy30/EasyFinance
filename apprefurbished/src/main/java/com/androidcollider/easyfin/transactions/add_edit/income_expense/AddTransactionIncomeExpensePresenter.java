package com.androidcollider.easyfin.transactions.add_edit.income_expense;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;

import com.androidcollider.easyfin.R;
import com.androidcollider.easyfin.common.managers.resources.ResourcesManager;
import com.androidcollider.easyfin.common.models.Transaction;
import com.androidcollider.easyfin.common.view_models.SpinAccountViewModel;
import com.androidcollider.easyfin.transactions.list.TransactionsFragment;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import rx.Observable;
import rx.Subscriber;

/**
 * @author Ihor Bilous
 */

class AddTransactionIncomeExpensePresenter implements AddTransactionIncomeExpenseMVP.Presenter {

    @Nullable
    private AddTransactionIncomeExpenseMVP.View view;
    private AddTransactionIncomeExpenseMVP.Model model;
    private Context context;
    private ResourcesManager resourcesManager;

    private int mode, transType;
    private Transaction transFromIntent;


    AddTransactionIncomeExpensePresenter(Context context,
                                         AddTransactionIncomeExpenseMVP.Model model,
                                         ResourcesManager resourcesManager) {
        this.context = context;
        this.model = model;
        this.resourcesManager = resourcesManager;
    }

    @Override
    public void setView(@Nullable AddTransactionIncomeExpenseMVP.View view) {
        this.view = view;
    }

    @Override
    public void setArguments(Bundle args) {
        mode = args.getInt(TransactionsFragment.MODE, 0);

        switch (mode) {
            case TransactionsFragment.MODE_ADD: {
                transType = args.getInt(TransactionsFragment.TYPE, 0);
                break;
            }
            case TransactionsFragment.MODE_EDIT: {
                transFromIntent = (Transaction) args.getSerializable(TransactionsFragment.TRANSACTION);
                if (transFromIntent != null) {
                    double amount = transFromIntent.getAmount();
                    transType = model.isDoubleNegative(amount) ? TransactionsFragment.TYPE_EXPENSE : TransactionsFragment.TYPE_INCOME;
                }
                break;
            }
        }
    }

    @Override
    public void loadAccounts() {
        model.getAllAccounts()
                .subscribe(new Subscriber<List<SpinAccountViewModel>>() {

                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(List<SpinAccountViewModel> accountList) {
                        setupView(accountList);
                    }
                });
    }

    @Override
    public void save() {
        switch (mode) {
            case TransactionsFragment.MODE_ADD:
                addTransaction();
                break;
            case TransactionsFragment.MODE_EDIT:
                editTransaction();
                break;
        }
    }

    @Override
    public int getTransactionType() {
        return transType;
    }

    private void addTransaction() {
        if (view != null) {
            String sum = model.prepareStringToParse(view.getAmount());
            if (checkSumField(sum)) {
                double amount = Double.parseDouble(sum);
                boolean isExpense = transType == TransactionsFragment.TYPE_EXPENSE;
                if (isExpense) amount *= -1;

                SpinAccountViewModel account = view.getAccount();

                double accountAmount = account.getAmount();

                if (checkIsEnoughCosts(isExpense, amount, accountAmount)) {
                    accountAmount += amount;

                    Transaction transaction = Transaction.builder()
                            .date(model.getMillisFromString(view.getDate()))
                            .amount(amount)
                            .category(view.getCategory())
                            .idAccount(account.getId())
                            .accountAmount(accountAmount)
                            .accountName(account.getName())
                            .accountType(account.getType())
                            .currency(account.getCurrency())
                            .build();

                    handleActionWithTransaction(
                            model.addNewTransaction(transaction)
                    );
                }
            }
        }
    }

    private void editTransaction() {
        if (view != null) {
            String sum = model.prepareStringToParse(view.getAmount());
            if (checkSumField(sum)) {
                double amount = Double.parseDouble(sum);
                boolean isExpense = transType == TransactionsFragment.TYPE_EXPENSE;
                if (isExpense) amount *= -1;

                SpinAccountViewModel account = view.getAccount();
                double accountAmount = account.getAmount();
                int accountId = account.getId();

                int oldAccountId = transFromIntent.getIdAccount();
                boolean isAccountTheSame = accountId == oldAccountId;
                double oldAmount = transFromIntent.getAmount();
                double oldAccountAmount = 0;

                if (isAccountTheSame) accountAmount -= oldAmount;
                else {
                    for (SpinAccountViewModel account1 : view.getAccounts()) {
                        if (oldAccountId == account1.getId()) {
                            oldAccountAmount = account1.getAmount() - oldAmount;
                            break;
                        }
                    }
                }

                if (checkIsEnoughCosts(isExpense, amount, accountAmount)) {
                    accountAmount += amount;

                    Transaction transaction = Transaction.builder()
                            .date(model.getMillisFromString(view.getDate()))
                            .amount(amount)
                            .category(view.getCategory())
                            .idAccount(account.getId())
                            .accountAmount(accountAmount)
                            .id(transFromIntent.getId())
                            .currency(account.getCurrency())
                            .accountType(account.getType())
                            .accountName(account.getName())
                            .build();

                    if (isAccountTheSame) {
                        handleActionWithTransaction(
                                model.updateTransaction(transaction)
                        );
                    } else {
                        handleActionWithTransaction(
                                model.updateTransactionDifferentAccounts(
                                        transaction,
                                        oldAccountAmount,
                                        oldAccountId
                                )
                        );
                    }
                }
            }
        }
    }

    private boolean checkSumField(String sum) {
        if (!sum.matches(".*\\d.*") || Double.parseDouble(sum) == 0) {
            if (view != null) {
                view.showMessage(context.getString(R.string.empty_amount_field));
            }
            return false;
        }
        return true;
    }

    private boolean checkIsEnoughCosts(boolean isExpense, double amount, double accountAmount) {
        if (isExpense && Math.abs(amount) > accountAmount) {
            if (view != null) {
                view.showMessage(context.getString(R.string.not_enough_costs));
            }
            return false;
        }
        return true;
    }

    private void handleActionWithTransaction(Observable<?> observable) {
        observable.subscribe(new Subscriber<Object>() {

            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(Object o) {
                if (view != null) {
                    view.performLastActionsAfterSaveAndClose();
                }
            }
        });
    }

    private void setupView(List<SpinAccountViewModel> accountList) {
        if (view != null) {
            if (accountList.isEmpty()) {
                view.notifyNotEnoughAccounts();
            } else {
                view.setAccounts(accountList);

                switch (mode) {
                    case TransactionsFragment.MODE_ADD: {
                        view.showAmount("0,00", transType);
                        view.openNumericDialog();
                        break;
                    }
                    case TransactionsFragment.MODE_EDIT: {
                        if (transFromIntent != null) {
                            double amount = transFromIntent.getAmount();
                            transType = model.isDoubleNegative(amount) ? TransactionsFragment.TYPE_EXPENSE : TransactionsFragment.TYPE_INCOME;
                            view.showAmount(model.getTransactionForEditAmount(transType, amount), transType);
                        }
                        break;
                    }
                }

                view.setAmountTextColor(ContextCompat.getColor(context,
                        transType == TransactionsFragment.TYPE_INCOME ? R.color.custom_green : R.color.custom_red));

                String[] categoryArray = resourcesManager.getStringArray(
                        transType == TransactionsFragment.TYPE_INCOME ?
                                ResourcesManager.STRING_TRANSACTION_CATEGORY_INCOME :
                                ResourcesManager.STRING_TRANSACTION_CATEGORY_EXPENSE
                );
                TypedArray categoryIcons = resourcesManager.getIconArray(
                        transType == TransactionsFragment.TYPE_INCOME ?
                                ResourcesManager.ICON_TRANSACTION_CATEGORY_INCOME :
                                ResourcesManager.ICON_TRANSACTION_CATEGORY_EXPENSE
                );

                view.setupSpinners(categoryArray, categoryIcons);

                if (mode == TransactionsFragment.MODE_EDIT && transFromIntent != null) {
                    String accountName = transFromIntent.getAccountName();

                    for (int i = 0; i < accountList.size(); i++) {
                        if (accountList.get(i).getName().equals(accountName)) {
                            view.showAccount(i);
                            break;
                        }
                    }

                    view.showCategory(transFromIntent.getCategory());
                }

                Calendar calendar = Calendar.getInstance();
                if (mode == TransactionsFragment.MODE_EDIT && transFromIntent != null) {
                    calendar.setTime(new Date(transFromIntent.getDate()));
                }
                view.setupDateTimeField(calendar);
            }
        }
    }
}