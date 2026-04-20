package com.example.scmsimulator.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.scmsimulator.managers.Storage;
import com.example.scmsimulator.R;
import com.example.scmsimulator.models.CrewMember;
import com.example.scmsimulator.models.Location;
import java.util.ArrayList;
import java.util.List;

public class QuartersActivity extends AppCompatActivity {
    private ListView listViewCrew;
    private Spinner spinnerQuartersCrew;
    private Button btnRestAll, btnTransferSim, btnTransferMission, btnTransferMedBase;
    private List<CrewMember> quartersCrew;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quarters);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        listViewCrew = findViewById(R.id.list_quarters_crew);
        spinnerQuartersCrew = findViewById(R.id.spinner_quarters_crew);
        btnRestAll = findViewById(R.id.btn_rest_all);
        btnTransferSim = findViewById(R.id.btn_transfer_simulator);
        btnTransferMission = findViewById(R.id.btn_transfer_mission);
        btnTransferMedBase = findViewById(R.id.btn_transfer_medbase);

        loadCrew();

        btnRestAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (CrewMember cm : quartersCrew) {
                    cm.rest(); // Restores full energy
                }
                Toast.makeText(QuartersActivity.this, "Everyone in Quarters fully rested!", Toast.LENGTH_SHORT).show();
                loadCrew();
            }
        });

        btnTransferSim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                transferSelected(Location.SIMULATOR);
            }
        });

        btnTransferMission.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                transferSelected(Location.MISSION_CONTROL);
            }
        });

        btnTransferMedBase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = spinnerQuartersCrew.getSelectedItemPosition();
                if (pos >= 0 && pos < quartersCrew.size()) {
                    CrewMember cm = quartersCrew.get(pos);
                    cm.setCurrentLocation(Location.MED_BASE);
                    cm.heal(100); 
                    Toast.makeText(QuartersActivity.this, cm.getName() + " transferred to Med Base and healed.", Toast.LENGTH_SHORT).show();
                    loadCrew();
                }
            }
        });
    }

    private void transferSelected(Location targetLocation) {
        int pos = spinnerQuartersCrew.getSelectedItemPosition();
        if (pos >= 0 && pos < quartersCrew.size()) {
            CrewMember cm = quartersCrew.get(pos);
            cm.setCurrentLocation(targetLocation);
            Toast.makeText(this, cm.getName() + " transferred successfully.", Toast.LENGTH_SHORT).show();
            loadCrew();
        } else {
            Toast.makeText(this, "No valid crew member selected.", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadCrew() {
        quartersCrew = Storage.getInstance().getCrewByLocation(Location.QUARTERS);
        
        com.example.scmsimulator.adapters.CrewCardAdapter listAdapter = new com.example.scmsimulator.adapters.CrewCardAdapter(this, quartersCrew);
        listViewCrew.setAdapter(listAdapter);
        
        if (!quartersCrew.isEmpty()) {
            com.example.scmsimulator.adapters.CrewSpinnerAdapter spinnerAdapter = new com.example.scmsimulator.adapters.CrewSpinnerAdapter(this, quartersCrew);
            spinnerQuartersCrew.setAdapter(spinnerAdapter);
        } else {
            spinnerQuartersCrew.setAdapter(null);
        }
        
        boolean hasCrew = !quartersCrew.isEmpty();
        btnTransferSim.setEnabled(hasCrew);
        btnTransferMission.setEnabled(hasCrew);
        btnTransferMedBase.setEnabled(hasCrew);
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
