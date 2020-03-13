package com.charikati.parkright;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import com.charikati.parkright.model.MonthReportsData;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.util.ArrayList;
import java.util.List;

public class MyStatusFragment extends Fragment {
    private BarChart barChart;
    private ArrayList<BarEntry> barEntryArrayList;
    private ArrayList<String> labelsNames;
    private ArrayList<MonthReportsData> monthReportsDataArrayList = new ArrayList<>();

    private PieChart pieChart;

    public MyStatusFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_mystatus, container, false);
        //Change the Toolbar title
        TextView heading = getActivity().findViewById(R.id.toolbar_title);
        heading.setText("MY STATUS");
        //Find barChart and initialize barEntry and labels
        barChart = v.findViewById(R.id.barChart);
        barEntryArrayList = new ArrayList<>();
        labelsNames = new ArrayList<>();
        fillMonthReports();
        //Fill barEntry and labels arrayList
        for(int i=0; i<monthReportsDataArrayList.size(); i++){
            String month = monthReportsDataArrayList.get(i).getMonth();
            int sales = monthReportsDataArrayList.get(i).getReports();
            barEntryArrayList.add(new BarEntry(i, sales));
            labelsNames.add(month);
        }
        //Create barDataset object from barEntry ArrayList
        final BarDataSet barDataSet = new BarDataSet(barEntryArrayList, "Monthly Sales");
        barDataSet.setColors(getResources().getColor(R.color.blue_clair));
        // Disable the description
        barChart.getDescription().setEnabled(false);
        //Disable the legend
        barChart.getLegend().setEnabled(false);
        //Set data to the barChart
        BarData barData = new BarData(barDataSet);
        barChart.setData(barData);
        //We need to set XAxis Value Formatter
        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labelsNames));
        //Set position of labels(Month names)
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(labelsNames.size());
        xAxis.setLabelRotationAngle(270);
        //Eliminate left and right axis and grid
        barChart.getAxisLeft().setDrawLabels(false);
        barChart.getAxisRight().setDrawLabels(false);
        barChart.getAxisLeft().setEnabled(false);
        barChart.getAxisRight().setEnabled(false);
        barChart.animateY(1000);
        barChart.invalidate();

        //Code for PieChart
        pieChart = v.findViewById(R.id.pieChart);
        // Disable the description
        pieChart.getDescription().setEnabled(false);
        pieChart.setRotationEnabled(true);
        pieChart.setHoleRadius(75f);
        pieChart.setTransparentCircleAlpha(0);
        pieChart.setDrawEntryLabels(true);
        pieChart.setEntryLabelTextSize(20);

        addDataSet();

        //Click on a pie slice shows employee name and generated sales
        pieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                //label of each entry
                String label = "";
                //index of each entry (0..2)
                int index = (int) h.getX();
                if(index == 0)
                    label = "Approved";
                else if(index == 1)
                    label = "Declined";
                else
                    label = "Pending";

                Toast.makeText(getContext(),(int)h.getY() + "  " + label + " Reports", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onNothingSelected() {

            }
        });

        return v;
    }

    /**
     * Fill Array List with months and reports for BarChart by requesting database
     */
    private void fillMonthReports(){
        monthReportsDataArrayList.clear();
        monthReportsDataArrayList.add(new MonthReportsData("Jan", 1));
        monthReportsDataArrayList.add(new MonthReportsData("Feb", 2));
        monthReportsDataArrayList.add(new MonthReportsData("Mar", 5));
        monthReportsDataArrayList.add(new MonthReportsData("Apr", 4));
        monthReportsDataArrayList.add(new MonthReportsData("Mai", 7));
        monthReportsDataArrayList.add(new MonthReportsData("Jun", 8));
        monthReportsDataArrayList.add(new MonthReportsData("Jul", 15));
        monthReportsDataArrayList.add(new MonthReportsData("Aug", 4));
        monthReportsDataArrayList.add(new MonthReportsData("Sept", 6));
        monthReportsDataArrayList.add(new MonthReportsData("Oct", 7));
        monthReportsDataArrayList.add(new MonthReportsData("Nov", 2));
        monthReportsDataArrayList.add(new MonthReportsData("Dec", 10));
    }

    /**
     * Fill PieChart with  reports data of a specific month
     */
    private void addDataSet() {
        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(3, "Approved"));
        entries.add(new PieEntry(3, "Declined"));
        entries.add(new PieEntry(4, "Pending"));
        //create the data set
        PieDataSet set = new PieDataSet(entries, "");

        //Hide values on the PieChart
        set.setDrawValues(false);
        //Hide labels on the PieChart
        pieChart.setDrawEntryLabels(false);
        set.setSliceSpace(0.5f);
        //add colors to dataSet
        set.setColors(new int[] { R.color.green, R.color.blue, R.color.yellow }, getContext());
        //add legend to chart
        Legend legend = pieChart.getLegend();
        legend.setFormSize(20f);
        legend.setForm(Legend.LegendForm.CIRCLE);
        legend.setTextSize(15f);
        legend.setTextColor(Color.BLACK);
        Typeface typeface = ResourcesCompat.getFont(getContext(), R.font.poppins);
        legend.setTypeface(typeface);
        // Set the legend position to Top Center
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);
        //Set data to the PieChart
        PieData data = new PieData(set);
        pieChart.setData(data);
        pieChart.animateY(2000);
        pieChart.invalidate(); // refresh

    }


}
