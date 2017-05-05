package com.dexter.requestmanagement;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.dexter.requestmanagement.Models.Request;
import com.dexter.requestmanagement.Models.RequestStatusType;
import com.dexter.requestmanagement.Models.User;
import com.dexter.requestmanagement.Models.UserRoleType;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class RequestDetailActivity extends AppCompatActivity {
    private TextView titleTextView;
    private TextView descriptionTextView;
    private ListView itemListView;
    private ListView agentListView;
    private Button addAgenButton;
    private ArrayAdapter<String> agentListAdapter;
    private Button dispatchButton;
    private CameraManager cameraManager;
    final HashMap<String, Bitmap> initialPhotos = new HashMap<String, Bitmap>();
    final HashMap<String, Bitmap> checkInPhotos = new HashMap<String, Bitmap>();
    final HashMap<String, Bitmap> completionPhotos = new HashMap<String, Bitmap>();
    private LinearLayout initialPhotoLayout;
    private LinearLayout checkInPhotoLayout;
    private LinearLayout completionPhotoLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        this.titleTextView = (TextView) findViewById(R.id.BuildingAndRoomNumberTextView);
        this.descriptionTextView = (TextView) findViewById(R.id.DescriptionTextView);
        this.itemListView = (ListView) findViewById(R.id.ItemListView);
        this.agentListView = (ListView) findViewById(R.id.AgentListView);
        this.addAgenButton = (Button) findViewById(R.id.AddAgentButton);
        this.dispatchButton = (Button) findViewById(R.id.DispatchButton);
        this.cameraManager = new CameraManager();
        this.initialPhotoLayout = (LinearLayout) findViewById(R.id.InitialPhotoLayout);
        this.checkInPhotoLayout = (LinearLayout) findViewById(R.id.CheckInPhotoLayout);
        this.completionPhotoLayout = (LinearLayout) findViewById(R.id.CompletionPhotoLayout);
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
                final String key = dataSnapshot.getKey();
                updateDispatchButton(request);
                Runnable onFailed = new Runnable() {
                    @Override
                    public void run() {
                        Toast toast = Toast.makeText(RequestDetailActivity.this, "Failed to load request data!", Toast.LENGTH_LONG);
                        toast.show();
                    }
                };
                cameraManager.downloadAllImages(RequestDetailActivity.this, request.getInitialPhotoUrls(), initialPhotos, 1204 * 1204, new Runnable() {
                    @Override
                    public void run() {
                        cameraManager.refreshImageScrollView(RequestDetailActivity.this, initialPhotoLayout, initialPhotos.values());
                    }
                }, onFailed);
                if (request.getStatus().equals(RequestStatusType.PROCESSING) || request.getStatus().equals(RequestStatusType.DONE)) {
                    cameraManager.downloadAllImages(RequestDetailActivity.this, request.getCheckInPhotoUrls(), checkInPhotos, 1024 * 1024, new Runnable() {
                        @Override
                        public void run() {
                            cameraManager.refreshImageScrollView(RequestDetailActivity.this, checkInPhotoLayout, checkInPhotos.values());
                        }
                    }, onFailed);
                }
                if (request.getStatus().equals(RequestStatusType.DONE)) {
                    cameraManager.downloadAllImages(RequestDetailActivity.this, request.getCompletionPhotoUrls(), completionPhotos, 1024 * 1024, new Runnable() {
                        @Override
                        public void run() {
                            cameraManager.refreshImageScrollView(RequestDetailActivity.this, completionPhotoLayout, completionPhotos.values());

                        }
                    }, onFailed);
                }
                titleTextView.setText("Building: " + request.getBuildingNumber() + "    Room: " + request.getRoomNumber());
                descriptionTextView.setText(request.getDescription());
                ArrayAdapter<String> itemListAdapter = new ArrayAdapter<String>(RequestDetailActivity.this, android.R.layout.simple_list_item_1);
                itemListView.setAdapter(itemListAdapter);
                itemListAdapter.addAll(request.getItems());
                agentListAdapter = new ArrayAdapter<String>(RequestDetailActivity.this, android.R.layout.simple_list_item_1);
                agentListView.setAdapter(agentListAdapter);
                agentListAdapter.addAll(request.getAssignedAgents());
                final ArrayList<User> agentList = new ArrayList<User>();
                FirebaseManager.getDatabase().child("users").orderByChild("role").equalTo(UserRoleType.AGENT.name()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot data : dataSnapshot.getChildren()) {
                            User user = data.getValue(User.class);
                            agentList.add(user);
                        }
                        addAgenButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(RequestDetailActivity.this);
                                builder.setTitle("Assign Agent");
                                final ArrayAdapter<User> arrayAdapter = new ArrayAdapter<>(RequestDetailActivity.this, android.R.layout.simple_list_item_1);
                                for (User agent : agentList) {
                                    arrayAdapter.add(agent);
                                }

                                builder.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        User agent = arrayAdapter.getItem(which);
                                        agentListAdapter.add(agent.getEmail());
                                        agentListAdapter.notifyDataSetChanged();
                                        request.getAssignedAgents().add(agent.getEmail());

                                        updateDispatchButton(request);
                                    }
                                });
                                builder.show();
                            }
                        });
                        progressDialog.dismiss();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast toast = Toast.makeText(RequestDetailActivity.this, databaseError.getMessage(), Toast.LENGTH_LONG);
                        toast.show();

                        finish();
                    }
                });
                agentListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(RequestDetailActivity.this);
                        builder.setTitle("You sure??");
                        builder.setMessage("You want to remove this agent from this task?");
                        builder.setNegativeButton("No", null);
                        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String agentEmail = agentListAdapter.getItem(position);
                                agentListAdapter.remove(agentEmail);
                                request.getAssignedAgents().remove(agentEmail);
                                updateDispatchButton(request);
                            }
                        });
                    }
                });
                dispatchButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        progressDialog.setMessage("Updating..");
                        progressDialog.show();
                        request.setStatus(RequestStatusType.DISPATCHED);
                        FirebaseManager.getDatabase().child("requests").child(key).setValue(request).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                progressDialog.hide();
                                Toast toast = Toast.makeText(RequestDetailActivity.this, "Dispatch Successful", Toast.LENGTH_LONG);
                                toast.show();
                            }
                        });

                    }
                });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Debug.log("Failed=[");
            }
        });
    }

    private void updateDispatchButton(Request request) {
        if (!request.getStatus().equals(RequestStatusType.PENDING)) {
            dispatchButton.setEnabled(false);
            addAgenButton.setEnabled(false);
        } else if (request.getAssignedAgents().isEmpty()) {

            dispatchButton.setEnabled(false);
        } else {
            dispatchButton.setEnabled(true);
        }
    }

}
