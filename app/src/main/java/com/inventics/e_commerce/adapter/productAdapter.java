package com.inventics.e_commerce.adapter;

import static androidx.core.content.ContentProviderCompat.requireContext;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.inventics.e_commerce.CustomItemClickListener;
import com.inventics.e_commerce.ProductdetailActivity;
import com.inventics.e_commerce.R;
import com.inventics.e_commerce.model.Product;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class productAdapter extends RecyclerView.Adapter<productAdapter.ViewHolder> {

    private ArrayList<Product> attendanceList;
    private ArrayList<Product> newList;
    private Context context;
    private CustomItemClickListener customItemClickListener;
    public productAdapter(ArrayList<Product> attendanceList,Context context,CustomItemClickListener customItemClickListener) {
        this.attendanceList = attendanceList;
        this.customItemClickListener = customItemClickListener;
        this.newList= attendanceList;
        this.context=context;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_product, parent, false);
        final ViewHolder myViewHolder = new ViewHolder(view);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // for click item listener
                customItemClickListener.onItemClick(attendanceList.get(myViewHolder.getAdapterPosition()),myViewHolder.getAdapterPosition());
            }
        });

        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product model = attendanceList.get(position);
        holder.mtitle.setText(model.getTitle());
        holder.mdesc.setText(model.getDescription());
        holder.mcat.setText(model.getCategory());
        holder.mprice.setText("₹ "+(float) model.getPrice());

        Picasso.get().load(model.getImage()).into(holder.mimage);

        holder.mrating.setRating((float) model.getRating().getRate());

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


    public  Filter getFilter() {

        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {

                String searchString = charSequence.toString();

                if (searchString.isEmpty()) {

                    attendanceList = newList;

                } else {

                    ArrayList<Product> tempFilteredList = new ArrayList<>();

                    for (Product user : newList) {

                        // search for user title
                        if (user.getTitle().toLowerCase().contains(searchString)) {

                            tempFilteredList.add(user);
                        }
                    }

                    attendanceList = tempFilteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = attendanceList;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                attendanceList = (ArrayList<Product>) filterResults.values;
                notifyDataSetChanged();
            }
        };
};

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView mtitle, mdesc, mprice, mcat;
        CardView mcard;
        ImageView mimage;

        RatingBar mrating;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mtitle = itemView.findViewById(R.id.productname);
            mdesc = itemView.findViewById(R.id.desc);
            mprice = itemView.findViewById(R.id.price);
            mcat = itemView.findViewById(R.id.category);

            mcard=itemView.findViewById(R.id.single_card);

            mimage=itemView.findViewById(R.id.image);

            mrating=itemView.findViewById(R.id.rating);

        }
    }
}
