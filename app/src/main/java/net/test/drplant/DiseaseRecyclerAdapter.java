package net.test.drplant;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

public class DiseaseRecyclerAdapter extends FirestoreRecyclerAdapter<Disease, DiseaseRecyclerAdapter.DiseaseHolder> {

    private Context context;

    DiseaseRecyclerAdapter(@NonNull FirestoreRecyclerOptions options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull DiseaseHolder holder, int position, @NonNull Disease model) {
        final int current = position;
        holder.nameAR.setText(model.getNameAR());
        holder.nameEN.setText(model.getNameEN());
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(context, "Disease No. " + (current + 1), Toast.LENGTH_LONG).show();
                Intent intent = new Intent(context, DiseaseDetailsActivity.class);
                intent.putExtra("docID", getSnapshots().getSnapshot(current).getId());
                context.startActivity(intent);
            }
        });
    }

    @NonNull
    @Override
    public DiseaseHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.single_item, parent, false);
        context = parent.getContext();
        return new DiseaseHolder(view);
    }


    class DiseaseHolder extends RecyclerView.ViewHolder {
        private final TextView nameAR;
        private final TextView nameEN;
        private final View mView;

        DiseaseHolder(View view) {
            super(view);
            nameAR = view.findViewById(R.id.nameAR);
            nameEN = view.findViewById(R.id.nameEN);
            mView = view;
        }
    }
}
