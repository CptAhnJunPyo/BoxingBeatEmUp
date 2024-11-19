package com.map.boxingbeatemup;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;


public class GameView extends SurfaceView implements SurfaceHolder.Callback {
    private static final int SPRITE_WIDTH = 256;
    private static final float ASPECT_RATIO = 16f / 9f; // Landscape aspect ratio
    private GameThread gameThread;
    private Boxer player1;
    private Boxer player2;
    private float touchX, touchY;
    private float lastTouchX;
    private long lastTouchTime;
    private UIManager uiManager;
    private BoxerAI ai;
    private MediaPlayer mp;
    private Bitmap backgroundImage;
    private ButtonController buttonController;
    private int backgroundColor;
    private int difficulty;
    private int roundTime;
    private int startingHealth;
    private float stageBoundaryLeft;
    private float stageBoundaryRight;
    private int currentRound = 1;
    private static final int MAX_ROUNDS = 3;
    private boolean roundOver = false;
    private static final int ROUND_START_DELAY = 3000; // 3 seconds
    private long roundEndTime = 0;
    private boolean isEndGame = false;
    private static final long DOUBLE_TAP_TIME = 300; // ms window for double tap

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        getHolder().addCallback(this);
        setFocusable(true);
        init(context, attrs);
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
        player1 = new Boxer(context, getResources(), R.drawable.boxer_sprites, 100, 450);
        player2 = new Boxer(context, getResources(), R.drawable.boxer_sprites, 1000, 450);
        player2.setFacingRight(false);
        post(() -> {
            uiManager = new UIManager(getWidth(), getHeight());
            ai = new BoxerAI(player2, player1);
            buttonController = new ButtonController(getWidth(), getHeight());
        });
        backgroundColor = Color.WHITE;
        difficulty = 1;
        roundTime = 99; // 99 seconds
        startingHealth = 100;
        stageBoundaryLeft = 0;
        stageBoundaryRight = getWidth();
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.GameView);
            try {
                int backgroundResId = a.getResourceId(R.styleable.GameView_backgroundResource, -1);
                loadBackgroundImage(backgroundResId);
                if (backgroundResId != -1) {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inScaled = true;
                    options.inPreferredConfig = Bitmap.Config.RGB_565;
                    backgroundImage = BitmapFactory.decodeResource(getResources(), backgroundResId, options);

                    // Scale background to screen size when view is laid out
                    post(() -> {
                        if (backgroundImage != null) {
                            backgroundImage = Bitmap.createScaledBitmap(
                                    backgroundImage,
                                    getWidth(),
                                    getHeight(),
                                    true
                            );
                        }
                    });
                }

                // Load other attributes
                difficulty = a.getInteger(R.styleable.GameView_difficulty, difficulty);
                roundTime = a.getInteger(R.styleable.GameView_roundTime, roundTime);
                startingHealth = a.getInteger(R.styleable.GameView_startingHealth, startingHealth);
                stageBoundaryLeft = a.getDimension(R.styleable.GameView_stageBoundaryLeft, 0);
                stageBoundaryRight = a.getDimension(R.styleable.GameView_stageBoundaryRight, getWidth());
            } finally {
                a.recycle();
            }
        }
    }
    private void loadBackgroundImage(int resourceId) {
        if (resourceId != -1) {
            try {
                // Load the background image
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inScaled = true;

                // Decode the image
                Bitmap originalBitmap = BitmapFactory.decodeResource(getResources(), resourceId, options);

                // Scale the bitmap to match the view size (will be set when view size is available)
                post(() -> {
                    if (originalBitmap != null) {
                        if (backgroundImage != null) {
                            backgroundImage.recycle();
                        }
                        backgroundImage = Bitmap.createScaledBitmap(
                                originalBitmap,
                                getWidth(),
                                getHeight(),
                                true
                        );

                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        // Ensure the view maintains landscape orientation
        if (width < height) {
            int temp = width;
            width = height;
            height = temp;
        }

        // Adjust height to maintain aspect ratio if needed
        height = (int) (width / ASPECT_RATIO);

        setMeasuredDimension(width, height);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // Reload and scale background image when surface changes
        if (backgroundImage != null) {
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(
                    backgroundImage,
                    width,
                    height,
                    true
            );
            if (scaledBitmap != backgroundImage) {
                backgroundImage.recycle();
                backgroundImage = scaledBitmap;
            }
        }

        // Update stage boundaries
        stageBoundaryLeft = 0;
        stageBoundaryRight = width;
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

        // Also clean up background here for safety
        if (backgroundImage != null) {
            backgroundImage.recycle();
            backgroundImage = null;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) { //player1 action
        float x = event.getX();
        float y = event.getY();

        // Handle the touch event
            boolean isAttacking = buttonController.handleTouch(x, y, event.getAction());
            if (buttonController.isLeftPressed()) {
                player1.move(-5);
            } else if (buttonController.isRightPressed()) {
                player1.move(5);
            }
            // Handle attack
            if(!player1.isHit()){
                if (isAttacking && event.getAction() == MotionEvent.ACTION_DOWN) {
                    player1.stopMoving();
                    player1.punch();
                }
            }
            // Handle movement state changes
            if (!buttonController.isHoldingMovement() && !player1.isAttacking()) {
                player1.setState(Boxer.State.IDLE);
            }

            // Check for hits
            if (player1.isAttacking() && checkCollision(player1, player2)) {
                if (player1.getCurrentState() == Boxer.State.KICK) {
                    player2.hit(ComboSystem.AttackType.KICK);
                } else if (player1.getCurrentState() == Boxer.State.PUNCH) {
                    player2.hit(ComboSystem.AttackType.PUNCH);
                }
            }
            BotLogic();
        return true;
    }
    public void BotLogic(){
        if(!player2.isFallen()){
            if(!player2.isHit()){
                if (ai.decideAttack()) {
                    player2.punch();
                    if(ai.chanceProcCombo()){
                        player2.punch();
                    }
                }
            }
            if (player2.isAttacking() && checkCollision(player2, player1)) {
                if (player2.getCurrentState() == Boxer.State.KICK) {
                    player1.hit(ComboSystem.AttackType.KICK);
                } else if (player2.getCurrentState() == Boxer.State.PUNCH) {
                    player1.hit(ComboSystem.AttackType.PUNCH);
                }
            }
        }
    }
    public void update() {
        if (player1 != null) player1.update();
        if (player2 != null) player2.update();
        keepBoxersInBounds();
        checkRoundStatus();
    }
    @Override
    protected void onDraw(Canvas canvas) {
        if (canvas != null) {
            // Clear the canvas first
            canvas.drawColor(backgroundColor);

            // Draw background if available
            if (backgroundImage != null) {
                //canvas.drawBitmap(backgroundImage, 0, 0, null);
            }

            // Draw game elements
            if (player1 != null) player1.draw(canvas);
            if (player2 != null) player2.draw(canvas);
            if (uiManager != null) {
                uiManager.drawUI(canvas, player1, player2);
            }
            if (buttonController != null) {
                buttonController.draw(canvas);
            }
        }
    }

    private void keepBoxersInBounds() {
        if (player1.getX() < stageBoundaryLeft) {
            player1.setX(stageBoundaryLeft);
        } else if (player1.getX() > stageBoundaryRight - SPRITE_WIDTH) {
            player1.setX(stageBoundaryRight - SPRITE_WIDTH);
        }

        if (player2.getX() < stageBoundaryLeft) {
            player2.setX(stageBoundaryLeft);
        } else if (player2.getX() > stageBoundaryRight - SPRITE_WIDTH) {
            player2.setX(stageBoundaryRight - SPRITE_WIDTH);
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
    private void checkRoundStatus() {
        if (!roundOver && (player1.isFallen() || player2.isFallen())) {
            roundOver = true;
            roundEndTime = System.currentTimeMillis();
        }

        // Check if it's time to start a new round
        if (roundOver && System.currentTimeMillis() - roundEndTime >= ROUND_START_DELAY) {
            startNewRound();
        }
    }
    private void startNewRound() {
        if (currentRound < MAX_ROUNDS) {
            // Reset players
            player1 = new Boxer(getContext(), getResources(), R.drawable.boxer_sprites, 100, 450);
            player2 = new Boxer(getContext(), getResources(), R.drawable.boxer_sprites, 1000, 450);
            player2.setFacingRight(false);

            // Reset round state
            roundOver = false;
            currentRound++;

            // Reset AI
            ai = new BoxerAI(player2, player1);
        } else {
            currentRound = 0;
            //isEndGame = true;
        }
    }
    public boolean isGameEnded(){
        return isEndGame;
    }
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        // Clean up bitmap resources
        if (backgroundImage != null) {
            backgroundImage.recycle();
            backgroundImage = null;
        }
    }

    public void pause() {
        if (gameThread != null) {
            gameThread.setRunning(false);
            try {
                gameThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void resume() {
        if (gameThread != null) {
            gameThread.setRunning(true);
            gameThread.start();
        }
    }
}