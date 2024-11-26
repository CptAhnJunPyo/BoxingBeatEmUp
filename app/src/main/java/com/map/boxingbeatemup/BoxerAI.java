package com.map.boxingbeatemup;

import java.util.Random;

public class BoxerAI {
    private int SPRITE_WIDTH = 256;
    private Boxer aiBoxer;
    private Boxer playerBoxer;
    private Random random = new Random();
    private long lastActionTime = 0;
    private static final long DECISION_DELAY = 500; // ms between AI decisions

    public BoxerAI(Boxer aiBoxer, Boxer playerBoxer) {
        this.aiBoxer = aiBoxer;
        this.playerBoxer = playerBoxer;
    }

    public void update() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastActionTime < DECISION_DELAY) return;

        float distance = Math.abs(aiBoxer.getX() - playerBoxer.getX());

        // AI Decision making
        if (distance > SPRITE_WIDTH * 2) {
            // Move towards player
            float moveDirection = playerBoxer.getX() > aiBoxer.getX() ? 5 : -5;
            aiBoxer.move(moveDirection);
        } else if (distance <= SPRITE_WIDTH * 1.5) {
            // In attack range
            if (random.nextFloat() < 0.7) { // 70% chance to attack
                decideAttack();
            } else {
                // Dodge or block
                float dodgeDirection = random.nextBoolean() ? 5 : -5;
                aiBoxer.move(dodgeDirection);
            }
        }
        lastActionTime = currentTime;
    }
    /*public boolean isMoving(){
        float distance = Math.abs(aiBoxer.getX() - playerBoxer.getX());
        if(distance > SPRITE_WIDTH * 2);
        }
    }*/
    public boolean decideAttack() {
        float r = random.nextFloat();
        if (r < 0.5) {
            return true;
        }
        else return false;
    }
    public boolean chanceProcCombo(){
        float r2 = random.nextFloat();
        if(r2<0.4){
            return true;
        }
        else return false;
    }
}
