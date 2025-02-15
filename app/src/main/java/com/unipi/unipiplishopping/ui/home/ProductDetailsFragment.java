package com.unipi.unipiplishopping.ui.home;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.unipi.unipiplishopping.R;
import com.unipi.unipiplishopping.databinding.FragmentProductDetailBinding;
import com.unipi.unipiplishopping.models.Orders;
import com.unipi.unipiplishopping.models.Products;

import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class ProductDetailsFragment extends Fragment {

    private FragmentProductDetailBinding binding;
    private FirebaseFirestore db;
    private FirebaseUser user;
    private String productId;
    private TextToSpeech textToSpeech;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProductDetailBinding.inflate(inflater, container, false);
        db = FirebaseFirestore.getInstance();

        // Initialize FirebaseAuth
        FirebaseAuth auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        // Retrieve the product name passed via the bundle
        if (getArguments() != null) {
            productId = getArguments().getString("productId");
            fetchProductDetails(productId);
        }

        // Initialize TextToSpeech
        textToSpeech = new TextToSpeech(getContext(), status -> {
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.setLanguage(Locale.getDefault());
            }
        });

        // Set up the order button click listener
        binding.orderButton.setOnClickListener(v -> placeOrder());

        // Set up the navigation button click listener
        binding.backButton.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.action_productDetailsFragment_to_navigation_home));

        // Set up the speak button click listener
        binding.speakButton.setOnClickListener(v -> speakDescription());

        return binding.getRoot();
    }

    // Fetch product details from Firestore
    private void fetchProductDetails(String productId) {
        db.collection("products")
                .document(productId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        Products product = task.getResult().toObject(Products.class);
                        if (product != null) {
                            binding.productName.setText(product.getTitle());
                            binding.productDescription.setText(product.getDescription());
                            binding.productPrice.setText(String.format("%s €", product.getPrice()));
                            binding.productReleaseDate.setText(product.getReleaseDate());

                            // Load image using Glide
                            String url = product.getImageURL();
                            if (url != null && !url.isEmpty()) {
                                Glide.with(requireContext())
                                        .load(url)
                                        .fitCenter()
                                        .placeholder(R.drawable.placeholder_image)
                                        .into(binding.productImage);
                            } else {
                                binding.productImage.setImageResource(R.drawable.placeholder_image);
                            }
                        } else {
                            Toast.makeText(getContext(), "Product not found", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getContext(), "Product not found", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Place an order for the product in the Firestore database
    private void placeOrder() {
        if (user != null) {

            // Get the logged-in user's details
            String customerEmail = user.getEmail();
            SharedPreferences sharedPreferences = requireActivity().getSharedPreferences(customerEmail, Context.MODE_PRIVATE);
            String customerFirstName = sharedPreferences.getString("firstName", "");
            String customerLastName = sharedPreferences.getString("lastName", "");

            // Check if any of the required fields are null or empty
            if (customerFirstName == null || customerFirstName.isEmpty() ||
                    customerLastName == null || customerLastName.isEmpty()) {
                Toast.makeText(getContext(), getString(R.string.missing_order_details), Toast.LENGTH_SHORT).show();
                return;
            }

            // place order
            Orders order = new Orders(customerFirstName, customerLastName, customerEmail, productId, new Date());

            db.collection("orders")
                    .add(order)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(getContext(), getString(R.string.order_placed_successfully), Toast.LENGTH_SHORT).show();
                        Navigation.findNavController(requireView()).navigate(R.id.action_productDetailsFragment_to_navigation_home);
                    })
                    .addOnFailureListener(e -> Toast.makeText(getContext(), getString(R.string.failed_to_place_order), Toast.LENGTH_SHORT).show());

        } else {
            Toast.makeText(getContext(), getString(R.string.no_user_logged_in), Toast.LENGTH_SHORT).show();
        }
    }

    // Speak the product description
    private void speakDescription() {
        String description = binding.productDescription.getText().toString();
        if (!description.isEmpty()) {
            textToSpeech.speak(description, TextToSpeech.QUEUE_FLUSH, null, null);
        } else {
            Toast.makeText(getContext(), "No description available", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null) {
            getActivity().setTitle(getString(R.string.product_details));
            Objects.requireNonNull(((AppCompatActivity) getActivity()).getSupportActionBar()).setDisplayHomeAsUpEnabled(false);
        }
    }
}