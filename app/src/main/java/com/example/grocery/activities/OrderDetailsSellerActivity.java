package com.example.grocery.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.grocery.Constans;
import com.example.grocery.R;
import com.example.grocery.adapters.AdapterOrderItem;
import com.example.grocery.models.ModelOrderItem;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class OrderDetailsSellerActivity extends AppCompatActivity {

    ImageButton backBtn,editBtn,mapBtn;
    TextView orderIdTv , dateTv ,orderStatusTv,emailTv,phoneTv,totalItemsTv,amountTv,addressTv ;
    RecyclerView itemsRv;

    String orderId , orderBy;

    String deliveryFee;

    //to open destination in map
    String sourceLatitude, sourceLongitude , destinationLatitude , destinationLongitude;

    private FirebaseAuth firebaseAuth;

    private ArrayList<ModelOrderItem> modelOrderItemArrayList;
    private AdapterOrderItem adapterOrderItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details_seller);

        backBtn = findViewById(R.id.backBtn);
        editBtn = findViewById(R.id.editBtn);
        mapBtn = findViewById(R.id.mapBtn);
        orderIdTv = findViewById(R.id.orderIdTv);
        dateTv = findViewById(R.id.dateTv);
        orderStatusTv = findViewById(R.id.orderStatusTv);
        emailTv = findViewById(R.id.emaildetailsSellerTv);
        phoneTv = findViewById(R.id.phoneTv);
        totalItemsTv = findViewById(R.id.totalItemsTv);
        amountTv = findViewById(R.id.amountTv);
        addressTv = findViewById(R.id.addressTv);
        itemsRv = findViewById(R.id.itemsRv);

        //get data from intent
        orderId = getIntent().getStringExtra("orderId");
        orderBy = getIntent().getStringExtra("orderBy");

        firebaseAuth = FirebaseAuth.getInstance();
        loadMyinfo();
        loadBuyerinfo();
        loadOrderDetails();
        loadOrderItems();

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        mapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMap();
            }
        });
        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //edit order status : In process , Completed , Cancelled
                editOrderStatusDialog();
            }
        });
    }

    private void editOrderStatusDialog() {
        //option to display dialog
        String[] options = {"In Process","Completed","Cancelled"};
        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Order Status")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        //handle item clicks
                        String selectedOption = options[i];
                        editOrderStatus(selectedOption);
                    }
                })
                .show();
    }

    private void editOrderStatus(String selectedOption) {
        //setup data to put in firebase db
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("orderStatus",""+selectedOption);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Orders").child(orderId).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //staus update
                        String message = "Order is now "+selectedOption;
                        Toast.makeText(OrderDetailsSellerActivity.this,message , Toast.LENGTH_SHORT).show();

                        prepareNotificationMessage(orderId,message);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(OrderDetailsSellerActivity.this, " "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void openMap() {
        String address = "https://maps.google.com/maps?addr="+ sourceLatitude + "," + sourceLongitude + "&addr=" + destinationLatitude + "," + sourceLongitude;
        Intent intent =  new Intent(Intent.ACTION_VIEW , Uri.parse(address));
        startActivity(intent);
    }

    private void loadMyinfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users");
        ref.child(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        sourceLatitude = ""+snapshot.child("latitude").getValue();
                        sourceLongitude = ""+snapshot.child("longitude").getValue();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
    private void loadBuyerinfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get buyer info
                        destinationLatitude = ""+snapshot.child("latitude").getValue();
                        destinationLongitude = ""+snapshot.child("longitude").getValue();
                        String email = ""+snapshot.child("email").getValue();
                        String phone = ""+snapshot.child("phone").getValue();
                        deliveryFee = ""+snapshot.child("deliveryFee").getValue();

                        //set info
                        emailTv.setText(email);
                        phoneTv.setText(phone);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
    private void loadOrderDetails() {
        //load detailed info of this order , based on order id
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Orders").child(orderId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get order info
                        String orderBy = ""+snapshot.child("orderBy").getValue();
                        String orderCost = ""+snapshot.child("orderCost").getValue();
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


                        //convert timestamp
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(Long.parseLong(orderTime));
                        String dateFormated = DateFormat.format("dd/MM/yyyy",calendar).toString();

                        //change order statuse text color
                        if (orderStatus.equals("In Process")){
                            orderStatusTv.setTextColor(getResources().getColor(R.color.colorPrimary));
                        }
                        else if (orderStatus.equals("Completed")){
                            orderStatusTv.setTextColor(getResources().getColor(R.color.colorGreen));
                        }
                        else if (orderStatus.equals("Cancelled")){
                            orderStatusTv.setTextColor(getResources().getColor(R.color.colorRed));
                        }

                        //set data
                        orderIdTv.setText(orderId);
                        orderStatusTv.setText(orderStatus);
                        amountTv.setText("$"+orderCost+" [Including delivery fee $"+deliveryFee+" "+discount+" ]");
                        dateTv.setText(dateFormated);

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

    private void loadOrderItems() {
        //init list
        modelOrderItemArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Orders").child(orderId).child("Items")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        modelOrderItemArrayList.clear();
                        for(DataSnapshot ds: snapshot.getChildren()){
                            ModelOrderItem modelOrderItem = ds.getValue(ModelOrderItem.class);
                            //add to list
                            modelOrderItemArrayList.add(modelOrderItem);
                        }
                        //all items added to list
                        //setup adapter
                        adapterOrderItem = new AdapterOrderItem(OrderDetailsSellerActivity.this,modelOrderItemArrayList);
                        //set adapter
                        itemsRv.setAdapter(adapterOrderItem);

                        //set items count
                        totalItemsTv.setText(""+snapshot.getChildrenCount());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    public void prepareNotificationMessage(String orderId , String message) {

        String NOTIFICATION_TOPIC = "/topics/"+ Constans.FCM_TOPIC;
        String NOTIFICATION_TITLE = "Your Order "+orderId;
        String NOTIFICATION_MESSAGE = ""+message;
        String NOTIFICATION_TYPE = "OrderStatusChanged";


        JSONObject notificationJo = new JSONObject();
        JSONObject notificationBodyJo = new JSONObject();
        try {
            //what to send
            notificationBodyJo.put("notificationType",NOTIFICATION_TYPE);
            notificationBodyJo.put("buyerUid",orderBy);
            notificationBodyJo.put("sellerUid",firebaseAuth.getUid());
            notificationBodyJo.put("orderId",orderId);
            notificationBodyJo.put("notificationTitle",NOTIFICATION_TITLE);
            notificationBodyJo.put("notificationMessage",NOTIFICATION_MESSAGE);

            //where to send
            notificationJo.put("to",NOTIFICATION_TOPIC);
            notificationJo.put("data",notificationBodyJo);

        } catch (Exception e) {
            Toast.makeText(OrderDetailsSellerActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        sendFcmNotification(notificationJo);

    }

    private void sendFcmNotification(JSONObject notificationJo ) {
        //send volley request
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest("https://fcm.googleapis.com/fcm/send", notificationJo, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                //notification send

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //notificationn failed


            }
        }){
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                //put required holders
                Map<String , String> headers = new HashMap<>();
                headers.put("Content-type","application/json");
                headers.put("Authorization","key="+Constans.FCM_KEY);

                return headers;
            }
        };

        //enqueue the volley request
        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }

}