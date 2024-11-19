package com.map.boxingbeatemup;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;

public class ButtonController {
    private RectF leftButton;
    private RectF rightButton;
    private RectF attackButton;
    private Paint buttonPaint;
    private Paint textPaint;

    private boolean leftPressed;
    private boolean rightPressed;
    private boolean isHoldingMovement;

    public ButtonController(int screenWidth, int screenHeight) {
        // Create buttons in the bottom portion of the screen
        int buttonSize = screenHeight / 8;
        int margin = buttonSize / 4;

        // Movement buttons on the left side
        leftButton = new RectF(
                margin,
                screenHeight - buttonSize - margin,
                margin + buttonSize,
                screenHeight - margin
        );

        rightButton = new RectF(
                margin + buttonSize + margin,
                screenHeight - buttonSize - margin,
                margin + buttonSize * 2 + margin,
                screenHeight - margin
        );

        // Attack button on the right side
        attackButton = new RectF(
                screenWidth - buttonSize - margin,
                screenHeight - buttonSize - margin,
                screenWidth - margin,
                screenHeight - margin
        );

        // Initialize paints
        buttonPaint = new Paint();
        buttonPaint.setColor(Color.argb(100, 255, 255, 255));
        buttonPaint.setStyle(Paint.Style.FILL);

        textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(buttonSize / 3);
        textPaint.setTextAlign(Paint.Align.CENTER);
    }

    public void draw(Canvas canvas) {
        // Draw movement buttons
        canvas.drawRoundRect(leftButton, 10, 10, buttonPaint);
        canvas.drawRoundRect(rightButton, 10, 10, buttonPaint);
        canvas.drawRoundRect(attackButton, 10, 10, buttonPaint);

        // Draw button labels
        float textY = leftButton.centerY() + textPaint.getTextSize() / 3;
        canvas.drawText("←", leftButton.centerX(), textY, textPaint);
        canvas.drawText("→", rightButton.centerX(), textY, textPaint);
        canvas.drawText("A", attackButton.centerX(), textY, textPaint);
    }

    public boolean handleTouch(float x, float y, int action) {
        switch (action) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                leftPressed = leftButton.contains(x, y);
                rightPressed = rightButton.contains(x, y);
                isHoldingMovement = false;  // Reset the holding state

                // Only set isHoldingMovement true if we're currently touching a movement button
                if (leftPressed || rightPressed) {
                    isHoldingMovement = true;
                }
                return attackButton.contains(x, y);

            case MotionEvent.ACTION_UP:
                isHoldingMovement = false;
                leftPressed = false;
                rightPressed = false;
                return false;

        }
        return false;
    }

    public void resetButtons() {
        leftPressed = false;
        rightPressed = false;
    }

    public boolean isLeftPressed() {
        return leftPressed;
    }

    public boolean isRightPressed() {
        return rightPressed;
    }
    public boolean isHoldingMovement() {
        return isHoldingMovement;
    }
}