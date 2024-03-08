package com.inventics.e_commerce.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.inventics.e_commerce.CustomItemClickListener;
import com.inventics.e_commerce.MainActivity;
import com.inventics.e_commerce.PhoneAuthActivity;
import com.inventics.e_commerce.R;
import com.inventics.e_commerce.SplashActivity;
import com.inventics.e_commerce.model.Product;
import com.inventics.e_commerce.model.catModel;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class catAdapter extends RecyclerView.Adapter<catAdapter.ViewHolder> {

    private ArrayList<String> attendanceList;

    private productAdapter mAdapter;
    private Context context;
    public catAdapter(ArrayList<String> attendanceList,Context context) {
        this.attendanceList = attendanceList;
        this.context=context;

    }

    @NonNull
    @Override
    public catAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_category, parent, false);
        final catAdapter.ViewHolder myViewHolder = new catAdapter.ViewHolder(view);

        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull catAdapter.ViewHolder holder, int position) {
        String category = attendanceList.get(position);

        // Assuming you have a TextView in your ViewHolder named textViewCategory
        holder.mname.setText(capitalizeFirstLetter(category));

        if (category.equals("electronics")) {
            holder.mimage.setImageResource(R.drawable.electronics);
        } else if (category.equals("jewelery")) {
            holder.mimage.setImageResource(R.drawable.jewellery);
        } else if (category.equals("men's clothing")) {
            holder.mimage.setImageResource(R.drawable.men_clothing);
        } else if (category.equals("women's clothing")) {
            holder.mimage.setImageResource(R.drawable.women_clothing);
        } else {
            // Default image
            holder.mimage.setImageResource(R.drawable.default_img);
        }


        holder.mmain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences myPreferences
                        = PreferenceManager.getDefaultSharedPreferences(context);
                SharedPreferences.Editor myEditor = myPreferences.edit();
                myEditor.putString("click_cat", category);
                myEditor.putString("activity", "home2");
                myEditor.apply();

                context.startActivity(new Intent(context, MainActivity.class));


            }
        });


    }

    public static String capitalizeFirstLetter(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }


    @Override
    public int getItemCount() {
        return attendanceList.size();
    }



    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView mname;

        ImageView mimage;

        RelativeLayout mmain;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mname = itemView.findViewById(R.id.cat_pro_name);

            mmain=itemView.findViewById(R.id.cat_main_layout);


            mimage=itemView.findViewById(R.id.cat_pro_img);



        }
    }
}
