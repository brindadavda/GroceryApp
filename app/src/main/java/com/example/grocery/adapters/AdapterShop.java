package com.example.grocery.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.grocery.R;
import com.example.grocery.activities.ShopDetailsActivity;
import com.example.grocery.models.ModleShop;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class AdapterShop  extends RecyclerView.Adapter<AdapterShop.HolderShop>{

    private Context context;
    public ArrayList<ModleShop> shopList;

    public AdapterShop(Context context, ArrayList<ModleShop> shopList) {
        this.context = context;
        this.shopList = shopList;
    }

    @NonNull
    @Override
    public HolderShop onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout new_shop.xml
        View view = LayoutInflater.from(context).inflate(R.layout.new_shop,parent,false);
        return new HolderShop(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderShop holder, int position) {
        //get data
        ModleShop modleShop = shopList.get(position);
        String accountType = modleShop.getAccountType();
        String address = modleShop.getAddress();
        String city = modleShop.getCity();
        String country = modleShop.getCountry();
        String deliveryFee = modleShop.getDelivery();
        String email = modleShop.getEmail();
        String latitude = modleShop.getLatitude();
        String longitude = modleShop.getLongitude();
        String online = modleShop.getOnline();
        String name = modleShop.getName();
        String phone = modleShop.getPhone();
        String uid = modleShop.getUid();
        String timestamp = modleShop.getTimestamp();
        String shopOpen = modleShop.getShopOpen();
        String state = modleShop.getState();
        String profileImage = modleShop.getProfileImage();
        String shopName = modleShop.getShopName();

        loadReviews(modleShop,holder);

        //set data
        holder.shopNameTv.setText(shopName);
        holder.phoneTv.setText(phone);
        holder.addressTv.setText(address);
        //check if online
        if(online.equals("true")){
            //shop owner is online
            holder.onlineIv.setVisibility(View.VISIBLE);
        }
        else {
            //shop owner is offline
            holder.onlineIv.setVisibility(View.GONE);
        }

        //check if shop open
        if (shopOpen.equals("true")){
            //shop open
            holder.shopClossedTv.setVisibility(View.GONE);
        }
        else {
            //sho[ closed
            holder.shopClossedTv.setVisibility(View.VISIBLE);
        }

        try {
            Picasso.get().load(profileImage).placeholder(R.drawable.ic_store_gray).into(holder.shopIv);
        }
        catch (Exception e){
            holder.shopIv.setImageResource(R.drawable.ic_store_gray);
        }

        //handle click listener, show shop details
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ShopDetailsActivity.class);
                intent.putExtra("shopUid",uid);
                context.startActivity(intent);
            }
        });


    }

    private float ratingSum = 0;
    private void loadReviews(ModleShop modleShop, HolderShop holder) {

        String shopUid = modleShop.getUid();

        //init list
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(shopUid).child("Ratings")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //clear list before adding data into it

                        ratingSum =0;
                        for (DataSnapshot ds : snapshot.getChildren()){
                            float rating = Float.parseFloat(""+ds.child("ratings").getValue());
                            ratingSum += rating; // for avg rating add all ratings , later will divide it by number of reviwes
                        }

                        long numberOfReviws = snapshot.getChildrenCount();
                        float avgRating = ratingSum/numberOfReviws;

                        holder.ratingBar.setRating(avgRating);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    @Override
    public int getItemCount() {
        return shopList.size(); //return number of records
    }

    //view holder
    class HolderShop extends RecyclerView.ViewHolder{

        //ui views of new_shop.xml
        private ImageView shopIv,onlineIv,nextIv;
        private TextView shopClossedTv,shopNameTv,phoneTv,addressTv;
        private RatingBar ratingBar;

        public HolderShop(@NonNull View itemView) {
            super(itemView);

            //init ui views
            shopIv = itemView.findViewById(R.id.shopIv);
            onlineIv = itemView.findViewById(R.id.onlineIv);
            nextIv = itemView.findViewById(R.id.nextIv);
            shopClossedTv = itemView.findViewById(R.id.shopClossedTv);
            shopNameTv = itemView.findViewById(R.id.shopNameTv);
            phoneTv = itemView.findViewById(R.id.phoneTv);
            addressTv = itemView.findViewById(R.id.addressTv);
            ratingBar = itemView.findViewById(R.id.ratingBar);


        }
    }

}
