package com.example.fyp_v2;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fyp_v2.Class.Receipt;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ReceiptAdapter extends RecyclerView.Adapter<ReceiptAdapter.ImageViewHolder> {

    private Context mContext;
    private List<Receipt> mReceipt;

    public ReceiptAdapter(Context context, List<Receipt> uploads){
        mContext = context;
        mReceipt = uploads;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.receipt, parent, false);
        return new ImageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        Receipt receiptCurrent = mReceipt.get(position);
        holder.description.setText(receiptCurrent.getReceiptDes());
        holder.date.setText(receiptCurrent.getDate());
        holder.total.setText(receiptCurrent.getTotal());

        Picasso.with(mContext)
                .load(receiptCurrent.getUri())
                .fit()
                .centerCrop()
                .into(holder.image);
    }

    @Override
    public int getItemCount() {
        return mReceipt.size();
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder{

        public TextView description;
        public TextView date;
        public TextView total;
        public ImageView image;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);

            description = itemView.findViewById(R.id.textDescription);
            date = itemView.findViewById(R.id.textDate);
            total = itemView.findViewById(R.id.textTotal);
            image = itemView.findViewById(R.id.receiptView);
        }
    }
}
