package com.example.scmsimulator.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.scmsimulator.managers.Storage;
import com.example.scmsimulator.R;
import com.example.scmsimulator.models.*;

public class RecruitActivity extends AppCompatActivity {
    private EditText editName;
    private Spinner spinnerRole;
    private Button btnRecruit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recruit);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        editName = findViewById(R.id.edit_recruit_name);
        spinnerRole = findViewById(R.id.spinner_role);
        btnRecruit = findViewById(R.id.btn_confirm_recruit);

        String[] roles = {"Pilot", "Engineer", "Medic", "Scientist", "Soldier"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, roles);
        spinnerRole.setAdapter(adapter);

        btnRecruit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = editName.getText().toString().trim();
                String role = spinnerRole.getSelectedItem().toString();

                if (name.isEmpty()) {
                    Toast.makeText(RecruitActivity.this, "Please enter a name", Toast.LENGTH_SHORT).show();
                    return;
                }

                CrewMember newMember = null;
                switch (role) {
                    case "Pilot": newMember = new Pilot(name); break;
                    case "Engineer": newMember = new Engineer(name); break;
                    case "Medic": newMember = new Medic(name); break;
                    case "Scientist": newMember = new Scientist(name); break;
                    case "Soldier": newMember = new Soldier(name); break;
                }

                if (newMember != null) {
                    Storage.getInstance().addCrewMember(newMember);
                    Toast.makeText(RecruitActivity.this, name + " recruited!", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });
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
