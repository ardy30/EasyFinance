package com.androidcollider.easyfin.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidcollider.easyfin.ActAccount;
import com.androidcollider.easyfin.R;
import com.androidcollider.easyfin.objects.Account;
import com.androidcollider.easyfin.utils.FormatUtils;

import java.util.ArrayList;

public class RecyclerAccountAdapter extends RecyclerView.Adapter<RecyclerAccountAdapter.ViewHolder> {

    Context context;
    ArrayList<Account> accountsList;

    final TypedArray typeIconsArray;
    final String[] typeArray;

    final String[] curArray;
    final String[] curLangArray;


    public RecyclerAccountAdapter(Context context, ArrayList<Account> accountsList) {
        this.context = context;
        this.accountsList = accountsList;

        typeIconsArray = context.getResources().obtainTypedArray(R.array.account_type_icons);
        typeArray = context.getResources().getStringArray(R.array.account_type_array);

        curArray = context.getResources().getStringArray(R.array.account_currency_array);
        curLangArray = context.getResources().getStringArray(R.array.account_currency_array_language);
    }

    @Override
    public int getItemCount() {return accountsList.size();}

    @Override
    public long getItemId(int position) {return position;}

    public Account getAccount(int position) {return accountsList.get(position);}


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_frg_account, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        Account account = getAccount(position);

        final int PRECISE = 100;
        final String FORMAT = "###,##0.00";

        String cur = account.getCurrency();
        String curLang = null;

        for (int i = 0; i < curArray.length; i++) {
            if (cur.equals(curArray[i])) {
                curLang = curLangArray[i];
            }
        }

        holder.tvAccountName.setText(account.getName());
        holder.tvAccountAmount.setText(FormatUtils.doubleToStringFormatter(account.getAmount(), FORMAT, PRECISE) +
                " " + curLang);

        holder.mView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Context context = v.getContext();

                Intent intent = new Intent(context, ActAccount.class);
                Account account = getAccount(position);

                intent.putExtra("account", account);
                intent.putExtra("mode", 1);
                context.startActivity(intent);
                return true;
            }
        });

        String type = account.getType();

        for (int i = 0; i < typeArray.length; i++) {
            if (typeArray[i].equals(type)) {
                holder.ivAccountType.setImageDrawable(typeIconsArray.getDrawable(i));
            }
        }
    }


    static class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final ImageView ivAccountType;
        public final TextView tvAccountName;
        public final TextView tvAccountAmount;


        public ViewHolder(View view) {
            super(view);
            mView = view;
            ivAccountType = (ImageView) view.findViewById(R.id.ivItemFragmentAccountType);
            tvAccountName = (TextView) view.findViewById(R.id.tvItemFragmentAccountName);
            tvAccountAmount = (TextView) view.findViewById(R.id.tvItemFragmentAccountAmount);
        }
    }
}
