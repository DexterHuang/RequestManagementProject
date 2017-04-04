package com.dexter.requestmanagement;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class RequestDetailActivity extends AppCompatActivity {
    private TextView titleTextView;
    private TextView descriptionTextView;
    private ImageView photoImageView;
    private ListView itemListView;
    private ListView agentListView;
    private Button addAgenButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        this.titleTextView = (TextView) findViewById(R.id.BuildingNumberTextView);
        this.descriptionTextView = (TextView) findViewById(R.id.DescriptionTextView);
        this.photoImageView = (ImageView) findViewById(R.id.photoImageView);
        this.itemListView = (ListView) findViewById(R.id.ItemListView);
        this.agentListView = (ListView) findViewById(R.id.AgentListView);
        this.addAgenButton = (Button) findViewById(R.id.AddAgentButton);
        String requestID = getIntent().getExtras().getString("requestID");
        if(requestID == null){
            Debug.log("RequestID is null =[");
        }
        FirebaseManager.getDatabase().child("requests").orderByChild(requestID).limitToFirst(1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Debug.log("Found!" + dataSnapshot.getValue().toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Debug.log("Failed=[");
            }
        });
    }


}
