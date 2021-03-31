package net.test.drplant;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class PesticideDetailsActivity extends AppCompatActivity {

    private final String TAG = "PesticideDetails";
    private DocumentSnapshot currentPesticide;
    private String docID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pesticide_details);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        docID = getIntent().getStringExtra("docID");
        db.collection("pesticides")
                .document(docID)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    assert document != null;
                    if (document.exists()) {
                        currentPesticide = document;
                        populateFields();
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });

    }

    private void populateFields() {
        setTitle(currentPesticide.getString("nameAR"));

        TextView nameEN = findViewById(R.id.pesticideNameEN);
        nameEN.setText(currentPesticide.getString("nameEN"));

        TextView detailsContent = findViewById(R.id.detailsContent);
        detailsContent.setText(currentPesticide.getString("details").replace("@", "\n"));

        TextView usage = findViewById(R.id.usageContent);
        usage.setText(currentPesticide.getString("usage"));

        TextView warnings = findViewById(R.id.warningsContent);
        warnings.setText(currentPesticide.getString("warnings").replace("@", "\n"));


        // Reference to an image file in Cloud Storage
        StorageReference storageReference = FirebaseStorage.getInstance().
                getReference()
                .child("pesticides/" + docID + ".jpg");

        // ImageView in your Activity
        ImageView imageView = findViewById(R.id.pesticideImage);

        // Download directly from StorageReference using Glide
        // (See MyAppGlideModule for Loader registration)
        Glide.with(this)
                .load(storageReference)
                .into(imageView);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return (super.onOptionsItemSelected(item));
    }
}
