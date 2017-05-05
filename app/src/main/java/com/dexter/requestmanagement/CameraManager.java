package com.dexter.requestmanagement;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.dexter.requestmanagement.Models.PhotoType;
import com.dexter.requestmanagement.Models.Request;
import com.dexter.requestmanagement.Models.TempPhoto;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import static com.dexter.requestmanagement.MakeRequestFragment.REQUEST_TAKE_PHOTO;

public class CameraManager {


    public TempPhoto createTempPhoto(Context context) throws IOException {
        String imageFileName = "TempFile";
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        TempPhoto photo = new TempPhoto(image);
        // Save a file: path for use with ACTION_VIEW intents
        return photo;
    }

    TempPhoto tempPhoto;

    public void dispatchTakePictureIntent(Fragment fragment) {
        Context context = fragment.getContext();
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(context.getPackageManager()) != null) {
            try {
                tempPhoto = createTempPhoto(context);
                if (tempPhoto != null) {
                    tempPhoto.setPhotoURI(FileProvider.getUriForFile(context,
                            "com.dexter.requestmanagement.fileprovider",
                            tempPhoto.getImageFile()));
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, tempPhoto.getPhotoURI());
                    Debug.log("dispatched!");
                    fragment.startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void dispatchTakePictureIntent(Activity activity) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(activity.getPackageManager()) != null) {
            try {
                tempPhoto = createTempPhoto(activity);
                if (tempPhoto != null) {
                    tempPhoto.setPhotoURI(FileProvider.getUriForFile(activity,
                            "com.dexter.requestmanagement.fileprovider",
                            tempPhoto.getImageFile()));
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, tempPhoto.getPhotoURI());
                    Debug.log("dispatched!");
                    activity.startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    @SuppressWarnings("VisibleForTests")
    public void uploadImage(final TempPhoto tempPhoto, Request request, PhotoType photoType, final Runnable onSuccess, final Runnable onFailure) {
        Bitmap bitmap = BitmapFactory.decodeFile(tempPhoto.getFilePath());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap = Bitmap.createScaledBitmap(bitmap, 800, 600, true);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
        byte[] data = baos.toByteArray();
        FirebaseManager.getStorage().child("photos/" + request.getID() + "/" + photoType.name() + "/" + tempPhoto.getUuid()).putBytes(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                tempPhoto.setDownloadURI(taskSnapshot.getDownloadUrl());
                onSuccess.run();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                e.printStackTrace();
            }
        });
    }

    public void uploadAllTempPhoto(final Context context, final ArrayList<TempPhoto> tempPhotos, final Request request, final ArrayList<String> targetURLList, PhotoType photoType, final Runnable onComplete) {
        final ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Processing");
        progressDialog.setMessage("Uploading Image To Server..");
        progressDialog.show();
        for (final TempPhoto tempPhoto : tempPhotos) {
            uploadImage(tempPhoto, request, photoType, new Runnable() {
                @Override
                public void run() {
                    targetURLList.add(tempPhoto.getDownloadURI());
                    for (TempPhoto tp : tempPhotos) {
                        if (tp.getDownloadURI() == null) {
                            return;
                        }
                    }
                    if (onComplete != null) {
                        progressDialog.dismiss();
                        onComplete.run();
                    }
                }
            }, new Runnable() {
                @Override
                public void run() {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Sorry =[");
                    builder.setMessage("Failed to upload image file.. Please contact dev");
                }
            });
        }
    }

    public void refreshImageScrollView(Context context, ViewGroup layout, ArrayList<TempPhoto> tempPhotos, final Runnable onClick) {
        layout.removeAllViews();
        for (TempPhoto tempPhoto : tempPhotos) {
            ImageView imageView = new ImageView(context);
            layout.addView(imageView);
            imageView.setImageURI(tempPhoto.getPhotoURI());
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onClick != null) {
                        onClick.run();
                    }
                }
            });
        }

    }

    public void refreshImageScrollView(Context context, ViewGroup layout, Collection<Bitmap> bitmaps) {
        layout.removeAllViews();
        for (Bitmap bitmap : bitmaps) {
            ImageView imageView = new ImageView(context);
            layout.addView(imageView);
            imageView.setImageBitmap(bitmap);
        }

    }

    public void downloadAllImages(Context context, final Collection<String> urls, final HashMap<String,
            Bitmap> map, int byteSize, final Runnable onAllCompleted, final Runnable onFailed) {

        final ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Processing");
        progressDialog.setMessage("Downloading Image From Server..");
        progressDialog.show();
        final ArrayList<String> doneList = new ArrayList<>();
        for (final String url : urls) {
            FirebaseManager.getStorageInstance().getReferenceFromUrl(url).getBytes(byteSize).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    {
                        doneList.add(url);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        map.put(url, bitmap);
                        if (doneList.size() == urls.size()) {
                            progressDialog.dismiss();
                            onAllCompleted.run();
                        }
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    onFailed.run();
                }
            });
        }
    }
}
