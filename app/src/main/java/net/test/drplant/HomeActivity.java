package net.test.drplant;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

public class
HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        // load default home fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.flContent, new HomeFragment()).commit();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Create a new fragment and specify the fragment to show based on nav item clicked
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.flContent);
        Class fragmentClass = null;

        // Handle navigation view item clicks here.
        switch (item.getItemId()) {
            case R.id.navigation_home:
                fragmentClass = HomeFragment.class;
                setTitle(item.getTitle());
                break;

            case R.id.navigation_research:
                fragmentClass = ResearchFragment.class;
                setTitle(item.getTitle());
                break;

            case R.id.navigation_diseases:
                fragmentClass = DiseasesFragment.class;
                setTitle(item.getTitle());
                break;

            case R.id.nav_share:
                Intent share = new Intent(Intent.ACTION_SEND);
                share.setType("text/plain");
                share.putExtra(Intent.EXTRA_TEXT, "Check this amazing app for plants diseases, It's called Dr. Planet http://bit.ly/Dr_Planet");
                startActivity(Intent.createChooser(share, getString(R.string.app_name)));
                break;

            case R.id.navigation_communication:
                sendEmail();
                break;

            default:
                fragmentClass = HomeFragment.class;
        }

        try {
            fragment = (Fragment) fragmentClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Insert the fragment by replacing any existing fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        assert fragment != null;
        fragmentManager.beginTransaction().replace(R.id.flContent, fragment).commit();

        // Highlight the selected item has been done by NavigationView
        item.setChecked(true);

        // Close the navigation drawer
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /*
    Helper method to send email
    */
    @SuppressLint("IntentReset")
    private void sendEmail() {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setType("text/plain");
        intent.setData(Uri.parse("mailto:info@naarrc.sa"));
        intent.putExtra(Intent.EXTRA_EMAIL, "info@naarrc.sa");
        intent.putExtra(Intent.EXTRA_SUBJECT, "Email from Dr. Plant app");
        intent.putExtra(Intent.EXTRA_TEXT, "Place your email message here ...");
        startActivity(Intent.createChooser(intent, "Send Email"));
    }
}
