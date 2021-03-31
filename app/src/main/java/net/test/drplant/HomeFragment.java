package net.test.drplant;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.util.Objects;

public class HomeFragment extends Fragment {

    private static final int REQUEST_TAKE_IMG = 101;

    public HomeFragment() {
        // Required empty public constructor
    }

    public static DiseasesFragment newInstance() {
        DiseasesFragment fragment = new DiseasesFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        Button pickImage = view.findViewById(R.id.loadImage);
        pickImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), AnalysisActivity.class);
                intent.putExtra("action", "loadImage");
                startActivity(intent);
            }
        });

        Button takeImage = view.findViewById(R.id.takeImage);
        takeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(
                        Objects.requireNonNull(getContext()),
                        Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.CAMERA},
                            REQUEST_TAKE_IMG);
                } else {
                    Intent intent = new Intent(getContext(), AnalysisActivity.class);
                    intent.putExtra("action", "takeImage");
                    startActivity(intent);
                }

            }
        });

        return view;
    }

}
