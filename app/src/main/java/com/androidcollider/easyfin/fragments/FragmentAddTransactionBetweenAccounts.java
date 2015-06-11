package com.androidcollider.easyfin.fragments;


import android.app.DatePickerDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.androidcollider.easyfin.R;
import com.androidcollider.easyfin.adapters.SpinnerAddTransExpenseAdapter;
import com.androidcollider.easyfin.database.DataSource;
import com.androidcollider.easyfin.objects.Account;
import com.androidcollider.easyfin.utils.DateFormat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class FragmentAddTransactionBetweenAccounts extends Fragment implements View.OnClickListener {


    private final String DATEFORMAT = "dd.MM.yyyy";

    private TextView tvTransactionBTWDate;
    private DatePickerDialog datePickerDialogBTW;
    private Spinner spinAddTransBTWExpense1, spinAddTransBTWExpense2;


    private View view;
    private DataSource dataSource;

    private List<Account> accountList1;




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_add_transaction_between_accounts, null);

        tvTransactionBTWDate = (TextView) view.findViewById(R.id.tvTransactionBTWDate);

        setDateTimeField();

        dataSource = new DataSource(getActivity());

        setSpinners();


        return view;
    }

    private void setSpinners() {
        setSpinnerExpense1();
        setSpinnerExpense2();
    }



    private void setSpinnerExpense1() {
        spinAddTransBTWExpense1 = (Spinner) view.findViewById(R.id.spinAddTransBTWExpense1);

        List<String> accounts1 = new ArrayList<>();

        accountList1 = dataSource.getAllAccountsInfo();

        for (Account acc : accountList1) {
            accounts1.add(acc.getName());
        }

        spinAddTransBTWExpense1.setAdapter(new SpinnerAddTransExpenseAdapter(getActivity(), R.layout.spinner_item,
                accounts1, accountList1));

        spinAddTransBTWExpense1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setSpinnerExpense2();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }


    private void setSpinnerExpense2() {

        spinAddTransBTWExpense2 = (Spinner) view.findViewById(R.id.spinAddTransBTWExpense2);
        spinAddTransBTWExpense2.setEnabled(false);
        spinAddTransBTWExpense2.setAdapter(null);

        if (accountList1.size() >= 2) {
            int pos = spinAddTransBTWExpense1.getSelectedItemPosition();

            String expense_first_name = accountList1.get(pos).getName();
            String expense_currency = accountList1.get(pos).getCurrency();


            List<Account> accountForTransferList = new ArrayList<>();
            List<String> accountsAvailable = new ArrayList<>();


            for (Account account : accountList1) {
                if (! account.getName().equals(expense_first_name) && account.getCurrency().equals(expense_currency)) {
                    accountForTransferList.add(account);
                    accountsAvailable.add(account.getName());
                }
            }

            if (accountForTransferList.size() >= 1) {
                spinAddTransBTWExpense2.setEnabled(true);

                spinAddTransBTWExpense2.setAdapter(new SpinnerAddTransExpenseAdapter(getActivity(), R.layout.spinner_item,
                        accountsAvailable, accountForTransferList));
            }
            else {
                showNoAvailableAccountsDialog();
            }
        }


    }


    private void showNoAvailableAccountsDialog() {
        new MaterialDialog.Builder(getActivity())
                .title(getActivity().getResources().getString(R.string.dialog_title_no_available_expense))
                .content(getActivity().getResources().getString(R.string.dialog_text_no_available_expense))
                .positiveText(getActivity().getResources().getString(R.string.get_it))
                .show();
    }










    private void closeActivity() {
        getActivity().finish();
    }



    private void setDateTimeField() {
        tvTransactionBTWDate.setOnClickListener(this);

        Calendar newCalendar = Calendar.getInstance();
        tvTransactionBTWDate.setText(DateFormat.dateToString(newCalendar.getTime(), DATEFORMAT));

        datePickerDialogBTW = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);
                tvTransactionBTWDate.setText(DateFormat.dateToString(newDate.getTime(), DATEFORMAT));
            }

        },newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));
    }


    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.tvTransactionBTWDate: datePickerDialogBTW.show(); break;
        }
    }
}
