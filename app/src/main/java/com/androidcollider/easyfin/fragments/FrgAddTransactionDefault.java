package com.androidcollider.easyfin.fragments;


import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.androidcollider.easyfin.R;
import com.androidcollider.easyfin.adapters.SpinAccountForTransHeadIconAdapter;
import com.androidcollider.easyfin.adapters.SpinIconTextHeadAdapter;
import com.androidcollider.easyfin.database.DataSource;
import com.androidcollider.easyfin.objects.Account;
import com.androidcollider.easyfin.objects.InfoFromDB;
import com.androidcollider.easyfin.objects.Transaction;
import com.androidcollider.easyfin.utils.DateFormat;
import com.androidcollider.easyfin.utils.Shake;

import java.util.ArrayList;
import java.util.Calendar;



public class FrgAddTransactionDefault extends Fragment implements View.OnClickListener {

    private TextView tvDate;
    private DatePickerDialog datePickerDialog;
    private Spinner spinCategory, spinAccount;

    private final String DATEFORMAT = "dd.MM.yyyy";

    private View view;




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.frg_add_transaction_default, container, false);

        tvDate = (TextView) view.findViewById(R.id.tvTransactionDate);

        setDateTimeField();

        setSpinner();

        return view;
    }



    private void setSpinner() {
        spinCategory = (Spinner) view.findViewById(R.id.spinAddTransCategory);
        spinAccount = (Spinner) view.findViewById(R.id.spinAddTransExpense);

        String[] category = getResources().getStringArray(R.array.cat_transaction_array);


        spinCategory.setAdapter(new SpinIconTextHeadAdapter(
                getActivity(),
                R.layout.spin_head_icon_text,
                R.id.tvSpinHeadIconText,
                R.id.ivSpinHeadIconText,
                R.layout.spin_drop_icon_text,
                R.id.tvSpinDropIconText,
                R.id.ivSpinDropIconText,
                category,
                getResources().obtainTypedArray(R.array.trans_categories_icons)));

        spinCategory.setSelection(category.length-1);


        ArrayList<Account> accountList = InfoFromDB.getInstance().getAccountList();

        spinAccount.setAdapter(new SpinAccountForTransHeadIconAdapter(
                getActivity(),
                R.layout.spin_head_icon_text,
                accountList));

        if (accountList.isEmpty()) {
            spinAccount.setEnabled(false);
        }
    }



    public void addTransaction() {
        EditText etSum = (EditText) view.findViewById(R.id.editTextTransSum);
        RadioButton rbCost = (RadioButton) view.findViewById(R.id.radioButtonCost);

        String sum = etSum.getText().toString();

        if (! sum.matches(".*\\d.*") || Double.parseDouble(sum) == 0) {
            Shake.highlightEditText(etSum);
            Toast.makeText(getActivity(), getResources().getString(R.string.transaction_empty_amount_field), Toast.LENGTH_LONG).show();
        }

        else {

            Long date = DateFormat.stringToDate(tvDate.getText().toString(), DATEFORMAT).getTime();

            double amount = Double.parseDouble(sum);
            if (rbCost.isChecked()) {
                amount *= -1;}


            Account account = (Account) spinAccount.getSelectedItem();

            double accountAmount = account.getAmount();

            if (rbCost.isChecked() && Math.abs(amount) > accountAmount) {
                Toast.makeText(getActivity(), getResources().getString(R.string.transaction_not_enough_costs) + " " +
                        Math.abs(amount), Toast.LENGTH_LONG).show();}
            else {
                accountAmount += amount;

                String category = spinCategory.getSelectedItem().toString();
                int idAccount = account.getId();

                Transaction transaction = new Transaction(date, amount, category, idAccount, accountAmount);
                new DataSource(getActivity()).insertNewTransaction(transaction);

                InfoFromDB.getInstance().updateAccountList();

                pushBroadcast();

                getActivity().finish();
            }
        }
    }


    private void setDateTimeField() {
        tvDate.setOnClickListener(this);

        Calendar newCalendar = Calendar.getInstance();
        tvDate.setText(DateFormat.dateToString(newCalendar.getTime(), DATEFORMAT));

        datePickerDialog = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);
                tvDate.setText(DateFormat.dateToString(newDate.getTime(), DATEFORMAT));
            }

        },newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.tvTransactionDate: datePickerDialog.show(); break;
        }
    }

    private void pushBroadcast() {
        Intent intentFragmentMain = new Intent(FrgMain.BROADCAST_FRAGMENT_MAIN_ACTION);
        intentFragmentMain.putExtra(FrgMain.PARAM_STATUS_FRAGMENT_MAIN, FrgMain.STATUS_UPDATE_FRAGMENT_MAIN);
        getActivity().sendBroadcast(intentFragmentMain);

        Intent intentFragmentTransaction = new Intent(FrgTransactions.BROADCAST_FRG_TRANSACTION_ACTION);
        intentFragmentTransaction.putExtra(FrgTransactions.PARAM_STATUS_FRG_TRANSACTION, FrgTransactions.STATUS_UPDATE_FRG_TRANSACTION);
        getActivity().sendBroadcast(intentFragmentTransaction);

        Intent intentFrgAccounts = new Intent(FrgAccounts.BROADCAST_FRG_ACCOUNT_ACTION);
        intentFrgAccounts.putExtra(FrgAccounts.PARAM_STATUS_FRG_ACCOUNT, FrgAccounts.STATUS_UPDATE_FRG_ACCOUNT);
        getActivity().sendBroadcast(intentFrgAccounts);
    }

}
