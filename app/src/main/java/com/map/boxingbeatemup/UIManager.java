package com.map.boxingbeatemup;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.LinearGradient;
import android.graphics.Shader;

public class UIManager {
    private Paint healthBarPaint;
    private Paint borderPaint;
    private Paint segmentPaint;
    private Paint textPaint;
    private Paint shadowPaint;
    private int screenWidth;
    private ComboSystem comboSystem;
    private final float HEALTH_BAR_LENGTH;
    private final float HEALTH_BAR_HEIGHT = 40f;
    private final float HEALTH_BAR_Y_POSITION = 30f;
    private final float MARGIN = 50f;
    private final int SEGMENTS = 10; // Number of segments in health bar
    private final float SEGMENT_GAP = 2f;

    public UIManager(int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        this.HEALTH_BAR_LENGTH = screenWidth / 2.5f; // Slightly smaller than half screen
        initializePaints();
        comboSystem = new ComboSystem();
    }

    private void initializePaints() {
        healthBarPaint = new Paint();
        healthBarPaint.setStyle(Paint.Style.FILL);

        borderPaint = new Paint();
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(4f);
        borderPaint.setColor(Color.WHITE);

        segmentPaint = new Paint();
        segmentPaint.setColor(Color.BLACK);
        segmentPaint.setStrokeWidth(1f);

        shadowPaint = new Paint();
        shadowPaint.setColor(Color.argb(100, 0, 0, 0));
        shadowPaint.setStyle(Paint.Style.FILL);

        textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(32);
        textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        textPaint.setShadowLayer(2, 2, 2, Color.BLACK);
    }

    public void drawUI(Canvas canvas, Boxer player1, Boxer player2) {
        drawHealthBar(canvas, player1, MARGIN, HEALTH_BAR_Y_POSITION, true);
        drawHealthBar(canvas, player2, screenWidth - HEALTH_BAR_LENGTH - MARGIN, HEALTH_BAR_Y_POSITION, false);
    }

    private void drawHealthBar(Canvas canvas, Boxer boxer, float x, float y, boolean isPlayer1) {
        float healthPercentage = boxer.getHealth() / 200f;
        float filledWidth = HEALTH_BAR_LENGTH * healthPercentage;

        // Draw shadow for depth
        canvas.drawRect(x + 4, y + 4, x + HEALTH_BAR_LENGTH + 4, y + HEALTH_BAR_HEIGHT + 4, shadowPaint);

        // Draw the dark background (lost health)
        healthBarPaint.setColor(Color.rgb(60, 60, 60));
        canvas.drawRect(x, y, x + HEALTH_BAR_LENGTH, y + HEALTH_BAR_HEIGHT, healthBarPaint);

        // Draw the health gradient
        LinearGradient healthGradient = new LinearGradient(
                x, y,
                x, y + HEALTH_BAR_HEIGHT,
                getHealthGradientColors(healthPercentage),
                null,
                Shader.TileMode.CLAMP
        );
        healthBarPaint.setShader(healthGradient);
        canvas.drawRect(x, y, x + filledWidth, y + HEALTH_BAR_HEIGHT, healthBarPaint);
        healthBarPaint.setShader(null);

        // Draw segments

        // Draw border
        canvas.drawRect(x, y, x + HEALTH_BAR_LENGTH, y + HEALTH_BAR_HEIGHT, borderPaint);

    }

    private int[] getHealthGradientColors(float percentage) {
        if (percentage > 0.6f) {
            return new int[]{
                    Color.rgb(120, 255, 120),  // Light green
                    Color.rgb(0, 200, 0)       // Dark green
            };
        } else if (percentage > 0.3f) {
            return new int[]{
                    Color.rgb(255, 255, 120),  // Light yellow
                    Color.rgb(200, 200, 0)     // Dark yellow
            };
        } else {
            return new int[]{
                    Color.rgb(255, 120, 120),  // Light red
                    Color.rgb(200, 0, 0)       // Dark red
            };
        }
    }
}