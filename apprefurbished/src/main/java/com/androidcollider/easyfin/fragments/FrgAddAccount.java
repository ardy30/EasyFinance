package com.androidcollider.easyfin.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.androidcollider.easyfin.R;
import com.androidcollider.easyfin.adapters.SpinIconTextHeadAdapter;
import com.androidcollider.easyfin.common.app.App;
import com.androidcollider.easyfin.common.events.UpdateFrgAccounts;
import com.androidcollider.easyfin.common.events.UpdateFrgHomeBalance;
import com.androidcollider.easyfin.managers.accounts_info.AccountsInfoManager;
import com.androidcollider.easyfin.managers.format.number.NumberFormatManager;
import com.androidcollider.easyfin.managers.resources.ResourcesManager;
import com.androidcollider.easyfin.managers.ui.hide_touch_outside.HideTouchOutsideManager;
import com.androidcollider.easyfin.managers.ui.shake_edit_text.ShakeEditTextManager;
import com.androidcollider.easyfin.managers.ui.toast.ToastManager;
import com.androidcollider.easyfin.models.Account;
import com.androidcollider.easyfin.repository.Repository;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import rx.Subscriber;

/**
 * @author Ihor Bilous
 */

public class FrgAddAccount extends CommonFragmentAddEdit implements FrgNumericDialog.OnCommitAmountListener {

    @BindView(R.id.spinAddAccountType)
    Spinner spinType;
    @BindView(R.id.spinAddAccountCurrency)
    Spinner spinCurrency;
    @BindView(R.id.editTextAccountName)
    EditText etName;
    @BindView(R.id.tvAddAccountAmount)
    TextView tvAmount;
    @BindView(R.id.layoutActAccountParent)
    RelativeLayout mainContent;

    private String oldName;
    private int idAccount, mode;
    private Account accFrIntent;

    @Inject
    Repository repository;

    @Inject
    AccountsInfoManager accountsInfoManager;

    @Inject
    ShakeEditTextManager shakeEditTextManager;

    @Inject
    ToastManager toastManager;

    @Inject
    HideTouchOutsideManager hideTouchOutsideManager;

    @Inject
    NumberFormatManager numberFormatManager;

    @Inject
    ResourcesManager resourcesManager;


    @Override
    public int getContentView() {
        return R.layout.frg_add_account;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((App) getActivity().getApplication()).getComponent().inject(this);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setMode();
        setToolbar();
        hideTouchOutsideManager.hideKeyboardByTouchOutsideEditText(mainContent, getActivity());
    }

    private void setMode() {
        mode = getArguments().getInt("mode", 0);
        switch (mode) {
            case 0:
                tvAmount.setText("0,00");
                openNumericDialog(tvAmount.getText().toString());
                break;
            case 1:
                accFrIntent = (Account) getArguments().getSerializable("account");
                setFields();
                break;
        }
        setSpinner();
    }

    private void setSpinner() {
        spinType.setAdapter(new SpinIconTextHeadAdapter(
                getActivity(),
                R.layout.spin_head_icon_text,
                R.id.tvSpinHeadIconText,
                R.id.ivSpinHeadIconText,
                R.layout.spin_drop_icon_text,
                R.id.tvSpinDropIconText,
                R.id.ivSpinDropIconText,
                resourcesManager.getStringArray(ResourcesManager.STRING_ACCOUNT_TYPE),
                resourcesManager.getIconArray(ResourcesManager.ICON_ACCOUNT_TYPE)
        ));

        spinCurrency.setAdapter(new SpinIconTextHeadAdapter(
                getActivity(),
                R.layout.spin_head_icon_text,
                R.id.tvSpinHeadIconText,
                R.id.ivSpinHeadIconText,
                R.layout.spin_drop_icon_text,
                R.id.tvSpinDropIconText,
                R.id.ivSpinDropIconText,
                resourcesManager.getStringArray(ResourcesManager.STRING_ACCOUNT_CURRENCY),
                resourcesManager.getIconArray(ResourcesManager.ICON_FLAGS)
        ));

        if (mode == 1) {
            spinType.setSelection(accFrIntent.getType());
            String[] currencyArray = resourcesManager.getStringArray(ResourcesManager.STRING_ACCOUNT_CURRENCY);

            for (int i = 0; i < currencyArray.length; i++) {
                if (currencyArray[i].equals(accFrIntent.getCurrency())) {
                    spinCurrency.setSelection(i);
                }
            }

            spinCurrency.setEnabled(false);
        }
    }

