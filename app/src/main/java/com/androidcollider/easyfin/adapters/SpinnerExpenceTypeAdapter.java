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

public class SpinnerExpenceTypeAdapter extends ArrayAdapter<String> {

    final TypedArray exp_type;
    final String[] expense_type;
    LayoutInflater inflater;

    public SpinnerExpenceTypeAdapter(Context context, int txtViewResourceId, String[] objects) {
        super(context, txtViewResourceId, objects);
        expense_type = objects;
        exp_type = context.getResources().obtainTypedArray(R.array.expense_type_24);
        inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getDropDownView(int position, View view, ViewGroup parent) {return getCustomView(position, view, parent);}
    @Override
    public View getView(int pos, View view, ViewGroup parent) {return getCustomTopView(pos, view, parent);}

    public View getCustomView(int position, View convertView, ViewGroup parent) {
        View mySpinner = inflater.inflate(R.layout.spinner_custom_dropdown_item, parent, false);
        TextView main_text = (TextView) mySpinner.findViewById(R.id.tvSpinCatCustom);
        main_text.setText(expense_type[position]);

        ImageView left_icon = (ImageView) mySpinner .findViewById(R.id.ivSpinCatCustom);
        left_icon.setImageResource(exp_type.getResourceId(position, 0));

        return mySpinner;
    }

    public View getCustomTopView(int position, View convertView, ViewGroup parent) {
        View topSpinner = inflater.inflate(R.layout.spinner_item, parent, false);
        TextView top_text = (TextView) topSpinner.findViewById(R.id.tvTopSpinCatTrans);
        top_text.setText(expense_type[position]);

        return topSpinner;
    }

}
