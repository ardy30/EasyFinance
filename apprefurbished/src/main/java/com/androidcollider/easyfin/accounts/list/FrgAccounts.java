package com.androidcollider.easyfin.accounts.list;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.androidcollider.easyfin.R;
import com.androidcollider.easyfin.common.ui.MainActivity;
import com.androidcollider.easyfin.common.app.App;
import com.androidcollider.easyfin.common.events.UpdateFrgAccounts;
import com.androidcollider.easyfin.common.events.UpdateFrgHomeBalance;
import com.androidcollider.easyfin.accounts.add_edit.FrgAddAccount;
import com.androidcollider.easyfin.common.ui.fragments.FrgMain;
import com.androidcollider.easyfin.common.ui.fragments.common.CommonFragmentWithEvents;
import com.androidcollider.easyfin.common.managers.format.number.NumberFormatManager;
import com.androidcollider.easyfin.common.managers.resources.ResourcesManager;
import com.androidcollider.easyfin.common.models.Account;
import com.androidcollider.easyfin.common.repository.Repository;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import rx.Subscriber;

/**
 * @author Ihor Bilous
 */

public class FrgAccounts extends CommonFragmentWithEvents {

    @BindView(R.id.recyclerAccount)
    RecyclerView recyclerView;
    @BindView(R.id.tvEmptyAccounts)
    TextView tvEmpty;
    private RecyclerAccountAdapter recyclerAdapter;
    private List<Account> accountList;

    @Inject
    Repository repository;

    @Inject
    NumberFormatManager numberFormatManager;

    @Inject
    ResourcesManager resourcesManager;


    @Override
    public int getContentView() {
        return R.layout.frg_accounts;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((App) getActivity().getApplication()).getComponent().inject(this);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        accountList = new ArrayList<>();
        setupRecyclerView();
        loadData();
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
        recyclerAdapter = new RecyclerAccountAdapter(numberFormatManager, resourcesManager);
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

    private void loadData() {
        repository.getAllAccounts()
                .subscribe(new Subscriber<List<Account>>() {

                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(List<Account> accountList) {
                        FrgAccounts.this.accountList.clear();
                        FrgAccounts.this.accountList.addAll(accountList);
                        recyclerAdapter.addItems(FrgAccounts.this.accountList);
                        setVisibility();
                    }
                });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(UpdateFrgAccounts event) {
        loadData();
    }

    private void setVisibility() {
        recyclerView.setVisibility(accountList.isEmpty() ? View.GONE : View.VISIBLE);
        tvEmpty.setVisibility(accountList.isEmpty() ? View.VISIBLE : View.GONE);
    }

    public boolean onContextItemSelected(MenuItem item) {
        int pos;
        try {
            pos = (int) recyclerAdapter.getPosition();
        } catch (Exception e) {
            return super.onContextItemSelected(item);
        }

        switch (item.getItemId()) {
            case R.id.ctx_menu_edit_account:
                goToEditAccount(pos);
                break;
            case R.id.ctx_menu_delete_account:
                showDialogDeleteAccount(pos);
                break;
        }
        return super.onContextItemSelected(item);
    }

    private void showDialogDeleteAccount(final int pos) {
        new MaterialDialog.Builder(getActivity())
                .title(getString(R.string.dialog_title_delete))
                .content(getString(R.string.dialog_text_delete_account))
                .positiveText(getString(R.string.delete))
                .negativeText(getString(R.string.cancel))
                .onPositive((dialog, which) -> deleteAccount(pos))
                .cancelable(false)
                .show();
    }

    private void goToEditAccount(int pos) {
        FrgAddAccount frgAddAccount = new FrgAddAccount();
        Bundle arguments = new Bundle();
        arguments.putInt("mode", 1);
        arguments.putSerializable("account", accountList.get(pos));
        frgAddAccount.setArguments(arguments);
        ((MainActivity) getActivity()).addFragment(frgAddAccount);
    }

    private void deleteAccount(int pos) {
        repository.deleteAccount(accountList.get(pos).getId())
                .subscribe(new Subscriber<Boolean>() {

                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Boolean aBoolean) {
                        accountList.remove(pos);
                        recyclerAdapter.deleteItem(pos);
                        setVisibility();
                        pushBroadcast();
                    }
                });
    }

    private void pushBroadcast() {
        EventBus.getDefault().post(new UpdateFrgHomeBalance());
    }
}