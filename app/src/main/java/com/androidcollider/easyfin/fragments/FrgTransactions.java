package com.androidcollider.easyfin.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
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
import com.androidcollider.easyfin.objects.InfoFromDB;
import com.androidcollider.easyfin.objects.Transaction;

import java.util.ArrayList;


public class FrgTransactions extends Fragment {

    public final static String BROADCAST_FRG_TRANSACTION_ACTION = "com.androidcollider.easyfin.frgtransaction.broadcast";
    public final static String PARAM_STATUS_FRG_TRANSACTION = "update_frg_transaction";
    public final static int STATUS_UPDATE_FRG_TRANSACTION = 3;

    private RecyclerView recyclerView;
    private TextView tvEmpty;

    private BroadcastReceiver broadcastReceiver;

    private ArrayList<Transaction> transactionList = null;
    private RecyclerTransactionAdapter recyclerAdapter;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.frg_transactions, container, false);

        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerTransaction);

        tvEmpty = (TextView) view.findViewById(R.id.tvEmptyTransactions);

        setItemTransaction();

        registerForContextMenu(recyclerView);

        makeBroadcastReceiver();

        return view;
    }

    private void setItemTransaction() {

        transactionList = InfoFromDB.getInstance().getDataSource().getAllTransactionsInfo();

        setVisibility();

        final LinearLayoutManager layoutManager = new LinearLayoutManager(recyclerView.getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerAdapter = new RecyclerTransactionAdapter(getActivity(), transactionList);
        recyclerView.setAdapter(recyclerAdapter);
    }

    private void makeBroadcastReceiver() {
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int status = intent.getIntExtra(PARAM_STATUS_FRG_TRANSACTION, 0);

                if (status == STATUS_UPDATE_FRG_TRANSACTION) {

                    transactionList.clear();
                    transactionList.addAll(InfoFromDB.getInstance().getDataSource().getAllTransactionsInfo());

                    setVisibility();

                    recyclerAdapter.notifyDataSetChanged();
                }
            }
        };

        IntentFilter intentFilter = new IntentFilter(BROADCAST_FRG_TRANSACTION_ACTION);

        getActivity().registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(broadcastReceiver);
    }

    private void setVisibility() {
        if (transactionList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);
        }

        else {
            tvEmpty.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
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

            {
                goToEditTransaction(pos);
                break;
            }

            case R.id.ctx_menu_delete_transaction:

            {
                showDialogDeleteTransaction(pos);
                break;
            }
        }
        return super.onContextItemSelected(item);
    }

    private void showDialogDeleteTransaction(final int pos) {

        new MaterialDialog.Builder(getActivity())
                .title(getString(R.string.dialog_title_delete))
                .content(getString(R.string.transaction_delete_warning))
                .positiveText(getString(R.string.delete))
                .negativeText(getString(R.string.cancel))
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        deleteTransaction(pos);
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {

                    }
                })
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
        addFragment(frgAddTransDef);
    }

    private void deleteTransaction(int pos) {
        Transaction transaction = transactionList.get(pos);
        int idAccount = transaction.getIdAccount();

        int idTrans = transaction.getId();

        double amount = transaction.getAmount();


        InfoFromDB.getInstance().getDataSource().deleteTransaction(idAccount, idTrans, amount);

        transactionList.remove(pos);

        setVisibility();

        recyclerAdapter.notifyDataSetChanged();

        InfoFromDB.getInstance().updateAccountList();

        pushBroadcast();
    }

    private void pushBroadcast() {
        Intent intentFragmentMain = new Intent(FrgHome.BROADCAST_FRG_MAIN_ACTION);
        intentFragmentMain.putExtra(FrgHome.PARAM_STATUS_FRG_MAIN, FrgHome.STATUS_UPDATE_FRG_MAIN);
        getActivity().sendBroadcast(intentFragmentMain);

        Intent intentFrgAccounts = new Intent(FrgAccounts.BROADCAST_FRG_ACCOUNT_ACTION);
        intentFrgAccounts.putExtra(FrgAccounts.PARAM_STATUS_FRG_ACCOUNT, FrgAccounts.STATUS_UPDATE_FRG_ACCOUNT);
        getActivity().sendBroadcast(intentFrgAccounts);
    }


    public void addFragment(Fragment f){
        treatFragment(f, true, false);
    }

    public Fragment getTopFragment(){
        return getActivity().getSupportFragmentManager().findFragmentById(R.id.fragment_container);
    }

    private void treatFragment(Fragment f, boolean addToBackStack, boolean replace){
        String tag = f.getClass().getName();
        FragmentTransaction ft =  getActivity().getSupportFragmentManager().beginTransaction();

        if (replace) {

            ft.replace(R.id.fragment_container, f, tag);

        } else {

            Fragment currentTop = getTopFragment();

            if (currentTop != null) ft.hide(currentTop);

            ft.add(R.id.fragment_container, f, tag);
        }

        if (addToBackStack) ft.addToBackStack(tag);

        ft.commitAllowingStateLoss();
    }

}
