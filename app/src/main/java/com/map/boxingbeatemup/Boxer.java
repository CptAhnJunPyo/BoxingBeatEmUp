package com.map.boxingbeatemup;

import static com.map.boxingbeatemup.ComboSystem.AttackType.KICK;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;

public class Boxer {
    private static final int SPRITE_WIDTH = 32;
    private static final int SPRITE_HEIGHT = 32;
    // Animation states
    public enum State {
        IDLE(0, 4),
        HIT(1, 2),
        FALL(2, 3),
        PUNCH(3, 8),    // Punch animation in row 3 with 8 frames
        KICK(5, 8),    // Assuming kick animation is in row 5 with 8 frames
        WALK(4, 6);    // Walk moved to row 5

        final int row;
        final int frameCount;

        State(int row, int frameCount) {
            this.row = row;
            this.frameCount = frameCount;
        }
    }

    private Bitmap spriteSheet;
    private State currentState = State.IDLE;
    private int currentFrame = 0;
    private long lastFrameTime = 0;
    private static final long FRAME_DELAY = 100; // milliseconds between frames
    private float x, y;
    private boolean facingRight = true;
    private Rect collisionBox;
    private int health = 100;
    private ComboSystem comboSystem;
    private SoundManager soundManager;
    private long lastAttackTime = 0;
    private boolean isAttacking = false;
    public Boxer(Context context, Resources resources, int resourceId, float startX, float startY) {
        spriteSheet = BitmapFactory.decodeResource(resources, resourceId);
        x = startX;
        y = startY;
        collisionBox = new Rect();
        updateCollisionBox();
        comboSystem = new ComboSystem();
    }

    private void updateCollisionBox() {
        collisionBox.left = (int)x;
        collisionBox.top = (int)y;
        collisionBox.right = (int)x + SPRITE_WIDTH;
        collisionBox.bottom = (int)y + SPRITE_HEIGHT;
    }

    public void update() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastFrameTime > FRAME_DELAY) {
            currentFrame = (currentFrame + 1) % currentState.frameCount;
            lastFrameTime = currentTime;

            if (isAttacking && currentFrame == currentState.frameCount - 1) {
                isAttacking = false;
                setState(State.IDLE);
            }
        }
        updateCollisionBox();

    }

    public void draw(Canvas canvas) {
        if (spriteSheet == null || canvas == null) return;

        Rect srcRect = new Rect(
                currentFrame * SPRITE_WIDTH,
                currentState.row * SPRITE_HEIGHT,
                (currentFrame + 1) * SPRITE_WIDTH,
                (currentState.row + 1) * SPRITE_HEIGHT
        );

        Rect destRect = new Rect(
                (int)x,
                (int)y,
                (int)x + SPRITE_WIDTH,
                (int)y + SPRITE_HEIGHT
        );

        // Flip the sprite horizontally if facing left
        if (!facingRight) {
            canvas.save();
            canvas.scale(-1, 1, x + SPRITE_WIDTH/2, y + SPRITE_HEIGHT/2);
        }

        canvas.drawBitmap(spriteSheet, srcRect, destRect, null);

        if (!facingRight) {
            canvas.restore();
        }
    }

    public void setState(State newState) {
        if (currentState != newState) {
            currentState = newState;
            currentFrame = 0;
            lastFrameTime = System.currentTimeMillis();
        }
    }

    public void move(float dx) {
        x += dx;
        facingRight = dx > 0;
        if (currentState == State.IDLE) {
            setState(State.WALK);
        }
    }

    public void punch() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastAttackTime >= ComboSystem.AttackType.PUNCH.recoveryTime) {
            setState(State.PUNCH);
            soundManager.playSound("punch");
            comboSystem.addPunch();
            lastAttackTime = currentTime;
            isAttacking = true;

            // Check if kick combo is ready
            if (comboSystem.isKickReady()) {
                performKick();
            }
        }
    }
    public void performKick(){
        setState(State.KICK);
        soundManager.playSound("kick");
        isAttacking = true;
        lastAttackTime = System.currentTimeMillis();
        comboSystem.resetCombo();
    }
    public void hit(ComboSystem.AttackType attackType) {
        int damage = attackType.damage;
        setState(State.HIT);
        health -= damage;
        if (health <= 0) {
            health = 0;
            setState(State.FALL);
        }
    }

    public boolean isColliding(Boxer other) {
        return Rect.intersects(this.collisionBox, other.collisionBox);
    }
    public State getCurrentState() {
        return currentState;
    }

    // Getter for attack state
    public boolean isAttacking() {
        return currentState == State.PUNCH || currentState == State.KICK;
    }

    // Getter for facing direction
    public boolean isFacingRight() {
        return facingRight;
    }

    // Getter for collision box
    public Rect getCollisionBox() {
        return collisionBox;
    }

    // Getter for position
    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    // Getter for health
    public int getHealth() {
        return health;
    }
}