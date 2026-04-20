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
import com.example.scmsimulator.managers.StatisticsManager;
import com.example.scmsimulator.R;
import com.example.scmsimulator.models.CrewMember;
import com.example.scmsimulator.models.Location;
import java.util.ArrayList;
import java.util.List;

public class SimulatorActivity extends AppCompatActivity {
    private ListView listViewCrew;
    private Spinner spinnerSimulatorCrew;
    private Button btnTrainAll, btnTransferQuarters;
    private List<CrewMember> simulatorCrew;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simulator);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        listViewCrew = findViewById(R.id.list_simulator_crew);
        spinnerSimulatorCrew = findViewById(R.id.spinner_simulator_crew);
        btnTrainAll = findViewById(R.id.btn_train_all);
        btnTransferQuarters = findViewById(R.id.btn_transfer_quarters);

        loadCrew();

        btnTrainAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (simulatorCrew.isEmpty()) {
                    Toast.makeText(SimulatorActivity.this, "Assign crew to Simulator in Quarters first!", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                boolean anyoneTrained = false;
                for (CrewMember cm : simulatorCrew) {
                    if (cm.getEnergy() >= 20) {
                        cm.train();
                        anyoneTrained = true;
                    }
                }
                if (anyoneTrained) {
                    StatisticsManager.getInstance().recordTraining();
                    Toast.makeText(SimulatorActivity.this, "Training complete! Experience gained.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(SimulatorActivity.this, "Not enough energy to train. Return to quarters.", Toast.LENGTH_SHORT).show();
                }
                loadCrew();
            }
        });

        btnTransferQuarters.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = spinnerSimulatorCrew.getSelectedItemPosition();
                if (pos >= 0 && pos < simulatorCrew.size()) {
                    CrewMember cm = simulatorCrew.get(pos);
                    cm.setCurrentLocation(Location.QUARTERS);
                    Toast.makeText(SimulatorActivity.this, cm.getName() + " returned to Quarters.", Toast.LENGTH_SHORT).show();
                    loadCrew();
                }
            }
        });
    }

    private void loadCrew() {
        simulatorCrew = Storage.getInstance().getCrewByLocation(Location.SIMULATOR); 
        
        com.example.scmsimulator.adapters.CrewCardAdapter listAdapter = new com.example.scmsimulator.adapters.CrewCardAdapter(this, simulatorCrew);
        listViewCrew.setAdapter(listAdapter);
        
        if (!simulatorCrew.isEmpty()) {
            com.example.scmsimulator.adapters.CrewSpinnerAdapter spinnerAdapter = new com.example.scmsimulator.adapters.CrewSpinnerAdapter(this, simulatorCrew);
            spinnerSimulatorCrew.setAdapter(spinnerAdapter);
        } else {
            spinnerSimulatorCrew.setAdapter(null);
        }
        
        btnTransferQuarters.setEnabled(!simulatorCrew.isEmpty());
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
