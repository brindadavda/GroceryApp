package com.example.grocery.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.grocery.R;
import com.example.grocery.adapters.AdapterPromotionShop;
import com.example.grocery.models.ModelPromotion;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class PromotionCodesActivity extends AppCompatActivity {

    private ImageButton backBtn,addPromotionBtn,filterBtn;
    private TextView filteredTv;
    private RecyclerView promoRv;

    private FirebaseAuth firebaseAuth;

    private ArrayList<ModelPromotion> promotionArrayList;
    private AdapterPromotionShop adapterPromotionShop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_promotion_codes);

        backBtn = findViewById(R.id.backBtn);
        addPromotionBtn = findViewById(R.id.addPromotionBtn);
        filterBtn = findViewById(R.id.filterBtn);
        filteredTv = findViewById(R.id.filteredTv);
        promoRv = findViewById(R.id.promoRv);

        firebaseAuth = FirebaseAuth.getInstance();
        loadAllPromoCodes();

        //handle click , go back
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        //handle click open add promotion activity
        addPromotionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(PromotionCodesActivity.this , AddPromotionCodesActivity.class));
            }
        });

        //handel filter button click  , show filter dialog
        filterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterDialog();
            }
        });
    }

    private void filterDialog() {
         //options to display dialog
        String[] options = {"All","Expired","Not Expired"};
        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Filter Promotion Code")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        //handle item click
                        if (i == 0) {
                            //All Clicked
                            filteredTv.setText("All Promotion Codes");
                            loadAllPromoCodes();

                        } else if (i == 1) {
                            //Expire Clicked
                            filteredTv.setText("Expired Promotion Codes");
                            loadExpiredPromoCodes();

                        } else if (i == 2) {
                            filteredTv.setText("Not Expired Promotion Codes");
                            loadnotExpiredPromoCodes();

                        }
                    }
                }).show();
    }

    private void loadAllPromoCodes() {
        //init list
        promotionArrayList = new ArrayList<>();

        //db referance user > current user > promotions > codes data
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Promotions")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //clear list before adding data
                        promotionArrayList.clear();

                        for (DataSnapshot ds : snapshot.getChildren()) {
                            ModelPromotion modelPromotion = ds.getValue(ModelPromotion.class);
                            //add to list
                            promotionArrayList.add(modelPromotion);
                        }
                        //setup adapter , add list to adapter
                        adapterPromotionShop = new AdapterPromotionShop(PromotionCodesActivity.this , promotionArrayList);
                        promoRv.setAdapter(adapterPromotionShop);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    private void loadExpiredPromoCodes() {
        //get current date
        DecimalFormat mFormat = new DecimalFormat("00");
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        String todayDate = day+"/"+month+"/"+year;

        //init list
        promotionArrayList = new ArrayList<>();

        //db referance user > current user > promotions > codes data
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Promotions")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //clear list before adding data
                        promotionArrayList.clear();

                        for (DataSnapshot ds : snapshot.getChildren()) {
                            ModelPromotion modelPromotion = ds.getValue(ModelPromotion.class);

                            String expDate =  modelPromotion.getExpireDate();

                            /*---check for expire---*/
                            try {
                                SimpleDateFormat sdFormate = new SimpleDateFormat("dd/MM/yyyy");
                                Date currentDate = sdFormate.parse(todayDate);
                                Date expireDate = sdFormate.parse(expDate);
                                if (expireDate.compareTo(currentDate) > 0) {
                                    //date 1 occure after date 2
                                    //add to list
                                    promotionArrayList.add(modelPromotion);
                                }
                                else if (expireDate.compareTo(currentDate) < 0) {
                                    //date 1 ocuure before date 2
                                }
                                else if (expireDate.compareTo(currentDate) == 0) {
                                    //both date equals
                                    //add to list
                                    promotionArrayList.add(modelPromotion);
                                }
                            } catch (Exception e) {

                            }
                            //add to list
                            promotionArrayList.add(modelPromotion);
                        }
                        //setup adapter , add list to adapter
                        adapterPromotionShop = new AdapterPromotionShop(PromotionCodesActivity.this , promotionArrayList);
                        promoRv.setAdapter(adapterPromotionShop);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    private void loadnotExpiredPromoCodes() {
        //get current date
        DecimalFormat mFormat = new DecimalFormat("00");
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        String todayDate = day+"/"+month+"/"+year;

        //init list
        promotionArrayList = new ArrayList<>();

        //db referance user > current user > promotions > codes data
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Promotions")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //clear list before adding data
                        promotionArrayList.clear();

                        for (DataSnapshot ds : snapshot.getChildren()) {
                            ModelPromotion modelPromotion = ds.getValue(ModelPromotion.class);

                            String expDate =  modelPromotion.getExpireDate();

                            /*---check for expire---*/
                            try {
                                SimpleDateFormat sdFormate = new SimpleDateFormat("dd/MM/yyyy");
                                Date currentDate = sdFormate.parse(todayDate);
                                Date expireDate = sdFormate.parse(expDate);
                                if (expireDate.compareTo(currentDate) > 0) {
                                    //date 1 occure after date 2
                                    //add to list
                                    promotionArrayList.add(modelPromotion);
                                }
                                else if (expireDate.compareTo(currentDate) < 0) {
                                    //date 1 ocuure before date 2
                                }
                                else if (expireDate.compareTo(currentDate) == 0) {
                                    //both date equals
                                }
                            } catch (Exception e) {

                            }
                            //add to list
                            promotionArrayList.add(modelPromotion);
                        }
                        //setup adapter , add list to adapter
                        adapterPromotionShop = new AdapterPromotionShop(PromotionCodesActivity.this , promotionArrayList);
                        promoRv.setAdapter(adapterPromotionShop);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

}