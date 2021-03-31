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

public class ResearchRecyclerAdapter extends FirestoreRecyclerAdapter<Research, ResearchRecyclerAdapter.ResearchHolder> {

    private Context context;

    ResearchRecyclerAdapter(@NonNull FirestoreRecyclerOptions options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull ResearchHolder holder, int position, @NonNull final Research model) {
        holder.nameAR.setText(model.getNameAR());
        holder.corp.setText(model.getcorp());

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ResearchDetailsActivity.class);
                intent.putExtra("nameAR", model.getNameAR());
                intent.putExtra("date", String.valueOf(model.getTimestamp().toDate()));
                intent.putExtra("corp", model.getcorp());
                intent.putExtra("content", model.getContent());
                context.startActivity(intent);
            }
        });
    }

    @NonNull
    @Override
    public ResearchHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.single_item, parent, false);
        context = parent.getContext();
        return new ResearchHolder(view);
    }


    class ResearchHolder extends RecyclerView.ViewHolder {
        private final TextView nameAR;
        private final TextView corp;
        private final View mView;

        ResearchHolder(View view) {
            super(view);
            nameAR = view.findViewById(R.id.nameAR);
            corp = view.findViewById(R.id.nameEN);
            mView = view;
        }
    }
}
