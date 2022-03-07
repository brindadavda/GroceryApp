package com.example.grocery.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.grocery.Constans;
import com.example.grocery.FilterProduct;
import com.example.grocery.FilterProductUser;
import com.example.grocery.R;
import com.example.grocery.activities.ShopDetailsActivity;
import com.example.grocery.models.ModeProduct;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.ArrayList;

import p32929.androideasysql_library.Column;
import p32929.androideasysql_library.EasyDB;

public class AdapterProductUser extends RecyclerView.Adapter<AdapterProductUser.HolderProductUser> implements Filterable {

    private Context context;
    public ArrayList<ModeProduct> productsList , filterList;
    private FilterProductUser filter;

    public AdapterProductUser(Context context, ArrayList<ModeProduct> productsList) {
        this.context = context;
        this.productsList = productsList;
        this.filterList = productsList;
    }

    @NonNull
    @Override
    public HolderProductUser onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout
        View view = LayoutInflater.from(context).inflate(R.layout.new_product_user,parent,false);
        return new HolderProductUser(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderProductUser holder, int position) {
        //put data
        ModeProduct modelProduct = productsList.get(position);
        String DiscountAvailable = modelProduct.getDiscountAvailable();
        String DiscountedNote = modelProduct.getDiscountedNote();
        String DiscountedPrice = modelProduct.getDiscountedPrice();
        String ProductCategory = modelProduct.getProductCategory();
        String ProductDescription = modelProduct.getProductDescription();
        String icon = modelProduct.getProductIcon();
        String ProductQuantity = modelProduct.getProductQuantity();
        String productTitle = modelProduct.getProductTitle();
        String productId = modelProduct.getProductId();
        String Timestamp = modelProduct.getTimestamp();
        String orignalPrice = modelProduct.getOriginalPrice();

        //set data
        holder.titleTv.setText(productTitle);
        holder.descriptionTv.setText(ProductDescription);
        holder.discountedNoteTv.setText(DiscountedNote);
        holder.discountedPriceTv.setText("$"+DiscountedPrice);
        holder.originalPriceTv.setText("$"+orignalPrice);

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

        holder.addToCartTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //handle item clicks , show item details (in bottom sheet)
                showQuantityDialog(modelProduct);
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

    }

    private double cost = 0;
    private double finalCost = 0;
    private int quantity = 0;
    private void showQuantityDialog(ModeProduct modelProduct) {
        //inflate layout for dialog
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_quantity,null);
        //init layout views
        ImageView productIv = view.findViewById(R.id.productIv);
        TextView titleIv = view.findViewById(R.id.titleTv);
        TextView pQuantityTv = view.findViewById(R.id.pQuantityTv);
        TextView descriptionTv = view.findViewById(R.id.descriptionTv);
        TextView discountedNoteTv = view.findViewById(R.id.discountedNoteTv);
        TextView originalPriceTv = view.findViewById(R.id.originalPriceTv);
        TextView priceDiscountedTv = view.findViewById(R.id.priceDiscountedTv);
        TextView finalPriceTv = view.findViewById(R.id.finalPriceTv);
        ImageButton decrementBtn = view.findViewById(R.id.decrementBtn);
        TextView quantityTv = view.findViewById(R.id.quantityTv);
        ImageButton incrementBtn = view.findViewById(R.id.incrementBtn);
        Button cotinueBtn = view.findViewById(R.id.cotinueBtn);

        //get data from modle
        String productId = modelProduct.getProductId();
        String title = modelProduct.getProductTitle();
        String productQuantity = modelProduct.getProductQuantity();
        String description = modelProduct.getProductDescription();
        String discountedNote = modelProduct.getDiscountedNote();
        String image = modelProduct.getProductIcon();

        String price;
        if(modelProduct.getDiscountAvailable().equals("true")){
            //product have discount
            price = modelProduct.getDiscountedPrice();
            discountedNoteTv.setVisibility(View.VISIBLE);
            originalPriceTv.setPaintFlags(originalPriceTv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG); // add strike through on orignal price


        }
        else {
            //product dosn't have discounted
            discountedNoteTv.setVisibility(View.GONE);
            priceDiscountedTv.setVisibility(View.GONE);
            price = modelProduct.getOriginalPrice();
        }

