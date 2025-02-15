package com.unipi.unipiplishopping.ui.home;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.unipi.unipiplishopping.R;
import com.unipi.unipiplishopping.databinding.FragmentHomeBinding;
import com.unipi.unipiplishopping.models.Products;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;
    private FirebaseFirestore db;
    private final List<Products> productList = new ArrayList<>();
    private ProductAdapter productAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Initialize Firebase Firestore
        db = FirebaseFirestore.getInstance();

        RecyclerView recyclerView = binding.recyclerView;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize the adapter
        productAdapter = new ProductAdapter(getContext(), productList);
        recyclerView.setAdapter(productAdapter);

        // Add padding to the bottom of the RecyclerView
        int bottomPadding = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 100, getResources().getDisplayMetrics());
        recyclerView.setPadding(0, 0, 0, bottomPadding);
        recyclerView.setClipToPadding(false);

        // Fetch the products from Firestore
        fetchProducts();

        return root;
    }

    // Fetch the products from Firestore
    @SuppressLint("NotifyDataSetChanged")
    private void fetchProducts() {
        db.collection("products")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        productList.clear();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Products product = document.toObject(Products.class);
                            productList.add(product);
                        }

                        // Notify the adapter that data has changed
                        productAdapter.notifyDataSetChanged();
                    } else {
                        Log.w("HomeFragment", getString(R.string.error_getting_documents), task.getException());
                    }
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null) {
            getActivity().setTitle(getString(R.string.title_home));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}