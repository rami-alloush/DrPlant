package net.test.drplant;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.canhub.cropper.CropImage;

import com.canhub.cropper.CropImageView;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.ml.common.FirebaseMLException;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.custom.FirebaseCustomLocalModel;
import com.google.firebase.ml.custom.FirebaseModelDataType;
import com.google.firebase.ml.custom.FirebaseModelInputOutputOptions;
import com.google.firebase.ml.custom.FirebaseModelInputs;
import com.google.firebase.ml.custom.FirebaseModelInterpreter;
import com.google.firebase.ml.custom.FirebaseModelInterpreterOptions;
import com.google.firebase.ml.custom.FirebaseModelOutputs;
import com.google.firebase.ml.modeldownloader.CustomModel;
import com.google.firebase.ml.modeldownloader.CustomModelDownloadConditions;
import com.google.firebase.ml.modeldownloader.DownloadType;
import com.google.firebase.ml.modeldownloader.FirebaseModelDownloader;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.tensorflow.lite.Interpreter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class AnalysisActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "AnalysisActivity";
    private Context mContext;

    private static final int REQUEST_TAKE_IMG = 101;
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 102;

    private Bitmap bitmap;
    private ImageView imageView;

    // Location Information
    private GoogleApiClient mGoogleApiClient;
    private LocationCallback mLocationCallback;
    private LocationRequest mLocationRequest;
    private FusedLocationProviderClient mFusedLocationClient;
    private Location mLocation;

    private String diseaseNumberStr;
    private String diseaseProbability;
    private FirebaseModelInterpreter interpreter;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analysis);

        imageView = findViewById(R.id.imageView);
        bitmap = getInputImage();
