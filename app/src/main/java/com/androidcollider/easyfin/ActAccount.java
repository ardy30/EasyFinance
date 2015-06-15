package com.androidcollider.easyfin;

import com.afollestad.materialdialogs.MaterialDialog;
import com.androidcollider.easyfin.adapters.SpinnerAccountCurrencyAdapter;
import com.androidcollider.easyfin.adapters.SpinnerAccountTypeAdapter;
import com.androidcollider.easyfin.database.DataSource;
import com.androidcollider.easyfin.fragments.FrgMain;
import com.androidcollider.easyfin.objects.Account;
import com.androidcollider.easyfin.utils.FormatUtils;
import com.androidcollider.easyfin.utils.Shake;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;


public class ActAccount extends AppCompatActivity {

    Spinner spinType, spinCurrency;
    EditText etName, etSum;
    private String oldName;
    int idAccount;

    Intent intent;
    DataSource dataSource;

    int mode;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_account);

        dataSource = new DataSource(this);

        initializeFields();

        setMode();
    }

    private void initializeFields() {
        spinType = (Spinner) findViewById(R.id.spinAddAccountType);
        spinCurrency = (Spinner) findViewById(R.id.spinAddAccountCurrency);

        etName = (EditText) findViewById(R.id.editTextAccountName);
        etSum = (EditText) findViewById(R.id.editTextAccountSum);

    }

    private void setMode () {
        intent = getIntent();
        mode = intent.getIntExtra("mode", 0);

        switch (mode) {
            case 0: {setToolbar(R.string.new_account); break;}
            case 1: {setToolbar(R.string.edit_account);
                     setEdits();
                     break;}
        }

        setSpinner();
    }

    private void setSpinner() {
        spinType.setAdapter(new SpinnerAccountTypeAdapter(this, R.layout.spin_custom_item,
                getResources().getStringArray(R.array.account_type_array)));

        spinCurrency.setAdapter(new SpinnerAccountCurrencyAdapter(this, R.layout.spin_custom_item,
                getResources().getStringArray(R.array.account_currency_array)));

        if (mode == 1) {
            String[] type = getResources().getStringArray(R.array.account_type_array);

            String typeVal = getIntent().getStringExtra("type");

            for (int i = 0; i < type.length; i++) {
                if (type[i].equals(typeVal)) {
                    spinType.setSelection(i);
                }
            }

            String[] currency = getResources().getStringArray(R.array.account_currency_array);

            String currencyVal = getIntent().getStringExtra("currency");

            for (int i = 0; i < currency.length; i++) {
                if (currency[i].equals(currencyVal)) {
                    spinCurrency.setSelection(i);
                }
            }
        }
    }

    private void setToolbar(int id) {
        Toolbar ToolBar = (Toolbar) findViewById(R.id.toolbarMain);
        assert getSupportActionBar() != null;
        setSupportActionBar(ToolBar);
        getSupportActionBar().setTitle(id);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ToolBar.inflateMenu(R.menu.toolbar_account_menu);
    }

    private void setEdits() {
        oldName = intent.getStringExtra("name");
        etName.setText(oldName);
        etName.setSelection(etName.getText().length());

        final int PRECISE = 100;
        final String FORMAT = "0.00";

        etSum.setText(FormatUtils.doubleFormatter(getIntent().getDoubleExtra("amount", 0.0), FORMAT, PRECISE));
        etSum.setSelection(etSum.getText().length());

        idAccount = intent.getIntExtra("idAccount", 0);
    }


    private void addAccount() {

        if(checkForFillNameSumFields()) {

            String name = etName.getText().toString();

                if (dataSource.checkAccountNameMatches(name)) {
                    Shake.highlightEditText(etName);
                    Toast.makeText(this, getResources().getString(R.string.account_name_exist), Toast.LENGTH_LONG).show();
                }

                else {

                    double amount = Double.parseDouble(etSum.getText().toString());
                    String type = spinType.getSelectedItem().toString();
                    String currency = spinCurrency.getSelectedItem().toString();

                    Account account = new Account(name, amount, type, currency);

                    dataSource.insertNewAccount(account);

                    pushBroadcast();

                    this.finish();
                }
            }
        }


    private void editAccount() {

        if (checkForFillNameSumFields()) {

                String name = etName.getText().toString();

                if (dataSource.checkAccountNameMatches(name) && ! name.equals(oldName)) {
                    Shake.highlightEditText(etName);
                    Toast.makeText(this, getResources().getString(R.string.account_name_exist), Toast.LENGTH_LONG).show();
                }

                else {

                    double amount = Double.parseDouble(etSum.getText().toString());
                    String type = spinType.getSelectedItem().toString();
                    String currency = spinCurrency.getSelectedItem().toString();

                    Account account = new Account(idAccount, name, amount, type, currency);

                    dataSource.editAccount(account);

                    pushBroadcast();

                    this.finish();
                }
            }
        }


    private boolean checkForFillNameSumFields() {

        String st = etName.getText().toString().replaceAll("\\s+", "");

        if (st.isEmpty()) {
            Shake.highlightEditText(etName);
            Toast.makeText(this, getResources().getString(R.string.account_empty_field_name), Toast.LENGTH_LONG).show();

            return false;
        }

        else {

            if (!etSum.getText().toString().matches(".*\\d.*")) {
                Shake.highlightEditText(etSum);
                Toast.makeText(this, getResources().getString(R.string.account_empty_field_sum), Toast.LENGTH_LONG).show();

                return false;
            }
        }

        return true;
    }

    private void pushBroadcast() {
        Intent intentFragmentMain = new Intent(FrgMain.BROADCAST_FRAGMENT_MAIN_ACTION);
        intentFragmentMain.putExtra(FrgMain.PARAM_STATUS_FRAGMENT_MAIN, FrgMain.STATUS_UPDATE_FRAGMENT_MAIN);
        sendBroadcast(intentFragmentMain);

        if (mode == 0) {
            SharedPreferences sharedPrefs = getSharedPreferences("SP_SnackBar", MODE_PRIVATE);
            if(!sharedPrefs.contains("initialized")) {

                Intent intentMainSnack = new Intent(MainActivity.BROADCAST_MAIN_SNACK_ACTION);
                intentMainSnack.putExtra(MainActivity.PARAM_STATUS_MAIN_SNACK, MainActivity.STATUS_MAIN_SNACK);
                sendBroadcast(intentMainSnack);
            }
        }
    }

    private void deleteAccountDialog() {

        new MaterialDialog.Builder(this)
                .title(getString(R.string.dialog_title_delete_account))
                .content(getString(R.string.dialog_text_delete_account))
                .positiveText(getString(R.string.dialog_button_delete_account))
                .negativeText(getString(R.string.dialog_button_delete_account_cancel))
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        deleteAccount();
                    }
                })
                .show();

    }

    private void deleteAccount() {
        if (dataSource.checkAccountTransactionExist(idAccount)) {
            dataSource.makeAccountInvisible(idAccount);
        }
        else {
            dataSource.deleteAccount(idAccount);}

        pushBroadcast();
        this.finish();
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

                return true;}

            case R.id.account_action_delete: {
                deleteAccountDialog();
                return true;}
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.toolbar_account_menu, menu);
        MenuItem deleteAccountItem = menu.findItem(R.id.account_action_delete);

        switch (mode) {
            case 0: {deleteAccountItem.setVisible(false); break;}
            case 1: {deleteAccountItem.setVisible(true); break;}
        }

        return true;
    }
}