package com.map.boxingbeatemup;

import java.util.ArrayList;
import java.util.List;

public class ComboSystem {
    private static final long COMBO_WINDOW = 800; // 800ms window for combo
    private static final int REQUIRED_PUNCHES = 3; // punches needed for kick
    private List<Long> punchTimes = new ArrayList<>();
    private boolean kickReady = false;

    public enum AttackType {
        PUNCH(10, 200),    // 10 damage, 200ms recovery
        KICK(25, 400),   // 25 damage, 400ms recovery
        PUNCH2(10,200);
        final int damage;
        final int recoveryTime;

        AttackType(int damage, int recoveryTime) {
            this.damage = damage;
            this.recoveryTime = recoveryTime;
        }
    }

    public void addPunch() {
        long currentTime = System.currentTimeMillis();
        clearOldPunches(currentTime);
        punchTimes.add(currentTime);

        if (punchTimes.size() >= REQUIRED_PUNCHES) {
            // Check if punches were within the combo window
            long firstPunchTime = punchTimes.get(0);
            if (currentTime - firstPunchTime <= COMBO_WINDOW) {
                kickReady = true;
            }
        }
    }

    private void clearOldPunches(long currentTime) {
        while (!punchTimes.isEmpty() && currentTime - punchTimes.get(0) > COMBO_WINDOW) {
            punchTimes.remove(0);
        }
        if (punchTimes.isEmpty()) {
            kickReady = false;
        }
    }

    public boolean isKickReady() {
        return kickReady;
    }

    public void resetCombo() {
        punchTimes.clear();
        kickReady = false;
    }
}
