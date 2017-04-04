package com.dexter.requestmanagement;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class FirebaseManager {

    private static DatabaseReference database;
    private static StorageReference storage;

    public static DatabaseReference getDatabase() {
        if (database == null) {
            database = FirebaseDatabase.getInstance().getReference();
        }
        return database;
    }

    public static StorageReference getStorage() {
        if (storage == null) {
            storage = FirebaseStorage.getInstance().getReference();
        }
        return storage;
    }

}