        cost = Double.parseDouble(price.replaceAll("$",""));
        finalCost = Double.parseDouble(price.replaceAll("$",""));
        quantity = 1;

        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(view);

        try{
            Picasso.get().load(image).placeholder(R.drawable.ic_cart_gray).into(productIv);
        }
        catch (Exception e){
            productIv.setImageResource(R.drawable.ic_cart_gray);
        }
        titleIv.setText(""+title);
        pQuantityTv.setText(""+productQuantity);
        descriptionTv.setText(""+description);
        discountedNoteTv.setText(""+discountedNote);
        quantityTv.setText(""+quantity);
        originalPriceTv.setText("$"+modelProduct.getOriginalPrice());
        priceDiscountedTv.setText("$"+modelProduct.getDiscountedPrice());
        finalPriceTv.setText("$"+finalCost);

        AlertDialog dialog = builder.create();
        dialog.show();

        //increase quantity of the product
        incrementBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finalCost = finalCost + cost;
                quantity++;

                finalPriceTv.setText("$"+finalCost);
                quantityTv.setText(""+quantity);
            }
        });
        decrementBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (quantity > 1){
                    finalCost = finalCost-cost;
                    quantity--;

                    finalPriceTv.setText("$"+finalCost);
                    quantityTv.setText(""+quantity);
                }
            }
        });
        cotinueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = titleIv.getText().toString().trim();
                String priceEach = price;
                String totalprice = finalPriceTv.getText().toString().trim();
                String quantity = quantityTv.getText().toString().trim();

                //add to db / SQLite
                addToCart(productId,title,priceEach,totalprice,quantity);

                dialog.dismiss();
            }
        });
    }

    private int itemId = 1;
    private void addToCart(String productId, String title, String priceEach, String price, String quantity) {
        itemId++;

        EasyDB easyDB = EasyDB.init(context,"ITEMS_DB")
                .setTableName("ITEMS_TABLE")
                .addColumn(new Column("Item_Id",new String[]{"text","uniqe"}))
                .addColumn(new Column("Item_PID",new String[]{"text","not null"}))
                .addColumn(new Column("Item_Name",new String[]{"text","not null"}))
                .addColumn(new Column("Item_Price_Each",new String[]{"text","not null"}))
                .addColumn(new Column("Item_Price",new String[]{"text","not null"}))
                .addColumn(new Column("Item_Quantity",new String[]{"text","not null"}))
                .doneTableColumn();

        Boolean b = easyDB.addData("Item_Id",itemId)
                .addData("Item_PID",productId)
                .addData("Item_Name",title)
                .addData("Item_Price_Each",priceEach)
                .addData("Item_Price" , price)
                .addData("Item_Quantity",quantity)
                .doneDataAdding();

        Toast.makeText(context, "Added to cart....", Toast.LENGTH_SHORT).show();

        //update cart count
        ((ShopDetailsActivity)context).cartCount();

    }

    @Override
    public int getItemCount() {
        return productsList.size();
    }

    @Override
    public Filter getFilter() {
        if (filter == null){
            filter = new FilterProductUser(this,filterList);
        }
        return filter;
    }

    class HolderProductUser extends RecyclerView.ViewHolder{

        //ui views
        private ImageView productIconIv,nextIv;
        private TextView discountedNoteTv,titleTv,addToCartTv,descriptionTv,discountedPriceTv,originalPriceTv;

        public HolderProductUser(@NonNull View itemView) {
            super(itemView);

            productIconIv = itemView.findViewById(R.id.productIconIv);
            nextIv = itemView.findViewById(R.id.nextIv);
            discountedNoteTv = itemView.findViewById(R.id.discountedNoteTv);
            titleTv = itemView.findViewById(R.id.titleTv);
            addToCartTv = itemView.findViewById(R.id.addToCartTv);
            descriptionTv = itemView.findViewById(R.id.descriptionTv);
            discountedPriceTv = itemView.findViewById(R.id.discountedPriceTv);
            originalPriceTv = itemView.findViewById(R.id.originalPriceTv);

        }
    }
}
