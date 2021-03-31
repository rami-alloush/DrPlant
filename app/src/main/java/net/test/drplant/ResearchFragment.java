package net.test.drplant;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Objects;


public class ResearchFragment extends Fragment {

    private RecyclerView recyclerView;
    private ResearchRecyclerAdapter adapter;
//    private Query researchQuery = FirebaseFirestore.getInstance().collection("research").orderBy("order");

    public ResearchFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_research, container, false);
        final TextView conditionalMsg = view.findViewById(R.id.conditionalMsg);

        SearchView researchSearch = view.findViewById(R.id.researchSearch);
        researchSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.i("MySearchFinal", query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Query newResearchQuery = FirebaseFirestore.getInstance().collection("research").whereArrayContains("keywords", newText);
                ResearchRecyclerAdapter newAdapter = new ResearchRecyclerAdapter(new FirestoreRecyclerOptions.Builder<Research>()
                        .setQuery(newResearchQuery, Research.class)
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
        Query researchQuery = FirebaseFirestore.getInstance().collection("research").orderBy("order");
        FirestoreRecyclerOptions<Research> options = new FirestoreRecyclerOptions.Builder<Research>()
                .setQuery(researchQuery, Research.class)
                .build();

        researchQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                // Check if data is retrieved
                if (Objects.requireNonNull(task.getResult()).isEmpty()) {
                    conditionalMsg.setText(getString(R.string.no_content_or_internet));
                    conditionalMsg.setVisibility(View.VISIBLE);
                }
            }
        });

        Context context = view.getContext();
        recyclerView = view.findViewById(R.id.diseasesRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        // Configure recycler adapter options:
        //  * query is the Query object defined above.
        //  * Disease.class instructs the adapter to convert each DocumentSnapshot to a Disease object

        // YOU HAVE TO startListening in onStart()
        adapter = new ResearchRecyclerAdapter(options);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
        adapter.startListening();
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }
}
