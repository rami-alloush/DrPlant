package net.test.drplant;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ResearchDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_research_details);

        String nameAR = getIntent().getStringExtra("nameAR");
        setTitle(nameAR);

        String date = getIntent().getStringExtra("date");
        TextView dateView = findViewById(R.id.date);
        dateView.setText(date);

        String corp = getIntent().getStringExtra("corp");
        TextView corpView = findViewById(R.id.corp);
        corpView.setText(corp);

        String content = getIntent().getStringExtra("content");
        TextView contentView = findViewById(R.id.researchContent);
        contentView.setText(content);

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
