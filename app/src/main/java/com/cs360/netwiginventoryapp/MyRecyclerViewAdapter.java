package com.cs360.netwiginventoryapp;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/*******************************************************************
 Author      : Chad Netwig
 App Name    : Chad's Inventory App
 Version     : 1.0
 Date        : March 31, 2022
 Updated     : April 14, 2022
             :
 Description : Implementation of the RecyclerView Adapter used to
             : hold a GridView layout of the inventory items.
             :
             : Comments have been added throughout to explain logic
********************************************************************/

public class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.ViewHolder> {

    public String[] mData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;

    // added to record previously-selected position in the RecyclerView (used for 'deselection')
    public int previousPosition;

    // data is passed into the constructor and inflated
    MyRecyclerViewAdapter(Context context, String[] data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
    }

    // inflates the cell layout from xml when needed
    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.inventory_item, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each cell
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.myTextView.setText(mData[position]); // populates cell with text

        Log.i("BIND", "Value of position is: " + position);
        Log.i("BIND", "Value of previousPosition is: " + previousPosition);
        // changes selected/deselected cell colors
        if(position == previousPosition) {
            // selected item color
            holder.myTextView.setBackgroundColor(Color.parseColor("#1435ED"));  // blue
            holder.myTextView.setTextColor(Color.parseColor("#FFFFFF"));        // white
        }
        else {
            // deselected item color
            holder.myTextView.setBackgroundColor(Color.parseColor("#9BBAE8"));  // light blue
            holder.myTextView.setTextColor(Color.parseColor("#FF000000"));      // black
        }
    }


    // total number of cells
    @Override
    public int getItemCount() {
        return mData.length;
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView myTextView;

        ViewHolder(View itemView) {
            super(itemView);
            myTextView = itemView.findViewById(R.id.inventory_item_name);
            itemView.setOnClickListener(this);

        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
            previousPosition = getLayoutPosition();
        }

    } // end Viewholder()


    // convenience method for getting data at click position
    String getItem(int id) {
        return mData[id];
    }


    // allows clicks events to be caught
    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

}