    private void setFields() {
        oldName = accFrIntent.getName();
        etName.setText(oldName);
        etName.setSelection(etName.getText().length());

        String amount = numberFormatManager.doubleToStringFormatterForEdit(
                accFrIntent.getAmount(),
                NumberFormatManager.FORMAT_1,
                NumberFormatManager.PRECISE_1
        );
        setTVTextSize(tvAmount, amount, 10, 15);
        tvAmount.setText(amount);

        idAccount = accFrIntent.getId();
    }

    private void addAccount() {
        if (checkForFillNameField()) {
            String name = etName.getText().toString();
            if (accountsInfoManager.checkForAccountNameMatches(name)) {
                shakeEditTextManager.highlightEditText(etName);
                toastManager.showClosableToast(getActivity(), getString(R.string.account_name_exist), ToastManager.SHORT);
            } else {
                Account account = Account.builder()
                        .name(name)
                        .amount(Double.parseDouble(numberFormatManager.prepareStringToParse(tvAmount.getText().toString())))
                        .type(spinType.getSelectedItemPosition())
                        .currency(spinCurrency.getSelectedItem().toString())
                        .build();

                repository.addNewAccount(account)
                        .subscribe(new Subscriber<Account>() {

                            @Override
                            public void onCompleted() {

                            }

                            @Override
                            public void onError(Throwable e) {

                            }

                            @Override
                            public void onNext(Account account1) {
                                lastActions();
                            }
                        });
            }
        }
    }

    private void editAccount() {
        if (checkForFillNameField()) {
            String name = etName.getText().toString();
            if (accountsInfoManager.checkForAccountNameMatches(name) && !name.equals(oldName)) {
                shakeEditTextManager.highlightEditText(etName);
                toastManager.showClosableToast(getActivity(), getString(R.string.account_name_exist), ToastManager.SHORT);
            } else {
                Account account = Account.builder()
                        .id(idAccount)
                        .name(name)
                        .amount(Double.parseDouble(numberFormatManager.prepareStringToParse(tvAmount.getText().toString())))
                        .type(spinType.getSelectedItemPosition())
                        .currency(spinCurrency.getSelectedItem().toString())
                        .build();

                repository.updateAccount(account)
                        .subscribe(new Subscriber<Account>() {

                            @Override
                            public void onCompleted() {

                            }

                            @Override
                            public void onError(Throwable e) {

                            }

                            @Override
                            public void onNext(Account account1) {
                                lastActions();
                            }
                        });
            }
        }
    }

    private void lastActions() {
        pushBroadcast();
        popAll();
    }

    private boolean checkForFillNameField() {
        if (etName.getText().toString().replaceAll("\\s+", "").isEmpty()) {
            shakeEditTextManager.highlightEditText(etName);
            toastManager.showClosableToast(getActivity(), getString(R.string.empty_name_field), ToastManager.SHORT);
            return false;
        }
        return true;
    }

    private void pushBroadcast() {
        EventBus.getDefault().post(new UpdateFrgHomeBalance());
        EventBus.getDefault().post(new UpdateFrgAccounts());
    }

    @OnClick({R.id.tvAddAccountAmount})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tvAddAccountAmount:
                openNumericDialog(tvAmount.getText().toString());
                break;
        }
    }

    @Override
    public void onCommitAmountSubmit(String amount) {
        setTVTextSize(tvAmount, amount, 10, 15);
        tvAmount.setText(amount);
    }

    @Override
    void handleSaveAction() {
        switch (mode) {
            case 0:
                addAccount();
                break;
            case 1:
                editAccount();
                break;
        }
    }
}