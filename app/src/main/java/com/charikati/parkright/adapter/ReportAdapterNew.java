package com.charikati.parkright.adapter;

import android.content.Context;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.charikati.parkright.DetailsActivity;
import com.charikati.parkright.R;
import com.charikati.parkright.TimeAgo;

import com.charikati.parkright.model.ViolationReport;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Date;

/**
 * The adapter class for the RecyclerView, contains the parking violations data.
 */
public class ReportAdapterNew extends RecyclerView.Adapter<ReportAdapterNew.ViewHolder> {

    private ArrayList<ViolationReport> mViolationsData;
    private Context mContext;

    public ReportAdapterNew(Context context, ArrayList<ViolationReport> violationsData) {
        this.mViolationsData = violationsData ;
        this.mContext = context;
    }

    @NonNull
    @Override
    public ReportAdapterNew.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(mContext).
                inflate(R.layout.report_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ReportAdapterNew.ViewHolder holder, int position) {
        // Get current violation.
        ViolationReport currentReport = mViolationsData.get(position);
        // Populate the textViews with data.
        holder.bindTo(currentReport);

    }

    @Override
    public int getItemCount() {
        return mViolationsData.size();
    }


    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        // Member Variables
        private TextView mReportType;
        private TextView mReportTime;
        private TextView mReportStatus;
        private ImageView mStatusImage;

        /**
         * Constructor for the ViewHolder, used in onCreateViewHolder().
         * @param itemView The rootView of the report_item.xml layout file.
         */
        ViewHolder(View itemView) {
            super(itemView);
            // Initialize the views.
            mReportType = itemView.findViewById(R.id.violation_txt);
            mReportTime = itemView.findViewById(R.id.time_txt);
            mReportStatus = itemView.findViewById(R.id.status_txt);
            mStatusImage = itemView.findViewById(R.id.status_img);
            // Set the OnClickListener to the entire view.
            itemView.setOnClickListener(this);
        }

        /**
         * Bind data from adapter to layout components
         */
        void bindTo(ViolationReport currentReport){
            // Populate the textViews with data.
            mReportType.setText(currentReport.getType());
            mReportTime.setText(TimeAgo.toRelative(new Date().getTime() - currentReport.getSendingTime(), 1));
            mReportStatus.setText(currentReport.getStatus());
            String status = currentReport.getStatus();
            if(status.equals("Pending")){
                mStatusImage.setImageResource(R.drawable.ic_hourglass);
            }else if(status.equals("Approved")){
                mStatusImage.setImageResource(R.drawable.ic_right);
            }else{
                mStatusImage.setImageResource(R.drawable.ic_close);
            }
        }

        /**
         * Click on an Report card will open DetailsActivity for that type of violation
         */
        @Override
        public void onClick(View v) {
            //get the Report object for the item that was clicked using getAdapterPosition()
            ViolationReport currentReport = mViolationsData.get(getAdapterPosition());
            // Launch DetailActivity using an explicit in tent
            Intent DetailsIntent = new Intent(mContext, DetailsActivity.class);
            DetailsIntent.putExtra("report", new Gson().toJson(currentReport));
            //Create a bundle and add violation index and type
            mContext.startActivity(DetailsIntent);
        }
    }
}
