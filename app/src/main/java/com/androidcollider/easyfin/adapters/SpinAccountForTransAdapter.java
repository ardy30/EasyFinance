package com.androidcollider.easyfin.adapters;


import android.content.Context;
import android.content.res.TypedArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidcollider.easyfin.R;
import com.androidcollider.easyfin.objects.Account;
import com.androidcollider.easyfin.utils.DoubleFormatUtils;

import java.util.ArrayList;


public class SpinAccountForTransAdapter extends ArrayAdapter<Account> {

    final TypedArray typeIconsArray;
    final ArrayList<Account> accountList;
    //final String[] typeArray;
    final String[] curArray;
    final String[] curLangArray;
    LayoutInflater inflater;


    public SpinAccountForTransAdapter(Context context, int headLayout, ArrayList<Account> accountL) {

        super(context, headLayout, accountL);
        accountList = accountL;

        typeIconsArray = context.getResources().obtainTypedArray(R.array.account_type_icons);
        //typeArray = context.getResources().getStringArray(R.array.account_type_array);
        curArray = context.getResources().getStringArray(R.array.account_currency_array);
        curLangArray = context.getResources().getStringArray(R.array.account_currency_array_language);
        inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getDropDownView(int position, View view, ViewGroup parent) {return getCustomDropView(position, parent);}
    @Override
    public View getView(int position, View view, ViewGroup parent) {return getCustomHeadView(position, parent);}

    public View getCustomDropView(int position, ViewGroup parent) {
        View dropSpinner = inflater.inflate(R.layout.spin_account_for_trans_dropdown, parent, false);

        TextView name = (TextView) dropSpinner.findViewById(R.id.tvSpinDropdownAccountName);
        name.setText(accountList.get(position).getName());

        ImageView icon = (ImageView) dropSpinner.findViewById(R.id.ivSpinDropdownAccountType);

        /*String typeAcc = accountList.get(position).getType();

        for (int i = 0; i < typeArray.length; i++) {
            if (typeArray[i].equals(typeAcc)) {
                icon.setImageResource(typeIconsArray.getResourceId(i, 0));
                break;
            }
        }*/


        icon.setImageResource(typeIconsArray.getResourceId(accountList.get(position).getType(), 0));

        TextView amountText = (TextView) dropSpinner.findViewById(R.id.tvSpinDropdownAccountAmount);

        final int PRECISE = 100;
        final String FORMAT = "0.00";

        String amount = DoubleFormatUtils.doubleToStringFormatter(accountList.get(position).getAmount(), FORMAT, PRECISE);
        String cur = accountList.get(position).getCurrency();

        String curLang = null;

        for (int i = 0; i < curArray.length; i++) {
            if (cur.equals(curArray[i])) {
                curLang = curLangArray[i];
                break;
            }
        }

        amountText.setText(amount + " " + curLang);

        return dropSpinner;
    }

    public View getCustomHeadView(int position, ViewGroup parent) {
        View headSpinner = inflater.inflate(R.layout.spin_head_text, parent, false);

        TextView headText = (TextView) headSpinner.findViewById(R.id.tvSpinHeadText);
        headText.setText(accountList.get(position).getName());

        return headSpinner;
    }


    public Account getItem (int position) {
        return accountList.get(position);
    }

}
