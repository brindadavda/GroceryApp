package com.example.grocery.adapters;

import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.grocery.Constans;
import com.example.grocery.FilterOrderShop;
import com.example.grocery.R;
import com.example.grocery.activities.OrderDetailsSellerActivity;
import com.example.grocery.activities.OrderDetailsUsersActivity;
import com.example.grocery.activities.mainSellerActivity;
import com.example.grocery.models.ModelOrderShop;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class AdapterOrderShop extends RecyclerView.Adapter<AdapterOrderShop.HolderOrderShop> implements Filterable {

    private mainSellerActivity constans;
    public ArrayList<ModelOrderShop> modelOrderShopArrayList , FilterList ;
    private FilterOrderShop filter;

    public AdapterOrderShop(mainSellerActivity constans, ArrayList<ModelOrderShop> modelOrderShopArrayList) {
        this.constans = constans;
        this.modelOrderShopArrayList = modelOrderShopArrayList;
        this.FilterList = modelOrderShopArrayList;
    }

    @NonNull
    @Override
    public HolderOrderShop onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(constans).inflate(R.layout.row_order_seller,parent,false);
        return new HolderOrderShop(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderOrderShop holder, int position) {
        ModelOrderShop modelOrderShop = modelOrderShopArrayList.get(position);
        String orderId = modelOrderShop.getOrderId();
        String orderBy = modelOrderShop.getOrderBy();
        String orderCost = modelOrderShop.getOrderCost();
        String orderStatus = modelOrderShop.getOrderStatus();
        String orderTime = modelOrderShop.getOrderTime();
        String orderTo = modelOrderShop.getOrderTo();

        // load user / buyer info
        loadUserInfo(modelOrderShop,holder);

        //set data
        holder.amountTv.setText("Amount :"+orderCost);
        holder.statusTv.setText(orderStatus);
        holder.orderIdTv.setText("Order ID: "+orderId);
        //change order status text color
        if (orderStatus.equals("In Process")){
            holder.statusTv.setTextColor(constans.getResources().getColor(R.color.colorPrimary));
        }
         else if (orderStatus.equals("Completed")){
            holder.statusTv.setTextColor(constans.getResources().getColor(R.color.colorGreen));
        }
        else if (orderStatus.equals("Cancelled")){
            holder.statusTv.setTextColor(constans.getResources().getColor(R.color.colorRed));
        }

        //convert time to prepare fomate
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Long.parseLong(orderTime));
        String formateDate = DateFormat.format("dd/MM/yyyy",calendar).toString();

        holder.orderDateTv.setText(formateDate);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //open order details
                Intent intent = new Intent(constans , OrderDetailsSellerActivity.class);
                intent.putExtra("orderId",orderId); // to load order info
                intent.putExtra("orderBy",orderBy); //to load info of user who placed order
                constans.startActivity(intent);
            }
        });

    }

    private void loadUserInfo(ModelOrderShop modelOrderShop, HolderOrderShop holder) {
        //to load email of the user / buyer : modelordershop.getOrderBy() contains uid of that user/buyer
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(modelOrderShop.getOrderBy())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String email = ""+snapshot.child("email").getValue();
                        holder.emailTv.setText(email);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return modelOrderShopArrayList.size();
    }

    @Override
    public Filter getFilter() {
        if (filter ==null){
            filter = new FilterOrderShop(this,FilterList);
        }
        return filter;
    }

    //view holder class for row_order_seller.xml
    class HolderOrderShop extends RecyclerView.ViewHolder{

        //ui views of row_order_seller.xml
        private TextView orderIdTv,orderDateTv , emailTv,amountTv,statusTv,nextIv;

        public HolderOrderShop(@NonNull View itemView) {
            super(itemView);

            orderIdTv = itemView.findViewById(R.id.orderIdTv);
            orderDateTv =itemView.findViewById(R.id.orderDateTv);
            emailTv = itemView.findViewById(R.id.emailTv);
            amountTv = itemView.findViewById(R.id.amountTv);
            statusTv = itemView.findViewById(R.id.statusTv);
        }



    }
}
