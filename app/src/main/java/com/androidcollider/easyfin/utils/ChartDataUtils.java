package com.androidcollider.easyfin.utils;


import android.content.Context;

import com.androidcollider.easyfin.R;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;

import java.util.ArrayList;


public class ChartDataUtils {



    public static BarData getDataSetMainBalanceHorizontalBarChart(double[] values, Context context) {


        float cash = (float) values[0];
        float card = (float) values[1];
        float deposit = (float) values[2];
        float debt = (float) Math.abs(values[3]);

        String[] accountType = context.getResources().getStringArray(R.array.account_type_array);

        ArrayList<BarEntry> valueSet1 = new ArrayList<>();
        valueSet1.add(new BarEntry(cash, 0));

        ArrayList<BarEntry> valueSet2 = new ArrayList<>();
        valueSet2.add(new BarEntry(card, 0));

        ArrayList<BarEntry> valueSet3 = new ArrayList<>();
        valueSet3.add(new BarEntry(deposit, 0));

        ArrayList<BarEntry> valueSet4 = new ArrayList<>();
        valueSet4.add(new BarEntry(debt, 0));

        BarDataSet barDataSet1 = new BarDataSet(valueSet1, accountType[0]);
        barDataSet1.setColor(context.getResources().getColor(R.color.custom_teal_dark));
        //barDataSet1.setAxisDependency(YAxis.AxisDependency.LEFT);
        BarDataSet barDataSet2 = new BarDataSet(valueSet2, accountType[1]);
        barDataSet2.setColor(context.getResources().getColor(R.color.custom_amber_dark));
        //barDataSet2.setAxisDependency(YAxis.AxisDependency.LEFT);
        BarDataSet barDataSet3 = new BarDataSet(valueSet3, accountType[2]);
        barDataSet3.setColor(context.getResources().getColor(R.color.custom_blue_dark));
        //barDataSet3.setAxisDependency(YAxis.AxisDependency.LEFT);
        BarDataSet barDataSet4 = new BarDataSet(valueSet4, accountType[3]);

        if (FormatUtils.isDoubleNegative(values[3])) {
            barDataSet4.setColor(context.getResources().getColor(R.color.custom_red_dark));
        }

        else {
            barDataSet4.setColor(context.getResources().getColor(R.color.custom_green));
        }
        //barDataSet4.setAxisDependency(YAxis.AxisDependency.LEFT);

        ArrayList<BarDataSet> dataSets = new ArrayList<>();
        dataSets.add(barDataSet4);
        dataSets.add(barDataSet3);
        dataSets.add(barDataSet2);
        dataSets.add(barDataSet1);


        BarData data = new BarData(getXAxisValues(), dataSets);
        data.setValueTextSize(12f);


        return data;
    }


    public static BarData getDataSetMainStatisticHorizontalBarChart(double[] values, Context context) {


        float cost = (float) Math.abs(values[0]);
        float income = (float) values[1];

        ArrayList<BarEntry> valueSet1 = new ArrayList<>();
        valueSet1.add(new BarEntry(income, 0));

        ArrayList<BarEntry> valueSet2 = new ArrayList<>();
        valueSet2.add(new BarEntry(cost, 0));


        BarDataSet barDataSet1 = new BarDataSet(valueSet1, "income");
        barDataSet1.setColor(context.getResources().getColor(R.color.custom_green));
        //barDataSet1.setAxisDependency(YAxis.AxisDependency.LEFT);
        BarDataSet barDataSet2 = new BarDataSet(valueSet2, "cost");
        barDataSet2.setColor(context.getResources().getColor(R.color.custom_red));
        //barDataSet2.setAxisDependency(YAxis.AxisDependency.LEFT);

        ArrayList<BarDataSet> dataSets = new ArrayList<>();
        dataSets.add(barDataSet2);
        dataSets.add(barDataSet1);


        BarData data = new BarData(getXAxisValues(), dataSets);
        data.setValueTextSize(12f);


        return data;
    }



    public static ArrayList<String> getXAxisValues() {
        ArrayList<String> xAxis = new ArrayList<>();
        xAxis.add("");
        return xAxis;
    }


    private static ArrayList<String> getXAxisValuesPie() {
        ArrayList<String> xAxis = new ArrayList<>();
        xAxis.add("");
        xAxis.add("");
        return xAxis;
    }




    public static PieData getDataSetMainStatisticPieChart (double[] values, Context context) {

        float cost = (float) Math.abs(values[0]);
        float income = (float) values[1];


        ArrayList<Entry> valueSet = new ArrayList<>();
        valueSet.add(new Entry(income, 0));
        valueSet.add(new Entry(cost, 0));

        PieDataSet pieDataSet = new PieDataSet(valueSet, "statistic");

        int[] colors = new int[2];
        colors[0] = context.getResources().getColor(R.color.custom_green);
        colors[1] = context.getResources().getColor(R.color.custom_red);

        pieDataSet.setColors(colors);

        pieDataSet.setSliceSpace(3);
        pieDataSet.setSelectionShift(5);


        PieData data = new PieData(getXAxisValuesPie(), pieDataSet);
        data.setValueTextSize(12f);


        return data;
    }


}
