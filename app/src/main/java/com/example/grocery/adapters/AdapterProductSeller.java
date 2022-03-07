package com.example.grocery.adapters;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.grocery.FilterProduct;
import com.example.grocery.R;
import com.example.grocery.activities.EditProductActivity;
import com.example.grocery.activities.mainSellerActivity;
import com.example.grocery.models.ModeProduct;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class AdapterProductSeller extends RecyclerView.Adapter<AdapterProductSeller.HolderProductSeller> implements Filterable {

    private mainSellerActivity context;
    public ArrayList<ModeProduct> productArrayList , FilterList;
    private FilterProduct filter;


    public AdapterProductSeller(mainSellerActivity context, ArrayList<ModeProduct> productArrayList) {
        this.context = context;
        this.productArrayList = productArrayList;
        this.FilterList = productArrayList;
    }

    @NonNull
    @Override
    public HolderProductSeller onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout
        View view = LayoutInflater.from(context).inflate(R.layout.new_product_seller,parent,false);
        return new HolderProductSeller(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderProductSeller holder, int position) {
        //get data
        ModeProduct modelProduct = productArrayList.get(position);
        String id = modelProduct.getProductId();
        String uid = modelProduct.getUid();
        String DiscountAvailable = modelProduct.getDiscountAvailable();
        String DiscountedNote = modelProduct.getDiscountedNote();
        String DiscountedPrice = modelProduct.getDiscountedPrice();
        String ProductCategory = modelProduct.getProductCategory();
        String ProductDescription = modelProduct.getProductDescription();
        String icon = modelProduct.getProductIcon();
        String ProductQuantity = modelProduct.getProductQuantity();
        String title = modelProduct.getProductTitle();
        String Timestamp = modelProduct.getTimestamp();
        String orignalPrice = modelProduct.getOriginalPrice();

        //set data
        holder.titleTv.setText(title);
        holder.discountedNoteTv.setText(DiscountedNote);
        holder.discountedPriceTv.setText("$"+DiscountedPrice);
        holder.originalPriceTv.setText("$"+orignalPrice);
        holder.quantityTv.setText(ProductQuantity);

        if(DiscountAvailable.equals("true")){
            //productis on discount
            holder.discountedPriceTv.setVisibility(View.VISIBLE);
            holder.discountedNoteTv.setVisibility(View.VISIBLE);
            holder.originalPriceTv.setPaintFlags(holder.originalPriceTv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG); // add strike through on orignal price
        }
        else
        {
            //product is not on discount
            holder.discountedPriceTv.setVisibility(View.GONE);
            holder.discountedNoteTv.setVisibility(View.GONE);
            holder.originalPriceTv.setPaintFlags(0);
        }
        try{
            Picasso.get().load(icon).placeholder(R.drawable.ic_baseline_add_shopping_cart_primary).into(holder.productIconIv);
        }
        catch (Exception e){
            holder.productIconIv.setImageResource(R.drawable.ic_baseline_add_shopping_cart_primary);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //handle item clicks , show item details (in bottom sheet)
                detailsBottomSheet(modelProduct); //here modelproduct contain details of clicked product
            }
        });


    }

    private void detailsBottomSheet(ModeProduct modelProduct) {
        //bottom sheet
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        //inflate view for bottomsheet
        View view = LayoutInflater.from(context).inflate(R.layout.bs_product_details_seller,null);
        //set view to bottomsheet
        bottomSheetDialog.setContentView(view);

        //init views of bottomsheet
        ImageButton backbtn = view.findViewById(R.id.backBtn);
        ImageButton deleteBtn = view.findViewById(R.id.deleteBtn);
        ImageButton editBtn = view.findViewById(R.id.editBtn);
        ImageView productIconIv = view.findViewById(R.id.productIconIv);
        TextView discountNoteTv = view.findViewById(R.id.discountedNoteTv);
        TextView titleTv = view.findViewById(R.id.titleTv);
        TextView descriptionTv = view.findViewById(R.id.descriptionTv);
        TextView categoryTv = view.findViewById(R.id.categoryTv);
        TextView quantityTv = view.findViewById(R.id.quantityTv);
        TextView discountedPriceTv = view.findViewById(R.id.discountedPriceTv);
        TextView originalPriceTv = view.findViewById(R.id.originalPriceTv);

        //get data
        String id = modelProduct.getProductId();
        String uid = modelProduct.getUid();
        String DiscountAvailable = modelProduct.getDiscountAvailable();
        String DiscountedNote = modelProduct.getDiscountedNote();
        String DiscountedPrice = modelProduct.getDiscountedPrice();
        String ProductCategory = modelProduct.getProductCategory();
        String ProductDescription = modelProduct.getProductDescription();
        String icon = modelProduct.getProductIcon();
        String ProductQuantity = modelProduct.getProductQuantity();
        String title = modelProduct.getProductTitle();
        String Timestamp = modelProduct.getTimestamp();
        String orignalPrice = modelProduct.getOriginalPrice();

        //set data
        titleTv.setText(title);
        descriptionTv.setText(ProductDescription);
        categoryTv.setText(ProductCategory);
        quantityTv.setText(ProductQuantity);
        discountNoteTv.setText(DiscountedNote);
        discountedPriceTv.setText("$"+DiscountedPrice);
        originalPriceTv.setText("$"+orignalPrice);

        if(DiscountAvailable.equals("true")){
            //productis on discount
            discountedPriceTv.setVisibility(View.VISIBLE);
            discountNoteTv.setVisibility(View.VISIBLE);
            originalPriceTv.setPaintFlags(originalPriceTv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG); // add strike through on orignal price
        }
        else
        {
            //product is not on discount
            discountedPriceTv.setVisibility(View.GONE);
            discountNoteTv.setVisibility(View.GONE);
        }
        try{
            Picasso.get().load(icon).placeholder(R.drawable.ic_baseline_add_shopping_cart_primary).into(productIconIv);
        }
        catch (Exception e){
            productIconIv.setImageResource(R.drawable.ic_baseline_add_shopping_cart_primary);
        }

        //show dialog
        bottomSheetDialog.show();

        //edit click
        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetDialog.dismiss();
                //open edit product activity , pass idof product
                Intent intent = new Intent(context, EditProductActivity.class);
                intent.putExtra("productId",id);
                context.startActivity(intent);

            }
        });
        //delete click
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetDialog.dismiss();
                //shoe delete confirm dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Delete")
                        .setMessage("Are tou sure you want to delete product"+title+" ?")
                        .setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //delete
                                deleteProduct(id); // id is the product id
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //cancel , dismiss dialog
                                dialog.dismiss();
                            }
                        })
                        .show();
            }
        });
        //back click
        backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //dismiss bottom sheet
                bottomSheetDialog.dismiss();
            }
        });
    }

    private void deleteProduct(String id) {
        //delete product using its id

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Products").child(id).removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //product deleted
                        Toast.makeText(context, "Product deleted..", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed deleting product
                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public int getItemCount() {
        return productArrayList.size();
    }

    @Override
    public Filter getFilter() {
        if(filter == null){
            filter = new FilterProduct(this,FilterList);
        }
        return filter;
    }

    class HolderProductSeller extends RecyclerView.ViewHolder{
//        holdes views of recyclerview

        private ImageView productIconIv , nextIv;
        private TextView discountedNoteTv ,titleTv , quantityTv , discountedPriceTv , originalPriceTv;

        public HolderProductSeller(@NonNull View itemView) {
            super(itemView);

            productIconIv= itemView.findViewById(R.id.productIconIv);
            nextIv = itemView.findViewById(R.id.nextIv);
            discountedNoteTv = itemView.findViewById(R.id.discountedNoteTv);
            titleTv = itemView.findViewById(R.id.titleTv);
            quantityTv = itemView.findViewById(R.id.quantityTv);
            discountedPriceTv = itemView.findViewById(R.id.discountedPriceTv);
            originalPriceTv = itemView.findViewById(R.id.originalPriceTv);


        }
    }
}
