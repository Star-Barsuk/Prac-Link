package com.example.client;

import com.example.client.databinding.ActivityRegisterBinding;
import com.example.client.network.AuthRequests;

public class RegisterActivity extends AuthBaseActivity {
    private ActivityRegisterBinding binding;

    @Override
    protected void getLayoutResource() {
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }

    @Override
    protected void initViews() {
        emailEt = binding.emailEt;
        passwordEt = binding.passwordEt;
        usernameEt = binding.usernameEt;
        progressBar = binding.progressBar;
        actionBtn = binding.signUpBtn;
        backBtn = binding.backBtn;
    }

    @Override
    protected void setupListeners() {
        binding.backBtn.setOnClickListener(v -> finish());
        binding.signUpBtn.setOnClickListener(v -> performAuthAction());
    }

    protected void performAuthAction() {
        String username = binding.usernameEt.getText().toString().trim();
        String email = binding.emailEt.getText().toString().trim();
        String password = binding.passwordEt.getText().toString().trim();

        if (!validateUsername(username) ||
                !validateEmail(email) ||
                !validatePassword(password)) {
            return;
        }

        setLoadingState(true);

        AuthRequests.register(this, username, email, password,
                (success, message, user) -> runOnUiThread(() -> {
                    setLoadingState(false);
                    if (success) {
                        finish();
                    } else {
                        handleAuthError(message);
                    }
                }));
    }
}