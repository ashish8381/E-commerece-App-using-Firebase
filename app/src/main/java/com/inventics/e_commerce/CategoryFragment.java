package com.inventics.e_commerce;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.inventics.e_commerce.adapter.cartAdapter;
import com.inventics.e_commerce.adapter.catAdapter;
import com.inventics.e_commerce.model.Product;
import com.inventics.e_commerce.model.catModel;

import java.util.ArrayList;


import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CategoryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CategoryFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    RecyclerView mrecyclerview;
    private ArrayList<catModel> ldata;
    private catAdapter mAdapter;

    private  View mMainView;

    private ProgressBar pd;

    public CategoryFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CategoryFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CategoryFragment newInstance(String param1, String param2) {
        CategoryFragment fragment = new CategoryFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mMainView= inflater.inflate(R.layout.fragment_category, container, false);

        pd=mMainView.findViewById(R.id.cat_progress);
        mrecyclerview=mMainView.findViewById(R.id.cat_recyclerview);
        mrecyclerview.setHasFixedSize(true);
        mrecyclerview.setLayoutManager(new LinearLayoutManager(requireContext()));
        ldata=new ArrayList<>();
        pd.setVisibility(View.VISIBLE);
        getCategory();

        return mMainView;
    }

    private void getCategory() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://fakestoreapi.com/products/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        RetrofitAPI retrofitAPI = retrofit.create(RetrofitAPI.class);
        Call<ArrayList<String>> call = retrofitAPI.getCategory();
        call.enqueue(new Callback<ArrayList<String>>() {
            @Override
            public void onResponse(Call<ArrayList<String>> call, Response<ArrayList<String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ArrayList<String> categories = response.body();
                    mAdapter=new catAdapter(categories,requireContext());
                    mrecyclerview.setAdapter(mAdapter);

                    mAdapter.notifyDataSetChanged();
                    pd.setVisibility(View.GONE);

                    // Use categories array to populate your RecyclerView or TextView
                } else {
                    Log.e("abcd", "Failed to fetch products: " + response.message());
                    pd.setVisibility(View.GONE);
                    Toast.makeText(requireContext(), "Failed to get the data..", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ArrayList<String>> call, Throwable t) {
                Log.e("abcd", "Failed to fetch products: " + t.getMessage());
                pd.setVisibility(View.GONE);
                Toast.makeText(requireContext(), "Failed to get the data..", Toast.LENGTH_SHORT).show();
            }
        });
    }


}