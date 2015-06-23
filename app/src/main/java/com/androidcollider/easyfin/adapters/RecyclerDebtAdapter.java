package com.androidcollider.easyfin.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.androidcollider.easyfin.R;
import com.androidcollider.easyfin.objects.Debt;
import com.androidcollider.easyfin.utils.DateFormat;
import com.androidcollider.easyfin.utils.FormatUtils;

import java.util.ArrayList;


public class RecyclerDebtAdapter extends RecyclerView.Adapter<RecyclerDebtAdapter.ViewHolder> {

    private Context context;
    private ArrayList<Debt> debtList;

    private final String[] currency;
    private final String[] currencyLanguage;


    public RecyclerDebtAdapter(Context context, ArrayList<Debt> debtList) {
        this.context = context;
        this.debtList = debtList;

        currency = context.getResources().getStringArray(R.array.account_currency_array);
        currencyLanguage = context.getResources().getStringArray(R.array.account_currency_array_language);
    }



    @Override
    public int getItemCount() {return debtList.size();}

    @Override
    public long getItemId(int position) {return position;}

    public Debt getDebt(int position) {return debtList.get(position);}



    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_debt, parent, false);
        return new ViewHolder(view);
    }



    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        Debt debt = getDebt(position);

        final int PRECISE = 100;
        final String FORMAT = "0.00";
        final String DATEFORMAT = "dd.MM.yyyy";

        holder.tvDebtName.setText(debt.getName());

        String cur = debt.getCurrency();
        String curLang = null;

        for (int i = 0; i < currency.length; i++) {
            if (cur.equals(currency[i])) {
                curLang = currencyLanguage[i];
            }
        }

        holder.tvAmount.setText(FormatUtils.doubleFormatter(debt.getAmount(), FORMAT, PRECISE)
        + " " + curLang);
        holder.tvAccountName.setText(debt.getAccountName());
        holder.tvDate.setText(DateFormat.longToDateString(debt.getDate(), DATEFORMAT));

        switch (debt.getType()) {
            case 0: {
                holder.tvAmount.setTextColor(context.getResources().getColor(R.color.custom_green));
                break;
            }
            case 1: {
                holder.tvAmount.setTextColor(context.getResources().getColor(R.color.custom_red));
                break;
            }
        }
    }



    static class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView tvDebtName;
        public final TextView tvAmount;
        public final TextView tvAccountName;
        public final TextView tvDate;


        public ViewHolder(View view) {
            super(view);
            mView = view;
            tvDebtName = (TextView) view.findViewById(R.id.tvItemDebtName);
            tvAmount = (TextView) view.findViewById(R.id.tvItemDebtAmount);
            tvAccountName = (TextView) view.findViewById(R.id.tvItemDebtAccountName);
            tvDate = (TextView) view.findViewById(R.id.tvItemDebtDate);
        }
    }
}
