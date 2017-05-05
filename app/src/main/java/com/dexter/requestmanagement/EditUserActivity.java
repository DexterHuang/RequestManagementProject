package com.dexter.requestmanagement;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.dexter.requestmanagement.Models.User;
import com.dexter.requestmanagement.Models.UserRoleType;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class EditUserActivity extends AppCompatActivity {


    Spinner userRoleSpinner;
    ArrayAdapter<String> userRoleAdapter;
    EditText nameEditText;
    EditText emailEditText;
    Button saveChangeButton;
    Button removeUserButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        userRoleSpinner = (Spinner) findViewById(R.id.UserRoleSpinner);
        userRoleAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item);

        for (UserRoleType role : UserRoleType.values()) {
            userRoleAdapter.add(role.name());
        }
        userRoleSpinner.setAdapter(userRoleAdapter);
        nameEditText = (EditText) findViewById(R.id.NameEditText);
        nameEditText.setEnabled(false);
        emailEditText = (EditText) findViewById(R.id.EmailEditText);
        emailEditText.setEnabled(false);
        saveChangeButton = (Button) findViewById(R.id.SaveChangeButton);
        removeUserButton = (Button) findViewById(R.id.RemoveUserButton);

        final String userEmail = getIntent().getExtras().getString("userEmail");
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading user information..");
        progressDialog.setTitle("Loading");
        progressDialog.show();
        FirebaseManager.getDatabase().child("users").orderByChild("email").equalTo(userEmail).limitToFirst(1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                progressDialog.dismiss();
                if (dataSnapshot.getChildrenCount() >= 1) {
                    DataSnapshot data = dataSnapshot.getChildren().iterator().next();
                    final User user = data.getValue(User.class);
                    if (user == null) {
                        return;
                    }
                    final String key = data.getKey();
                    nameEditText.setText(user.getFirstName() + "  " + user.getLastName());
                    emailEditText.setText(user.getEmail());
                    userRoleSpinner.setSelection(user.getRole().ordinal());
                    saveChangeButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            UserRoleType role = UserRoleType.values()[userRoleSpinner.getSelectedItemPosition()];
                            user.setRole(role);
                            progressDialog.setMessage("Saving user information..");
                            progressDialog.setTitle("Saving..");
                            progressDialog.show();
                            FirebaseManager.getDatabase().child("users").child(key).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    progressDialog.dismiss();
                                    Toast toast = Toast.makeText(EditUserActivity.this, "Successfully saved", Toast.LENGTH_SHORT);
                                    toast.show();
                                    finish();
                                }
                            });
                        }
                    });
                    removeUserButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(EditUserActivity.this);
                            builder.setTitle("You sure?");
                            builder.setMessage("Are you sure you want to remove this user permanently?");
                            builder.setNegativeButton("No", null);
                            builder.setPositiveButton("Yes!", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    progressDialog.setMessage("Removing User..");
                                    progressDialog.setTitle("Removing..");
                                    progressDialog.show();
                                    FirebaseManager.getDatabase().child("users").child(key).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            progressDialog.dismiss();
                                            Toast toast = Toast.makeText(EditUserActivity.this, "Successfully removed", Toast.LENGTH_SHORT);
                                            toast.show();
                                            finish();
                                        }
                                    });
                                }
                            });
                            builder.show();
                        }
                    });
                } else {
                    Toast toast = Toast.makeText(EditUserActivity.this, "Cant find user with this email", Toast.LENGTH_LONG);
                    toast.show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        NavigationHelper.setBackBundle(this, R.id.nav_registerUser);
        super.onBackPressed();
    }
}
