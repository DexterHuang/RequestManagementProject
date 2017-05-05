package com.dexter.requestmanagement;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.dexter.requestmanagement.Models.PhotoType;
import com.dexter.requestmanagement.Models.Request;
import com.dexter.requestmanagement.Models.RequestStatusType;
import com.dexter.requestmanagement.Models.TempPhoto;
import com.dexter.requestmanagement.Models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

import static com.dexter.requestmanagement.MakeRequestFragment.REQUEST_TAKE_PHOTO;

public class AgentRequestActivity extends AppCompatActivity {

    private TextView titleTextView;
    private TextView descriptionTextView;
    private ImageView photoImageView;
    private ListView itemListView;
    private ListView agentListView;
    private ArrayAdapter<User> agentListAdapter;

    private LinearLayout initialPhotoLayout;
    private LinearLayout checkInPhotoLayout;
    private LinearLayout completionPhotoLayout;
    private CameraManager cameraManager;
    private Button checkInPhotoButton;
    private Button completionPhotoButton;
    final HashMap<String, Bitmap> initialPhotos = new HashMap<String, Bitmap>();
    final HashMap<String, Bitmap> checkInPhotos = new HashMap<String, Bitmap>();
    final HashMap<String, Bitmap> completionPhotos = new HashMap<String, Bitmap>();
    private Request request;
    private Button checkInButton;
    private Button completionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agent_request);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        titleTextView = (TextView) findViewById(R.id.BuildingAndRoomNumberTextView);
        descriptionTextView = (TextView) findViewById(R.id.DescriptionTextView);
        itemListView = (ListView) findViewById(R.id.ItemListView);
        agentListView = (ListView) findViewById(R.id.AgentListView);
        this.initialPhotoLayout = (LinearLayout) findViewById(R.id.InitialPhotoLayout);
        this.checkInPhotoLayout = (LinearLayout) findViewById(R.id.CheckInPhotoLayout);
        this.completionPhotoLayout = (LinearLayout) findViewById(R.id.CompletionPhotoLayout);
        cameraManager = new CameraManager();
        final ArrayAdapter<String> itemAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        itemListView.setAdapter(itemAdapter);
        final ArrayAdapter<String> agentAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        agentListView.setAdapter(agentAdapter);
        checkInPhotoButton = (Button) findViewById(R.id.TakeCheckInPhotoButton);
        completionPhotoButton = (Button) findViewById(R.id.TakeCompletionPhotoButton);
        checkInButton = (Button) findViewById(R.id.CheckInButton);
        completionButton = (Button) findViewById(R.id.CompletionButton);
        checkInButton.setEnabled(false);
        completionButton.setEnabled(false);

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
                request = dataSnapshot.getValue(Request.class);
                final String key = dataSnapshot.getKey();
                final Runnable onFailed = new Runnable() {
                    @Override
                    public void run() {
                        Toast toast = Toast.makeText(AgentRequestActivity.this, "Failed to load request data!", Toast.LENGTH_LONG);
                        toast.show();
                        NavigationHelper.showMainActivity(AgentRequestActivity.this, R.id.nav_pendingRequestList);
                    }
                };
                titleTextView.setText(request.toString());
                descriptionTextView.setText(request.getDescription());
                itemAdapter.addAll(request.getItems());
                agentAdapter.addAll(request.getAssignedAgents());
                cameraManager.downloadAllImages(AgentRequestActivity.this, request.getInitialPhotoUrls(), initialPhotos, 1024 * 1024, new Runnable() {
                    @Override
                    public void run() {
                        cameraManager.refreshImageScrollView(AgentRequestActivity.this, initialPhotoLayout, initialPhotos.values());
                    }
                }, onFailed);

                if (request.getStatus().equals(RequestStatusType.PROCESSING) || request.getStatus().equals(RequestStatusType.DONE)) {
                    cameraManager.downloadAllImages(AgentRequestActivity.this, request.getCheckInPhotoUrls(), checkInPhotos, 1024 * 1024, new Runnable() {
                        @Override
                        public void run() {
                            cameraManager.refreshImageScrollView(AgentRequestActivity.this, checkInPhotoLayout, checkInPhotos.values());
                        }
                    }, onFailed);
                }
                if (request.getStatus().equals(RequestStatusType.DONE)) {
                    cameraManager.downloadAllImages(AgentRequestActivity.this, request.getCompletionPhotoUrls(), completionPhotos, 1024 * 1024, new Runnable() {
                        @Override
                        public void run() {
                            cameraManager.refreshImageScrollView(AgentRequestActivity.this, completionPhotoLayout, completionPhotos.values());

                        }
                    }, onFailed);
                }
                progressDialog.dismiss();


                if (request.getStatus().equals(RequestStatusType.DISPATCHED)) {
                    checkInPhotoButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            cameraManager.dispatchTakePictureIntent(AgentRequestActivity.this);
                        }
                    });
                } else {
                    checkInPhotoButton.setEnabled(false);
                }
                if (request.getStatus().equals(RequestStatusType.PROCESSING)) {
                    completionPhotoButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            cameraManager.dispatchTakePictureIntent(AgentRequestActivity.this);
                        }
                    });
                } else {
                    completionPhotoButton.setEnabled(false);
                }
                checkInButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        cameraManager.uploadAllTempPhoto(AgentRequestActivity.this, checkInTempPhotos, request, request.getCheckInPhotoUrls(), PhotoType.CHECK_IN, new Runnable() {
                            @Override
                            public void run() {
                                request.setStatus(RequestStatusType.PROCESSING);
                                final ProgressDialog progressDialog = new ProgressDialog(AgentRequestActivity.this);
                                progressDialog.setTitle("Processing");
                                progressDialog.setMessage("Updating Request Status..");
                                progressDialog.show();
                                FirebaseManager.getDatabase().child("requests").child(key).setValue(request).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        progressDialog.dismiss();
                                        finish();
                                        startActivity(getIntent());
                                    }
                                });
                            }
                        });
                    }
                });
                completionButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        cameraManager.uploadAllTempPhoto(AgentRequestActivity.this, completionTempPhotos, request, request.getCompletionPhotoUrls(), PhotoType.COMPLETION, new Runnable() {
                            @Override
                            public void run() {
                                request.setStatus(RequestStatusType.DONE);
                                final ProgressDialog progressDialog = new ProgressDialog(AgentRequestActivity.this);
                                progressDialog.setTitle("Processing");
                                progressDialog.setMessage("Updating Request Status..");
                                progressDialog.show();
                                FirebaseManager.getDatabase().child("requests").child(key).setValue(request).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        progressDialog.dismiss();
                                        finish();
                                        startActivity(getIntent());
                                    }
                                });
                            }
                        });
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private ArrayList<TempPhoto> checkInTempPhotos = new ArrayList<>();
    private ArrayList<TempPhoto> completionTempPhotos = new ArrayList<>();

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            ArrayList<TempPhoto> tempPhotos = null;
            LinearLayout layout = null;
            Runnable onClick = null;
            if (request.getStatus().equals(RequestStatusType.DISPATCHED)) {
                tempPhotos = checkInTempPhotos;
                layout = checkInPhotoLayout;
                checkInButton.setEnabled(true);
                onClick = new Runnable() {
                    @Override
                    public void run() {
                        AlertDialog.Builder builder = new AlertDialog.Builder(AgentRequestActivity.this);
                        builder.setTitle("Are you sure?");
                        builder.setMessage("You sure want to delete all photos?");
                        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                checkInTempPhotos.clear();
                                checkInPhotoLayout.removeAllViews();
                                checkInButton.setEnabled(false);
                            }
                        });
                        builder.setNegativeButton("No", null);
                        builder.show();
                    }
                };
            } else if (request.getStatus().equals(RequestStatusType.PROCESSING)) {
                tempPhotos = completionTempPhotos;
                layout = completionPhotoLayout;
                completionButton.setEnabled(true);
                onClick = new Runnable() {
                    @Override
                    public void run() {
                        AlertDialog.Builder builder = new AlertDialog.Builder(AgentRequestActivity.this);
                        builder.setTitle("Are you sure?");
                        builder.setMessage("You sure want to delete all photos?");
                        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                completionTempPhotos.clear();
                                completionPhotoLayout.removeAllViews();
                                checkInButton.setEnabled(false);
                            }
                        });
                        builder.setNegativeButton("No", null);
                        builder.show();
                    }
                };
            }
            tempPhotos.add(cameraManager.tempPhoto);
            cameraManager.refreshImageScrollView(this, layout, tempPhotos, onClick);
        } else {
            Toast toast = Toast.makeText(this, "Failed to take photo", Toast.LENGTH_SHORT);
            toast.show();
        }
    }
}
