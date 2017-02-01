package com.androidcollider.easyfin.debts.list;

import android.content.Context;
import android.support.v4.content.ContextCompat;

import com.androidcollider.easyfin.R;
import com.androidcollider.easyfin.common.managers.format.date.DateFormatManager;
import com.androidcollider.easyfin.common.managers.format.number.NumberFormatManager;
import com.androidcollider.easyfin.common.managers.resources.ResourcesManager;
import com.androidcollider.easyfin.common.models.Debt;
import com.androidcollider.easyfin.common.repository.Repository;
import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;

import java.util.List;

import rx.Observable;

/**
 * @author Ihor Bilous
 */

class DebtsModel implements DebtsMVP.Model {

    private Repository repository;
    private DateFormatManager dateFormatManager;
    private NumberFormatManager numberFormatManager;
    private Context context;
    private final String[] curArray, curLangArray;


    DebtsModel(Repository repository,
               DateFormatManager dateFormatManager,
               NumberFormatManager numberFormatManager,
               ResourcesManager resourcesManager,
               Context context) {
        this.repository = repository;
        this.dateFormatManager = dateFormatManager;
        this.numberFormatManager = numberFormatManager;
        this.context = context;
        curArray = resourcesManager.getStringArray(ResourcesManager.STRING_ACCOUNT_CURRENCY);
        curLangArray = resourcesManager.getStringArray(ResourcesManager.STRING_ACCOUNT_CURRENCY_LANG);
    }

    @Override
    public Observable<List<DebtViewModel>> getDebtList() {
        return repository.getAllDebts()
                .map(this::transformDebtListToViewModelList);
    }

    @Override
    public Observable<Debt> getDebtById(int id) {
        return repository.getAllDebts()
                .flatMap(Observable::from)
                .filter(debt -> debt.getId() == id);
    }

    @Override
    public Observable<Boolean> deleteDebtById(int id) {
        return getDebtById(id)
                .flatMap(debt ->
                        repository.deleteDebt(
                                debt.getIdAccount(),
                                debt.getId(),
                                debt.getAmountCurrent(),
                                debt.getType()
                        )
                );
    }

    private DebtViewModel transformDebtToViewModel(Debt debt) {
        DebtViewModel.DebtViewModelBuilder builder = DebtViewModel.builder();

        builder.id(debt.getId());
        builder.name(debt.getName());

        String curLang = null;

        for (int i = 0; i < curArray.length; i++) {
            if (debt.getCurrency().equals(curArray[i])) {
                curLang = curLangArray[i];
                break;
            }
        }

        double amountCurrent = debt.getAmountCurrent();
        double amountAll = debt.getAmountAll();

        builder.amount(
                String.format("%1$s %2$s",
                        numberFormatManager.doubleToStringFormatter(
                                amountCurrent,
                                NumberFormatManager.FORMAT_1,
                                NumberFormatManager.PRECISE_1
                        ),
                        curLang
                )
        );
        builder.accountName(debt.getAccountName());
        builder.date(dateFormatManager.longToDateString(debt.getDate(), DateFormatManager.DAY_MONTH_YEAR_DOTS));

        int progress = (int) (amountCurrent / amountAll * 100);
        builder.progress(progress);
        builder.progressPercents(String.format("%s%%", progress));

        builder.colorRes(ContextCompat.getColor(context,
                debt.getType() == 1 ? R.color.custom_red : R.color.custom_green));

        return builder.build();
    }

    private List<DebtViewModel> transformDebtListToViewModelList(List<Debt> debtList) {
        return Stream.of(debtList).map(this::transformDebtToViewModel).collect(Collectors.toList());
    }
}