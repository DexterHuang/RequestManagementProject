package com.dexter.requestmanagement;

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

public class RegisterUser extends AppCompatActivity {
    Spinner userRoleSpinner;
    ArrayAdapter<String> userRoleAdapter;
    EditText firstNameEditText;
    EditText lastNameEditText;
    EditText emailEditText;
    EditText passwordEditText;
    Button confirmButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_user);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        userRoleSpinner = (Spinner) findViewById(R.id.UserRoleSpinner);
        userRoleAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item);

        for (UserRoleType role : UserRoleType.values()) {
            userRoleAdapter.add(role.name());
        }
        userRoleSpinner.setAdapter(userRoleAdapter);
        firstNameEditText = (EditText) findViewById(R.id.FirstNameEditText);
        lastNameEditText = (EditText) findViewById(R.id.LastNameEditText);
        emailEditText = (EditText) findViewById(R.id.EmailEditText);
        passwordEditText = (EditText) findViewById(R.id.PasswordEditText);
        confirmButton = (Button) findViewById(R.id.ConfirmButton);

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String firstName = firstNameEditText.getText().toString();
                final String lastName = lastNameEditText.getText().toString();
                final String email = emailEditText.getText().toString();
                final String password = passwordEditText.getText().toString().toLowerCase();
                final UserRoleType role = UserRoleType.valueOf(userRoleSpinner.getSelectedItem().toString());

                if (firstName.length() <= 0) {
                    Toast toast = Toast.makeText(RegisterUser.this, "First name cannot be empty", Toast.LENGTH_SHORT);
                    toast.show();
                }
                if (lastName.length() <= 0) {
                    Toast toast = Toast.makeText(RegisterUser.this, "Last name cannot be empty", Toast.LENGTH_SHORT);
                    toast.show();
                }
                if (email.length() <= 0) {
                    Toast toast = Toast.makeText(RegisterUser.this, "Email name cannot be empty", Toast.LENGTH_SHORT);
                    toast.show();
                }
                if (password.length() <= 0) {
                    Toast toast = Toast.makeText(RegisterUser.this, "Password name cannot be empty", Toast.LENGTH_SHORT);
                    toast.show();
                }
                FirebaseManager.getDatabase().child("users").orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Debug.log(dataSnapshot.toString());
                        if (dataSnapshot.getChildrenCount() == 0) {
                            User user = new User(email, firstName, lastName, role);
                            FirebaseManager.getDatabase().child("users").push().setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    FirebaseManager.getAuth().createUserWithEmailAndPassword(email.toLowerCase(), password);
                                    Toast toast = Toast.makeText(RegisterUser.this, "User Created", Toast.LENGTH_LONG);
                                    toast.show();
                                    finish();
                                }
                            });
                        } else {
                            AlertDialog.Builder builder = new AlertDialog.Builder(RegisterUser.this);
                            builder.setTitle("Error!");
                            builder.setMessage("There is already a existing user with this email");
                            builder.show();
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Debug.log(databaseError.toString());

                    }
                });
            }
        });
    }

}
