package com.dexter.requestmanagement;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.dexter.requestmanagement.Models.Request;
import com.dexter.requestmanagement.Models.User;
import com.dexter.requestmanagement.Models.UserRoleType;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        MakeRequestFragment.OnFragmentInteractionListener,
        requestListFragment.OnListFragmentInteractionListener,
        UserListFragment.OnFragmentInteractionListener,
        ServiceListFragment.OnFragmentInteractionListener,
        PendingRequestListFragment.OnFragmentInteractionListener {

    private User currentUser;
    private DatabaseReference mDatabase;

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void onListFragmentInteraction(Request item) {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View headerView = navigationView.getHeaderView(0);
        TextView emailTextView = (TextView) headerView.findViewById(R.id.emailTextView);
        String i = "TestingPotato@Gmail.com";
        try {
            if (getIntent().getExtras().containsKey("ID")) {
                i = getIntent().getExtras().getString("ID");
            }
        } catch (Exception e) {
        }
        if (i.equals("TestingPotato@Gmail.com")) {
            FirebaseAuth.getInstance().signInWithEmailAndPassword("admin@localhost.com", "123456789");
        }
        final String id = i.toLowerCase();
        emailTextView.setText(id);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        final DatabaseReference userRef = mDatabase.child("users");
        Query users = userRef.orderByChild("email").equalTo(id);
        users.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()) {
                    User u = dataSnapshot.getValue(User.class);
                    currentUser = u;
                } else {
                    currentUser = new User(id, Math.random() + "", "tatopo", UserRoleType.ADMIN);
                    userRef.push().setValue(currentUser);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "Failed to retrieve your user data",
                        Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        displayView(R.id.nav_makeRequest);


    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        displayView(item.getItemId());
        return true;
    }

    public void displayView(int viewId) {

        Fragment fragment = null;
        String title = getString(R.string.app_name);

        switch (viewId) {
            case R.id.nav_makeRequest:
                fragment = new MakeRequestFragment();
                title = "Make Request";

                break;
            case R.id.nav_requestList:
                fragment = new requestListFragment();
                title = "Request List";
                break;
            case R.id.nav_registerUser:
                fragment = new UserListFragment();
                title = "User List";
                break;
            case R.id.nav_createService:
                fragment = new ServiceListFragment();
                title = "Service List";
                break;
            case R.id.nav_pendingRequestList:
                fragment = new PendingRequestListFragment();
                title = "Pending Request List";
                break;
        }

        if (fragment != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content_frame, fragment);
            ft.commit();
            getSupportFragmentManager().executePendingTransactions();
        }

        // set the toolbar title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
    }
}
