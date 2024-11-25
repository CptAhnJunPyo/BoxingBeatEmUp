package com.map.boxingbeatemup;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.view.MotionEvent;

public class JoystickController {
    private float joystickCenterX;
    private float joystickCenterY;
    private float joystickRadius;
    private float knobRadius;
    private float currentKnobX;
    private float currentKnobY;
    private boolean isJoystickActive;

    private RectF attackButton;
    private boolean isAttackPressed;

    private Paint joystickBasePaint;
    private Paint joystickKnobPaint;
    private Paint attackButtonPaint;
    private Paint attackButtonTextPaint;

    // Movement thresholds and speed control
    private static final float MOVEMENT_THRESHOLD = 0.2f;
    private static final float MAX_MOVEMENT_SPEED = 1.5f; // Reduced from 5.0f to 2.5f
    private boolean isMovingLeft;
    private boolean isMovingRight;
    private float currentMovementSpeed;

    public JoystickController(int screenWidth, int screenHeight) {
        // Initialize joystick dimensions
        joystickRadius = screenHeight / 6f;
        knobRadius = joystickRadius / 2f;

        // Position joystick in bottom left quarter
        joystickCenterX = joystickRadius + 50;
        joystickCenterY = screenHeight - joystickRadius - 50;

        // Initialize knob at center position
        currentKnobX = joystickCenterX;
        currentKnobY = joystickCenterY;

        // Create large circular attack button on the right
        float attackButtonSize = screenHeight / 4f;
        attackButton = new RectF(
                screenWidth - attackButtonSize - 50,
                screenHeight - attackButtonSize - 50,
                screenWidth - 50,
                screenHeight - 50
        );

        // Initialize paints
        joystickBasePaint = new Paint();
        joystickBasePaint.setColor(Color.argb(100, 200, 200, 200));
        joystickBasePaint.setStyle(Paint.Style.FILL);

        joystickKnobPaint = new Paint();
        joystickKnobPaint.setColor(Color.argb(180, 150, 150, 150));
        joystickKnobPaint.setStyle(Paint.Style.FILL);

        attackButtonPaint = new Paint();
        attackButtonPaint.setColor(Color.argb(100, 255, 100, 100));
        attackButtonPaint.setStyle(Paint.Style.FILL);

        attackButtonTextPaint = new Paint();
        attackButtonTextPaint.setColor(Color.BLACK);
        attackButtonTextPaint.setTextSize(attackButtonSize / 3);
        attackButtonTextPaint.setTextAlign(Paint.Align.CENTER);
    }

    public void draw(Canvas canvas) {
        // Draw joystick base
        canvas.drawCircle(joystickCenterX, joystickCenterY, joystickRadius, joystickBasePaint);

        // Draw joystick knob
        canvas.drawCircle(currentKnobX, currentKnobY, knobRadius, joystickKnobPaint);

        // Draw attack button
        float centerX = attackButton.centerX();
        float centerY = attackButton.centerY();
        float radius = (attackButton.width() / 2);

        // Draw the circular attack button
        canvas.drawCircle(centerX, centerY, radius, attackButtonPaint);

        // Draw the "A" label
        float textY = centerY + attackButtonTextPaint.getTextSize() / 3;
        canvas.drawText("A", centerX, textY, attackButtonTextPaint);
    }

    public boolean handleTouch(float x, float y, int action) {
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                // Check if touch is within joystick area
                float distanceFromCenter = distance(x, y, joystickCenterX, joystickCenterY);
                if (distanceFromCenter < joystickRadius * 1.5f) {
                    isJoystickActive = true;
                    updateJoystickPosition(x, y);
                }

                // Check if attack button is pressed
                float centerX = attackButton.centerX();
                float centerY = attackButton.centerY();
                float radius = attackButton.width() / 2;
                if (distance(x, y, centerX, centerY) < radius) {
                    isAttackPressed = true;
                    return true;
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (isJoystickActive) {
                    updateJoystickPosition(x, y);
                }
                break;

            case MotionEvent.ACTION_UP:
                // Reset joystick
                resetJoystick();
                resetAttack();
        }

        return isAttackPressed;
    }

    private void updateJoystickPosition(float x, float y) {
        float deltaX = x - joystickCenterX;
        float deltaY = y - joystickCenterY;
        float distance = distance(x, y, joystickCenterX, joystickCenterY);

        if (distance < joystickRadius) {
            currentKnobX = x;
            currentKnobY = y;
        } else {
            float angle = (float) Math.atan2(deltaY, deltaX);
            currentKnobX = joystickCenterX + joystickRadius * (float) Math.cos(angle);
            currentKnobY = joystickCenterY + joystickRadius * (float) Math.sin(angle);
            distance = joystickRadius;
        }

        // Calculate movement direction and speed based on knob position
        float normalizedX = (currentKnobX - joystickCenterX) / joystickRadius;

        // Set movement direction
        isMovingLeft = normalizedX < -MOVEMENT_THRESHOLD;
        isMovingRight = normalizedX > MOVEMENT_THRESHOLD;

        // Calculate movement speed based on distance from center
        float speedFactor = Math.abs(normalizedX);
        // Apply smooth acceleration curve
        speedFactor = speedFactor * speedFactor; // Square for smoother acceleration
        currentMovementSpeed = MAX_MOVEMENT_SPEED * speedFactor;
    }

    private float distance(float x1, float y1, float x2, float y2) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }
    public boolean isJoystickArea(float x, float y) {
        return distance(x, y, joystickCenterX, joystickCenterY) < joystickRadius * 1.5f;
    }

    public boolean isAttackButtonArea(float x, float y) {
        float centerX = attackButton.centerX();
        float centerY = attackButton.centerY();
        float radius = attackButton.width() / 2;
        return distance(x, y, centerX, centerY) < radius;
    }
    public boolean isMovingLeft() {
        return isMovingLeft;
    }

    public boolean isMovingRight() {
        return isMovingRight;
    }

    public float getMovementSpeed() {
        return currentMovementSpeed;
    }

    public boolean isJoystickActive() {
        return isJoystickActive;
    }
    public boolean isAttackPressed(){
        return isAttackPressed;
    }
    public void resetJoystick() {
        isJoystickActive = false;
        currentKnobX = joystickCenterX;
        currentKnobY = joystickCenterY;
        isMovingLeft = false;
        isMovingRight = false;
        currentMovementSpeed = 0;
    }
    public void resetAttack(){
        isAttackPressed = false;
    }
}