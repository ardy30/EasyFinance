package com.androidcollider.easyfin;


import com.androidcollider.easyfin.adapters.SpinIconTextHeadAdapter;
import com.androidcollider.easyfin.fragments.FrgAccounts;
import com.androidcollider.easyfin.fragments.FrgHome;
import com.androidcollider.easyfin.fragments.FrgMain;
import com.androidcollider.easyfin.objects.Account;
import com.androidcollider.easyfin.objects.InfoFromDB;
import com.androidcollider.easyfin.utils.EditTextAmountWatcher;
import com.androidcollider.easyfin.utils.DoubleFormatUtils;
import com.androidcollider.easyfin.utils.HideKeyboardUtils;
import com.androidcollider.easyfin.utils.ShakeEditText;
import com.androidcollider.easyfin.utils.SharedPref;
import com.androidcollider.easyfin.utils.ToastUtils;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import android.widget.EditText;
import android.widget.Spinner;



public class ActAccount extends AppCompatActivity {

    private Spinner spinType, spinCurrency;
    private EditText etName, etSum;
    private String oldName;
    private int idAccount;

    private int mode;

    private Account accFrIntent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_account);

        initializeFields();

        setMode();

        HideKeyboardUtils.setupUI(findViewById(R.id.layoutActAccountParent), this);
    }

    private void initializeFields() {
        spinType = (Spinner) findViewById(R.id.spinAddAccountType);
        spinCurrency = (Spinner) findViewById(R.id.spinAddAccountCurrency);

        etName = (EditText) findViewById(R.id.editTextAccountName);
        etSum = (EditText) findViewById(R.id.editTextAccountSum);
        etSum.addTextChangedListener(new EditTextAmountWatcher(etSum));
    }

    private void setMode () {
        Intent intent = getIntent();
        mode = intent.getIntExtra("mode", 0);

        switch (mode) {
            case 0: {setToolbar(R.string.new_account); break;}
            case 1: {setToolbar(R.string.edit_account);
                     accFrIntent = (Account) intent.getSerializableExtra("account");
                     setEdits();
                     break;}
        }

        setSpinner();
    }

    private void setSpinner() {

        spinType.setAdapter(new SpinIconTextHeadAdapter(
                this,
                R.layout.spin_head_icon_text,
                R.id.tvSpinHeadIconText,
                R.id.ivSpinHeadIconText,
                R.layout.spin_drop_icon_text,
                R.id.tvSpinDropIconText,
                R.id.ivSpinDropIconText,
                getResources().getStringArray(R.array.account_type_array),
                getResources().obtainTypedArray(R.array.account_type_icons)));


        spinCurrency.setAdapter(new SpinIconTextHeadAdapter(
                this,
                R.layout.spin_head_icon_text,
                R.id.tvSpinHeadIconText,
                R.id.ivSpinHeadIconText,
                R.layout.spin_drop_icon_text,
                R.id.tvSpinDropIconText,
                R.id.ivSpinDropIconText,
                getResources().getStringArray(R.array.account_currency_array),
                getResources().obtainTypedArray(R.array.flag_icons)));

        if (mode == 1) {

            spinType.setSelection(accFrIntent.getType());

            String[] currencyArray = getResources().getStringArray(R.array.account_currency_array);

            String currencyVal = accFrIntent.getCurrency();

            for (int i = 0; i < currencyArray.length; i++) {
                if (currencyArray[i].equals(currencyVal)) {
                    spinCurrency.setSelection(i);
                }
            }

            spinCurrency.setEnabled(false);
        }
    }

    private void setToolbar(int id) {
        Toolbar toolBar = (Toolbar) findViewById(R.id.toolbarMain);
        setSupportActionBar(toolBar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(id);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        toolBar.inflateMenu(R.menu.toolbar_account_menu);
    }

    private void setEdits() {

        oldName = accFrIntent.getName();
        etName.setText(oldName);
        etName.setSelection(etName.getText().length());

        final int PRECISE = 100;
        final String FORMAT = "0.00";

        etSum.setText(DoubleFormatUtils.doubleToStringFormatter(accFrIntent.getAmount(), FORMAT, PRECISE));
        etSum.setSelection(etSum.getText().length());

        idAccount = accFrIntent.getId();
    }

    private void addAccount() {

        if(checkForFillNameSumFields()) {

            String name = etName.getText().toString();

                if (InfoFromDB.getInstance().checkForAccountNameMatches(name)) {
                    ShakeEditText.highlightEditText(etName);
                    ToastUtils.showClosableToast(this, getString(R.string.account_name_exist), 1);
                }

                else {

                    double amount = Double.parseDouble(DoubleFormatUtils.prepareStringToParse(etSum.getText().toString()));
                    String currency = spinCurrency.getSelectedItem().toString();
                    int type = spinType.getSelectedItemPosition();

                    Account account = new Account(name, amount, type, currency);

                    InfoFromDB.getInstance().getDataSource().insertNewAccount(account);

                    lastActions();
                }
            }
        }

    private void editAccount() {

        if (checkForFillNameSumFields()) {

                String name = etName.getText().toString();

                if (InfoFromDB.getInstance().checkForAccountNameMatches(name) && ! name.equals(oldName)) {
                    ShakeEditText.highlightEditText(etName);
                    ToastUtils.showClosableToast(this, getString(R.string.account_name_exist), 1);
                }

                else {

                    String sum = DoubleFormatUtils.prepareStringToParse(etSum.getText().toString());
                    double amount = Double.parseDouble(sum);
                    String currency = spinCurrency.getSelectedItem().toString();
                    int type = spinType.getSelectedItemPosition();

                    Account account = new Account(idAccount, name, amount, type, currency);

                    InfoFromDB.getInstance().getDataSource().editAccount(account);

                    lastActions();
                }
            }
        }

    private void lastActions() {
        InfoFromDB.getInstance().updateAccountList();
        pushBroadcast();
        this.finish();
    }

    private boolean checkForFillNameSumFields() {

        String st = etName.getText().toString().replaceAll("\\s+", "");

        if (st.isEmpty()) {
            ShakeEditText.highlightEditText(etName);
            ToastUtils.showClosableToast(this, getString(R.string.empty_name_field), 1);

            return false;
        }

        else {

            if (!DoubleFormatUtils.prepareStringToParse(etSum.getText().toString()).matches(".*\\d.*")) {
                ShakeEditText.highlightEditText(etSum);
                ToastUtils.showClosableToast(this, getString(R.string.empty_amount_field), 1);

                return false;
            }
        }

        return true;
    }

    private void pushBroadcast() {
        Intent intentFrgMain = new Intent(FrgHome.BROADCAST_FRG_MAIN_ACTION);
        intentFrgMain.putExtra(FrgHome.PARAM_STATUS_FRG_MAIN, FrgHome.STATUS_UPDATE_FRG_MAIN_BALANCE);
        sendBroadcast(intentFrgMain);

        Intent intentFrgAccounts = new Intent(FrgAccounts.BROADCAST_FRG_ACCOUNT_ACTION);
        intentFrgAccounts.putExtra(FrgAccounts.PARAM_STATUS_FRG_ACCOUNT, FrgAccounts.STATUS_UPDATE_FRG_ACCOUNT);
        sendBroadcast(intentFrgAccounts);


        if (mode == 0) {
            SharedPref sp = new SharedPref(this);
            if(!sp.isSnackBarAccountDisable()) {

                Intent intentMainSnack = new Intent(FrgMain.BROADCAST_MAIN_SNACK_ACTION);
                intentMainSnack.putExtra(FrgMain.PARAM_STATUS_MAIN_SNACK, FrgMain.STATUS_MAIN_SNACK);
                sendBroadcast(intentMainSnack);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                this.finish();
                return true;}

            case R.id.account_action_save: {

                switch (mode) {
                    case 0: {addAccount(); break;}
                    case 1: {editAccount(); break;}
                }

                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.toolbar_account_menu, menu);

        return true;
    }

}
