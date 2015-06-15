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

public class SpinnerTransCategoriesAdapter extends ArrayAdapter<String> {

    final TypedArray icons;
    final String[] categories;
    LayoutInflater inflater;

    public SpinnerTransCategoriesAdapter(Context context, int txtViewResourceId, String[] objects) {
        super(context, txtViewResourceId, objects);
        categories = objects;
        icons = context.getResources().obtainTypedArray(R.array.trans_categories_icons);
        inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getDropDownView(int position, View view, ViewGroup parent) {return getCustomView(position, parent);}
    @Override
    public View getView(int pos, View view, ViewGroup parent) {return getCustomTopView(pos, parent);}

    public View getCustomView(int position, ViewGroup parent) {
        View mySpinner = inflater.inflate(R.layout.spin_custom_dropdown_item, parent, false);
        TextView mainText = (TextView) mySpinner.findViewById(R.id.tvSpinDropdownCategory);
        mainText.setText(categories[position]);

        ImageView leftIcon = (ImageView) mySpinner .findViewById(R.id.ivSpinDropdownCategory);
        leftIcon.setImageResource(icons.getResourceId(position, 0));

        return mySpinner;
    }

    public View getCustomTopView(int position, ViewGroup parent) {
        View headSpinner = inflater.inflate(R.layout.spin_custom_item, parent, false);
        TextView headText = (TextView) headSpinner.findViewById(R.id.tvSpinTopText);
        headText.setText(categories[position]);

        return headSpinner;
    }

}