//        firebaseMLKit();

        String action = Objects.requireNonNull(getIntent().getExtras()).getString("action");
        assert action != null;
        Log.d(TAG, "received action " + action);

        if (action.equals("loadImage")) {
            // Create intent to Open Image applications like Gallery, Google Photos
            CropImage.INSTANCE.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setFixAspectRatio(true)
                    .setMinCropResultSize(256, 256)
                    .setRequestedSize(256, 256, CropImageView.RequestSizeOptions.RESIZE_INSIDE)
                    .start(this);

        } else if (action.equals("takeImage")) {
            if (checkSelfPermission(Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA},
                        REQUEST_TAKE_IMG);
            } else {
                Log.d(TAG, "allowed to takeImage");
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_IMG);
            }
        }

        Button saveBtn = findViewById(R.id.saveBtn);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveData();
            }
        });

        // Location functions
        mContext = AnalysisActivity.this;
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        mLocationCallback = new LocationCallback() {
            // better than the old LocationListener
            /* This makes it a lot easier to deal with receiving multiple locations simultaneously
            - a case you'll run into quite often if you're properly batching location requests by setting setMaxWaitTime(). */
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    Log.i(TAG, "We received NULL location");
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    // Update location data
                    mLocation = location;
                    if (mLocation != null) {
                        Log.i(TAG, "We received location data " + String.format(getResources().getConfiguration().locale, "Long: %1$.2f Lat: %2$.2f", mLocation.getLongitude(), mLocation.getLatitude()));
                        uploadData();
                    }
                }
            }
        };
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult");

        // CropImage Activity
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.INSTANCE.getActivityResult(data);
            Log.d(TAG, "CROP_IMAGE_ACTIVITY_REQUEST_CODE");

            if (resultCode == RESULT_OK) {
                Log.d(TAG, "CROP_IMAGE_ACTIVITY_REQUEST_CODE -> RESULT_OK");

                Uri resultUri = result.getUri();
                assert resultUri != null;
                try {
                    bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(resultUri));
                    imageView.setImageBitmap(bitmap);
                    firebaseMLKit();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toast.makeText(this, R.string.no_image_picked, Toast.LENGTH_LONG).show();
                finish();
            } else {
                finish();
            }

        } else if (requestCode == REQUEST_TAKE_IMG) {

            if (resultCode == RESULT_OK && null != data) {
                // Get the Image from data
                Log.d(TAG, "Image data exists");
                Bundle extras = data.getExtras();
                assert extras != null;
                bitmap = (Bitmap) extras.get("data");
                imageView.setImageBitmap(bitmap);
                firebaseMLKit();

            } else {
                Toast.makeText(this, R.string.no_image_picked, Toast.LENGTH_LONG).show();
                finish();
            }

        } else {
            Log.d(TAG, "No proper resultCode");
        }
    }

    private void firebaseMLKit() {
        // Configure a Firebase-hosted model source
        FirebaseModelDownloadConditions.Builder conditionsBuilder =
                new FirebaseModelDownloadConditions.Builder().requireWifi();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // Enable advanced conditions on Android Nougat and newer.
            conditionsBuilder = conditionsBuilder
                    .requireCharging()
                    .requireDeviceIdle();
        }
        FirebaseModelDownloadConditions conditions = conditionsBuilder.build();

        FirebaseCustomLocalModel localModel = new FirebaseCustomLocalModel.Builder()
                .setAssetFilePath("Plant_Disease_CNN_Keras.tflite")
                .build();

        FirebaseModelInterpreter interpreter = null;
        try {
            FirebaseModelInterpreterOptions options =
                    new FirebaseModelInterpreterOptions.Builder(localModel).build();
            interpreter = FirebaseModelInterpreter.getInstance(options);
        } catch (FirebaseMLException e) {
            // ...
        }

        int imgSize = 256;
        int noClasses = 10;
        // Specify the model's input and output
        FirebaseModelInputOutputOptions inputOutputOptions = null;
        try {
            inputOutputOptions = new FirebaseModelInputOutputOptions.Builder()
                    .setInputFormat(0, FirebaseModelDataType.FLOAT32, new int[]{1, imgSize, imgSize, 3})
                    .setOutputFormat(0, FirebaseModelDataType.FLOAT32, new int[]{1, noClasses})
                    .build();
        } catch (FirebaseMLException e) {
            e.printStackTrace();
        }

        // Perform inference on input data
        int batchNum = 0;
        float[][][][] input = new float[1][imgSize][imgSize][3];
        for (int x = 0; x < imgSize; x++) {
            for (int y = 0; y < imgSize; y++) {
                int pixel = bitmap.getPixel(x, y);
                // Normalize channel values to [0.0, 1.0]. This requirement varies by
                // model. For example, some models might require values to be normalized
                // to the range [-1.0, 1.0] instead.
                input[batchNum][x][y][0] = Color.red(pixel) / 255.0f;
                input[batchNum][x][y][1] = Color.green(pixel) / 255.0f;
                input[batchNum][x][y][2] = Color.blue(pixel) / 255.0f;
            }
        }

        FirebaseModelInputs inputs = null;
        try {
            inputs = new FirebaseModelInputs.Builder()
                    .add(input)  // add() as many input arrays as your model requires
                    .build();
        } catch (FirebaseMLException e) {
            e.printStackTrace();
        }

        assert inputOutputOptions != null;
        assert inputs != null;
        Log.i(TAG, "TryingInterpret");
        interpreter.run(inputs, inputOutputOptions)
                .addOnSuccessListener(
                        new OnSuccessListener<FirebaseModelOutputs>() {
                            @Override
                            public void onSuccess(FirebaseModelOutputs result) {
                                float[][] output = result.getOutput(0);
                                float[] probabilities = output[0];
                                updateUI(probabilities);
                                Log.i(TAG, String.valueOf(max(probabilities)));
                                Log.i(TAG, String.valueOf(find(probabilities, max(probabilities))));
//                                    BufferedReader reader = null;
//                                    try {
//                                        reader = new BufferedReader(
//                                                new InputStreamReader(getAssets().open("retrained_labels.txt")));
//                                    } catch (IOException e) {
//                                        e.printStackTrace();
//                                    }
//                                    for (int i = 0; i < probabilities.length; i++) {
//                                        String label = null;
//                                        try {
//                                            label = reader.readLine();
//                                            Log.i(TAG, String.format("%s: %1.4f", label, probabilities[i]));
//                                        } catch (IOException e) {
//                                            e.printStackTrace();
//                                        }
//                                    }
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Task failed with an exception
                                Log.i(TAG, "Failed " + e.getMessage());
                                Log.i(TAG, "Failed " + e.getCause());
                                Log.i(TAG, "Failed " + Arrays.toString(e.getStackTrace()));
                            }
                        });
    }

