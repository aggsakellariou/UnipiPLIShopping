package com.unipi.unipiplishopping;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.gms.location.LocationRequest;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.unipi.unipiplishopping.databinding.ActivityMainBinding;
import com.unipi.unipiplishopping.helper.LocationHelper;
import com.unipi.unipiplishopping.helper.NotificationHelper;
import com.unipi.unipiplishopping.ui.login.LoginActivity;
import com.unipi.unipiplishopping.ui.settings.FontSizeContextWrapper;

import java.util.Locale;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private NotificationHelper notificationHelper;
    private LocationHelper locationHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Apply saved theme and language from SharedPreferences
        applySavedTheme();
        applySavedLanguage();

        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Set up the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set up Bottom Navigation View
        BottomNavigationView navView = findViewById(R.id.nav_view);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_settings, R.id.navigation_notifications)
                .build();
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment_activity_main);
        assert navHostFragment != null;
        NavController navController = navHostFragment.getNavController();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        // Get an instance of the Firebase Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize NotificationHandler
        notificationHelper = new NotificationHelper(this);
    }

    // Notification
    public void showNotification(String productTitle, String productId) {
        notificationHelper.showNotification(productTitle, productId);
    }

    // Theme
    private void applySavedTheme() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            SharedPreferences sharedPreferences = getSharedPreferences(currentUser.getEmail(), MODE_PRIVATE);

            // Apply the saved theme
            String themeMode = sharedPreferences.getString("theme", "Light");

            if ("Dark".equals(themeMode)) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        }
    }

    // Language
    // Apply the saved language preference
    private void applySavedLanguage() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            SharedPreferences sharedPreferences = getSharedPreferences(currentUser.getEmail(), MODE_PRIVATE);
            String languageCode = sharedPreferences.getString("language", "en");
            setAppLocale(languageCode);
        }
    }
    // Helper method to set the app locale
    private void setAppLocale(String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
    }

    // Helper method to update locale for attachBaseContext
    private Context updateLocale(Context context, String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        return context.createConfigurationContext(config);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            SharedPreferences sharedPreferences = newBase.getSharedPreferences(currentUser.getEmail(), Context.MODE_PRIVATE);

            String languageCode = sharedPreferences.getString("language", "en");
            newBase = updateLocale(newBase, languageCode);

            String fontSize = sharedPreferences.getString("fontSize", "Medium");
            float textSize;

            switch (Objects.requireNonNull(fontSize)) {
                case "Small":
                    textSize = 0.85f; // Small font size
                    break;
                case "Large":
                    textSize = 1.15f; // Large font size
                    break;
                default:
                    textSize = 1.0f; // Medium font size
                    break;
            }

            newBase = FontSizeContextWrapper.wrap(newBase, textSize);
        }
        super.attachBaseContext(newBase);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Check if the user is authenticated
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            // Redirect to Login Activity if the user is not authenticated
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        } else {
            // Initialize location
            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationRequest.setInterval(5000);
            locationRequest.setFastestInterval(2000);

            locationHelper = new LocationHelper(this, db, locationRequest);
            locationHelper.getCurrentLocation();
        }
    }

    // Request location permission
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (locationHelper.isGPSEnabled()) {
                    locationHelper.getCurrentLocation();
                } else {
                    locationHelper.turnOnGPS();
                }
            }
        }
    }

    // Handle the result of the location settings dialog
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 2) {
            if (resultCode == Activity.RESULT_OK) {
                locationHelper.getCurrentLocation();
            }
        }
    }
}