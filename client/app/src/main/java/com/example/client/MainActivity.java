package com.example.client;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.client.bottom_nav.practice.PracticeFragment;
import com.example.client.bottom_nav.chats.ChatsFragment;
import com.example.client.bottom_nav.users.UsersFragment;
import com.example.client.bottom_nav.profile.ProfileFragment;
import com.example.client.view_models.UserViewModel;
import com.example.client.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        observeData();
        setupFragments();
        setupBottomNavigation();
        setupBackPress();
    }

    private void observeData() {
        UserViewModel.getInstance().getCurrentUser().observe(this, user -> {
            if (user == null) {
                startActivity(new Intent(this, LoginActivity.class));
                finishAffinity();
            }
        });
    }

    private void setupFragments() {
        replaceFragment(new PracticeFragment());
        binding.bottomNav.setSelectedItemId(R.id.practice);
    }

    private void setupBottomNavigation() {
        binding.bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.chats) {
                replaceFragment(new ChatsFragment());
            } else if (itemId == R.id.new_chat) {
                replaceFragment(new UsersFragment());
            } else if (itemId == R.id.profile) {
                replaceFragment(new ProfileFragment());
            } else if (itemId == R.id.practice) {
                replaceFragment(new PracticeFragment());
            }
            return true;
        });
    }

    private void replaceFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(binding.fragmentContainer.getId(), fragment)
                .commit();
    }

    private void setupBackPress() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (binding.bottomNav.getSelectedItemId() != R.id.practice) {
                    binding.bottomNav.setSelectedItemId(R.id.practice);
                } else {
                    finishAffinity();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        UserViewModel.getInstance().logout();
    }
}