//    private void firebaseRemoteCustom() {
//
//        CustomModelDownloadConditions conditions = new CustomModelDownloadConditions.Builder()
//                .requireWifi()  // Also possible: .requireCharging() and .requireDeviceIdle()
//                .build();
//
//        FirebaseModelDownloader.getInstance()
//                .getModel("Custom_CNN_Keras", DownloadType.LOCAL_MODEL_UPDATE_IN_BACKGROUND, conditions)
//                .addOnSuccessListener(new OnSuccessListener<CustomModel>() {
//                    @Override
//                    public void onSuccess(CustomModel model) {
//                        // Download complete. Depending on your app, you could enable the ML
//                        // feature, or switch from the local model to the remote model, etc.
//
//                        // The CustomModel object contains the local path of the model file,
//                        // which you can use to instantiate a TensorFlow Lite interpreter.
//                        Log.d(TAG, "Starting TensorFlow Lite interpreter");
//                        File modelFile = model.getFile();
//                        if (modelFile != null) {
//                            interpreter = new Interpreter(modelFile);
//
////                            Bitmap bitmap = Bitmap.createScaledBitmap(yourInputImage, 224, 224, true);
//                            ByteBuffer input = ByteBuffer.allocateDirect(224 * 224 * 3 * 4).order(ByteOrder.nativeOrder());
//                            for (int y = 0; y < 224; y++) {
//                                for (int x = 0; x < 224; x++) {
//                                    int px = bitmap.getPixel(x, y);
//
//                                    // Get channel values from the pixel value.
//                                    int r = Color.red(px);
//                                    int g = Color.green(px);
//                                    int b = Color.blue(px);
//
//                                    // Normalize channel values to [-1.0, 1.0]. This requirement depends
//                                    // on the model. For example, some models might require values to be
//                                    // normalized to the range [0.0, 1.0] instead.
//                                    float rf = (r - 127) / 255.0f;
//                                    float gf = (g - 127) / 255.0f;
//                                    float bf = (b - 127) / 255.0f;
//
//                                    input.putFloat(rf);
//                                    input.putFloat(gf);
//                                    input.putFloat(bf);
//                                }
//                            }
//
//                            int bufferSize = 1000 * java.lang.Float.SIZE / java.lang.Byte.SIZE;
//                            ByteBuffer modelOutput = ByteBuffer.allocateDirect(bufferSize).order(ByteOrder.nativeOrder());
//                            firebaseInterpreter.run(input, modelOutput);
//
//                            modelOutput.rewind();
//                            FloatBuffer probabilities = modelOutput.asFloatBuffer();
//                            Log.d(TAG, String.valueOf(probabilities));
//                        }
//                    }
//                });
//    }

    private void updateUI(float[] probabilities) {
        // Get disease info
        final float diseaseNumber = find(probabilities, max(probabilities));
        diseaseProbability = String.format(Locale.US, "%.1f", (max(probabilities) * 100)) + "%";
        diseaseNumberStr = String.valueOf(diseaseNumber);

        TextView probabilitiesTextView = findViewById(R.id.probabilitiesTextView);
        probabilitiesTextView.setText(diseaseProbability);

        if (diseaseNumber == 2) {
            TextView healthyTextView = findViewById(R.id.healthyTextView);
            healthyTextView.setVisibility(View.VISIBLE);
            healthyTextView.setText(getString(R.string.healthy));

            TextView diseaseDetailsLabel = findViewById(R.id.diseaseDetailsLabel);
            diseaseDetailsLabel.setVisibility(View.INVISIBLE);

            RecyclerView diseaseRecycler = findViewById(R.id.diseaseRecycler);
            diseaseRecycler.setVisibility(View.INVISIBLE);

            TextView pesticidesDetailsLabel = findViewById(R.id.pesticidesDetailsLabel);
            pesticidesDetailsLabel.setVisibility(View.INVISIBLE);

            RecyclerView pesticideRecycler = findViewById(R.id.pesticideRecycler);
            pesticideRecycler.setVisibility(View.INVISIBLE);

        } else {
            // Using FirebaseUI
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            // Get Pesticides
            CollectionReference pesticideColRef = db.collection("pesticides");
            Query pesticidesQuery = pesticideColRef.whereArrayContains("usedwith", String.valueOf((int) diseaseNumber));

            pesticidesQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    Log.i(TAG, "pesticidesQuery Value " + String.valueOf((int) diseaseNumber));
                    Log.i(TAG, "pesticidesQuery Size " + String.valueOf(task.getResult().size()));
                }
            });

            final RecyclerView pesticideRecyclerView = findViewById(R.id.pesticideRecycler);
            pesticideRecyclerView.setLayoutManager(new LinearLayoutManager(this));

            FirestoreRecyclerOptions pesticideOptions = new FirestoreRecyclerOptions.Builder<Pesticide>()
                    .setQuery(pesticidesQuery, Pesticide.class)
                    .build();

            // YOU HAVE TO startListening in onStart()
            final PesticideRecyclerAdapter pesticideAdapter = new PesticideRecyclerAdapter(pesticideOptions);
            pesticideRecyclerView.setItemAnimator(new DefaultItemAnimator());
            pesticideRecyclerView.setAdapter(pesticideAdapter);
            pesticideAdapter.startListening();

            // Get disease
            CollectionReference diseasesColRef = db.collection("diseases");
            Query diseasesQuery = diseasesColRef.whereEqualTo("order", diseaseNumber);

            RecyclerView recyclerView = findViewById(R.id.diseaseRecycler);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));

            // Configure recycler diseaseAdapter options:
            //  * query is the Query object defined above.
            //  * Disease.class instructs the diseaseAdapter to convert each DocumentSnapshot to a Disease object
            FirestoreRecyclerOptions options = new FirestoreRecyclerOptions.Builder<Disease>()
                    .setQuery(diseasesQuery, Disease.class)
                    .build();

            // YOU HAVE TO startListening in onStart()
            DiseaseRecyclerAdapter diseaseAdapter = new DiseaseRecyclerAdapter(options);
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            recyclerView.setAdapter(diseaseAdapter);
            diseaseAdapter.startListening();
        }
    }

    private Bitmap getInputImage() {
        Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        imageView.setImageBitmap(bitmap);
        return bitmap;
    }

    private void saveData() {
        mGoogleApiClient.connect();
        checkLocationPermissionAndExecute();
    }

    // Helper Functions
    private static float max(float... n) {
        int i = 0;
        float max = n[i];
        while (++i < n.length)
            if (n[i] > max)
                max = n[i];
        return max;
    }

    private static float find(float[] a, float target) {
        for (int i = 0; i < a.length; i++)
            if (a[i] == target)
                return i;

        return -1;
    }

    // Menu Functions
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem item = menu.add(Menu.NONE, 0, Menu.NONE, getString(R.string.upload_data));
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 0:
                saveData();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // Location Functions
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "GoogleApiClient connection successful");

        // Create location request and set its priority and frequency (every second)
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(2000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        checkLocationPermissionAndExecute();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "GoogleApiClient connection suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(TAG, "GoogleApiClient connection failed");
    }

    /*
Helper method to check for location permission before execution
 */
    private void checkLocationPermissionAndExecute() {
        if (ContextCompat.checkSelfPermission(mContext,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            Log.i(TAG, "Permission is not granted");

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(AnalysisActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Show an explanation to the user *asynchronously*
                Log.i(TAG, "Show an explanation to the user");

                showMessageOKCancel(getString(R.string.allow_location_access),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(AnalysisActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
                            }
                        });
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(AnalysisActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);

                Log.i(TAG, "No explanation needed; request the permission");
                // Try again after the request
                // checkLocationPermissionAndExecute();
            }
        } else {
            // Permission has already been granted
            Log.i(TAG, "Permission has already been granted");
            executeWithPermission();
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(AnalysisActivity.this)
                .setMessage(message)
                .setPositiveButton(R.string.OK, okListener)
                .setNegativeButton(R.string.Cancel, null)
                .create()
                .show();
    }

    @SuppressLint("MissingPermission")
    private void executeWithPermission() {
        Log.i(TAG, "Execute with permission granted");
        // Request Location Updates
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
    }

    private void uploadData() {
        // Save to Firestore and Firebase Storage

        // Add to Firestore
        final String[] newUID = {null};
        Map<String, Object> userData = new HashMap<>();
        userData.put("diseaseNumberStr", diseaseNumberStr);
        userData.put("diseaseProbability", diseaseProbability);
        userData.put("timestamp", new Timestamp(System.currentTimeMillis()));
        userData.put("location", String.format(getResources().getConfiguration().locale, "Long: %1$.2f Lat: %2$.2f", mLocation.getLongitude(), mLocation.getLatitude()));

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("usersData").add(userData)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        newUID[0] = documentReference.getId();
                        Log.d(TAG, "DocumentSnapshot userData written with ID: " + documentReference.getId());

                        // upload the image
                        // Check if new image loaded
                        if (bitmap != null) {
                            // Get the data from an ImageView as bytes
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                            byte[] data = baos.toByteArray();

                            // Upload lab test image to Firestore Storage
                            StorageReference mountainsRef = FirebaseStorage.getInstance().getReference("usersData/" + newUID[0] + ".jpg");
                            UploadTask uploadTask = mountainsRef.putBytes(data);
                            uploadTask.addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception exception) {
                                    // Handle unsuccessful uploads
                                }
                            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                                    Toast.makeText(AnalysisActivity.this, getString(R.string.data_uploaded), Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    }
                });

        // Disconnect to GoogleApiClient
        mGoogleApiClient.disconnect();
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);

    }

}