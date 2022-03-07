package com.example.grocery.adapters;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.grocery.R;
import com.example.grocery.activities.OrderDetailsUsersActivity;
import com.example.grocery.models.ModelCartitem;
import com.example.grocery.models.ModelOrderUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;

public class AdapterOrderUser extends RecyclerView.Adapter<AdapterOrderUser.HolderOrderUser> {

    private Context context;
    private ArrayList<ModelOrderUser> orderUsersList;

    public AdapterOrderUser(Context context, ArrayList<ModelOrderUser> orderUsersList) {
        this.context = context;
        this.orderUsersList = orderUsersList;
    }

    @NonNull
    @Override
    public HolderOrderUser onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout row_cartitem.xml
        View view = LayoutInflater.from(context).inflate(R.layout.row_order_user,parent,false);
        return new HolderOrderUser(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderOrderUser holder, int position) {

        ModelOrderUser orderUser = orderUsersList.get(position);
        String orderId = orderUser.getOrderId();
        String orderTime = orderUser.getOrderTime();
        String orderStatus = orderUser.getOrderStatus();
        String orderCost = orderUser.getOrderCost();
        String orderBy = orderUser.getOrderBy();
        String orderTo = orderUser.getOrderTo();

        //get shop info
        loadShopInfo(orderUser,holder);

        //set data
        holder.amountTv.setText("Amount : $"+orderCost);
        holder.statusTv.setText("Order Status :"+orderStatus);
        holder.orderIdTv.setText(orderId);

        //change order statuse text color
        if (orderStatus.equals("In Process")){
            holder.statusTv.setTextColor(context.getResources().getColor(R.color.colorPrimary));
        }
        else if (orderStatus.equals("Comleted")){
            holder.statusTv.setTextColor(context.getResources().getColor(R.color.colorGreen));
        }
        else if (orderStatus.equals("Cancelled")){
            holder.statusTv.setTextColor(context.getResources().getColor(R.color.colorRed));
        }


        //convert timestamp to proper format
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Long.parseLong(orderTime));
        String formateDate = DateFormat.format("dd/MM/yyyy",calendar).toString(); //e.g 16/06/2020

        holder.dateTv.setText(formateDate);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //open order details, we need to keys there , orderId , orderTo
                Intent intent = new Intent(context, OrderDetailsUsersActivity.class);
                intent.putExtra("orderTo",orderTo);
                intent.putExtra("orderId",orderId);
                context.startActivity(intent); // now get these values through intent on OrderDetailsUserActivity

            }
        });
    }

    private void loadShopInfo(ModelOrderUser orderUser, HolderOrderUser holder) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(orderUser.getOrderTo())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String shopName = ""+snapshot.child("shopName").getValue();
                        holder.shopNameTv.setText(shopName);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return orderUsersList.size();
    }

    //view holder class
    class HolderOrderUser extends RecyclerView.ViewHolder{

        private TextView orderIdTv,dateTv,shopNameTv,amountTv,statusTv;
        private ImageView nextIv;

        public HolderOrderUser(@NonNull View itemView) {
            super(itemView);

            //init view of layout
            orderIdTv =itemView.findViewById(R.id.orderIdTv);
            dateTv =itemView.findViewById(R.id.dateTv);
            shopNameTv =itemView.findViewById(R.id.shopNameTv);
            amountTv =itemView.findViewById(R.id.amountTv);
            nextIv =itemView.findViewById(R.id.nextIv);
            statusTv = itemView.findViewById(R.id.statusTv);
        }
    }
}
