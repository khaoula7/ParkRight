package com.charikati.parkright;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import java.util.ArrayList;

public class ReportAdapter extends ArrayAdapter<ViolationReport> {
    private static final String TAG = "ReportAdapter";
    /**
     * Create a new {@link ReportAdapter} object.
     *
     * @param context is the current context (i.e. Activity) that the adapter is being created in.
     * @param violations is the list of {@link ViolationReport}s to be displayed.
     */
    ReportAdapter(Context context, int resource, ArrayList<ViolationReport> violations) {
            super(context, resource, violations);
        }

    @Override
    public View getView(int position, View reportItemView, ViewGroup parent) {
        // Check if an existing view is being reused, otherwise inflate the view
        if (reportItemView == null) {
            reportItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.report_item, parent, false);
        }
        // Get the {@link Violation} object located at this position in the list
        ViolationReport currentViolation = getItem(position);

        // Find the TextView in the list_item.xml layout with the ID status_txt.
        TextView statusTextView= reportItemView.findViewById(R.id.status_txt);
        String status = currentViolation.getStatus();
        //Apply a different background drawable according to the violation status
        if(status == "Pending"){
            statusTextView.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.status_text_style));
        }else if(status.equals("Accepted")){
            statusTextView.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.status_text_style));
        }else{
            statusTextView.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.status_text_style));
        }

        // Find the TextView in the list_item.xml layout with the ID violation_txt.
        TextView typeTextView= (TextView) reportItemView.findViewById(R.id.violation_txt);
        // Get the violation type from the currentWord object and set it to type TextView
        typeTextView.setText(currentViolation.getType());

        // Find the TextView in the list_item.xml layout with the ID date_txt.
        TextView dateTextView = reportItemView.findViewById(R.id.date_txt);
        // Find the TextView in the list_item.xml layout with the ID time_txt
        TextView timeTextView = reportItemView.findViewById(R.id.time_txt);
        // Get the sending time from the currentWord object and split it between date and time TextViews
        String [] dateTime = currentViolation.getSendingTime().split("\\s");
        //Set the date and time
        dateTextView.setText(dateTime[0]);
        timeTextView.setText(dateTime[1]);

        // Return the whole list item layout so that it can be shown in the ListView.
        return reportItemView;
    }
}
