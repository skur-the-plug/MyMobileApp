package com.example.track_my_money.views.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.track_my_money.HomeFragment;
import com.example.track_my_money.PrivacyPolicyFragment;
//import com.example.track_my_money.ProfileFragment;
import com.example.track_my_money.R;
import com.example.track_my_money.RateUsFragment;
import com.example.track_my_money.ShareUsFragment;
import com.example.track_my_money.StatsFragment;
import com.example.track_my_money.TransactionHistoryFragment;
import com.example.track_my_money.Utils.Constants;
import com.example.track_my_money.ViewModel.MainViewModel;
import com.example.track_my_money.databinding.ActivityMainBinding;
import com.google.android.material.navigation.NavigationView;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    ActivityMainBinding binding;
    Calendar calendar;
    Menu botom_nav_menu, drawer_menu;

    public MainViewModel viewModel;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        replaceFragment(new HomeFragment());
        binding.navView.setCheckedItem(R.id.nav_home);

        binding.navView.bringToFront();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this,
                binding.drawerLayout,
                binding.toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        binding.drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        binding.navView.setNavigationItemSelectedListener(this);

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        Constants.setCatogeries();

        calendar = Calendar.getInstance();

        // -------------------------
        // Bottom Nav (Home, Stats, Accounts) - removed More
        // -------------------------
        botom_nav_menu = binding.navigationBar.getMenu();

        MenuItem homeItem = botom_nav_menu.findItem(R.id.bottom_home);
        MenuItem statsItem = botom_nav_menu.findItem(R.id.bottom_stats);
        MenuItem accountsItem = botom_nav_menu.findItem(R.id.bottom_accounts);

        homeItem.setOnMenuItemClickListener(item -> {
            replaceFragment(new HomeFragment());
            homeItem.setChecked(true);
            binding.toolbar.setTitle("Transactions");
            return true;
        });

        statsItem.setOnMenuItemClickListener(item -> {
            replaceFragment(new StatsFragment());
            statsItem.setChecked(true);
            binding.toolbar.setTitle("Statistics");
            return true;
        });

        accountsItem.setOnMenuItemClickListener(item -> {
            // empty / future feature
            accountsItem.setChecked(true);
            binding.toolbar.setTitle("Accounts");
            return true;
        });

        // -------------------------
        // Drawer Menu
        // -------------------------
        drawer_menu = binding.navView.getMenu();
        //MenuItem profile = drawer_menu.findItem(R.id.nav_profile);
        MenuItem home = drawer_menu.findItem(R.id.nav_home);
        MenuItem history = drawer_menu.findItem(R.id.nav_history);
        MenuItem rateUs = drawer_menu.findItem(R.id.nav_rate_us);
        MenuItem ShareUs = drawer_menu.findItem(R.id.nav_share_us);
        MenuItem privacyPolicy = drawer_menu.findItem(R.id.nav_privacy_policy);
        MenuItem LogOut = drawer_menu.findItem(R.id.nav_log_out);

        //profile.setOnMenuItemClickListener(item -> {
        //    replaceFragment(new ProfileFragment());
        //    profile.setChecked(true);
        //    binding.drawerLayout.closeDrawer(GravityCompat.START);
        //    binding.toolbar.setTitle("Your Profile");
        //    return true;
        //});

        history.setOnMenuItemClickListener(item -> {
            replaceFragment(new TransactionHistoryFragment());
            history.setChecked(true);
            binding.drawerLayout.closeDrawer(GravityCompat.START);
            binding.toolbar.setTitle("Transaction History");
            return true;
        });

        home.setOnMenuItemClickListener(item -> {
            replaceFragment(new HomeFragment());
            home.setChecked(true);
            binding.drawerLayout.closeDrawer(GravityCompat.START);
            binding.toolbar.setTitle("Transactions");
            return true;
        });

        ShareUs.setOnMenuItemClickListener(item -> {
            replaceFragment(new ShareUsFragment());
            ShareUs.setChecked(true);
            binding.drawerLayout.closeDrawer(GravityCompat.START);
            binding.toolbar.setTitle("Transactions");
            return true;
        });

        rateUs.setOnMenuItemClickListener(item -> {
            replaceFragment(new RateUsFragment());
            rateUs.setChecked(true);
            binding.drawerLayout.closeDrawer(GravityCompat.START);
            binding.toolbar.setTitle("Transactions");
            return true;
        });

        privacyPolicy.setOnMenuItemClickListener(item -> {
            replaceFragment(new PrivacyPolicyFragment());
            privacyPolicy.setChecked(true);
            binding.drawerLayout.closeDrawer(GravityCompat.START);
            binding.toolbar.setTitle("Transactions");
            return true;
        });

        LogOut.setOnMenuItemClickListener(item -> {
            Toast.makeText(MainActivity.this, "Logout clicked", Toast.LENGTH_SHORT).show();
            replaceFragment(new HomeFragment());
            LogOut.setChecked(true);
            binding.drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    private void replaceFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frameLayout, fragment)
                .commit();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return false;
    }

    @Override
    public void onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
