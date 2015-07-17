package com.androidcollider.easyfin.fragments;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.androidcollider.easyfin.AppController;
import com.androidcollider.easyfin.R;
import com.androidcollider.easyfin.database.DbHelper;
import com.androidcollider.easyfin.utils.DBExportImportUtils;
import com.androidcollider.easyfin.utils.ToastUtils;

import java.io.IOException;


public class FrgPref extends PreferenceFragment {

    private static final int FILE_SELECT_CODE = 0;

    private final String TAG = "COLLIDER";

    private static String filePath;

    private static final Context context = AppController.getContext();


    private MaterialDialog importDialog;
    private TextView tvBrowseDB;
    private Button btnImportDB;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        buildImportDialog();
        initializeImportDialogViews();


        Preference exportDB = findPreference("export_db");
        exportDB.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                DBExportImportUtils.backupDB();

                return false;
            }
        });


        Preference importDB = findPreference("import_db");
        importDB.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                importDialog.show();

                return false;
            }
        });
    }


    private void buildImportDialog() {

        importDialog = new MaterialDialog.Builder(getActivity())
                .title(R.string.import_db)
                .customView(R.layout.item_import_db, true)
                .positiveText(R.string.close)
                .build();
    }

    private void initializeImportDialogViews() {

        View view = importDialog.getCustomView();

        if (view != null) {

            tvBrowseDB = (TextView) view.findViewById(R.id.tvItemImportDb);
            btnImportDB = (Button) view.findViewById(R.id.btnItemImportDB);

            tvBrowseDB.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    openFileExplorer();

                }
            });

            btnImportDB.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    importDB();
                }
            });
        }

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
            // Potentially direct the user to the Market with a Dialog

        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case FILE_SELECT_CODE:
                if (resultCode == getActivity().RESULT_OK) {
                    // Get the Uri of the selected file
                    Uri uri = data.getData();
                    Log.d(TAG, "File Uri: " + uri.toString());
                    // Get the path

                    try {String path = uri.getPath();
                        Log.d(TAG, "File Path: " + uri.getPath());
                        tvBrowseDB.setText(path);
                        filePath = path;
                    }

                    catch (Exception e) {
                        e.printStackTrace();
                    }
                    // Get the file instance
                    // File file = new File(path);
                    // Initiate the upload
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    private void importDB() {

        DbHelper dbHelper = new DbHelper(context);

        boolean importDB = false;

        try {
            importDB = dbHelper.importDatabase(filePath);
        }

        catch (IOException e) {
            e.printStackTrace();
        }

        if (importDB) {
            ToastUtils.showClosableToast(context, "DONE!", 2);
        }

        else {
            ToastUtils.showClosableToast(context, "ERROR!", 2);
        }
    }





    @Override
    public String getTitle() {
        return getString(R.string.settings);
    }

}
