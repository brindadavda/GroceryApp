package com.example.grocery.adapters;

import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.grocery.Constans;
import com.example.grocery.R;
import com.example.grocery.activities.ShopReviewsActivity;
import com.example.grocery.models.ModelReview;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;

public class AdapterReview extends RecyclerView.Adapter<AdapterReview.HolderReview>  {

    private ShopReviewsActivity constans;
    private ArrayList<ModelReview> modelReviewArrayList;

    public AdapterReview(ShopReviewsActivity constans, ArrayList<ModelReview> modelReviewArrayList) {
        this.constans = constans;
        this.modelReviewArrayList = modelReviewArrayList;
    }

    @NonNull
    @Override
    public HolderReview onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflater layout row_review
        View view = LayoutInflater.from(constans).inflate(R.layout.row_review,parent,false);
        return new HolderReview(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderReview holder, int position) {

        //get data at position
        ModelReview modelReview = modelReviewArrayList.get(position);
        String uid = modelReview.getUid();
        String ratings = modelReview.getRatings();
        String timestamp = modelReview.getTimestamp();
        String review = modelReview.getReview();

        //we also need info of user who wrote the review: we can do it using uid of user
        loadUserDeatil(modelReview , holder);

        //convert timestamp to proper formate dd/mm/yyyy
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Long.parseLong(timestamp));
        String dateFormat = DateFormat.format("dd/MM/yyyy",calendar).toString();

        //set data
        holder.ratingBar.setRating(Float.parseFloat(ratings));
        holder.reviewTv.setText(review);
        holder.dateTv.setText(dateFormat);


    }

    private void loadUserDeatil(ModelReview modelReview, HolderReview holder) {
        //uid of user who wrote review
        String uid = modelReview.getUid();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(uid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get user info
                        String name = ""+snapshot.child("name").getValue();
                        String profileimage = ""+snapshot.child("profileImage").getValue();

                        //set data
                        holder.nameTv.setText(name);
                        try {
                            Picasso.get().load(profileimage).placeholder(R.drawable.ic_person_gray).into(holder.profileIv);
                        }
                        catch (Exception e){
                            //if anything goes wrong setting image (exception occure) , set defult image
                            holder.profileIv.setImageResource(R.drawable.ic_store_gray);

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }



    @Override
    public int getItemCount() {
        return modelReviewArrayList.size();
    }

    //view holder class , holds / init view of recyclerview
    class HolderReview extends RecyclerView.ViewHolder{

        //ui views of layout row_review
        private LinearLayout profileLl;
        private ImageView profileIv;
        private TextView nameTv , dateTv , reviewTv;
        private RatingBar ratingBar;



        public HolderReview(@NonNull View itemView) {
            super(itemView);

            //init views of row_review
            profileLl = itemView.findViewById(R.id.profileLl);
            profileIv = itemView.findViewById(R.id.profileIv);
            nameTv = itemView.findViewById(R.id.nameTv);
            dateTv = itemView.findViewById(R.id.dateTv);
            reviewTv = itemView.findViewById(R.id.reviewTv);
            ratingBar = itemView.findViewById(R.id.ratingBar);
        }
    }
}
