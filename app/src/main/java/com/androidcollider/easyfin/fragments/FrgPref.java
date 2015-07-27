package com.androidcollider.easyfin.fragments;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;

import com.afollestad.materialdialogs.MaterialDialog;
import com.androidcollider.easyfin.AppController;
import com.androidcollider.easyfin.R;
import com.androidcollider.easyfin.database.DbHelper;
import com.androidcollider.easyfin.objects.InfoFromDB;
import com.androidcollider.easyfin.utils.DBExportImportUtils;
import com.androidcollider.easyfin.utils.ToastUtils;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.io.IOException;


public class FrgPref extends PreferenceFragment {

    private static final int FILE_SELECT_CODE = 0;

    private static Uri uri;

    private static final Context context = AppController.getContext();

    private Preference exportDBPref, importDBPref;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        initializePrefs();


        Tracker mTracker = AppController.tracker;
        mTracker.setScreenName(this.getClass().getName());
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    private void initializePrefs() {

        exportDBPref = findPreference("export_db");
        exportDBPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                exportDBPref.setEnabled(false);
                DBExportImportUtils.backupDB();
                return false;
            }
        });


        importDBPref = findPreference("import_db");
        importDBPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                openFileExplorer();
                return false;
            }
        });
    }

    private void showDialogImportDB() {

        new MaterialDialog.Builder(getActivity())
                .title(getString(R.string.import_db))
                .content(getString(R.string.import_dialog_warning))
                .positiveText(getString(R.string.confirm))
                .negativeText(getString(R.string.cancel))
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {

                        importDB();
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {}
                })
                .cancelable(false)
                .show();
    }

    private void openFileExplorer() {

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {

            startActivityForResult(
                    Intent.createChooser(intent, "Select a File to Upload"),
                    FILE_SELECT_CODE);
        } catch (android.content.ActivityNotFoundException ex) {

            ToastUtils.showClosableToast(context, getString(R.string.import_no_file_explorer), 2);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {

            case FILE_SELECT_CODE:

                if (resultCode == -1) {

                    // Get the Uri of the selected file
                    uri = data.getData();

                    if (!uri.toString().contains(DbHelper.DATABASE_NAME)) {
                        ToastUtils.showClosableToast(context, getString(R.string.import_wrong_file_type), 2);
                    }

                    else {

                        try {

                            showDialogImportDB();

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void importDB() {

        boolean importDB = false;

        try {
            importDB = InfoFromDB.getInstance().getDataSource().importDatabase(uri);
        }

        catch (IOException e) {
            e.printStackTrace();
        }

        if (importDB) {
            importDBPref.setEnabled(false);
            ToastUtils.showClosableToast(context, getString(R.string.import_complete), 2);
            pushBroadcast();
        }

        else {
            ToastUtils.showClosableToast(context, getString(R.string.import_error), 2);
        }
    }

    private void pushBroadcast() {

        Intent intentFragmentMain = new Intent(FrgHome.BROADCAST_FRG_MAIN_ACTION);
        intentFragmentMain.putExtra(FrgHome.PARAM_STATUS_FRG_MAIN, FrgHome.STATUS_UPDATE_FRG_MAIN);
        getActivity().sendBroadcast(intentFragmentMain);

        Intent intentFragmentTransaction = new Intent(FrgTransactions.BROADCAST_FRG_TRANSACTION_ACTION);
        intentFragmentTransaction.putExtra(FrgTransactions.PARAM_STATUS_FRG_TRANSACTION, FrgTransactions.STATUS_UPDATE_FRG_TRANSACTION);
        getActivity().sendBroadcast(intentFragmentTransaction);

        Intent intentFrgAccounts = new Intent(FrgAccounts.BROADCAST_FRG_ACCOUNT_ACTION);
        intentFrgAccounts.putExtra(FrgAccounts.PARAM_STATUS_FRG_ACCOUNT, FrgAccounts.STATUS_UPDATE_FRG_ACCOUNT);
        getActivity().sendBroadcast(intentFrgAccounts);

        Intent intentDebt = new Intent(FrgDebts.BROADCAST_DEBT_ACTION);
        intentDebt.putExtra(FrgDebts.PARAM_STATUS_DEBT, FrgDebts.STATUS_UPDATE_DEBT);
        getActivity().sendBroadcast(intentDebt);

        InfoFromDB.getInstance().setRatesForExchange();

        InfoFromDB.getInstance().updateAccountList();
    }

    @Override
    public String getTitle() {
        return getString(R.string.settings);
    }

}