package com.dexter.requestmanagement.Models;

import android.net.Uri;

import java.io.File;
import java.util.UUID;

public class TempPhoto {

    String filePath;
    File imageFile;
    Uri photoURI;
    UUID uuid;
    Uri downloadURI;

    public TempPhoto(File imageFile) {
        this.imageFile = imageFile;
        this.filePath = imageFile.getPath();
        this.uuid = UUID.randomUUID();
    }

    public String getFilePath() {
        return filePath;
    }

    public File getImageFile() {
        return imageFile;
    }

    public Uri getPhotoURI() {
        return photoURI;
    }

    public void setPhotoURI(Uri photoURI) {
        this.photoURI = photoURI;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getDownloadURI() {
        if (downloadURI == null) {
            return null;
        }
        return downloadURI.toString();
    }

    public void setDownloadURI(Uri downloadURI) {
        this.downloadURI = downloadURI;
    }
}
