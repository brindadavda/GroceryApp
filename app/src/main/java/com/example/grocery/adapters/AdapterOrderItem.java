package com.example.grocery.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.grocery.R;
import com.example.grocery.models.ModelOrderItem;

import java.util.ArrayList;

public class AdapterOrderItem extends RecyclerView.Adapter<AdapterOrderItem.HolderOrderItem> {

    private Context context;
    private ArrayList<ModelOrderItem> modelOrderItemList;

    public AdapterOrderItem(Context context, ArrayList<ModelOrderItem> modelOrderItemList) {
        this.context = context;
        this.modelOrderItemList = modelOrderItemList;
    }

    @NonNull
    @Override
    public HolderOrderItem onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout
        View view = LayoutInflater.from(context).inflate(R.layout.row_orderitem,parent,false);
        return new HolderOrderItem(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderOrderItem holder, int position) {
        //get data at position
        ModelOrderItem modleOrderItem = modelOrderItemList.get(position);
        String getId = modleOrderItem.getpId();
        String name = modleOrderItem.getName();
        String cost = modleOrderItem.getCost();
        String price = modleOrderItem.getPrice();
        String quantity = modleOrderItem.getQuantity();

        //set data
        holder.itemTitleTv.setText(name);
        holder.itemPriceEachtv.setText("$"+price);
        holder.itemQuantityTv.setText("["+quantity+"]");
        holder.itemPriceTv.setText(cost);
    }

    @Override
    public int getItemCount() {
        return modelOrderItemList.size();
    }


    class HolderOrderItem extends RecyclerView.ViewHolder{

        private TextView itemTitleTv,itemPriceEachtv,itemQuantityTv,itemPriceTv;

        public HolderOrderItem(@NonNull View itemView) {
            super(itemView);

            itemTitleTv = itemView.findViewById(R.id.itemTitleTv);
            itemPriceEachtv = itemView.findViewById(R.id.itemPriceEachtv);
            itemQuantityTv = itemView.findViewById(R.id.itemQuantityTv);
            itemPriceTv = itemView.findViewById(R.id.itemPriceTv);
        }
    }
}
