package com.map.boxingbeatemup;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;

public class UIManager {
    private Paint healthBarPaint;
    private Paint textPaint;
    private Rect healthBarBounds;
    private int screenWidth;
    private ComboSystem comboSystem;

    public UIManager(int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        initializePaints();
        comboSystem = new ComboSystem();
    }

    private void initializePaints() {
        healthBarPaint = new Paint();
        healthBarPaint.setStyle(Paint.Style.FILL);

        textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(40);
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);
    }

    public void drawUI(Canvas canvas, Boxer player1, Boxer player2) {
        // Draw health bars
        drawHealthBar(canvas, player1, 50, 30, true);
        drawHealthBar(canvas, player2, screenWidth - 450, 30, false);
        // Draw combo counter
    }

    private void drawHealthBar(Canvas canvas, Boxer boxer, float x, float y, boolean isPlayer1) {
        float healthPercentage = boxer.getHealth() / 100f;
        float barWidth = 200 * healthPercentage;

        // Background
        healthBarPaint.setColor(Color.GRAY);
        canvas.drawRect(x, y, x + 200, y + 20, healthBarPaint);

        // Health
        healthBarPaint.setColor(getHealthColor(healthPercentage));
        canvas.drawRect(x, y, x + barWidth, y + 20, healthBarPaint);
    }

    private int getHealthColor(float percentage) {
        if (percentage > 0.6f) return Color.GREEN;
        if (percentage > 0.3f) return Color.YELLOW;
        return Color.RED;
    }
}
