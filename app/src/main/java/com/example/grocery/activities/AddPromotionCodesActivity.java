package com.example.grocery.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.grocery.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.HashMap;

public class AddPromotionCodesActivity extends AppCompatActivity {

    ImageButton backBtn , addPromotionBtn;
    EditText promoCodeEt , promoDescriptionEt,promoPriceEt,minimumPriceEt ;
    TextView expireDateTv, titleTv;
    Button addBtn;

    //firebase auth
    FirebaseAuth firebaseAuth;
    //progress dialog
    ProgressDialog progressDialog;


    private String promoId;

    private boolean isUpdating = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_promotion_codes);

        backBtn = findViewById(R.id.backBtn);
        addPromotionBtn = findViewById(R.id.addPromotionBtn);
        promoCodeEt = findViewById(R.id.promoCodeEt);
        promoDescriptionEt = findViewById(R.id.promoDescriptionEt);
        promoPriceEt = findViewById(R.id.promoPriceEt);
        minimumPriceEt = findViewById(R.id.minimumPriceEt);
        expireDateTv = findViewById(R.id.expireDateTv);
        titleTv = findViewById(R.id.titleTv);
        addBtn = findViewById(R.id.addBtn);

        //init firebase
        firebaseAuth = FirebaseAuth.getInstance();
        //init / setup progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait");
        progressDialog.setCanceledOnTouchOutside(false);

        //get promo Id from intent
        Intent intent = getIntent();
        if (intent.getStringExtra("promoId") != null) {
            //come here from adapter to update the record
            promoId = intent.getStringExtra("promoId");

            titleTv.setText("Update Promotion Code");
            addBtn.setText("Update");

            isUpdating = true;

            loadPromoInfo(); // load promo code to set our views ,so we can also update single value
        } else {
            //come from promo codes list activity to add new promo code
            titleTv.setText("Add Promotion Code");
            addBtn.setText("Add");

            isUpdating = false;
        }

        //handle click , go back
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        //handle click , pick data
        expireDateTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePickDialog();
            }
        });

        //handle click , add promotion to firebase db
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputData();
            }
        });
    }

    private void loadPromoInfo() {
        //db path to promo code
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Promotions").child(promoId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get info of promo code
                        String id = ""+snapshot.child("id").getValue();
                        String timestamp = ""+snapshot.child("timestamp").getValue();
                        String description = ""+snapshot.child("description").getValue();
                        String promoCode = ""+snapshot.child("promoCode").getValue();
                        String promoPrice = ""+snapshot.child("promoPrice").getValue();
                        String minimumOrderPrice = ""+snapshot.child("minimumOrderPrice").getValue();
                        String expireDate = ""+snapshot.child("expireDate").getValue();

                        promoCodeEt.setText(promoCode);
                        promoDescriptionEt.setText(description);
                        promoPriceEt.setText(promoPrice);
                        minimumPriceEt.setText(minimumOrderPrice);
                        expireDateTv.setText(expireDate);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    public void datePickDialog() {
        //Get current date to set on calender
        Calendar c = Calendar.getInstance();
        int mYear = c.get(Calendar.YEAR);
        int mMonth = c.get(Calendar.MONTH);
        int mDay = c.get(Calendar.DAY_OF_MONTH);

        Log.d("hi there:",""+mMonth);

        //date pick dialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

                DecimalFormat mFormat = new DecimalFormat("00");
                String pDay = mFormat.format(dayOfMonth);
                String pMonth = mFormat.format(month);
                String pYear = ""+year;
                String pDate = pDay+"/"+pMonth+"/"+pYear;

                expireDateTv.setText(pDate);
            }
        },mYear,mMonth,mDay);

        //show dialog
        datePickerDialog.show();
        //disable past Date selection on calender
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis()-1000);
    }

    private String description , promoCode , promoPrice , minimumOrderPrice , expireDate;
    private void inputData() {

        //input data
        promoCode = promoCodeEt.getText().toString().trim();
        description = promoDescriptionEt.getText().toString().trim();
        promoPrice = promoPriceEt.getText().toString().trim();
        minimumOrderPrice = minimumPriceEt.getText().toString().trim();
        expireDate = expireDateTv.getText().toString().trim();

        //validate from data
        if (TextUtils.isEmpty(promoCode)) {
            Toast.makeText(this, "Enter discount code...", Toast.LENGTH_SHORT).show();
            return; // don't procede further
        }
        if (TextUtils.isEmpty(description)) {
            Toast.makeText(this, "Enter description...", Toast.LENGTH_SHORT).show();
            return; // don't procede further
        }
        if (TextUtils.isEmpty(promoPrice)) {
            Toast.makeText(this, "Enter Promotion Price...", Toast.LENGTH_SHORT).show();
            return; // don't procede further
        }
        if (TextUtils.isEmpty(minimumOrderPrice)) {
            Toast.makeText(this, "Enter Minimum Order Price...", Toast.LENGTH_SHORT).show();
            return; // don't procede further
        }
        if (TextUtils.isEmpty(expireDate)) {
            Toast.makeText(this, "Choose Expire date...", Toast.LENGTH_SHORT).show();
            return; // don't procede further
        }


        //all fields entered , add/update data to db
        if (isUpdating) {
            //update data
            UpdateDataToDb();

        } else {
            //add data
            AddDataToDb();
        }

    }

    private void UpdateDataToDb() {
        progressDialog.setMessage("Updating Promotion Code...");
        progressDialog.show();

        //setup data to add in db
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("description",""+description);
        hashMap.put("promoCode",""+promoCode);
        hashMap.put("promoPrice",""+promoPrice);
        hashMap.put("minimumOrderPrice",""+minimumOrderPrice);
        hashMap.put("expireDate",""+expireDate);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Promotions").child(promoId)
                .updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //code added
                        progressDialog.dismiss();
                        Toast.makeText(AddPromotionCodesActivity.this, "Updated..", Toast.LENGTH_SHORT).show();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //adding code failed
                        progressDialog.dismiss();
                        Toast.makeText(AddPromotionCodesActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void AddDataToDb() {
        //all fields entered , show progress dialog
        progressDialog.setMessage("Adding Promotion Code...");
        progressDialog.show();

        String timestamp = ""+System.currentTimeMillis();
        //setup data to add in db
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("id",""+timestamp);
        hashMap.put("timestamp",""+timestamp);
        hashMap.put("description",""+description);
        hashMap.put("promoCode",""+promoCode);
        hashMap.put("promoPrice",""+promoPrice);
        hashMap.put("minimumOrderPrice",""+minimumOrderPrice);
        hashMap.put("expireDate",""+expireDate);

        //init db reference user > Current User > Promotions > promoId > promoDate
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Promotions").child(timestamp)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //code added
                        progressDialog.dismiss();
                        Toast.makeText(AddPromotionCodesActivity.this, "Promotion Code Added..", Toast.LENGTH_SHORT).show();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //adding code failed
                        progressDialog.dismiss();
                        Toast.makeText(AddPromotionCodesActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}