package com.map.boxingbeatemup;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;


public class GameView extends SurfaceView implements SurfaceHolder.Callback {
    private static final int SPRITE_WIDTH = 32;
    private GameThread gameThread;
    private Boxer player1;
    private Boxer player2;
    private float touchX, touchY;
    private float lastTouchX;
    private long lastTouchTime;
    private UIManager uiManager;
    private BoxerAI ai;
    private SoundManager soundManager;
    private static final long DOUBLE_TAP_TIME = 300; // ms window for double tap
    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        getHolder().addCallback(this);
        setFocusable(true);
    }
    public GameView(Context context) {
        super(context);
        init(context, null);
    }
    public GameView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        gameThread = new GameThread(getHolder(), this);
        gameThread.setRunning(true);
        gameThread.start();
    }
    private void init(Context context, AttributeSet attrs) {
        getHolder().addCallback(this);
        setFocusable(true);

        // Initialize game objects
        player1 = new Boxer(context, getResources(), R.drawable.boxer_sprites, 100, 300);
        player2 = new Boxer(context, getResources(), R.drawable.boxer_sprites, 500, 300);
        soundManager = new SoundManager(context);
        post(() -> {
            uiManager = new UIManager(getWidth(), getHeight());
            ai = new BoxerAI(player2, player1);
        });
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.GameView);

            int backgroundColor = a.getColor(R.styleable.GameView_backgroundColor, Color.BLACK);
            int difficulty = a.getInteger(R.styleable.GameView_difficulty, 1); // default medium

            a.recycle();
        }
    }
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry = true;
        gameThread.setRunning(false);
        while (retry) {
            try {
                gameThread.join();
                retry = false;
            } catch (InterruptedException e) {
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        long currentTime = System.currentTimeMillis();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastTouchX = x;
                if (currentTime - lastTouchTime < DOUBLE_TAP_TIME) {
                    // Double tap detected - punch
                    player1.punch();
                }
                lastTouchTime = currentTime;
                break;

            case MotionEvent.ACTION_MOVE:
                float dx = x - lastTouchX;
                if (Math.abs(dx) > 10) { // Small threshold to prevent accidental movement
                    player1.move(dx > 0 ? 5 : -5);
                }
                lastTouchX = x;
                break;

            case MotionEvent.ACTION_UP:
                if (!player1.isAttacking()) {
                    player1.setState(Boxer.State.IDLE);
                }
                break;

        }
        if (player1.isAttacking() && checkCollision(player1, player2)) {
            if (player1.getCurrentState() == Boxer.State.KICK) {
                player2.hit(ComboSystem.AttackType.KICK);
            } else if (player1.getCurrentState() == Boxer.State.PUNCH) {
                player2.hit(ComboSystem.AttackType.PUNCH);
            }
        }
        return true;
    }

    public void update() {
        player1.update();
        player2.update();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (canvas != null) {
            canvas.drawColor(Color.WHITE);
            player1.draw(canvas);
            player2.draw(canvas);
            uiManager.drawUI(canvas, player1, player2);
        }
    }
    private boolean checkCollision(Boxer attacker, Boxer defender) {
        // Expand collision box for kick attack
        Rect attackBox = attacker.getCollisionBox();
        if (attacker.getCurrentState() == Boxer.State.KICK) {
        // Extend attack range for kick
            if (attacker.isFacingRight()) {
            attackBox.right += SPRITE_WIDTH / 2;
            } else {
                attackBox.left -= SPRITE_WIDTH / 2;
            }
    }
    return Rect.intersects(attackBox, defender.getCollisionBox());
    }
    public void pause() {
        if (gameThread != null) {
            gameThread.setRunning(false);
        }
    }

    public void resume() {
        if (gameThread != null) {
            gameThread.setRunning(true);
        }
    }
}
