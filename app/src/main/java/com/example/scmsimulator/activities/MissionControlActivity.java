package com.example.scmsimulator.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.scmsimulator.managers.Storage;
import com.example.scmsimulator.R;
import com.example.scmsimulator.models.*;
import java.util.ArrayList;
import java.util.List;
import com.example.scmsimulator.adapters.CrewSpinnerAdapter;

public class MissionControlActivity extends AppCompatActivity {
    private Spinner spinnerSlot1, spinnerSlot2, spinnerSlot3, spinnerRecall;
    private Button btnLaunchMission, btnTransferQuarters;
    private List<CrewMember> availableCrew;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mission_control);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        spinnerSlot1 = findViewById(R.id.spinner_slot1);
        spinnerSlot2 = findViewById(R.id.spinner_slot2);
        spinnerSlot3 = findViewById(R.id.spinner_slot3);
        spinnerRecall = findViewById(R.id.spinner_mission_recall_crew);
        btnLaunchMission = findViewById(R.id.btn_launch_mission);
        btnTransferQuarters = findViewById(R.id.btn_transfer_quarters);

        loadCrewData();

        btnTransferQuarters.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = spinnerRecall.getSelectedItemPosition();
                if (pos >= 0 && pos < availableCrew.size()) {
                    CrewMember cm = availableCrew.get(pos);
                    cm.setCurrentLocation(Location.QUARTERS);
                    Toast.makeText(MissionControlActivity.this, cm.getName() + " returned to Quarters.", Toast.LENGTH_SHORT).show();
                    loadCrewData();
                }
            }
        });

        btnLaunchMission.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos1 = spinnerSlot1.getSelectedItemPosition();
                int pos2 = spinnerSlot2.getSelectedItemPosition();
                int pos3 = spinnerSlot3.getSelectedItemPosition();

                ArrayList<String> selectedIds = new ArrayList<>();
                boolean hasDuplicate = false;

                if (pos1 > 0) {
                    selectedIds.add(availableCrew.get(pos1 - 1).getId());
                }
                if (pos2 > 0) {
                    String id2 = availableCrew.get(pos2 - 1).getId();
                    if (selectedIds.contains(id2)) hasDuplicate = true;
                    else selectedIds.add(id2);
                }
                if (pos3 > 0) {
                    String id3 = availableCrew.get(pos3 - 1).getId();
                    if (selectedIds.contains(id3)) hasDuplicate = true;
                    else selectedIds.add(id3);
                }

                if (hasDuplicate) {
                    Toast.makeText(MissionControlActivity.this, "Error: A crew member cannot occupy multiple slots at once!", Toast.LENGTH_LONG).show();
                    return;
                }

                if (selectedIds.isEmpty()) {
                    Toast.makeText(MissionControlActivity.this, "Select at least 1 crew member!", Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent intent = new Intent(MissionControlActivity.this, CombatActivity.class);
                intent.putStringArrayListExtra("squad_ids", selectedIds);
                startActivity(intent);
                finish(); // Close mission control when launching combat
            }
        });
    }

    private void loadCrewData() {
        // Only get crew assigned explicitly to Mission Control
        availableCrew = Storage.getInstance().getCrewByLocation(Location.MISSION_CONTROL);

        if (availableCrew.isEmpty()) {
            Toast.makeText(this, "No crew members assigned to Mission Control!", Toast.LENGTH_LONG).show();
            btnLaunchMission.setEnabled(false);
            btnTransferQuarters.setEnabled(false);
        } else {
            btnLaunchMission.setEnabled(true);
            btnTransferQuarters.setEnabled(true);
        }

        List<CrewMember> slotCrew = new ArrayList<>();
        slotCrew.add(null); // Represents "-- None --"
        slotCrew.addAll(availableCrew);

        CrewSpinnerAdapter slotAdapter = new CrewSpinnerAdapter(this, slotCrew);
        spinnerSlot1.setAdapter(slotAdapter);
        spinnerSlot2.setAdapter(slotAdapter);
        spinnerSlot3.setAdapter(slotAdapter);

        if (!availableCrew.isEmpty()) {
            CrewSpinnerAdapter recallAdapter = new CrewSpinnerAdapter(this, availableCrew);
            spinnerRecall.setAdapter(recallAdapter);
        } else {
            spinnerRecall.setAdapter(null);
        }

        // Pre-select some slots if possible
        if (availableCrew.size() >= 1) spinnerSlot1.setSelection(1);
        if (availableCrew.size() >= 2) spinnerSlot2.setSelection(2);
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
