package com.charikati.parkright.adapter;
import android.content.Context;
import android.content.Intent;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.charikati.parkright.PhotoActivity;
import com.charikati.parkright.R;
import com.charikati.parkright.model.ViolationPreview;
import java.util.ArrayList;

/**
 * The adapter class for the RecyclerView, contains the parking violations data.
 */
public class ViolationAdapter extends RecyclerView.Adapter<ViolationAdapter.ViewHolder> {
    private ArrayList<ViolationPreview> mViolationsData;
    private Context mContext;
    /**
     * Constructor that passes in the parking violations data and the context.
     * @param violationsData ArrayList containing the violationsdata.
     * @param context Context of the application.
     */
    public ViolationAdapter(Context context, ArrayList<ViolationPreview> violationsData) {
        this.mViolationsData = violationsData ;
        this.mContext = context;
    }

    /**
     * Required method for creating the viewholder objects.
     * @param parent The ViewGroup into which the new View will be added
     * after it is bound to an adapter position.
     * @param viewType The view type of the new View.
     * @return The newly created ViewHolder.
     */
    @Override
    public ViolationAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(mContext).
                inflate(R.layout.violation_item, parent, false));
    }

    /**
     * Required method that binds the data to the viewholder.
     * @param holder The viewholder into which the data should be put.
     * @param position The adapter position.
     */
    @Override
    public void onBindViewHolder(ViolationAdapter.ViewHolder holder, int position) {
        // Get current violation.
        ViolationPreview currentViolation = mViolationsData.get(position);
        // Populate the textviews with data.
        holder.bindTo(currentViolation);
    }

    /**
     * Required method for determining the size of the data set.
     * @return Size of the data set.
     */
    @Override
    public int getItemCount() {
        return mViolationsData.size();
    }

    /**
     * ViewHolder class that represents each row of data in the RecyclerView.
     */
    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        // Member Variables for the TextView and ImageView
        private TextView mViolationType;
        private ImageView mViolationImage;

        /**
         * Constructor for the ViewHolder, used in onCreateViewHolder().
         * @param itemView The rootview of the violation_item.xml layout file.
         */
        ViewHolder(View itemView) {
            super(itemView);
            // Initialize the views.
            mViolationType = itemView.findViewById(R.id.message_txt);
            mViolationImage = itemView.findViewById(R.id.violation_img);
            // Set the OnClickListener to the entire view.
            itemView.setOnClickListener(this);
        }

        /**
         * Bind data from adapter to layout components
         */
        void bindTo(ViolationPreview currentViolation){
            // Populate the textview with data.
            mViolationType.setText(currentViolation.getViolationType());
            //Get the image resource from the Violation object and load it into the ImageView using Glide
            Glide.with(mContext).load(currentViolation.getImageResourceId()).into(mViolationImage);
        }

        /**
         * Click on an violation card will open Photo Activity for that type of violation
         */
        @Override
        public void onClick(View v) {
            //get the Violation object for the item that was clicked using getAdapterPosition()
            ViolationPreview currentViolation = mViolationsData.get(getAdapterPosition());
            // Launch DetailActivity using an explicit in tent
            Intent photoIntent = new Intent(mContext, PhotoActivity.class);
            //Create a bundle and add violation index and type
            Bundle extras = new Bundle();
            extras.putInt("VIOLATION_INDEX", getAdapterPosition());
            extras.putString("VIOLATION_TYPE", currentViolation.getViolationType());
            extras.putInt("VIOLATION_RESOURCE", currentViolation.getImageResourceId());
            photoIntent.putExtras(extras);
            mContext.startActivity(photoIntent);
        }
    }
}