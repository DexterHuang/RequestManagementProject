package com.dexter.requestmanagement;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.dexter.requestmanagement.Models.ServiceType;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class CreateServiceActivity extends AppCompatActivity {

    private EditText serviceNameEditText;
    private EditText priceEditText;
    private EditText descriptionEditText;
    private Button confirmButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_service);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        serviceNameEditText = (EditText) findViewById(R.id.ServiceNameEditText);
        priceEditText = (EditText) findViewById(R.id.PriceEditText);
        descriptionEditText = (EditText) findViewById(R.id.DescriptionEditText);
        confirmButton = (Button) findViewById(R.id.ConfirmButton);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = serviceNameEditText.getText().toString();
                String priceString = priceEditText.getText().toString();
                String description = descriptionEditText.getText().toString();

                if(name.length() <= 0 ){
                    AlertDialog.Builder builder = new AlertDialog.Builder(CreateServiceActivity.this);
                    builder.setTitle("ERROR!");
                    builder.setMessage("Name cannot be empty!");
                    return;
                }
                float price;
                try{
                    price = Float.parseFloat(priceString);
                }catch(Exception e){
                    AlertDialog.Builder builder = new AlertDialog.Builder(CreateServiceActivity.this);
                    builder.setTitle("ERROR!");
                    builder.setMessage(e.getMessage());
                    return;
                }
                if(price <= 0){
                    AlertDialog.Builder builder = new AlertDialog.Builder(CreateServiceActivity.this);
                    builder.setTitle("ERROR!");
                    builder.setMessage("Price has to a positive value.");
                }

                ServiceType service = new ServiceType(name, price);
                service.setDescription(description);
                final ProgressDialog progressDialog = new ProgressDialog(CreateServiceActivity.this);
                progressDialog.setTitle("Saving..");
                progressDialog.setMessage("Saving Service please wait..");
                progressDialog.show();
                FirebaseManager.getDatabase().child("services").push().setValue(service).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        progressDialog.dismiss();
                        finish();
                    }
                });
            }
        });

    }

    @Override
    public void onBackPressed() {
        NavigationHelper.setBackBundle(this, R.id.nav_createService);
        super.onBackPressed();
    }
}
