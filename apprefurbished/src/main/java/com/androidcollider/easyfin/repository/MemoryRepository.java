package com.androidcollider.easyfin.repository;

import com.androidcollider.easyfin.models.Account;
import com.androidcollider.easyfin.models.Debt;
import com.androidcollider.easyfin.models.Rates;
import com.androidcollider.easyfin.models.Transaction;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

/**
 * @author Ihor Bilous
 */

public class MemoryRepository implements Repository {

    private List<Account> accountList;
    private List<Transaction> transactionList;
    private List<Debt> debtList;
    private double[] ratesArray;


    @Override
    public Observable<Boolean> addNewAccount(Account account) {
        return Observable.<Boolean>create(subscriber -> {
            boolean b = accountList.add(account);
            subscriber.onNext(b);
            subscriber.onCompleted();
        })
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<List<Account>> getAllAccounts() {
        return accountList == null ? null : Observable.just(accountList);
    }

    @Override
    public Observable<Boolean> updateAccount(Account account) {
        return Observable.<Boolean>create(subscriber -> {
            int pos = accountList.indexOf(account);
            boolean b = pos >= 0 && accountList.set(pos, account) != null;
            subscriber.onNext(b);
            subscriber.onCompleted();
        })
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<Boolean> deleteAccount(int id) {
        return null;
    }

    @Override
    public Observable<Boolean> transferBTWAccounts(int idAccount1, double accountAmount1, int idAccount2, double accountAmount2) {
        return null;
    }

    @Override
    public Observable<Boolean> addNewTransaction(Transaction transaction) {
        return Observable.<Boolean>create(subscriber -> {
            boolean b = transactionList.add(transaction);
            subscriber.onNext(b);
            subscriber.onCompleted();
        })
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<List<Transaction>> getAllTransactions() {
        return transactionList == null ? null : Observable.just(transactionList);
    }

    @Override
    public Observable<Boolean> updateTransaction(Transaction transaction) {
        return null;
    }

    @Override
    public Observable<Boolean> updateTransactionDifferentAccounts(Transaction transaction, double oldAccountAmount, int oldAccountId) {
        return null;
    }

    @Override
    public Observable<Boolean> deleteTransaction(int idAccount, int idTransaction, double amount) {
        return null;
    }

    @Override
    public Observable<Boolean> addNewDebt(Debt debt) {
        return Observable.<Boolean>create(subscriber -> {
            boolean b = debtList.add(debt);
            subscriber.onNext(b);
            subscriber.onCompleted();
        })
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<List<Debt>> getAllDebts() {
        return debtList == null ? null : Observable.just(debtList);
    }

    @Override
    public Observable<Boolean> updateDebt(Debt debt) {
        return null;
    }

    @Override
    public Observable<Boolean> updateDebtDifferentAccounts(Debt debt, double oldAccountAmount, int oldAccountId) {
        return null;
    }

    @Override
    public Observable<Boolean> deleteDebt(int idAccount, int idDebt, double amount, int type) {
        return null;
    }

    @Override
    public Observable<Boolean> payFullDebt(int idAccount, double accountAmount, int idDebt) {
        return null;
    }

    @Override
    public Observable<Boolean> payPartOfDebt(int idAccount, double accountAmount, int idDebt, double debtAmount) {
        return null;
    }

    @Override
    public Observable<Boolean> takeMoreDebt(int idAccount, double accountAmount, int idDebt, double debtAmount, double debtAllAmount) {
        return null;
    }

    @Override
    public Observable<Map<String, double[]>> getTransactionsStatistic(int position) {
        return null;
    }

    @Override
    public Observable<Map<String, double[]>> getAccountsAmountSumGroupByTypeAndCurrency() {
        return null;
    }

    @Override
    public Observable<Boolean> updateRates(List<Rates> ratesList) {
        return null;
    }

    @Override
    public Observable<double[]> getRates() {
        return ratesArray == null ? null : Observable.just(ratesArray);
    }

    @Override
    public Observable<Boolean> setAllAccounts(List<Account> accountList) {
        return Observable.<Boolean>create(subscriber -> {
            this.accountList = new ArrayList<>();
            this.accountList.addAll(accountList);
            subscriber.onNext(true);
            subscriber.onCompleted();
        })
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<Boolean> setAllTransactions(List<Transaction> transactionList) {
        return Observable.<Boolean>create(subscriber -> {
            this.transactionList = new ArrayList<>();
            this.transactionList.addAll(transactionList);
            subscriber.onNext(true);
            subscriber.onCompleted();
        })
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<Boolean> setAllDebts(List<Debt> debtList) {
        return Observable.<Boolean>create(subscriber -> {
            this.debtList = new ArrayList<>();
            this.debtList.addAll(debtList);
            subscriber.onNext(true);
            subscriber.onCompleted();
        })
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<Boolean> setRates(double[] rates) {
        return Observable.<Boolean>create(subscriber -> {
            this.ratesArray = new double[4];
            System.arraycopy(rates, 0, this.ratesArray, 0, rates.length);
            subscriber.onNext(true);
            subscriber.onCompleted();
        })
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread());
    }
}