package com.example.grocery.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.grocery.Constans;
import com.example.grocery.R;
import com.example.grocery.adapters.AdapterCartitem;
import com.example.grocery.adapters.AdapterProductSeller;
import com.example.grocery.adapters.AdapterProductUser;
import com.example.grocery.adapters.AdapterReview;
import com.example.grocery.models.ModeProduct;
import com.example.grocery.models.ModelCartitem;
import com.example.grocery.models.ModelReview;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;
import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import p32929.androideasysql_library.Column;
import p32929.androideasysql_library.EasyDB;
import p32929.androideasysql_library.EasyDB;

public class ShopDetailsActivity extends AppCompatActivity {

    //delete ui views
    public ImageView shopIv;
    public TextView shopNameTv,phoneTv,emailTv,openCloseTv,deliveryFeeTv,addressTv,filteredProductsTv,cartCountTv , promoDescriptionTv ,discountTv;
    public ImageButton callBtn,mapBtn,cartBtn,backBtn,filterProductBtn , reviewBtn ;
    public EditText searchProductEt , promoCodeEt;
    public RecyclerView productsRv , ordersRv;
    public RatingBar ratingBar;
    public Button applyBtn;

    private String shopUid , myLatitude , myLongitude , shopLatitude , shopLongitude ,  shopName , shopEmail, shopPhone , shopAddress,myphone;
    public String deliveryFee;
    private FirebaseAuth firebaseAuth;

    //process dialog
    private ProgressDialog progressDialog;

    private ArrayList<ModeProduct> productList;
    private AdapterProductUser adapterProductUser;

    //cart
    private ArrayList<ModelCartitem> cartitemList;
    private AdapterCartitem adapterCartitem;

