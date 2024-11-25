package com.map.boxingbeatemup;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.media.MediaPlayer;

public class Boxer {
    private static final float MOVEMENT_SPEED = 5f;
    private static final int SPRITE_WIDTH = 256;
    private static final int SPRITE_HEIGHT = 256;
    private Context context;
    // Animation states
    public enum State {
        IDLE(0, 4),
        HIT(1, 2),
        FALL(2, 3),
        PUNCH(3, 3),    // Punch animation in row 3 with 8 frames
        KICK(5, 5),    // Assuming kick animation is in row 5 with 8 frames
        WALK(4, 6),    // Walk moved to row 5
        FALLEN(6, 1);
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
    private int health = 200;
    private ComboSystem comboSystem;
    private MediaPlayer mp, mp1;
    private boolean isMoving;
    private float moveDirection;
    private long lastAttackTime = 0;
    private boolean isAttacking = false;
    private boolean isFallen = false;

    private boolean isHit = false;
    public Boxer(Context context, Resources resources, int resourceId, float startX, float startY) {
        spriteSheet = BitmapFactory.decodeResource(resources, resourceId);
        x = startX;
        y = startY;
        collisionBox = new Rect();
        updateCollisionBox();
        comboSystem = new ComboSystem();
        this.facingRight = facingRight;
        this.context = context;
    }

    private void updateCollisionBox() {
        if(isFacingRight()){
            collisionBox.left = (int)x + SPRITE_WIDTH*(1/4);
            collisionBox.top = (int)y;
            collisionBox.right = (int)x + SPRITE_WIDTH*(4/4);
            collisionBox.bottom = (int)y + SPRITE_HEIGHT;
        }
        else {
            collisionBox.left = (int)x + SPRITE_WIDTH*(0/4);
            collisionBox.top = (int)y;
            collisionBox.right = (int)x + SPRITE_WIDTH*(3/4);
            collisionBox.bottom = (int)y + SPRITE_HEIGHT;
        }
    }

    public void update() {
        long currentTime = System.currentTimeMillis();

        // Update animation frame
        if (currentTime - lastFrameTime > FRAME_DELAY) {
            currentFrame = (currentFrame + 1) % currentState.frameCount;
            lastFrameTime = currentTime;

            // Handle attack animation completion
            if (isAttacking && currentFrame == currentState.frameCount - 1) {
                isAttacking = false;
                setState(State.IDLE);
            }
            if(isHit && currentFrame == currentState.frameCount - 1){
                isHit = false;
                if(isFallen){
                    setState(State.FALLEN);
                }
                else setState(State.IDLE);
            }
        }

        // Update movement
        if (isMoving) {
            x += moveDirection;
            updateCollisionBox();
            setState(State.WALK);
        }

        updateCollisionBox();
    }
    public void stopMoving() {
        isMoving = false;
        setState(State.IDLE);

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
        isMoving = true;
        moveDirection = dx;
        // Only change to WALK state if not already in WALK state or if not attacking
        if (currentState != State.WALK && !isAttacking) {
            setState(State.WALK);
        }
    }

    public void punch() {
        long currentTime = System.currentTimeMillis();
        context = context.getApplicationContext();
        if (currentTime - lastAttackTime >= ComboSystem.AttackType.PUNCH.recoveryTime) {
            setState(State.PUNCH);
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
        context = context.getApplicationContext();
        isAttacking = true;
        lastAttackTime = System.currentTimeMillis();
        comboSystem.resetCombo();
    }
    public void hit(ComboSystem.AttackType attackType) {
        int damage = attackType.damage;
        if(attackType == ComboSystem.AttackType.PUNCH){
            mp = MediaPlayer.create(context,R.raw.kick);
            mp.start();
        }
        else if(attackType == ComboSystem.AttackType.KICK){
            mp1 = MediaPlayer.create(context,R.raw.kick);
            mp1.start();
        }
        isHit = true;
        health -= damage;
        if(health >=0){
            setState(State.HIT);
            //setState(State.IDLE);
        }
        else if (health <= 0) {
            health = 0;
            setState(State.FALL);
            isFallen = true;
            setState(State.FALLEN);
        }

    }
    public State getCurrentState() {
        return currentState;
    }

    public void setFacingRight(boolean facingRight) {
        this.facingRight = facingRight;
    }

    public void setX(float x) {
        this.x = x;
    }
    public void setY(float y) {
        this.y = y;
    }
    // Getter for attack state
    public boolean isAttacking() {
        return currentState == State.PUNCH || currentState == State.KICK;
    }
    public boolean isHit(){
        return currentState == State.HIT;
    }
    // Getter for facing direction
    public boolean isFacingRight() {
        return facingRight;
    }
    public boolean isFallen(){
        return currentState == State.FALLEN;
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