package com.example.grocery.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.grocery.R;
import com.example.grocery.adapters.AdapterReview;
import com.example.grocery.models.ModelReview;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ShopReviewsActivity extends AppCompatActivity {

    private String shopUid;

    //ui views
    private RelativeLayout toolbarRl;
    private ImageButton backBtn;
    private ImageView profileIv;
    private TextView shopNameTv , ratingTv;
    private RatingBar ratingBar;
    private RecyclerView reviewRv;

    private FirebaseAuth firebaseAuth;

    private ArrayList<ModelReview> modelReviewArrayList; // will contain list of all reviews
    private AdapterReview adapterReview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_reviews);

        //get shop uid from intent
        shopUid = getIntent().getStringExtra("shopUid");

        //init view
        toolbarRl = findViewById(R.id.toolbarRl);
        backBtn = findViewById(R.id.backBtn);
        profileIv = findViewById(R.id.profileIv);
        shopNameTv = findViewById(R.id.shopNameTv);
        ratingTv = findViewById(R.id.ratingTv);
        ratingBar = findViewById(R.id.ratingBar);
        reviewRv = findViewById(R.id.reviewRv);

        //get shop uid from intent
        shopUid = getIntent().getStringExtra("shopUid");

        firebaseAuth = FirebaseAuth.getInstance();
        loadshopDetails(); // for shop name , image
        loadRewies(); // for reviwes list , avg rating

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

    }

    private float ratingSum = 0;
    private void loadRewies() {
        //init list
        modelReviewArrayList = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(shopUid).child("Ratings")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //clear list before adding data into it
                        modelReviewArrayList.clear();
                        ratingSum =0;
                        for (DataSnapshot ds : snapshot.getChildren()){
                            float rating = Float.parseFloat(""+ds.child("ratings").getValue());
                            ratingSum += rating; // for avg rating add all ratings , later will divide it by number of reviwes

                            ModelReview modelReview = ds.getValue(ModelReview.class);
                            modelReviewArrayList.add(modelReview);
                        }
                        //setuo adapter
                        adapterReview = new AdapterReview(ShopReviewsActivity.this , modelReviewArrayList);
                        //set to recyclerview
                        reviewRv.setAdapter(adapterReview);

                        long numberOfReviws = snapshot.getChildrenCount();
                        float avgRating = ratingSum/numberOfReviws;

                        ratingTv.setText(String.format("%.2f",avgRating)+" ["+numberOfReviws+"]"); // e.g 4.7 [10]
                        ratingBar.setRating(avgRating);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    private void loadshopDetails() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(shopUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String shopName = ""+snapshot.child("shopName").getValue();
                        String profileImage = ""+snapshot.child("profileImage").getValue();

                        shopNameTv.setText(shopName);
                        try {
                            Picasso.get().load(profileImage).placeholder(R.drawable.ic_store_gray).into(profileIv);
                        }
                        catch (Exception e){
                            //if anything goes wrong setting image (exception occure) , set defult image
                            profileIv.setImageResource(R.drawable.ic_store_gray);

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}