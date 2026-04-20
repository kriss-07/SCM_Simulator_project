package com.example.scmsimulator.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.scmsimulator.managers.Storage;
import com.example.scmsimulator.R;
import com.example.scmsimulator.models.CrewMember;
import com.example.scmsimulator.models.Location;
import java.util.ArrayList;
import java.util.List;

public class MedBaseActivity extends AppCompatActivity {
    private ListView listViewCrew;
    private Button btnHealAll;
    private List<CrewMember> medBaseCrew;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_med_base);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        listViewCrew = findViewById(R.id.list_medbase_crew);
        btnHealAll = findViewById(R.id.btn_heal_all);

        loadCrew();

        btnHealAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (medBaseCrew.isEmpty()) {
                    Toast.makeText(MedBaseActivity.this, "No injured crew to heal.", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                for (CrewMember cm : medBaseCrew) {
                    cm.fullRecovery();
                }
                Toast.makeText(MedBaseActivity.this, "All injured crew fully healed and sent to Quarters!", Toast.LENGTH_SHORT).show();
                loadCrew();
            }
        });
    }

    private void loadCrew() {
        medBaseCrew = Storage.getInstance().getCrewByLocation(Location.MED_BASE);
        
        com.example.scmsimulator.adapters.CrewCardAdapter adapter = new com.example.scmsimulator.adapters.CrewCardAdapter(this, medBaseCrew);
        listViewCrew.setAdapter(adapter);
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
