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

public class PesticideRecyclerAdapter extends FirestoreRecyclerAdapter<Pesticide, PesticideRecyclerAdapter.PesticideHolder> {

    private Context context;

    PesticideRecyclerAdapter(@NonNull FirestoreRecyclerOptions options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull PesticideHolder holder, int position, @NonNull Pesticide model) {
        final int current = position;
        holder.nameAR.setText(model.getNameAR());
        holder.nameEN.setText(model.getNameEN());
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PesticideDetailsActivity.class);
                intent.putExtra("docID", getSnapshots().getSnapshot(current).getId());
                context.startActivity(intent);
            }
        });
    }

    @NonNull
    @Override
    public PesticideHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.single_item, parent, false);
        context = parent.getContext();
        view.setBackgroundColor(context.getResources().getColor(R.color.colorBKG));
        return new PesticideHolder(view);
    }

    class PesticideHolder extends RecyclerView.ViewHolder {
        private final TextView nameAR;
        private final TextView nameEN;
        private final View mView;

        PesticideHolder(View view) {
            super(view);
            nameAR = view.findViewById(R.id.nameAR);
            nameEN = view.findViewById(R.id.nameEN);
            mView = view;
        }
    }
}
