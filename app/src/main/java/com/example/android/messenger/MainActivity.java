package com.example.android.messenger;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private Toolbar mToolbar;

    private ViewPager viewPager;

    private SectionsPagerAdapter sectionsPagerAdapter;

    private DatabaseReference mDatabase;

    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();




        mToolbar = findViewById(R.id.mainpagetoolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Messenger");

        viewPager = (ViewPager) findViewById(R.id.tabPager);

        tabLayout = findViewById(R.id.main_tabs);

        sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        viewPager.setAdapter(sectionsPagerAdapter);

        tabLayout.setupWithViewPager(viewPager);

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser == null)
        {
            updateUI();
        }


    }



    public void updateUI()
    {
        Intent startIntent = new Intent(getApplicationContext(),StartActivity.class);
        startActivity(startIntent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.main_menu,menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);

        if(item.getItemId()==R.id.main_logout_button)
        {
            FirebaseAuth.getInstance().signOut();
            updateUI();
        }

        if(item.getItemId()==R.id.acc_settings)
        {
            Intent settingsIntent = new Intent(getApplicationContext(),SettingsActivity.class);
            startActivity(settingsIntent);
        }

        if(item.getItemId()==R.id.all_users)
        {
            Intent allUsersIntent = new Intent(getApplicationContext(),AllUsersActivity.class);
            startActivity(allUsersIntent);
        }

        return true;
    }
}