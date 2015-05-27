package com.androidcollider.easyfin.adapters;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.androidcollider.easyfin.R;
import com.androidcollider.easyfin.objects.Account;
import com.androidcollider.easyfin.utils.FormatUtils;

import java.util.ArrayList;

public class ExpenseItemAdapter extends BaseAdapter {

    Context context;
    LayoutInflater layoutInflater;
    ArrayList<Account> accountArrayList;

    public ExpenseItemAdapter(Context context, ArrayList<Account> accountArrayList) {
        this.context = context;
        this.accountArrayList = accountArrayList;
        layoutInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {return accountArrayList.size();}

    @Override
    public Object getItem(int position) {return accountArrayList.get(position);}

    @Override
    public long getItemId(int position) {return position;}


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;

        ViewHolder holder;

        if (view == null) {
            view = layoutInflater.inflate(R.layout.item_fragment_expense, parent, false);

            holder = new ViewHolder();
            holder.tvItemFragmentExpenseName = (TextView) view.findViewById(R.id.tvItemFragmentExpenseName);
            holder.tvItemFragmentExpenseType = (TextView) view.findViewById(R.id.tvItemFragmentExpenseType);
            holder.tvItemFragmentExpenseAmount = (TextView) view.findViewById(R.id.tvItemFragmentExpenseAmount);

            view.setTag(holder);
        }
        else {
            holder = (ViewHolder) view.getTag();
        }

        Account account = getAccount(position);

        final int PRECISE = 100;
        final String FORMAT = "0.00";

        holder.tvItemFragmentExpenseName.setText(account.getName());
        holder.tvItemFragmentExpenseType.setText(account.getType());
        holder.tvItemFragmentExpenseAmount.setText(FormatUtils.doubleFormatter(account.getAmount(), FORMAT, PRECISE) +
                " " + account.getCurrency());


        return view;
    }


    Account getAccount(int position) {
        return (Account) getItem(position);
    }

    static class ViewHolder {
        public TextView tvItemFragmentExpenseName;
        public TextView tvItemFragmentExpenseType;
        public TextView tvItemFragmentExpenseAmount;
    }
}
