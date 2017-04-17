package com.dexter.requestmanagement;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.dexter.requestmanagement.Models.Request;
import com.dexter.requestmanagement.Models.User;
import com.dexter.requestmanagement.Models.UserRoleType;
import com.google.android.gms.common.ErrorDialogFragment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

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
                                        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(RequestDetailActivity.this, android.R.layout.simple_list_item_1);
                                        for (User agent : agentList) {
                                            arrayAdapter.add(agent.getFirstName() + " " + agent.getLastName());
                                        }

                                        builder.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                agentListAdapter.add(arrayAdapter.getItem(which));
                                                agentListAdapter.notifyDataSetChanged();
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
                                        agentListAdapter.remove(agentListAdapter.getItem(position));
                                    }
                                });
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


}
