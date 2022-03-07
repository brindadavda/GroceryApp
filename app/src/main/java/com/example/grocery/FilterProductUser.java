package com.example.grocery;

import android.widget.Filter;

import com.example.grocery.adapters.AdapterProductSeller;
import com.example.grocery.adapters.AdapterProductUser;
import com.example.grocery.models.ModeProduct;

import java.util.ArrayList;

public class FilterProductUser extends Filter {

    private AdapterProductUser adadpter;
    private ArrayList<ModeProduct> filterList;

    public FilterProductUser(AdapterProductUser adadpter, ArrayList<ModeProduct> filterList) {
        this.adadpter = adadpter;
        this.filterList = filterList;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults results = new FilterResults();
        //validate data for search queary
        if(constraint != null && constraint.length() > 0){
            //search filed not empty, searching somthing , perform search

            //change to upper case , to mske case insensitive
            constraint = constraint.toString().toUpperCase();
            //store our filtered list
            ArrayList<ModeProduct> filteredModels = new ArrayList<>();
            for (int i=0;i<filterList.size();i++){
                //check, search by title and category
                if(filterList.get(i).getProductTitle().toUpperCase().contains(constraint) ||
                        filterList.get(i).getProductCategory().toUpperCase().contains(constraint))  {
                    //add filtered data to list
                    filteredModels.add(filterList.get(i));
                }
            }

            results.count = filteredModels.size();
            results.values = filteredModels;
        }
        else {
            //search filed  empty, not searching , return original / all / complete list

            results.count = filterList.size();
            results.values = filterList;
        }
        return results;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
        adadpter.productsList = (ArrayList<ModeProduct>) results.values;
        //refresh adapter
        adadpter.notifyDataSetChanged();

    }
}
