package com.dexter.requestmanagement;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.dexter.requestmanagement.Models.Request;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

import static android.app.Activity.RESULT_OK;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MakeRequestFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MakeRequestFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MakeRequestFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    private Uri photoURI;
    private ProgressDialog progressDialog;

    public MakeRequestFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MakeRequestFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MakeRequestFragment newInstance(String param1, String param2) {
        MakeRequestFragment fragment = new MakeRequestFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    private TextView buildingNumberTextView;
    private TextView roomNumberTextView;
    private TextView descriptionTextView;
    private Button sendRequestButton;
    private Button addItemButton;
    private ListView itemListView;
    private ImageView photoImageView;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }

    @SuppressWarnings("VisibleForTests")
    public void trySendRequest() {
        progressDialog.setMessage("Uploading");
        String buildingNumber = buildingNumberTextView.getText().toString();
        String roomNumber = roomNumberTextView.getText().toString();
        ArrayAdapter<String> adapter = (ArrayAdapter<String>) itemListView.getAdapter();
        ArrayList<String> items = new ArrayList<String>();
        for (int i = 0; i < adapter.getCount(); i++) {
            items.add(adapter.getItem(i));
        }
        if (buildingNumber.length() <= 0) {
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());
            builder.setMessage("Please enter Building Number!")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).create().show();
            return;
        }
        if (roomNumber.length() <= 0) {
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());
            builder.setMessage("Please enter Room Number!")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).create().show();
            return;
        }
        if (photoURI == null) {
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());
            builder.setMessage("Please insert a photo!")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).create().show();
            return;
        }

        final Request request = new Request(UUID.randomUUID().toString(), buildingNumber, roomNumber, items);
        progressDialog.show();
        request.setDescription(descriptionTextView.getText().toString());
        Bitmap bitmap = BitmapFactory.decodeFile(photoFilePath);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap = Bitmap.createScaledBitmap(bitmap, 800, 600, true);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
        byte[] data = baos.toByteArray();
        FirebaseManager.getStorage().child("photos/" + request.getID()).putBytes(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                request.setPhotoUrl(downloadUrl.toString());
                FirebaseManager.getDatabase().child("requests").child(request.getID()).setValue(request).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        progressDialog.dismiss();
                        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                        ft.replace(R.id.content_frame, new requestListFragment());
                        ft.commit();
                        getActivity().getSupportFragmentManager().executePendingTransactions();
                    }
                });
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                progressDialog.setMessage("Sending image [" + (int) (taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount() * 100) + "%]");
                Debug.log("Progress");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Sorry =[");
                builder.setMessage("Failed to upload image file.. Please contact dev\n" + e.getMessage());
                e.printStackTrace();
            }
        });

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_make_request, container, false);
        // Inflate the layout for this fragment
        roomNumberTextView = (TextView) v.findViewById(R.id.RoomNumberTextView);
        buildingNumberTextView = (TextView) v.findViewById(R.id.BuildingNumberTextView);
        sendRequestButton = (Button) v.findViewById(R.id.RequestButton);
        addItemButton = (Button) v.findViewById(R.id.AddItemButton);
        itemListView = (ListView) v.findViewById(R.id.ItemListView);
        descriptionTextView = (EditText) v.findViewById(R.id.DescriptionEditText);
        final ArrayAdapter<String> itemListArrayAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1);
        itemListView.setAdapter(itemListArrayAdapter);
        photoImageView = (ImageView) v.findViewById(R.id.PhotoImageView);

        sendRequestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                trySendRequest();
            }
        });

        addItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Context context = getView().getContext();
                AlertDialog.Builder builderSingle = new AlertDialog.Builder(context);
                builderSingle.setIcon(R.drawable.ic_menu_send);
                builderSingle.setTitle("Select One Name:-");

                final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(context, android.R.layout.select_dialog_singlechoice);
                arrayAdapter.add("JobType1");
                arrayAdapter.add("JobType2");
                arrayAdapter.add("JobType3");
                arrayAdapter.add("JobType4");
                arrayAdapter.add("JobType5");

                builderSingle.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String strName = arrayAdapter.getItem(which);
                        itemListArrayAdapter.add(strName);
                        itemListArrayAdapter.notifyDataSetChanged();
                    }
                });
                builderSingle.show();
            }
        });
        itemListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final String item = itemListArrayAdapter.getItem(position);
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.ThemeOverlay_AppCompat_Dialog_Alert);
                builder.setTitle("Are you sure?");
                builder.setMessage("Do you really want to delete " + item + "from the list?");
                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        itemListArrayAdapter.remove(item);
                        itemListArrayAdapter.notifyDataSetChanged();

                    }
                });
                builder.setNegativeButton("nvm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.show();
            }
        });
        v.findViewById(R.id.TakePhotoButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });
        progressDialog = new ProgressDialog(getContext());
        return v;
    }

    static final int REQUEST_TAKE_PHOTO = 1;

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getContext().getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
            }
            if (photoFile != null) {
                photoURI = FileProvider.getUriForFile(getContext(),
                        "com.dexter.requestmanagement.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {

            photoImageView.setScaleType(ImageView.ScaleType.FIT_XY);
            photoImageView.setImageURI(photoURI);
        } else {
            Toast toast = Toast.makeText(this.getContext(), "Failed to take photo", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    private String photoFilePath;

    private File createImageFile() throws IOException {
        String imageFileName = "TempFile";
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        photoFilePath = image.getPath();
        // Save a file: path for use with ACTION_VIEW intents
        return image;
    }
    // TODO: Rename method, update argument and hook method into UI event

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
