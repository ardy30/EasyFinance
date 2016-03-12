package com.androidcollider.easyfin.fragments;


import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.afollestad.materialdialogs.MaterialDialog;
import com.androidcollider.easyfin.R;
import com.androidcollider.easyfin.adapters.ViewPagerFragmentAdapter;
import com.androidcollider.easyfin.objects.InfoFromDB;
import com.androidcollider.easyfin.utils.SharedPref;
import com.androidcollider.easyfin.utils.TabletTesterUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class FrgMain extends CommonFragment {
    public final static String BROADCAST_MAIN_SNACK_ACTION = "com.androidcollider.easyfin.mainsnack.broadcast";
    public final static String PARAM_STATUS_MAIN_SNACK = "show_main_snack";
    public final static int STATUS_MAIN_SNACK = 5;

    private BroadcastReceiver broadcastReceiver;
    private SharedPref sharedPref;

    private View view;
    private ViewPager pager;
    private FloatingActionButton faButtonMain, faButtonExpense, faButtonIncome, faButtonBTW;

    private boolean isSnackBarDisabled, expanded = false;
    public static float offset1, offset2, offset3;
    final private boolean isApiHoneycombAndHigher = android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.frg_main, container, false);

        setViewPager();

        final ViewGroup fabContainer = (ViewGroup) view.findViewById(R.id.coordinatorLayoutFloatMain);

        faButtonMain = (FloatingActionButton) view.findViewById(R.id.btnFloatMain);
        faButtonMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPageNum();
            }
        });

        faButtonExpense = (FloatingActionButton) view.findViewById(R.id.btnFloatAddTransExpense);
        faButtonIncome = (FloatingActionButton) view.findViewById(R.id.btnFloatAddTransIncome);
        faButtonBTW = (FloatingActionButton) view.findViewById(R.id.btnFloatAddTransBTW);

        faButtonExpense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToAddTransaction(0);
                setFloatButtonsVisibility();
            }
        });

        faButtonIncome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToAddTransaction(1);
                setFloatButtonsVisibility();
            }
        });

        faButtonBTW.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToAddTransBTW();
                setFloatButtonsVisibility();
            }
        });

        if (isApiHoneycombAndHigher) {
            fabContainer.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    fabContainer.getViewTreeObserver().removeOnPreDrawListener(this);
                    offset1 = faButtonMain.getY() + faButtonMain.getHeight() / 6 - faButtonExpense.getY();
                    faButtonExpense.setTranslationY(offset1);
                    offset2 = faButtonMain.getY() + faButtonMain.getHeight() / 6 - faButtonIncome.getY();
                    faButtonIncome.setTranslationY(offset2);
                    offset3 = faButtonMain.getY() + faButtonMain.getHeight() / 6 - faButtonBTW.getY();
                    faButtonBTW.setTranslationY(offset3);
                    return true;
                }
            });
        } else {
            faButtonExpense.setVisibility(View.GONE);
            faButtonIncome.setVisibility(View.GONE);
            faButtonBTW.setVisibility(View.GONE);
        }

        if (InfoFromDB.getInstance().getAccountsNumber() == 0)
            showDialogNoAccount();

        sharedPref = new SharedPref(getActivity());
        isSnackBarDisabled = sharedPref.isSnackBarAccountDisable();

        if (!isSnackBarDisabled && !TabletTesterUtils.isTablet(getActivity()))
            makeBroadcastReceiver();

        checkForAndroidMPermissions();

        return view;
    }

    private void setViewPager() {
        pager = (ViewPager) view.findViewById(R.id.pagerMain);
        ViewPagerFragmentAdapter adapterPager = new ViewPagerFragmentAdapter(getChildFragmentManager());
        adapterPager.addFragment(new FrgHome(), getResources().getString(R.string.tab_home).toUpperCase());
        adapterPager.addFragment(new FrgTransactions(), getResources().getString(R.string.tab_transactions).toUpperCase());
        adapterPager.addFragment(new FrgAccounts(), getResources().getString(R.string.tab_accounts).toUpperCase());

        pager.setAdapter(adapterPager);
        pager.setOffscreenPageLimit(3);

        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                if (isApiHoneycombAndHigher) {
                    if (position == 2 && expanded) {
                        collapseFab();
                        faButtonMain.setImageResource(R.drawable.ic_plus_white_48dp);
                        expanded = !expanded;
                    }
                } else {
                    if (position == 2 && faButtonExpense.getVisibility() == View.VISIBLE) {
                        faButtonExpense.setVisibility(View.GONE);
                        faButtonIncome.setVisibility(View.GONE);
                        faButtonBTW.setVisibility(View.GONE);
                        faButtonMain.setImageResource(R.drawable.ic_plus_white_48dp);
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        ((TabLayout) view.findViewById(R.id.tabsMain)).setupWithViewPager(pager);
    }

    private void makeBroadcastReceiver() {
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getIntExtra(PARAM_STATUS_MAIN_SNACK, 0) == STATUS_MAIN_SNACK)
                    showSnackBar();
            }
        };
        IntentFilter intentFilter = new IntentFilter(BROADCAST_MAIN_SNACK_ACTION);
        getActivity().registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (!isSnackBarDisabled)
            getActivity().unregisterReceiver(broadcastReceiver);
    }

    private void showSnackBar() {
        final CoordinatorLayout coordinatorLayout = (CoordinatorLayout) view.findViewById(R.id.coordinatorLayoutFloatMain);

        Runnable task = new Runnable() {
            public void run() {
                Snackbar.make(coordinatorLayout, R.string.snack_account_list, Snackbar.LENGTH_LONG)
                        .setAction(R.string.got_it, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                sharedPref.disableSnackBarAccount();
                            }
                        })
                        .show();
            }
        };
        worker.schedule(task, 2, TimeUnit.SECONDS);
    }

    private static final ScheduledExecutorService worker =
            Executors.newSingleThreadScheduledExecutor();

    public void openSelectedPage(int page) {
        pager.setCurrentItem(page);
    }

    public void checkPageNum() {
        switch (pager.getCurrentItem()) {
            case 0:
            case 1: {
                setFloatButtonsVisibility();
                break;
            }
            case 2: {
                goToAddAccount();
                break;
            }
        }
    }

    private void setFloatButtonsVisibility() {
        if (isApiHoneycombAndHigher) {
            expanded = !expanded;
            if (expanded) {
                expandFab();
                faButtonMain.setImageResource(R.drawable.ic_close_white_24dp);
            } else {
                collapseFab();
                faButtonMain.setImageResource(R.drawable.ic_plus_white_48dp);
            }
        } else {
            if (faButtonExpense.getVisibility() == View.GONE) {
                faButtonExpense.setVisibility(View.VISIBLE);
                faButtonIncome.setVisibility(View.VISIBLE);
                faButtonBTW.setVisibility(View.VISIBLE);
                faButtonMain.setImageResource(R.drawable.ic_close_white_24dp);
            } else {
                faButtonExpense.setVisibility(View.GONE);
                faButtonIncome.setVisibility(View.GONE);
                faButtonBTW.setVisibility(View.GONE);
                faButtonMain.setImageResource(R.drawable.ic_plus_white_48dp);
            }
        }
    }

    private void goToAddTransaction(int type) {
        FrgAddTransactionDefault frgAddTransDef = new FrgAddTransactionDefault();
        Bundle arguments = new Bundle();
        arguments.putInt("mode", 0);
        arguments.putInt("type", type);
        frgAddTransDef.setArguments(arguments);
        addFragment(frgAddTransDef);
    }

    private void goToAddTransBTW() {
        addFragment(new FrgAddTransactionBetweenAccounts());
    }

    private void goToAddAccount() {
        FrgAddAccount frgAddAccount = new FrgAddAccount();
        Bundle arguments = new Bundle();
        arguments.putInt("mode", 0);
        frgAddAccount.setArguments(arguments);
        addFragment(frgAddAccount);
    }

    private void showDialogNoAccount() {
        new MaterialDialog.Builder(getActivity())
                .title(getString(R.string.no_account))
                .content(getString(R.string.dialog_text_main_no_accounts))
                .positiveText(getString(R.string.new_account))
                .negativeText(getString(R.string.later))
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        goToAddAccount();
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                    }
                })
                .cancelable(false)
                .show();
    }

    private void collapseFab() {
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(createCollapseAnimator(faButtonExpense, offset1),
                createCollapseAnimator(faButtonIncome, offset2),
                createCollapseAnimator(faButtonBTW, offset3));
        animatorSet.start();
    }

    private void expandFab() {
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(createExpandAnimator(faButtonExpense, offset1),
                createExpandAnimator(faButtonIncome, offset2),
                createExpandAnimator(faButtonBTW, offset3));
        animatorSet.start();
    }

    private static final String TRANSLATION_Y = "translationY";

    private Animator createCollapseAnimator(View view, float offset) {
        return ObjectAnimator.ofFloat(view, TRANSLATION_Y, 0, offset)
                .setDuration(getResources().getInteger(android.R.integer.config_mediumAnimTime));
    }

    private Animator createExpandAnimator(View view, float offset) {
        return ObjectAnimator.ofFloat(view, TRANSLATION_Y, offset, 0)
                .setDuration(getResources().getInteger(android.R.integer.config_mediumAnimTime));
    }

    private void checkForAndroidMPermissions() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            int hasStoragePermission = getActivity().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            List<String> permissions = new ArrayList<>();
            if (hasStoragePermission != PackageManager.PERMISSION_GRANTED)
                permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (!permissions.isEmpty())
                requestPermissions(permissions.toArray(new String[permissions.size()]),
                        REQUEST_CODE_SOME_FEATURES_PERMISSIONS);
        }
    }

    private static final int REQUEST_CODE_SOME_FEATURES_PERMISSIONS = 123;

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_SOME_FEATURES_PERMISSIONS: {
                for (int i = 0; i < permissions.length; i++) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED)
                        Log.d("Permissions", "Permission Granted: " + permissions[i]);
                    else if (grantResults[i] == PackageManager.PERMISSION_DENIED)
                        Log.d("Permissions", "Permission Denied: " + permissions[i]);
                }
            }
            break;
            default: {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }
        }
    }

    @Override
    public String getTitle() {
        return getString(R.string.app_name);
    }

}
