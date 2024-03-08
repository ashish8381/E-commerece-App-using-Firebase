package com.inventics.e_commerce.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.inventics.e_commerce.ProductdetailActivity;
import com.inventics.e_commerce.R;
import com.inventics.e_commerce.model.Product;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class cartAdapter extends RecyclerView.Adapter<cartAdapter.ViewHolder> {

    private ArrayList<Product> attendanceList;
    private Context context;
    public cartAdapter(ArrayList<Product> attendanceList,Context context) {
        this.attendanceList = attendanceList;
        this.context=context;

    }

    @NonNull
    @Override
    public cartAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_cart, parent, false);
        final cartAdapter.ViewHolder myViewHolder = new cartAdapter.ViewHolder(view);

        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull cartAdapter.ViewHolder holder, int position) {
        Product model = attendanceList.get(position);
        holder.mname.setText(model.getTitle());
        holder.mqty.setText(model.getQty()+"");
        holder.mcat.setText(model.getCategory());
        holder.mprice.setText("₹ "+((float) model.getPrice())*model.getQty());

        Picasso.get().load(model.getImage()).into(holder.mimage);


        holder.mcard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SharedPreferences myPreferences
                        = PreferenceManager.getDefaultSharedPreferences(context);
                SharedPreferences.Editor myEditor = myPreferences.edit();
                myEditor.putString("p_id", model.getKey());
                myEditor.apply();

                Intent intent = new Intent(context, ProductdetailActivity.class);
                context.startActivity(intent);
            }
        });

    }


    @Override
    public int getItemCount() {
        return attendanceList.size();
    }


    
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView mname, mqty, mprice, mcat;

        RelativeLayout mcard;
        
        ImageView mimage;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mname = itemView.findViewById(R.id.cart_pro_name);
            mqty = itemView.findViewById(R.id.cart_pro_qty);
            mprice = itemView.findViewById(R.id.cart_pro_price);
            mcat = itemView.findViewById(R.id.cart_pro_cat);

            mcard=itemView.findViewById(R.id.cart_main);


            mimage=itemView.findViewById(R.id.cart_pro_img);

           

        }
    }
}