    private EasyDB easyDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_details);

        shopIv = findViewById(R.id.shopIv);
        shopNameTv = findViewById(R.id.shopNameTv);


        phoneTv = findViewById(R.id.phoneTv);
        emailTv = findViewById(R.id.emailTv);
        openCloseTv = findViewById(R.id.openCloseTv);
        deliveryFeeTv = findViewById(R.id.deliveryFeeTv);
        addressTv = findViewById(R.id.addressTv);
        filteredProductsTv = findViewById(R.id.filteredProductsTv);
        cartCountTv = findViewById(R.id.cartCountTv);
        callBtn = findViewById(R.id.callBtn);
        mapBtn = findViewById(R.id.mapBtn);
        cartBtn = findViewById(R.id.cartBtn);
        backBtn = findViewById(R.id.backBtn);
        filterProductBtn = findViewById(R.id.filterProductBtn);
        productsRv = findViewById(R.id.productsRv);
        searchProductEt = findViewById(R.id.searchProductEt);
        reviewBtn = findViewById(R.id.reviewBtn);
        ratingBar = findViewById(R.id.ratingBar);
        ordersRv = findViewById(R.id.ordersRv);

        //get uid of the shop from intent
        shopUid = getIntent().getStringExtra("shopUid");

        firebaseAuth = FirebaseAuth.getInstance();

        //init progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait.");
        progressDialog.setCanceledOnTouchOutside(false);

        loadMyInfo();
        loadShopDetails();
        loadShopProducts();
        loadReviews(); // avg rating , set on ratingBar

        easyDB = EasyDB.init(this,"ITEMS_DB")
                .setTableName("ITEMS_TABLE")
                .addColumn(new Column("Item_Id",new String[]{"text","uniqe"}))
                .addColumn(new Column("Item_PID",new String[]{"text","not null"}))
                .addColumn(new Column("Item_Name",new String[]{"text","not null"}))
                .addColumn(new Column("Item_Price_Each",new String[]{"text","not null"}))
                .addColumn(new Column("Item_Price",new String[]{"text","not null"}))
                .addColumn(new Column("Item_Quantity",new String[]{"text","not null"}))
                .doneTableColumn();

        //eatch shop have its own products and orders so if user add items to cart and go back and open cart in different shop than cart should be different
        //so delete cart data whenever user open this activity
        deleteCartData();
        cartCount();
        searchProductEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    adapterProductUser.getFilter().filter(s);
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //go previous activity
                onBackPressed();
            }
        });
        cartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show cart dialog
                showCartDialog();
            }
        });
        callBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialPhone();
            }
        });
        mapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMap();
            }
        });
        filterProductBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(ShopDetailsActivity.this);
                    builder.setTitle("Choose Category:")
                            .setItems(Constans.productCategories1, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //get selected item
                                    String selected = Constans.productCategories1[which];
                                    filteredProductsTv.setText(selected);
                                    if(selected.equals("All")){
                                        //load all
                                        loadShopProducts();
                                    }
                                    else
                                    {
                                        //load filtered
                                        adapterProductUser.getFilter().filter(selected);
                                    }
                                }
                            })
                            .show();
            }
        });
        //handle reviweBtn click , open review activity
        reviewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //pass shop uid to show its reviews
                Intent intent = new Intent(ShopDetailsActivity.this,ShopReviewsActivity.class);
                intent.putExtra("shopUid",shopUid);
                startActivity(intent);
            }
        });

    }

    private float ratingSum = 0;
    private void loadReviews() {
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

                        ratingBar.setRating(avgRating);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    public void cartCount(){
        //we have to access this in adapter
        //get cart count
        int count = easyDB.getAllData().getCount();
        if(count <= 0){
            //No item in cart , hide cart count
            cartCountTv.setVisibility(View.GONE);
        }
        else{
            //have items in cart count textview and set count
            cartCountTv.setVisibility(View.VISIBLE);
            cartCountTv.setText(""+count);
        }
    }

    private void priceWithDiscount() {
        discountTv.setText("$"+promoPrice);
        deliveryFeeTv.setText("$"+deliveryFee);
        sTotalTv.setText("$"+String.format("%.2f",allTotalPrice));
        allTotalPriceTv.setText("$"+(allTotalPrice+Double.parseDouble(deliveryFee.replace("$",""))-Double.parseDouble(promoPrice)));
    }

    private void priceWithoutDiscount(){
        discountTv.setText("$0");
        deliveryFeeTv.setText("$"+deliveryFee);
        sTotalTv.setText("$"+String.format("%.2f",allTotalPrice));
        allTotalPriceTv.setText("$"+(allTotalPrice+Double.parseDouble(deliveryFee.replace("$",""))));
    }

    public boolean isPromoCodeApplicable= false;
    public String promoId , promoTimestamp , promoCode , promoDescription , promoExpDate , promoPrice , promoMinimumOrderPrice;
    private void checkCodeActivity(String promotionCode){
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait");
        progressDialog.setMessage("Checking promo code...");
        progressDialog.setCanceledOnTouchOutside(false);

        isPromoCodeApplicable = false;
        applyBtn.setText("Apply");

        priceWithoutDiscount();

        //check promo code avalibility
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(shopUid).child("Promotions").orderByChild("promoCode").equalTo(promotionCode)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //check if promo code exists
                        if (snapshot.exists()){
                            //promo code exists
                            progressDialog.dismiss();
                            for (DataSnapshot ds : snapshot.getChildren()){
                                promoId = ""+ds.child("id").getValue();
                                promoTimestamp = ""+ds.child("timestamp").getValue();
                                promoCode = ""+ds.child("promoCode").getValue();
                                promoDescription = ""+ds.child("description").getValue();
                                promoExpDate = ""+ds.child("expireDate").getValue();
                                promoMinimumOrderPrice = ""+ds.child("minimumOrderPrice").getValue();
                                promoPrice = ""+ds.child("promoPrice").getValue();

                                //now check if code is expired or not
                                checkCodeExpireDate();

                            }
                        }
                        else {
                            //entered promo code doesn't exists
                            progressDialog.dismiss();
                            Toast.makeText(ShopDetailsActivity.this, "Invalid promo Code.", Toast.LENGTH_SHORT).show();
                            applyBtn.setVisibility(View.GONE);
                            promoDescriptionTv.setVisibility(View.GONE);
                            promoDescriptionTv.setText("");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void checkCodeExpireDate() {
        //get current date
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        String todayDate = day+"/"+month+"/"+year;

        try {
            SimpleDateFormat sdFormat = new SimpleDateFormat("dd/MM/yyyy");
            Date currentDate = sdFormat.parse(todayDate);
            Date expireDate = sdFormat.parse(promoExpDate);
            //compaire date
            if (expireDate.compareTo(currentDate) > 0) {
                checkMinimumOrderPrice();
            }
            else if (expireDate.compareTo(currentDate) < 0) {
                Toast.makeText(ShopDetailsActivity.this, "Sorry but your promo Code expire on "+promoExpDate, Toast.LENGTH_SHORT).show();
                applyBtn.setVisibility(View.GONE);
                promoDescriptionTv.setVisibility(View.GONE);
                promoDescriptionTv.setText("");
            }
            else if (expireDate.compareTo(currentDate) == 0) {
                checkMinimumOrderPrice();
            }
        } catch (Exception e) {
            Toast.makeText(ShopDetailsActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            applyBtn.setVisibility(View.GONE);
            promoDescriptionTv.setVisibility(View.GONE);
            promoDescriptionTv.setText("");
        }
    }

    private void checkMinimumOrderPrice() {
        if (Double.parseDouble(String.format("%.2f", allTotalPrice)) < Double.parseDouble(promoMinimumOrderPrice)) {
            Toast.makeText(ShopDetailsActivity.this, "This code is valid for order with minimum amount : $" + promoMinimumOrderPrice, Toast.LENGTH_SHORT).show();
            applyBtn.setVisibility(View.GONE);
            promoDescriptionTv.setVisibility(View.GONE);
            promoDescriptionTv.setText("");
        } else {
            applyBtn.setVisibility(View.VISIBLE);
            promoDescriptionTv.setVisibility(View.VISIBLE);
            promoDescriptionTv.setText(promoDescription);
        }
    }

    private void deleteCartData() {
        //declare it to class level and init in onCreate
        easyDB.deleteAllDataFromTable();//delete all records from cart
    }

    public double allTotalPrice = 0.00;
    //need to access views in adapter so making public
    public TextView sTotalTv,dFeeTv,allTotalPriceTv;

    private void showCartDialog() {
        //init list
        cartitemList = new ArrayList<>();

        //inflate cart layout
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_cart,null);
        //init views
        TextView shopNameTv = view.findViewById(R.id.shopNameTv);
        RecyclerView cartItemRv = view.findViewById(R.id.cartItemRv);
        sTotalTv = view.findViewById(R.id.sTotalTv);
        dFeeTv = view.findViewById(R.id.dFeeTv);
        allTotalPriceTv = view.findViewById(R.id.totalTv);
        Button checkoutBtn = view.findViewById(R.id.checkoutBtn);
        promoDescriptionTv = view.findViewById(R.id.promoDescriptionTv);
        promoCodeEt = view.findViewById(R.id.promoCodeEt);
        applyBtn = view.findViewById(R.id.applyBtn);
        FloatingActionButton validateBtn = view.findViewById(R.id.validateBtn);
        discountTv = view.findViewById(R.id.discountTv);

        if (isPromoCodeApplicable) {
            applyBtn.setVisibility(View.VISIBLE);
            promoDescriptionTv.setVisibility(View.VISIBLE);
            promoDescriptionTv.setText(promoDescription);
            applyBtn.setText("Applied");
            promoCodeEt.setText(promoCode);
        } else {
            applyBtn.setVisibility(View.GONE);
            promoDescriptionTv.setVisibility(View.GONE);
            applyBtn.setText("Apply");
        }

        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //set view to dialog
        builder.setView(view);

        shopNameTv.setText(shopName);

        EasyDB easyDB = EasyDB.init(this,"ITEMS_DB")
                .setTableName("ITEMS_TABLE")
                .addColumn(new Column("Item_Id",new String[]{"text","uniqe"}))
                .addColumn(new Column("Item_PID",new String[]{"text","not null"}))
                .addColumn(new Column("Item_Name",new String[]{"text","not null"}))
                .addColumn(new Column("Item_Price_Each",new String[]{"text","not null"}))
                .addColumn(new Column("Item_Price",new String[]{"text","not null"}))
                .addColumn(new Column("Item_Quantity",new String[]{"text","not null"}))
                .doneTableColumn();

        //get all record from db
        Cursor res = easyDB.getAllData();
        while (res.moveToNext()){
            String id = res.getString(1);
            String pId = res.getString(2);
            String name = res.getString(3);
            String price = res.getString(4);
            String cost = res.getString(5);
            String quantity = res.getString(6);

            allTotalPrice = allTotalPrice+Double.parseDouble(cost.replace("$",""));

            ModelCartitem modelCartitem = new ModelCartitem(
                    ""+id,
                    ""+pId,
                    ""+name,
                    ""+price,
                    ""+cost,
                    ""+quantity
            );

            cartitemList.add(modelCartitem);
        }

        //set adapter
        adapterCartitem = new AdapterCartitem(this,cartitemList);
        //set to recyclerview
        cartItemRv.setAdapter(adapterCartitem);

        if (isPromoCodeApplicable){
            priceWithDiscount();
        }
        else {
            priceWithoutDiscount();
        }

        dFeeTv.setText(""+deliveryFee);
        sTotalTv.setText("$"+String.format("%.2f",allTotalPrice));
        allTotalPriceTv.setText("$"+allTotalPrice);

        //show dialog
        AlertDialog dialog = builder.create();
        dialog.show();

        //reset total price on dialog dismiss
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                allTotalPrice = 0.00;
            }
        });


        //place order
        checkoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //first validate delivery address
                if(myLatitude.equals("") || myLatitude.equals("null") || myLatitude.equals("") || myLatitude.equals("null")){
                    //under didn't enter address in profile
                    Toast.makeText(ShopDetailsActivity.this, "Please enter your address in you profile before placing order...", Toast.LENGTH_SHORT).show();
                    return;//don't procede further
                }
                if(myphone.equals("") || myphone.equals("null")){
                    Toast.makeText(ShopDetailsActivity.this, "Please enter your Phone Number in you profile before placing order...", Toast.LENGTH_SHORT).show();
                    return;//don't procede further
                }
                if (cartitemList.size() == 0){
                    //cart list is empty
                    Toast.makeText(ShopDetailsActivity.this, "No item is cart", Toast.LENGTH_SHORT).show();
                    return; // don't procced further
                }

                submitOrder();
            }
        });

        validateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String promotionCode = promoCodeEt.getText().toString().trim();
                if (TextUtils.isEmpty(promotionCode)){
                    Toast.makeText(ShopDetailsActivity.this, "Please enter promo code..", Toast.LENGTH_SHORT).show();
                }
                else {
                    checkCodeActivity(promotionCode);
                }
            }
        });

        applyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isPromoCodeApplicable = true;
                applyBtn.setText("Applied");

                priceWithDiscount();
            }
        });

    }

    private void submitOrder() {
        //show progress dialog
        progressDialog.setMessage("Please order..");
        progressDialog.show();

        //for order id and order time
        String timestamp = ""+System.currentTimeMillis();

        String cost = allTotalPriceTv.getText().toString().trim().replace("$",""); // remove $ if contains

        //add latitude lonitude of user to each order | delete prvious orders from firebase or add manually to then

        //setup prder data
        HashMap<String,String > hashMap = new HashMap<>();
        hashMap.put("orderId",""+timestamp);
        hashMap.put("orderTime",""+timestamp);
        hashMap.put("orderStatus","In Process"); // In Progress / completed / cancelled
        hashMap.put("orderCost",""+cost);
        hashMap.put("orderBy",""+firebaseAuth.getUid());
        hashMap.put("orderTo",""+shopUid);
        hashMap.put("latitude",""+myLatitude);
        hashMap.put("longitude",""+myLongitude);
        if (isPromoCodeApplicable){
            hashMap.put("discount",promoPrice);
        }
        else {
            hashMap.put("discount","0");
        }

        //add to db
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(shopUid).child("Orders");
        ref.child(timestamp).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //order info added now add order items
                        for(int i=0;i<cartitemList.size();i++){
                            String id = cartitemList.get(i).getId();
                            String pId = cartitemList.get(i).getpId();
                            String name = cartitemList.get(i).getName();
                            String price = cartitemList.get(i).getPrice();
                            String cost = cartitemList.get(i).getCost();
                            String quantity = cartitemList.get(i).getQuantity();

                            HashMap<String,String > hashMap1 = new HashMap<>();
                            hashMap1.put("pId",pId);
                            hashMap1.put("name",name);
                            hashMap1.put("cost",cost);
                            hashMap1.put("price",price);
                            hashMap1.put("quantity",quantity);

                            ref.child(timestamp).child("Items").child(pId).setValue(hashMap1);
                        }

                        progressDialog.dismiss();
                        Toast.makeText(ShopDetailsActivity.this, "Order Placed Successfully...", Toast.LENGTH_SHORT).show();

                        prepareNotificationMessage(timestamp);

                        //after placing order open order details page

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Toast.makeText(ShopDetailsActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
    }//now lets show the placed orders, create  for recyclerview

    private void openMap() {
        String address = "https://maps.google.com/maps?addr="+ myLatitude + "," + myLongitude + "&addr=" + shopLatitude + "," + shopLongitude;
        Intent intent =  new Intent(Intent.ACTION_VIEW , Uri.parse(address));
        startActivity(intent);
    }

    private void dialPhone() {
        startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"+Uri.encode(shopPhone))));
        Toast.makeText(this, ""+shopPhone, Toast.LENGTH_SHORT).show();
    }

    private void loadMyInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get user data
                        for(DataSnapshot ds : snapshot.getChildren()){
                            String name = ""+ds.child("name").getValue();
                            String email = ""+ds.child("email").getValue();
                            myphone = ""+ds.child("phone").getValue();
                            String profileImage = ""+ds.child("profileImage").getValue();
                            String accountType = ""+ds.child("accountType").getValue();
                            String city = ""+ds.child("city").getValue();
                            myLatitude = ""+ds.child("latitude").getValue();
                            myLongitude = ""+ds.child("longitude").getValue();

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadShopDetails() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(shopUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = ""+snapshot.child("name").getValue();
                shopName = ""+snapshot.child("shopName").getValue();
                shopEmail = ""+snapshot.child("email").getValue();
                shopPhone = ""+snapshot.child("phone").getValue();
                shopAddress=""+snapshot.child("address").getValue();
                shopLatitude = ""+snapshot.child("latitude").getValue();
                shopLongitude = ""+snapshot.child("longitude").getValue();
                deliveryFee = ""+snapshot.child("deliveryFee").getValue();
                String profileImage = ""+snapshot.child("profileImage").getValue();
                String shopOpen = ""+snapshot.child("shopOpen").getValue();

                //set Data
                shopNameTv.setText(shopName);
                emailTv.setText(shopEmail);
                deliveryFeeTv.setText("Delivery Fee: $"+deliveryFee);
                addressTv.setText(shopAddress);
                phoneTv.setText(shopPhone);
                if (shopOpen.equals("true")){
                    openCloseTv.setText("Open");
                }
                else {
                    openCloseTv.setText("Closed");
                }
                try{
                    Picasso.get().load(profileImage).into(shopIv);
                }
                catch (Exception e){

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadShopProducts() {
        //init list
        productList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(shopUid).child("Products")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //clear list before adding items
                        productList.clear();
                        for (DataSnapshot ds: snapshot.getChildren() ){
                            ModeProduct modeProduct = ds.getValue(ModeProduct.class);
                            productList.add(modeProduct);
                        }
                        //setuo adapter
                        adapterProductUser = new AdapterProductUser(ShopDetailsActivity.this,productList);
                        //set adapter
                        productsRv.setAdapter(adapterProductUser);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    public void prepareNotificationMessage(String orderId) {

        String NOTIFICATION_TOPIC = "/topics/"+Constans.FCM_TOPIC;
        String NOTIFICATION_TITLE = "New Order "+orderId;
        String NOTIFICATION_MESSAGE = "Congratulation.. ! You have new order.";
        String NOTIFICATION_TYPE = "NewOrder";


        JSONObject notificationJo = new JSONObject();
        JSONObject notificationBodyJo = new JSONObject();
        try {
            //what to send
            notificationBodyJo.put("notificationType",NOTIFICATION_TYPE);
            notificationBodyJo.put("buyerUid",firebaseAuth.getUid());
            notificationBodyJo.put("sellerUid",shopUid);
            notificationBodyJo.put("orderId",orderId);
            notificationBodyJo.put("notificationTitle",NOTIFICATION_TITLE);
            notificationBodyJo.put("notificationMessage",NOTIFICATION_MESSAGE);

            //where to send
            notificationJo.put("to",NOTIFICATION_TOPIC);
            notificationJo.put("data",notificationBodyJo);

        } catch (Exception e) {
            Toast.makeText(ShopDetailsActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        sendFcmNotification(notificationJo , orderId);

    }

    private void sendFcmNotification(JSONObject notificationJo, String orderId) {
        //send volley request
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest("https://fcm.googleapis.com/fcm/send", notificationJo, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                //after sending fcm start oder detailes activity
                Intent intent = new Intent(ShopDetailsActivity.this, OrderDetailsUsersActivity.class);
                intent.putExtra("orderTo",shopUid);
                intent.putExtra("orderId",orderId);
                startActivity(intent); // now get these values through intent on OrderDetailsUserActivity


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //if faied sending fcm , still start order detailes activity
                Intent intent = new Intent(ShopDetailsActivity.this, OrderDetailsUsersActivity.class);
                intent.putExtra("orderTo",shopUid);
                intent.putExtra("orderId",orderId);
                startActivity(intent); // now get these values through intent on OrderDetailsUserActivity

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