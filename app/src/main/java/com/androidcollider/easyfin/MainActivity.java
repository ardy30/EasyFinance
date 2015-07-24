package com.androidcollider.easyfin;


import android.content.Context;
import android.os.Bundle;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.androidcollider.easyfin.adapters.NavigationDrawerRecyclerAdapter;
import com.androidcollider.easyfin.fragments.CommonFragment;
import com.androidcollider.easyfin.fragments.CommonFragmentAddEdit;
import com.androidcollider.easyfin.fragments.FrgAddAccount;
import com.androidcollider.easyfin.fragments.FrgDebts;
import com.androidcollider.easyfin.fragments.FrgFAQ;
import com.androidcollider.easyfin.fragments.FrgMain;
import com.androidcollider.easyfin.fragments.FrgPref;
import com.androidcollider.easyfin.fragments.PreferenceFragment;
import com.androidcollider.easyfin.objects.InfoFromDB;
import com.androidcollider.easyfin.utils.ToastUtils;


public class MainActivity extends AppCompatActivity implements FragmentManager.OnBackStackChangedListener {

    private DrawerLayout drawerLayout;
    private RecyclerView recyclerNavDrawer;

    private MaterialDialog appAboutDialog;

    private static long backPressExitTime;

    private Toolbar toolbar;

    private final int TOOLBAR_DEFAULT = 1;

    ActionBarDrawerToggle mDrawerToggle;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nav_drawer_root_layout);

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.addOnBackStackChangedListener(this);

        InfoFromDB.getInstance().updateRatesForExchange();

        initializeViews();

        setToolbar(getString(R.string.app_name), TOOLBAR_DEFAULT);

        buildAppAboutDialog();

        addFragment(new FrgMain());
    }

    private void initializeViews() {
        toolbar = (Toolbar) findViewById(R.id.toolbarMain);
        setSupportActionBar(toolbar);

        drawerLayout = (DrawerLayout) findViewById(R.id.navDrawerLayout);

        recyclerNavDrawer = (RecyclerView) findViewById(R.id.recyclerViewNavDrawer);
        recyclerNavDrawer.setHasFixedSize(true);

        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerNavDrawer.setLayoutManager(layoutManager);


        recyclerNavDrawer.setAdapter(new NavigationDrawerRecyclerAdapter(this));

        mDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.app_name, R.string.app_name) {

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }

        };

        drawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

        final GestureDetector gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return true;
            }

        });

        recyclerNavDrawer.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {

                View child = rv.findChildViewUnder(e.getX(),e.getY());

                if(child != null && gestureDetector.onTouchEvent(e)) {

                    int position = recyclerNavDrawer.getChildAdapterPosition(child);

                    if (position != 0 && position != 5) {
                        drawerLayout.closeDrawers();

                        switch (position) {

                            case 1: {
                                openSelectedFrgMainPage(0);
                                break;
                            }
                            case 2: {
                                openSelectedFrgMainPage(1);
                                break;
                            }
                            case 3: {
                                openSelectedFrgMainPage(2);
                                break;
                            }
                            case 4: {
                                addFragment(new FrgDebts());
                                break;
                            }
                            case 6: {
                                addFragment(new FrgPref());
                                break;
                            }
                            case 7: {
                                addFragment(new FrgFAQ());
                                break;
                            }
                            case 8: {
                                appAboutDialog.show();
                                break;
                            }
                        }
                    }
                }

                return false;
            }

            @Override
            public void onTouchEvent(RecyclerView rv, MotionEvent e) {}

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {}
        });
    }

    private void setToolbar (String title, int mode) {
        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {

            switch (mode) {

                case TOOLBAR_DEFAULT: {
                    actionBar.setDisplayShowCustomEnabled(false);
                    actionBar.setDisplayShowTitleEnabled(true);
                    actionBar.setTitle(title);
                    actionBar.setDisplayHomeAsUpEnabled(true);
                    toolbar.setNavigationIcon(R.drawable.ic_menu);
                    break;
                }
            }
        }
    }

    private void openSelectedFrgMainPage(int page) {
        popFragments();

        Fragment f = getTopFragment();

        if (f instanceof FrgMain) {

            FrgMain frgMain = (FrgMain) f;
            frgMain.openSelectedPage(page);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void buildAppAboutDialog() {

        appAboutDialog = new MaterialDialog.Builder(this)
                .title(R.string.app_about)
                .customView(R.layout.app_about, true)
                .positiveText(R.string.ok)
                .build();

        View appAboutLayout = appAboutDialog.getCustomView();
        if (appAboutLayout != null) {
            TextView tvVersion = (TextView) appAboutLayout.findViewById(R.id.tvAboutAppVersion);
            tvVersion.setText(getString(R.string.about_app_version) + " " + BuildConfig.VERSION_NAME);
        }
    }


    public void addFragment(Fragment f){
        treatFragment(f, true, false);
    }

    public void replaceFragment(Fragment f){
        treatFragment(f, false, true);
    }

    public Fragment getTopFragment(){
        return getSupportFragmentManager().findFragmentById(R.id.fragment_container);
    }
    
    private void treatFragment(Fragment f, boolean addToBackStack, boolean replace){
        String tag = f.getClass().getName();
        FragmentTransaction ft =  getSupportFragmentManager().beginTransaction();

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

    @Override
    public void onBackStackChanged() {

            Fragment topFragment = getTopFragment();

            if (topFragment instanceof CommonFragmentAddEdit) {
                //set toolbar from fragment class
            }

            else if (topFragment instanceof CommonFragment) {
                setToolbar((((CommonFragment) topFragment).getTitle()), TOOLBAR_DEFAULT);
            }
            else if (topFragment instanceof PreferenceFragment) {
                setToolbar((((PreferenceFragment) topFragment).getTitle()), TOOLBAR_DEFAULT);
            }

        hideKeyboard();
    }

    public void hideKeyboard() {
        InputMethodManager inputManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        View view = this.getCurrentFocus();
        if (view != null) {
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    private void popFragments() {
        getSupportFragmentManager().popBackStackImmediate(1, FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }

    private void popFragment() {
        getSupportFragmentManager().popBackStack();
    }

    @Override
    public void onBackPressed() {

        Fragment fragment = getTopFragment();

        if (fragment instanceof FrgMain) {

            if (backPressExitTime + 2000 > System.currentTimeMillis()) {

                this.finish();

            } else {

                ToastUtils.showClosableToast(this, getString(R.string.press_again_to_exit), 1);
                backPressExitTime = System.currentTimeMillis();
            }
        }

        else if (fragment instanceof FrgAddAccount) {

            popFragments();
        }

        else if (fragment instanceof CommonFragmentAddEdit) {

            popFragment();
        }

        else {

            popFragments();
        }
    }

}