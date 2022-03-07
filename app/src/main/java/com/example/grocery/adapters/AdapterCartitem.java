package com.example.grocery.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.grocery.R;
import com.example.grocery.activities.ShopDetailsActivity;
import com.example.grocery.models.ModelCartitem;
import com.example.grocery.models.ModleShop;

import org.w3c.dom.Text;

import java.util.ArrayList;

import p32929.androideasysql_library.Column;
import p32929.androideasysql_library.EasyDB;

public class AdapterCartitem  extends RecyclerView.Adapter<AdapterCartitem.HolderCaertItem>{

    private Context context;
    private ArrayList<ModelCartitem> cartItems;

    public AdapterCartitem(Context context, ArrayList<ModelCartitem> cartItems) {
        this.context = context;
        this.cartItems = cartItems;
    }

    @NonNull
    @Override
    public HolderCaertItem onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout row_cartitem.xml
        View view = LayoutInflater.from(context).inflate(R.layout.row_cartitem,parent,false);
        return new HolderCaertItem(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderCaertItem holder, @SuppressLint("RecyclerView") int position) {
        //get data
        ModelCartitem modelCartitem = cartItems.get(position);
        String id = modelCartitem.getId();
        String getpId = modelCartitem.getpId();
        String title = modelCartitem.getName();
        String cost = modelCartitem.getCost();
        String price = modelCartitem.getPrice();
        String Quantity = modelCartitem.getQuantity();

        //set data
        holder.itemTitleTv.setText(""+title);
        holder.itemPriceTv.setText(""+cost);
        holder.itemQuantityTv.setText(""+Quantity+""); // e.g. [3]
        holder.itemPriceEachtv.setText(""+price);

        //handle remove click listener, delete item from cart
        holder.itemRemoveTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //will create table if not exist, but in that case will not exist
                EasyDB easyDB = EasyDB.init(context,"ITEMS_DB")
                        .setTableName("ITEMS_TABLE")
                        .addColumn(new Column("Item_Id",new String[]{"text","uniqe"}))
                        .addColumn(new Column("Item_PID",new String[]{"text","not null"}))
                        .addColumn(new Column("Item_Name",new String[]{"text","not null"}))
                        .addColumn(new Column("Item_Price_Each",new String[]{"text","not null"}))
                        .addColumn(new Column("Item_Price",new String[]{"text","not null"}))
                        .addColumn(new Column("Item_Quantity",new String[]{"text","not null"}))
                        .doneTableColumn();

                easyDB.deleteRow(1,id);
                Toast.makeText(context, "Removed from cart..", Toast.LENGTH_SHORT).show();

                //refresh list
                cartItems.remove(position);
                notifyItemChanged(position);
                notifyDataSetChanged();

                //ajest sub total after product remove
                double subTotalWithoutDiscount = ((ShopDetailsActivity)context).allTotalPrice;
                double subTotalAfterRemoveProduct = subTotalWithoutDiscount - Double.parseDouble(cost.replace("$",""));
                ((ShopDetailsActivity)context).allTotalPrice = subTotalAfterRemoveProduct;
                ((ShopDetailsActivity)context).sTotalTv.setText("$"+String.format("%.2f",((ShopDetailsActivity)context).allTotalPrice));

                double promoPrice = Double.parseDouble(((ShopDetailsActivity)context).promoPrice);
                double deliveryFee = Double.parseDouble(((ShopDetailsActivity)context).deliveryFee.replace("$",""));

                if (((ShopDetailsActivity)context).isPromoCodeApplicable){
                    if (subTotalAfterRemoveProduct < Double.parseDouble(((ShopDetailsActivity)context).promoMinimumOrderPrice)){
                        Toast.makeText(context, "This code is valid for order with minimum amount : $"+((ShopDetailsActivity) context).promoMinimumOrderPrice, Toast.LENGTH_SHORT).show();
                        ((ShopDetailsActivity)context).applyBtn.setVisibility(View.GONE);
                        ((ShopDetailsActivity)context).promoDescriptionTv.setVisibility(View.GONE);
                        ((ShopDetailsActivity)context).promoDescriptionTv.setText("");
                        ((ShopDetailsActivity)context).discountTv.setText("$0");
                        ((ShopDetailsActivity)context).isPromoCodeApplicable = false;

                        ((ShopDetailsActivity)context).allTotalPriceTv.setText("$"+String.format("%.2f",Double.parseDouble(String.format("%.2f",subTotalAfterRemoveProduct + deliveryFee))));

                    }
                    else {
                        ((ShopDetailsActivity)context).applyBtn.setVisibility(View.VISIBLE);
                        ((ShopDetailsActivity)context).promoDescriptionTv.setVisibility(View.VISIBLE);
                        ((ShopDetailsActivity)context).promoDescriptionTv.setText(((ShopDetailsActivity)context).promoDescription);
                        ((ShopDetailsActivity)context).isPromoCodeApplicable = true;

                        ((ShopDetailsActivity)context).allTotalPriceTv.setText("$"+String.format("%.2f",Double.parseDouble(String.format("%.2f",subTotalAfterRemoveProduct + deliveryFee - promoPrice))));


                    }
                }
                else {
                    ((ShopDetailsActivity)context).allTotalPriceTv.setText("$"+String.format("%.2f",Double.parseDouble(String.format("%.2f",subTotalAfterRemoveProduct + deliveryFee))));
                }

                //after removing item from cart , update cart count
                ((ShopDetailsActivity)context).cartCount();
            }
        });

    }

    @Override
    public int getItemCount() {
        return cartItems.size(); // return number of records
    }

    //view holder class
    class HolderCaertItem extends RecyclerView.ViewHolder{

        private TextView itemTitleTv, itemPriceTv ,itemPriceEachtv,itemQuantityTv,itemRemoveTv;

        public HolderCaertItem(@NonNull View itemView) {
            super(itemView);

            itemTitleTv = itemView.findViewById(R.id.itemTitleTv);
            itemPriceTv = itemView.findViewById(R.id.itemPriceTv);
            itemPriceEachtv = itemView.findViewById(R.id.itemPriceEachtv);
            itemQuantityTv = itemView.findViewById(R.id.itemQuantityTv);
            itemRemoveTv = itemView.findViewById(R.id.itemRemoveTv);
        }
    }
}
