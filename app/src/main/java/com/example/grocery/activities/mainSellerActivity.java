package com.example.grocery.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.grocery.adapters.AdapterOrderShop;
import com.example.grocery.adapters.AdapterProductSeller;
import com.example.grocery.Constans;
import com.example.grocery.models.ModeProduct;
import com.example.grocery.R;
import com.example.grocery.models.ModelOrderShop;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

public class mainSellerActivity extends AppCompatActivity {

    private TextView nameTv , shopNameTv , emailTv , tabProductsTv , tabOrdersTv , filteredProductsTv , filteredOrdersTv ;
    private EditText searchProductEt;
    private ImageButton logoutBtn ,editProfileBtn , addProductBtn,filterProductBtn , filterOrderBtn, moreBtn ;
    private ImageView profileIv;
    private RelativeLayout productsRl,ordersRl;
    private RecyclerView productsRv,ordersRv;

    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    private ArrayList<ModeProduct> productList;
    private AdapterProductSeller adapterProductSeller;

    private ArrayList<ModelOrderShop> modelOrderShopArrayList;
    private AdapterOrderShop adapterOrderShop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_seller);

        nameTv = findViewById(R.id.nameTv);
        logoutBtn = findViewById(R.id.logoutBtn);
        editProfileBtn = findViewById(R.id.editProfileBtn);
        addProductBtn = findViewById(R.id.editProductBtn);
        emailTv = findViewById(R.id.emailTv);
        tabProductsTv = findViewById(R.id.tabProductsTv);
        tabOrdersTv = findViewById(R.id.tabOrdersTv);
        profileIv = findViewById(R.id.profileIv);
        shopNameTv = findViewById(R.id.shopNameTv);
        searchProductEt = findViewById(R.id.searchProductEt);
        filterProductBtn = findViewById(R.id.filterProductBtn);
        filteredOrdersTv = findViewById(R.id.filteredOrdersTv);
        filterOrderBtn = findViewById(R.id.filterOrderBtn);
        moreBtn = findViewById(R.id.moreBtn);
        filteredProductsTv = findViewById(R.id.filteredProductsTv);

        productsRl = findViewById(R.id.productsRl);
        ordersRl = findViewById(R.id.ordersRl);
        productsRv = findViewById(R.id.productsRv);
        ordersRv = findViewById(R.id.ordersRv);

        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait");
        progressDialog.setCanceledOnTouchOutside(false);
        checkUser();
        loadAllProducts();
        loadAllOrders();

        showProductsUI();

        //search
        searchProductEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    adapterProductSeller.getFilter().filter(s);
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeMeOffline();
            }
        });

        editProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //open edit  profile activity
                startActivity(new Intent(mainSellerActivity.this , ProfileEditSallerActivity.class));
            }
        });

        addProductBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //open edit product activity
                startActivity(new Intent(mainSellerActivity.this,AddProductActivity.class));

            }
        });

        tabProductsTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //load products
                showProductsUI();

            }
        });

        tabOrdersTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //load Orders
                showOrdersUI();

            }
        });

        filterProductBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mainSellerActivity.this);
                builder.setTitle("Choose Category:")
                        .setItems(Constans.productCategories1, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //get selected item
                                String selected = Constans.productCategories1[which];
                                filteredProductsTv.setText(selected);
                                if(selected.equals("All")){
                                    //load all
                                    loadAllProducts();
                                }
                                else
                                {
                                    //load filtered
                                    loadFilteredProducts(selected);
                                }
                            }
                        })
                .show();
            }
        });

        filterOrderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //option to display in dialog
                String[] options = {"All","In Process","Completed","Cancelled"};
                //dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(mainSellerActivity.this);
                builder.setTitle("Filter Orders:")
                        .setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //handle item clicks
                                if(which == 0){
                                    //All clicked
                                    filteredOrdersTv.setText("Showing All Orders");
                                    adapterOrderShop.getFilter().filter(""); //show all orders
                                }
                                else {
                                    String optionClicked = options[which];
                                    filteredOrdersTv.setText("Showing "+optionClicked+" Orders"); // e.g showing completed orders
                                    adapterOrderShop.getFilter().filter(optionClicked);

                                }
                            }
                        })
                .show();
            }
        });


        //popup menu
        PopupMenu  popupMenu = new PopupMenu(mainSellerActivity.this , moreBtn);
        //add menu items to our menu
        popupMenu.getMenu().add("Settings");
        popupMenu.getMenu().add("Reviews");
        popupMenu.getMenu().add("Promotion Codes");

        //handle menu item click
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getTitle() == "Settings") {
                    startActivity(new Intent(mainSellerActivity.this , SettingsActivity.class));
                }
                else if (item.getTitle() == "Reviews") {
                    //open same reviews activity as used in user main page
                    Intent intent = new Intent(mainSellerActivity.this , ShopReviewsActivity.class);
                    intent.putExtra("shopUid",""+firebaseAuth.getUid());
                    startActivity(intent);
                }
                else if (item.getTitle() == "Promotion Codes") {
                    //start promotion list screen
                    startActivity(new Intent(mainSellerActivity.this , PromotionCodesActivity.class));

                }
                return true;
            }
        });

        //show more options//Settings , Reviews , promotion code
        moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show pop up menu
                popupMenu.show();
            }
        });
    }

    private void loadAllOrders() {
        //init array list
        modelOrderShopArrayList = new ArrayList<>();

        //load orders of shop
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Orders")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //clear list before adding new data in it
                        modelOrderShopArrayList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()){
                            ModelOrderShop modelOrderShop = ds.getValue(ModelOrderShop.class);
                            //add to list
                            modelOrderShopArrayList.add(modelOrderShop);
                        }
                        //setup adapter
                        adapterOrderShop = new AdapterOrderShop(mainSellerActivity.this,modelOrderShopArrayList);
                        //set adapter to recyclerview
                        ordersRv.setAdapter(adapterOrderShop);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadFilteredProducts(String selected) {
        productList = new ArrayList<>();

        //get all products
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Products")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //before getting reset list
                        for(DataSnapshot ds: snapshot.getChildren()){

                            String productCategory = ""+ds.child("productCategory").getValue();

                            //if selected category matches product category the add in list
                            if(selected.equals(productCategory)){
                                ModeProduct modeProduct = ds.getValue(ModeProduct.class);
                                productList.add(modeProduct);
                            }


                        }
                        //setup adapter
                        adapterProductSeller = new AdapterProductSeller(mainSellerActivity.this,productList);
                        //set adapter
                        productsRv.setAdapter(adapterProductSeller);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadAllProducts() {
        productList = new ArrayList<>();

        //get all products
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Products")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //before getting reset list
                        for(DataSnapshot ds: snapshot.getChildren()){
                            ModeProduct modeProduct = ds.getValue(ModeProduct.class);
                            productList.add(modeProduct);

                        }
                        //setup adapter
                        adapterProductSeller = new AdapterProductSeller(mainSellerActivity.this,productList);
                        //set adapter
                        productsRv.setAdapter(adapterProductSeller);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    private void showOrdersUI() {
        //show orders ui and hide products ui
        ordersRl.setVisibility(View.VISIBLE);
        productsRl.setVisibility(View.GONE);

        tabProductsTv.setTextColor(getResources().getColor(R.color.colorWhite));
        tabProductsTv.setBackgroundColor(getResources().getColor(android.R.color.transparent));

        tabOrdersTv.setTextColor(getResources().getColor(R.color.colorBlack));
        tabOrdersTv.setBackgroundResource(R.drawable.shape_rect04);
    }

    private void showProductsUI() {
        //show products ui and hide orders ui
        productsRl.setVisibility(View.VISIBLE);
        ordersRl.setVisibility(View.GONE);

        tabProductsTv.setTextColor(getResources().getColor(R.color.colorBlack));
        tabProductsTv.setBackgroundResource(R.drawable.shape_rect04);

        tabOrdersTv.setTextColor(getResources().getColor(R.color.colorWhite));
        tabOrdersTv.setBackgroundColor(getResources().getColor(android.R.color.transparent));

    }

    private void makeMeOffline() {

        progressDialog.setMessage("Logging Out...");

        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("online","false");

        //update value to db
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //update successfully
                        firebaseAuth.signOut();
                        checkUser();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed updating
                        progressDialog.dismiss();
                        Toast.makeText(mainSellerActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkUser() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if(user == null){
            startActivity(new Intent(mainSellerActivity.this,LoginActivity.class));
            finish();
        }
        else
        {
            loadMyInfo();
        }
    }

    private void loadMyInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot ds : snapshot.getChildren()){
                            //get data from database
                            String name = ""+ds.child("name").getValue();
                            String accountType = ""+ds.child("accountType").getValue();
                            String email = ""+ds.child("email").getValue();
                            String shopName = ""+ds.child("shopName").getValue();
                            String profileImage = ""+ds.child("profileImage").getValue();

                            //set data in ui
                            nameTv.setText(name);
                            shopNameTv.setText(shopName);
                            emailTv.setText(email);
                            try {
                                Picasso.get().load(profileImage).placeholder(R.drawable.ic_store_gray).into(profileIv);
                            }
                            catch (Exception e){
                                profileIv.setImageResource(R.drawable.ic_store_gray);
                            }

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }


}