package net.test.drplant;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class DiseaseDetailsActivity extends AppCompatActivity {

    private final String TAG = "DiseaseDetailsActivity";
    private DocumentSnapshot currentDisease;
    private String docID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_disease_details);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        docID = getIntent().getStringExtra("docID");
        db.collection("diseases")
                .document(docID)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    assert document != null;
                    if (document.exists()) {
                        currentDisease = document;
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
        setTitle(currentDisease.getString("nameAR"));

        TextView nameEN = findViewById(R.id.diseaseNameEN);
        nameEN.setText(currentDisease.getString("nameEN"));

        TextView symptoms = findViewById(R.id.symptomsContent);
        symptoms.setText(currentDisease.getString("symptoms").replace("@", "\n"));

        TextView causes = findViewById(R.id.causesContent);
        causes.setText(currentDisease.getString("causes").replace("@", "\n"));

        TextView precautions = findViewById(R.id.precautionsContent);
        precautions.setText(currentDisease.getString("precautions").replace("@", "\n"));

        TextView remedy = findViewById(R.id.remedyContent);
        remedy.setText(currentDisease.getString("remedy").replace("@", "\n"));

        // Reference to an image file in Cloud Storage
        StorageReference storageReference = FirebaseStorage.getInstance().
                getReference()
                .child("diseases/" + docID + ".jpg");

        populatePesticides();

        // ImageView in your Activity
//        ImageView imageView = findViewById(R.id.diseaseImage);
//
//        // Download directly from StorageReference using Glide
//        // (See MyAppGlideModule for Loader registration)
//        GlideApp.with(this)
//                .load(storageReference)
//                .into(imageView);

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

    private void populatePesticides() {
        CollectionReference pesticideColRef = FirebaseFirestore.getInstance().collection("pesticides");
        Query pesticidesQuery = pesticideColRef.whereArrayContains("usedwith", String.valueOf(currentDisease.getDouble("order").intValue()));
        Log.i(TAG, String.valueOf(currentDisease.getDouble("order").intValue()));

        RecyclerView pesticideRecyclerView = findViewById(R.id.pesticideRecyclerInDisease);
        pesticideRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        FirestoreRecyclerOptions pesticideOptions = new FirestoreRecyclerOptions.Builder<Pesticide>()
                .setQuery(pesticidesQuery, Pesticide.class)
                .build();

        // YOU HAVE TO startListening in onStart()
        PesticideRecyclerAdapter pesticideAdapter = new PesticideRecyclerAdapter(pesticideOptions);
        pesticideRecyclerView.setItemAnimator(new DefaultItemAnimator());
        pesticideRecyclerView.setAdapter(pesticideAdapter);
        pesticideAdapter.startListening();
    }
}
