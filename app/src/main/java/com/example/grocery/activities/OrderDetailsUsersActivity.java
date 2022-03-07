package com.example.grocery.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.grocery.R;
import com.example.grocery.adapters.AdapterOrderItem;
import com.example.grocery.models.ModelOrderItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class OrderDetailsUsersActivity extends AppCompatActivity {

    private String orderTo, orderId;

    //ui vews
    private ImageButton backBtn , writeReviewBtn;
    private TextView orderIdTv, dataTv , orderStatusTv,shopNameTv , totalItemTv , amountTv , addressTv;
    private RecyclerView itemsRv;
    private FirebaseAuth firebaseAuth;

    private ArrayList<ModelOrderItem> orderItemArrayList;
    private AdapterOrderItem adapterOrderItem;

    String deliveryFee;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details_users);

        //init view
        backBtn = findViewById(R.id.backBtn);
        writeReviewBtn = findViewById(R.id.writeReviewBtn);
        dataTv = findViewById(R.id.dateTv);
        orderIdTv = findViewById(R.id.orderIdTv);
        orderStatusTv = findViewById(R.id.orderStatusTv);
        shopNameTv = findViewById(R.id.shopNameTv);
        totalItemTv = findViewById(R.id.totalItemTv);
        amountTv = findViewById(R.id.amountTv);
        addressTv = findViewById(R.id.addressTv);
        itemsRv = findViewById(R.id.itemsRv);

        Intent intent = getIntent();
        orderTo = intent.getStringExtra("orderTo");//orderto contains uid of hte shop where we placed order
        orderId = intent.getStringExtra("orderId");

        firebaseAuth = FirebaseAuth.getInstance();
        loadShopInfo();
        loadOrderInfo();
        loadOrderitems();

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        //hendal write review Btn click , start write review activity
        writeReviewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(OrderDetailsUsersActivity.this,WriteReviewActivity.class);
                intent1.putExtra("shopUid",orderTo); // to write review we must have a uid of shop
                startActivity(intent1);

            }
        });
    }

    private void loadOrderitems() {
        //init list
        orderItemArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(orderTo).child("Orders").child(orderId).child("Items")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        orderItemArrayList.clear();
                        for(DataSnapshot ds: snapshot.getChildren()){
                            ModelOrderItem modelOrderItem = ds.getValue(ModelOrderItem.class);
                            //add to list
                            orderItemArrayList.add(modelOrderItem);
                        }
                        //all items added to list
                        //setup adapter
                        adapterOrderItem = new AdapterOrderItem(OrderDetailsUsersActivity.this,orderItemArrayList);
                        //set adapter
                        itemsRv.setAdapter(adapterOrderItem);

                        //set items count
                        totalItemTv.setText(""+snapshot.getChildrenCount());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    private void loadOrderInfo() {
        //load order detais
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(orderTo).child("Orders").child(orderId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get data
                        String orderBy = ""+snapshot.child("orderBy").getValue();
                        String orderCost = "" + snapshot.child("orderCost").getValue();
                        String orderId = ""+snapshot.child("orderId").getValue();
                        String orderStatus = ""+snapshot.child("orderStatus").getValue();
                        String orderTime = ""+snapshot.child("orderTime").getValue();
                        String orderTo = ""+snapshot.child("orderTo").getValue();
                        String latitude = ""+snapshot.child("latitude").getValue();
                        String longitude = ""+snapshot.child("longitude").getValue();
                        String discount = ""+snapshot.child("discount").getValue();

                        if (discount.equals("null") || discount.equals("0")){
                            discount = "& discount $0";
                        }
                        else {
                            discount = " & discount $"+discount;
                        }


                        //conver timestamp to proper formate
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(Long.parseLong(orderTime));
                        String formateData = DateFormat.format("dd/MM/yyyy hh:mm a", calendar).toString(); // e.g. 20/05/202 12:01 PM

                        if(orderStatus.equals("In Process")){
                            orderStatusTv.setTextColor(getResources().getColor(R.color.colorPrimary));
                        }

                        else if(orderStatus.equals("Completed")){
                            orderStatusTv.setTextColor(getResources().getColor(R.color.colorGreen));
                        }

                        else if(orderStatus.equals("Cancelled")){
                            orderStatusTv.setTextColor(getResources().getColor(R.color.colorRed));
                        }

                        //set data
                        orderIdTv.setText(orderId);
                        orderStatusTv.setText(orderStatus);
                        amountTv.setText("$"+orderCost+" [Including delivery fee $"+deliveryFee+" "+discount+" ]");
                        dataTv.setText(formateData);


                        findAddress(latitude,longitude);


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    private void findAddress(String latitude, String longitude) {
        double lat = Double.parseDouble(latitude);
        double lan = Double.parseDouble(longitude);

        //find address,country , state , city
        Geocoder geocoder;
        List<Address> addressList;
        geocoder = new Geocoder(this, Locale.getDefault());

        try{
            addressList = geocoder.getFromLocation(lat,lan,1);

            String address =  addressList.get(0).getAddressLine(0); // complete addres
            addressTv.setText(address);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void loadShopInfo() {
        //get shop info

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(orderTo)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String shopName = ""+ snapshot.child("shopName").getValue();
                        deliveryFee = ""+snapshot.child("deliveryFee").getValue();

                        shopNameTv.setText(shopName);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }


}