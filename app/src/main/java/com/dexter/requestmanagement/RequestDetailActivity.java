package com.dexter.requestmanagement;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.dexter.requestmanagement.Models.Request;
import com.google.android.gms.common.ErrorDialogFragment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
    private ArrayAdapter<String> agentListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        this.titleTextView = (TextView) findViewById(R.id.BuildingAndRoomNumberTextView);
        this.descriptionTextView = (TextView) findViewById(R.id.DescriptionTextView);
        this.photoImageView = (ImageView) findViewById(R.id.photoImageView);
        this.itemListView = (ListView) findViewById(R.id.ItemListView);
        this.agentListView = (ListView) findViewById(R.id.AgentListView);
        this.addAgenButton = (Button) findViewById(R.id.AddAgentButton);
        final String requestID = getIntent().getExtras().getString("requestID");
        if (requestID == null) {
            Debug.log("RequestID is null =[");
        }
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading..");
        progressDialog.show();
        FirebaseManager.getDatabase().child("requests").child(requestID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final Request request = dataSnapshot.getValue(Request.class);
                FirebaseManager.getStorage().child("photos/" + request.getID()).getBytes(1204 * 1204).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.hide();
                        ErrorDialogFragment d = new ErrorDialogFragment();
                        d.show(getFragmentManager(), "Could not download the image! :(");
                    }
                }).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        photoImageView.setImageBitmap(bitmap);
                        titleTextView.setText("Building: " + request.getBuildingNumber() + "    Room: " + request.getRoomNumber());
                        descriptionTextView.setText(request.getDescription());
                        ArrayAdapter<String> itemListAdapter = new ArrayAdapter<String>(RequestDetailActivity.this, android.R.layout.simple_list_item_1);
                        itemListView.setAdapter(itemListAdapter);
                        itemListAdapter.addAll(request.getItems());
                        agentListAdapter = new ArrayAdapter<String>(RequestDetailActivity.this, android.R.layout.simple_list_item_1);
                        agentListView.setAdapter(agentListAdapter);
                        progressDialog.hide();
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Debug.log("Failed=[");
            }
        });
    }


}
