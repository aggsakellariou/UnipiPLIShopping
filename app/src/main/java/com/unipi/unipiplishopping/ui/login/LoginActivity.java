package com.unipi.unipiplishopping.ui.login;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.unipi.unipiplishopping.MainActivity;
import com.unipi.unipiplishopping.R;

import java.util.Locale;
import java.util.Objects;

public class LoginActivity extends AppCompatActivity {
    EditText emailText, passwordText;
    Button loginButton, registerButton;
    TextView loginText, registerText, toRegisterText, toLoginText;
    Spinner languageSpinner;
    FirebaseAuth auth;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set the locale based on saved preferences
        SharedPreferences sharedPreferences = getSharedPreferences("settings", Context.MODE_PRIVATE);
        String currentLanguage = sharedPreferences.getString("language", "en");
        updateLocale(currentLanguage);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        emailText = findViewById(R.id.emailText);
        passwordText = findViewById(R.id.passwordText);
        loginButton = findViewById(R.id.loginButton);
        registerButton = findViewById(R.id.registerButton);
        loginText = findViewById(R.id.loginText);
        registerText = findViewById(R.id.registerText);
        toRegisterText = findViewById(R.id.toRegisterText);
        toLoginText = findViewById(R.id.toLoginText);
        languageSpinner = findViewById(R.id.languageLoginSpinner);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        setupLanguageSpinner();
    }

    // Login the user from the Firebase authentication
    public void login(View view) {
        String email = emailText.getText().toString().trim();
        String password = passwordText.getText().toString().trim();

        if (email.isEmpty()) {
            emailText.setError(getString(R.string.email_required));
            emailText.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            passwordText.setError(getString(R.string.password_required));
            passwordText.requestFocus();
            return;
        }

        showProgressIndicator(view, getString(R.string.logging_in));

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    hideProgressIndicator(view);

                    if (task.isSuccessful()) {
                        Intent intent = new Intent(this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        handleFirebaseError(task.getException(), view);
                    }
                });
    }

    // Register a new user to firebase authentication
    public void register(View view) {
        String email = emailText.getText().toString().trim();
        String password = passwordText.getText().toString().trim();

        if (email.isEmpty()) {
            emailText.setError(getString(R.string.email_required));
            emailText.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            passwordText.setError(getString(R.string.password_required));
            passwordText.requestFocus();
            return;
        }

        if (password.length() < 6) {
            passwordText.setError(getString(R.string.password_min_length));
            passwordText.requestFocus();
            return;
        }

        showProgressIndicator(view, getString(R.string.registering));

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    hideProgressIndicator(view);

                    if (task.isSuccessful()) {
                        Snackbar.make(view, getString(R.string.registration_successful), Snackbar.LENGTH_LONG).show();
                        Intent intent = new Intent(this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        handleFirebaseError(task.getException(), view);
                    }
                });
    }

    // Handle Firebase errors
    private void handleFirebaseError(Exception exception, View view) {
        if (exception instanceof FirebaseAuthInvalidCredentialsException) {
            Snackbar.make(view, getString(R.string.invalid_credentials), Snackbar.LENGTH_LONG).show();
        } else if (exception instanceof FirebaseAuthInvalidUserException) {
            Snackbar.make(view, getString(R.string.no_account_found), Snackbar.LENGTH_LONG).show();
        } else if (exception instanceof FirebaseNetworkException) {
            Snackbar.make(view, getString(R.string.network_error), Snackbar.LENGTH_LONG).show();
        } else {
            Snackbar.make(view, Objects.requireNonNull(Objects.requireNonNull(exception).getMessage()), Snackbar.LENGTH_LONG).show();
        }
    }

    // Show a progress indicator
    private void showProgressIndicator(View view, String message) {
        Snackbar.make(view, message, Snackbar.LENGTH_INDEFINITE).show();
    }

    // Hide the progress indicator
    @SuppressLint("ShowToast")
    private void hideProgressIndicator(View view) {
        Snackbar.make(view, "", Snackbar.LENGTH_SHORT).dismiss();
    }

    // Switch to the register view
    public void onClickLogin(View view) {
        // Hide login-related views
        loginButton.setVisibility(View.GONE);
        loginText.setVisibility(View.GONE);
        toRegisterText.setVisibility(View.GONE);

        // Show register-related views
        registerButton.setVisibility(View.VISIBLE);
        registerText.setVisibility(View.VISIBLE);
        toLoginText.setVisibility(View.VISIBLE);

        // Clear textboxes
        emailText.setText("");
        passwordText.setText("");
    }

    // Switch to the login view
    public void onClickRegister(View view) {
        // Hide register-related views
        registerButton.setVisibility(View.GONE);
        registerText.setVisibility(View.GONE);
        toLoginText.setVisibility(View.GONE);

        // Show login-related views
        loginButton.setVisibility(View.VISIBLE);
        loginText.setVisibility(View.VISIBLE);
        toRegisterText.setVisibility(View.VISIBLE);

        // Clear textboxes
        emailText.setText("");
        passwordText.setText("");
    }

    // Setup the language spinner
    private void setupLanguageSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.language_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        languageSpinner.setAdapter(adapter);

        // Set the current language selection
        SharedPreferences sharedPreferences = getSharedPreferences("settings", Context.MODE_PRIVATE);
        String currentLanguage = sharedPreferences.getString("language", "en");
        assert currentLanguage != null;
        int spinnerPosition;
        switch (currentLanguage) {
            case "el":
                spinnerPosition = 1;
                break;
            case "es":
                spinnerPosition = 2;
                break;
            default:
                spinnerPosition = 0;
                break;
        }
        languageSpinner.setSelection(spinnerPosition);

        languageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedLanguage = (String) parent.getItemAtPosition(position);
                String languageCode;
                switch (selectedLanguage) {
                    case "Ελληνικά":
                        languageCode = "el";
                        break;
                    case "Español":
                        languageCode = "es";
                        break;
                    default:
                        languageCode = "en";
                        break;
                }
                if (!languageCode.equals(currentLanguage)) {
                    updateLocale(languageCode);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("language", languageCode);
                    editor.apply();
                    recreate();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    // Update the locale of the app
    private void updateLocale(String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);

        Configuration config = new Configuration();
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
    }
}