package com.unipi.unipiplishopping.ui.settings;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.unipi.unipiplishopping.R;
import com.unipi.unipiplishopping.ui.login.LoginActivity;
import com.unipi.unipiplishopping.databinding.FragmentSettingsBinding;

public class SettingsFragment extends Fragment {
    private FragmentSettingsBinding binding;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private FirebaseAuth auth;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Initialize FirebaseAuth
        auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        if (user != null) {
            String email = user.getEmail();

            // Display the logged-in user's email
            binding.settingsEmail.setText(email);

            // Initialize SharedPreferences with user-specific key
            sharedPreferences = requireActivity().getSharedPreferences(email, Context.MODE_PRIVATE);
            editor = sharedPreferences.edit();

            // Load saved data
            loadUserSettings();
        } else {
            Toast.makeText(getContext(), "No user is logged in.", Toast.LENGTH_SHORT).show();
        }

        // Set Save Button listener
        binding.buttonSaveSettings.setOnClickListener(this::saveUserSettings);

        // Set Logout Button listener
        binding.buttonLogout.setOnClickListener(v -> logout());

        // Set up language spinner
        setupLanguageSpinner();

        return root;
    }

    // Load user settings method
    private void loadUserSettings() {
        String firstName = sharedPreferences.getString("firstName", "");
        String lastName = sharedPreferences.getString("lastName", "");
        String theme = sharedPreferences.getString("theme", "Light");
        String fontSize = sharedPreferences.getString("fontSize", "Medium");

        binding.settingsFirstName.setText(firstName);
        binding.settingsLastName.setText(lastName);
        binding.settingsTheme.setSelection(getSpinnerIndex(binding.settingsTheme, theme));
        binding.fontSizeSpinner.setSelection(getSpinnerIndex(binding.fontSizeSpinner, fontSize));
    }

    private int getSpinnerIndex(AdapterView<?> spinner, String value) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equals(value)) {
                return i;
            }
        }
        return 0;
    }

    // Save user settings method
    public void saveUserSettings(View view) {
        // Get input from EditTexts
        String firstName = binding.settingsFirstName.getText().toString().trim();
        String lastName = binding.settingsLastName.getText().toString().trim();
        String selectedTheme = binding.settingsTheme.getSelectedItem().toString();
        String selectedLanguage = binding.languageSpinner.getSelectedItem().toString();

        // Get the selected font size from the spinner
        String selectedFontSize = binding.fontSizeSpinner.getSelectedItem().toString();

        // Validate inputs
        if (TextUtils.isEmpty(firstName) || TextUtils.isEmpty(lastName)) {
            Toast.makeText(requireContext(), getString(R.string.please_fill_out_all_fields), Toast.LENGTH_SHORT).show();
            return;
        }

        // Map the selected language to the appropriate language code
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

        // Save inputs to SharedPreferences
        editor.putString("firstName", firstName);
        editor.putString("lastName", lastName);
        editor.putString("theme", selectedTheme);
        editor.putString("fontSize", selectedFontSize);
        editor.putString("language", languageCode);
        editor.apply();

        Toast.makeText(requireContext(), getString(R.string.settings_saved_successfully), Toast.LENGTH_SHORT).show();

        // Restart the activity to apply the changes globally
        requireActivity().recreate();
    }

    // Logout method
    private void logout() {
        // Sign out the user from Firebase
        auth.signOut();

        // Redirect to the Login Activity
        Intent intent = new Intent(getContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    // Language Spinner setup method
    private void setupLanguageSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.language_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.languageSpinner.setAdapter(adapter);

        // Set the current language selection
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
        binding.languageSpinner.setSelection(spinnerPosition);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}