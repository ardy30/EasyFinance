package com.androidcollider.easyfin.fragments;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.androidcollider.easyfin.R;
import com.androidcollider.easyfin.adapters.RecyclerTransactionAdapter;
import com.androidcollider.easyfin.common.MainActivity;
import com.androidcollider.easyfin.common.app.App;
import com.androidcollider.easyfin.events.UpdateFrgAccounts;
import com.androidcollider.easyfin.events.UpdateFrgHome;
import com.androidcollider.easyfin.events.UpdateFrgTransactions;
import com.androidcollider.easyfin.models.Transaction;
import com.androidcollider.easyfin.repository.Repository;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import rx.Subscriber;

public class FrgTransactions extends CommonFragmentWithEvents {

    private RecyclerView recyclerView;
    private TextView tvEmpty;
    private List<Transaction> transactionList;
    private RecyclerTransactionAdapter recyclerAdapter;

    @Inject
    Repository repository;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frg_transactions, container, false);
        ((App) getActivity().getApplication()).getComponent().inject(this);
        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerTransaction);
        tvEmpty = (TextView) view.findViewById(R.id.tvEmptyTransactions);
        setupRecyclerView();
        registerForContextMenu(recyclerView);
        super.onCreateView(inflater, container, savedInstanceState);
        return view;
    }

    private void setupRecyclerView() {
        repository.getAllTransactions()
                .subscribe(new Subscriber<List<Transaction>>() {

                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(List<Transaction> transactionList) {
                        FrgTransactions.this.transactionList = new ArrayList<>();
                        FrgTransactions.this.transactionList.addAll(transactionList);
                        setVisibility();
                        final LinearLayoutManager layoutManager = new LinearLayoutManager(recyclerView.getContext());
                        recyclerView.setLayoutManager(layoutManager);
                        recyclerAdapter = new RecyclerTransactionAdapter(getActivity(), FrgTransactions.this.transactionList);
                        recyclerView.setAdapter(recyclerAdapter);
                        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

                            @Override
                            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                                super.onScrollStateChanged(recyclerView, newState);
                            }

                            @Override
                            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                                FrgMain parentFragment = (FrgMain) getParentFragment();
                                if (parentFragment != null) {
                                    if (dy > 0) {
                                        parentFragment.hideMenu();
                                    } else if (dy < 0) {
                                        parentFragment.showMenu();
                                    }
                                }
                                super.onScrolled(recyclerView, dx, dy);
                            }
                        });
                    }
                });
    }

    private void setVisibility() {
        recyclerView.setVisibility(transactionList.isEmpty() ? View.GONE : View.VISIBLE);
        tvEmpty.setVisibility(transactionList.isEmpty() ? View.VISIBLE : View.GONE);
    }

    public boolean onContextItemSelected(MenuItem item) {
        int pos;
        try {
            pos = (int) recyclerAdapter.getPosition();
        } catch (Exception e) {
            return super.onContextItemSelected(item);
        }

        switch (item.getItemId()) {
            case R.id.ctx_menu_edit_transaction:
                goToEditTransaction(pos);
                break;
            case R.id.ctx_menu_delete_transaction:
                showDialogDeleteTransaction(pos);
                break;
        }
        return super.onContextItemSelected(item);
    }

    private void showDialogDeleteTransaction(final int pos) {
        new MaterialDialog.Builder(getActivity())
                .title(getString(R.string.dialog_title_delete))
                .content(getString(R.string.transaction_delete_warning))
                .positiveText(getString(R.string.delete))
                .negativeText(getString(R.string.cancel))
                .onPositive((dialog, which) -> deleteTransaction(pos))
                .cancelable(false)
                .show();
    }

    private void goToEditTransaction(int pos) {
        Transaction transaction = transactionList.get(pos);

        FrgAddTransactionDefault frgAddTransDef = new FrgAddTransactionDefault();
        Bundle arguments = new Bundle();
        arguments.putInt("mode", 1);
        arguments.putSerializable("transaction", transaction);
        frgAddTransDef.setArguments(arguments);
        ((MainActivity) getActivity()).addFragment(frgAddTransDef);
    }

    private void deleteTransaction(int pos) {
        Transaction transaction = transactionList.get(pos);
        int idAccount = transaction.getIdAccount();
        int idTrans = transaction.getId();
        double amount = transaction.getAmount();

        //InMemoryRepository.getInstance().getDataSource().deleteTransaction(idAccount, idTrans, amount);

        repository.deleteTransaction(idAccount, idTrans, amount)
                .subscribe(new Subscriber<Boolean>() {

                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Boolean aBoolean) {
                        transactionList.remove(pos);
                        setVisibility();
                        recyclerAdapter.notifyDataSetChanged();
                        //InMemoryRepository.getInstance().updateAccountList();
                        pushBroadcast();
                    }
                });
    }

    private void pushBroadcast() {
        EventBus.getDefault().post(new UpdateFrgHome());
        EventBus.getDefault().post(new UpdateFrgAccounts());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(UpdateFrgTransactions event) {
        repository.getAllTransactions()
                .subscribe(new Subscriber<List<Transaction>>() {

                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(List<Transaction> transactionList) {
                        FrgTransactions.this.transactionList.clear();
                        FrgTransactions.this.transactionList.addAll(transactionList);
                        setVisibility();
                        recyclerAdapter.notifyDataSetChanged();
                    }
                });
    }
}