package net.test.drplant;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Objects;

public class DiseasesFragment extends Fragment {

    private DiseaseRecyclerAdapter adapter;
    private RecyclerView recyclerView;

    public DiseasesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_diseases, container, false);
        final TextView conditionalMsg = view.findViewById(R.id.conditionalMsg);

        SearchView researchSearch = view.findViewById(R.id.diseaseSearch);
        researchSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Query newResearchQuery = FirebaseFirestore.getInstance().collection("diseases").whereArrayContains("keywords", newText);
                DiseaseRecyclerAdapter newAdapter = new DiseaseRecyclerAdapter(new FirestoreRecyclerOptions.Builder<Disease>()
                        .setQuery(newResearchQuery, Disease.class)
                        .build());
                newAdapter.startListening();
                recyclerView.setAdapter(newAdapter);
                if (newText.equals("")) {
                    recyclerView.setAdapter(adapter);
                }
                return false;
            }
        });

        // Using FirebaseUI
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference diseasesColRef = db.collection("diseases");
        Query diseasesQuery = diseasesColRef.orderBy("order");
        diseasesQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                // Check if data is retrieved
                if (Objects.requireNonNull(task.getResult()).isEmpty()) {
                    conditionalMsg.setText(getString(R.string.no_content_or_internet));
                }
            }
        });

        Context context = view.getContext();
        recyclerView = view.findViewById(R.id.diseasesRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        // Configure recycler adapter options:
        //  * query is the Query object defined above.
        //  * Disease.class instructs the adapter to convert each DocumentSnapshot to a Disease object
        FirestoreRecyclerOptions options = new FirestoreRecyclerOptions.Builder<Disease>()
                .setQuery(diseasesQuery, Disease.class)
                .build();

        // YOU HAVE TO startListening in onStart()
        adapter = new DiseaseRecyclerAdapter(options);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
//        adapter.startListening();

        // Create ArrayAdapter and ArrayList for the Spinner
//        List<String> list = new ArrayList<String>();
//        list.add("محاصيل الخضار");
//        list.add("---- محصول الطماطم");
//        list.add("محاصيل الفواكة");
//        list.add("محاصيل أخرى");
//        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this.getActivity(),
//                android.R.layout.simple_spinner_item, list);
//        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//
//        Spinner spinnerCrops = view.findViewById(R.id.spinnerCrops);
//        spinnerCrops.setAdapter(dataAdapter);
//
//        spinnerCrops.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                if (position < 2) {
//                    adapter.startListening();
//                } else {
//                    adapter.stopListening();
//                }
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//
//            }
//        });

        return view;
    }

}