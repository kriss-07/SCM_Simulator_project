package com.example.scmsimulator.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.scmsimulator.managers.StatisticsManager;
import com.example.scmsimulator.managers.Storage;
import com.example.scmsimulator.R;
import com.example.scmsimulator.models.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CombatActivity extends AppCompatActivity {

    private TextView textThreat;
    private ImageView imgThreat;
    private ProgressBar progThreatHP;
    private TextView textThreatHP;
    
    private LinearLayout layoutSquadContainer;
    
    private TextView textTurnPrompt;
    
    private Button btnAttack, btnDefend, btnSpecial, btnDone;
    
    private ImageView imgProjectile;
    
    private List<CrewMember> squad;
    private Threat threat;
    private int currentTurnIndex;
    
    private StringBuilder logBuilder;

    private class SquadSlot {
        View rootView;
        TextView textName;
        ImageView imgPortrait;
        ImageView imgAura;
        ImageView imgShield;
        ProgressBar progHP, progEnergy;
        TextView textHP, textEnergy;
    }
    private List<SquadSlot> squadSlots = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_combat);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        textThreat = findViewById(R.id.text_combat_threat);
        imgThreat = findViewById(R.id.img_combat_threat);
        progThreatHP = findViewById(R.id.progress_threat_hp);
        textThreatHP = findViewById(R.id.text_threat_hp);
        layoutSquadContainer = findViewById(R.id.layout_squad_container);
        
        textTurnPrompt = findViewById(R.id.text_turn_prompt);
        imgProjectile = findViewById(R.id.img_projectile);
        
        btnAttack = findViewById(R.id.btn_action_attack);
        btnDefend = findViewById(R.id.btn_action_defend);
        btnSpecial = findViewById(R.id.btn_action_special);
        btnDone = findViewById(R.id.btn_combat_done);
        
        logBuilder = new StringBuilder("Combat initiated...\n");

        ArrayList<String> squadIds = getIntent().getStringArrayListExtra("squad_ids");
        squad = new ArrayList<>();
        
        int totalLevel = 0;
        Storage storage = Storage.getInstance();
        if (squadIds != null) {
            for (String id : squadIds) {
                CrewMember cm = storage.getCrewMember(id);
                if (cm != null) {
                    squad.add(cm);
                    totalLevel += cm.getLevel();
                }
            }
        }
        
        if (squad.isEmpty()) {
            appendLog("Error: No valid squad members deployed!");
            finishCombat(false);
            return;
        }
        
        int avgLevel = Math.max(1, totalLevel / squad.size());
        threat = new Threat(avgLevel);
        
        progThreatHP.setMax(threat.getHealth());
        
        initSquadSlots();
        
        currentTurnIndex = 0;
        
        setupButtons();
        updateUI();
        startNextTurn();
    }
    
    private void initSquadSlots() {
        LayoutInflater inflater = LayoutInflater.from(this);
        for (int i = 0; i < squad.size(); i++) {
            CrewMember cm = squad.get(i);
            View slotView = inflater.inflate(R.layout.item_combat_crew, layoutSquadContainer, false);
            
            SquadSlot slot = new SquadSlot();
            slot.rootView = slotView;
            slot.textName = slotView.findViewById(R.id.text_combat_name);
            slot.imgPortrait = slotView.findViewById(R.id.img_combat_portrait);
            slot.imgAura = slotView.findViewById(R.id.img_combat_aura);
            slot.imgShield = slotView.findViewById(R.id.img_combat_shield);
            slot.progHP = slotView.findViewById(R.id.progress_combat_hp);
            slot.progEnergy = slotView.findViewById(R.id.progress_combat_energy);
            slot.textHP = slotView.findViewById(R.id.text_combat_hp);
            slot.textEnergy = slotView.findViewById(R.id.text_combat_energy);
            
            slot.textName.setText(cm.getName());
            slot.progHP.setMax(100);
            int maxEnergy = cm.getMaxEnergy();
            slot.progEnergy.setMax(maxEnergy);
            
            String role = cm.getRole();
            if ("Engineer".equalsIgnoreCase(role)) {
                slot.imgPortrait.setImageResource(R.drawable.img_engineer_standing);
            } else if ("Scientist".equalsIgnoreCase(role)) {
                slot.imgPortrait.setImageResource(R.drawable.img_scientist_standing);
            } else if ("Soldier".equalsIgnoreCase(role)) {
                slot.imgPortrait.setImageResource(R.drawable.img_soldier_standing);
            } else if ("Medic".equalsIgnoreCase(role)) {
                slot.imgPortrait.setImageResource(R.drawable.img_medic_standing);
            } else {
                slot.imgPortrait.setImageResource(R.drawable.img_pilot_standing);
            }
            
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.bottomMargin = 8;
            layoutSquadContainer.addView(slotView, lp);
            squadSlots.add(slot);
        }
    }
    
    private void setupButtons() {
        btnAttack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                executePlayerAction("Attack");
            }
        });
        
        btnDefend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                executePlayerAction("Defend");
            }
        });
        
        btnSpecial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                executePlayerAction("Special");
            }
        });
        
        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
    
    private void updateUI() {
        textThreat.setText("Threat: " + threat.getName());
        progThreatHP.setProgress(threat.getHealth());
        textThreatHP.setText(Math.max(0, threat.getHealth()) + " HP");
        
        for (int i = 0; i < squad.size(); i++) {
            CrewMember cm = squad.get(i);
            SquadSlot slot = squadSlots.get(i);
            
            slot.progHP.setProgress(cm.getHealth());
            slot.textHP.setText(Math.max(0, cm.getHealth()) + "/100");
            
            slot.progEnergy.setProgress(cm.getEnergy());
            slot.textEnergy.setText(Math.max(0, cm.getEnergy()) + "/" + cm.getMaxEnergy());
            
            if (i == currentTurnIndex && !isSquadDefeated() && !threat.isDefeated()) {
                slot.rootView.setBackgroundColor(0x3303DAC5); 
                
                slot.rootView.animate()
                    .scaleX(1.1f)
                    .scaleY(1.1f)
                    .setDuration(200)
                    .start();

                LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) slot.rootView.getLayoutParams();
                if (lp.topMargin != 24) {
                    lp.topMargin = 24;
                    lp.bottomMargin = 24;
                    slot.rootView.setLayoutParams(lp);
                }
            } else {
                slot.rootView.setBackgroundResource(R.drawable.bg_spinner);
                
                slot.rootView.animate()
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .setDuration(200)
                    .start();

                LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) slot.rootView.getLayoutParams();
                if (lp.topMargin != 0) {
                    lp.topMargin = 0;
                    lp.bottomMargin = 8;
                    slot.rootView.setLayoutParams(lp);
                }
            }
            
            if (cm.getHealth() <= 0) {
                slot.imgPortrait.setAlpha(0.3f); 
                slot.imgShield.setVisibility(View.GONE);
                slot.imgAura.setVisibility(View.GONE);
            }
        }
    }
    
    private void appendLog(String message) {
        logBuilder.append(message).append("\n");
    }
    
    private boolean isSquadDefeated() {
        for (CrewMember cm : squad) {
            if (cm.getHealth() > 0) return false;
        }
        return true;
    }
    
    // --- ANIMATIONS ---
    
    private void animateLunge(View target, float delta) {
        ObjectAnimator anim = ObjectAnimator.ofFloat(target, "translationX", 0f, delta, 0f);
        anim.setDuration(300);
        anim.start();
    }
    
    private void animateFireball(View sourceView, View targetView, boolean isAlien) {
        int[] srcLoc = new int[2];
        sourceView.getLocationOnScreen(srcLoc);
        
        int[] dstLoc = new int[2];
        targetView.getLocationOnScreen(dstLoc);
        
        float startX = srcLoc[0] + (sourceView.getWidth() / 2f) - (imgProjectile.getWidth() / 2f) + 16f;
        float startY = srcLoc[1] + (sourceView.getHeight() / 2f) - (imgProjectile.getHeight() / 2f) - 64f;
        
        float endX = dstLoc[0] + (targetView.getWidth() / 2f) - (imgProjectile.getWidth() / 2f) + 16f;
        float endY = dstLoc[1] + (targetView.getHeight() / 2f) - (imgProjectile.getHeight() / 2f) - 64f;
        
        imgProjectile.setImageResource(isAlien ? R.drawable.anim_fireball_alien : R.drawable.anim_fireball);
        imgProjectile.setX(startX);
        imgProjectile.setY(startY);
        imgProjectile.setVisibility(View.VISIBLE);
        
        ObjectAnimator moveX = ObjectAnimator.ofFloat(imgProjectile, "x", startX, endX);
        ObjectAnimator moveY = ObjectAnimator.ofFloat(imgProjectile, "y", startY, endY);
        
        AnimatorSet animSet = new AnimatorSet();
        animSet.playTogether(moveX, moveY);
        animSet.setDuration(400);
        
        animSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                imgProjectile.setVisibility(View.GONE);
            }
        });
        
        animSet.start();
    }
    
    private void playShieldAnim(ImageView shieldTarget) {
        shieldTarget.setAlpha(0f);
        shieldTarget.setVisibility(View.VISIBLE);
        ObjectAnimator fade = ObjectAnimator.ofFloat(shieldTarget, "alpha", 0f, 1f);
        fade.setDuration(500);
        fade.start();
    }
    
    private void playAuraAnim(ImageView auraTarget) {
        auraTarget.setVisibility(View.VISIBLE);
        auraTarget.setScaleX(0.5f);
        auraTarget.setScaleY(0.5f);
        
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(auraTarget, "scaleX", 0.5f, 1.5f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(auraTarget, "scaleY", 0.5f, 1.5f, 1f);
        
        AnimatorSet animSet = new AnimatorSet();
        animSet.playTogether(scaleX, scaleY);
        animSet.setInterpolator(new OvershootInterpolator());
        animSet.setDuration(600);
        animSet.start();
    }
    
    private void startNextTurn() {
        if (threat.isDefeated()) {
            appendLog("\nMISSION SUCCESSFUL!");
            finishCombat(true);
            return;
        }
        
        if (isSquadDefeated()) {
            appendLog("\nMISSION FAILED!");
            finishCombat(false);
            return;
        }
        
        for(SquadSlot s : squadSlots) {
             s.imgAura.setVisibility(View.GONE); 
        }

        while (currentTurnIndex < squad.size() && squad.get(currentTurnIndex).getHealth() <= 0) {
            currentTurnIndex++;
        }
        
        if (currentTurnIndex >= squad.size()) {
            executeThreatTurn();
            return;
        }
        
        CrewMember activeMember = squad.get(currentTurnIndex);
        activeMember.setDefending(false); 
        squadSlots.get(currentTurnIndex).imgShield.setVisibility(View.GONE);

        textTurnPrompt.setText("Turn: " + activeMember.getName() + " (" + activeMember.getRole() + ")");
        enableActionButtons(true);
        updateUI();
    }
    
    private void executePlayerAction(String action) {
        CrewMember activeMember = squad.get(currentTurnIndex);
        SquadSlot slot = squadSlots.get(currentTurnIndex);
        
        int delay = 800;
        
        switch (action) {
            case "Attack":
                if (activeMember.getEnergy() >= 5) {
                    activeMember.useEnergy(5);
                    animateLunge(slot.imgPortrait, -50f);
                    animateFireball(slot.imgPortrait, imgThreat, false);
                    delay = 1000; 
                    threat.takeDamage(activeMember.getSkill() + 10);
                } else {
                    activeMember.useEnergy(-10);
                }
                break;
            case "Defend":
                activeMember.setDefending(true);
                playShieldAnim(slot.imgShield);
                break;
            case "Special":
                if (activeMember.getEnergy() >= 10) {
                    activeMember.useEnergy(10);
                    playAuraAnim(slot.imgAura);
                    if (activeMember instanceof Soldier || activeMember instanceof Scientist || activeMember instanceof Engineer) {
                        animateLunge(slot.imgPortrait, -50f);
                        animateFireball(slot.imgPortrait, imgThreat, false);
                        delay = 1000;
                    }
                    applySpecialEffects(activeMember);
                } else {
                    threat.takeDamage(activeMember.getSkill() + 5);
                }
                break;
        }
        
        enableActionButtons(false);
        currentTurnIndex++;
        updateUI();
        
        getWindow().getDecorView().postDelayed(new Runnable() {
            @Override
            public void run() {
                startNextTurn();
            }
        }, delay);
    }
    
    private void applySpecialEffects(CrewMember cm) {
        if (cm instanceof Soldier) {
            threat.takeDamage(cm.getSkill() * 2 + 10);
        } else if (cm instanceof Medic) {
            for (int i = 0; i < squad.size(); i++) {
                CrewMember ally = squad.get(i);
                if (ally.getHealth() > 0) {
                    ally.heal(20);
                    // play aura on healed allies too
                    playAuraAnim(squadSlots.get(i).imgAura);
                }
            }
        } else if (cm instanceof Scientist) {
            threat.takeDamage(cm.getSkill() + 5);
        } else if (cm instanceof Engineer) {
            cm.setDefending(true);
            squadSlots.get(currentTurnIndex).imgShield.setVisibility(View.VISIBLE);
            threat.takeDamage(cm.getSkill() + 5);
        } else if (cm instanceof Pilot) {
            cm.setDefending(true);
            squadSlots.get(currentTurnIndex).imgShield.setVisibility(View.VISIBLE);
            threat.takeDamage(cm.getSkill() + 5);
        }
    }
    
    private void executeThreatTurn() {
        List<CrewMember> aliveSquad = new ArrayList<>();
        List<SquadSlot> aliveSlots = new ArrayList<>();
        
        for (int i = 0; i < squad.size(); i++) {
            if (squad.get(i).getHealth() > 0) {
                aliveSquad.add(squad.get(i));
                aliveSlots.add(squadSlots.get(i));
            }
        }
        
        if (!aliveSquad.isEmpty()) {
            animateLunge(imgThreat, 50f);
            
            Random rand = new Random();
            int rIndex = rand.nextInt(aliveSquad.size());
            CrewMember target = aliveSquad.get(rIndex);
            SquadSlot targetSlot = aliveSlots.get(rIndex);
            
            animateFireball(imgThreat, targetSlot.imgPortrait, true);
            
            target.takeDamage(threat.getAttack());
            
            targetSlot.rootView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    targetSlot.rootView.setBackgroundColor(0x55FF0000);
                }
            }, 300);
        }
        
        currentTurnIndex = 0;
        updateUI();
        
        getWindow().getDecorView().postDelayed(new Runnable() {
            @Override
            public void run() {
                startNextTurn();
            }
        }, 1200);
    }
    
    private void enableActionButtons(boolean enabled) {
        btnAttack.setEnabled(enabled);
        btnDefend.setEnabled(enabled);
        btnSpecial.setEnabled(enabled);
    }
    
    private void finishCombat(boolean victory) {
        enableActionButtons(false);
        StatisticsManager.getInstance().recordMission(victory);
        if (victory) {
             textTurnPrompt.setText("Combat Complete. VICTORY!");
        } else {
             textTurnPrompt.setText("Combat Complete. DEFEAT!");
        }
        btnDone.setVisibility(View.VISIBLE);
        updateUI();
        
        for (CrewMember cm : squad) {
            if (cm.getHealth() <= 0) {
                cm.setCurrentLocation(Location.MED_BASE);
            }
        }
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
