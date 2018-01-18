package superAlone40k.ecs;

import jdk.nashorn.internal.runtime.Debug;
import superAlone40k.Main;
import superAlone40k.renderer.Renderer;
import superAlone40k.util.*;
import superAlone40k.window.WindowWithFlattenedECS;

import javax.sound.midi.MidiChannel;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.TreeSet;

public class FlattenedEngine {
	// Turn on to see fancy shadow debug lines
	public static boolean DEBUG_SHADOWS = false;
	
    private ArrayList<float[]> entities = new ArrayList<>();
    private ArrayList<float[]>[] systemViews;

    private ArrayList<float[]> entitiesToAdd = new ArrayList<>();
    private ArrayList<float[]> entitiesToDelete = new ArrayList<>();

    private int[] systemBitmasks = new int[] {SystemBitmask.INPUT.getSystemMask(), SystemBitmask.COLLIDER_SORTING.getSystemMask(), SystemBitmask.MOVEMENT_SYSTEM.getSystemMask(), SystemBitmask.LIGHT_SYSTEM.getSystemMask(), SystemBitmask.TRIGGER_SYSTEM.getSystemMask(), SystemBitmask.LIFETIME_SYSTEM.getSystemMask(), SystemBitmask.CLEANUP_SYSTEM.getSystemMask(), SystemBitmask.PLATFORM_MOVEMENT_SYSTEM.getSystemMask() };
	private SystemMethod[] systemMethods = new SystemMethod[] {
			FlattenedEngine::inputProcessing,
			FlattenedEngine::colliderSorting,
			FlattenedEngine::movementSystem,
			FlattenedEngine::lightingSystem,
			FlattenedEngine::triggerSystem,
			FlattenedEngine::lifetimeSystem,
			FlattenedEngine::cleanupSystem,
			FlattenedEngine::platformMovementSystem };

    private final TreeSet<Ray> angleSortedRays = new TreeSet<>();
    
    private boolean updating;

    private static double totalTime = 0.0d;

    private double currentTimeScale = 1.0f;

    private int highScore = 0;

    private boolean firstRender = true;

    private Font brandonBig = new Font("BrandonGrotesque-Black", Font.PLAIN, 350);
    private Font brandonSmall = new Font("BrandonGrotesque-Black", Font.PLAIN, 150);
    private Font brandonTiny = new Font("BrandonGrotesque-Black", Font.PLAIN, 20);

    private int writeScore;
    private int oldWriteScore;
    private boolean first = true;
    private int playerY;
    private int scoreHeight = Main.HEIGHT / 2 - 50;

    private FontMetrics metricsBrandonBig;
    private FontMetrics metricsBrandonSmall;
    private FontMetrics metricsBrandonTiny;
    
    private MidiChannel playerJumpChannel = Sound.getChannelBy(Sound.PLAYER_JUMP);
    private MidiChannel playerCollisionChannel = Sound.getChannelBy(Sound.PLAYER_COLLIDE);
    private MidiChannel playerDeathChannel = Sound.getChannelBy(Sound.PLAYER_DEATH);
    private MidiChannel playerScoreChannel = Sound.getChannelBy(Sound.PLAYER_SCORE);

    public FlattenedEngine() {
        systemViews = new ArrayList[systemMethods.length];
        for(int i = 0; i < systemViews.length; i++){
            systemViews[i] = new ArrayList<>();
        }
    }

    public void update(double deltaTime) {
        updating = true;

        updateSystems(deltaTime);

        updateEntities();

        updating = false;
    }

    public void render(Graphics2D graphics) {
    	final List<float[]> lights = systemViews[3];

    	graphics.setTransform(camera);
    	for(float[] lightEntity : lights) {
    		calculateShadows(lightEntity, graphics);
    	}

        if (firstRender) {
    	    firstRender = false;
    	    metricsBrandonBig = getFontMetrics(graphics, brandonBig);
    	    metricsBrandonSmall = getFontMetrics(graphics, brandonSmall);
    	    metricsBrandonTiny = getFontMetrics(graphics, brandonTiny);
        }

		if (Level.getGameState() == 0) {
            drawCenteredString(graphics,"SUPER ALONE", Main.WIDTH / 2, Main.HEIGHT / 2, brandonSmall, Renderer.BULLET_COLOR, metricsBrandonSmall);
            drawCenteredString(graphics,"PRESS SPACE TO START", Main.WIDTH / 2, (int) (Main.HEIGHT * 0.9f), brandonTiny, Renderer.BULLETTRAIL_GRADIENT_DARK, metricsBrandonTiny);
        }

        if (Level.getGameState() == 1) {

            first = true;

            highScore =  (int) Math.max((-camera.getTranslateX() - 180) / (Main.WIDTH / 8), 0);

            int playerX = (int)Entities.getFirstPlayer()[EntityIndex.POSITION_X.getIndex()];

            if (playerX < 1000) {
                playerY = (int) Entities.getFirstPlayer()[EntityIndex.POSITION_Y.getIndex()] - 130;

                playerX = Math.min(playerX, 1000);

                drawLeftCenteredString(graphics, "A & D",  playerX + 5, playerY - 50, brandonTiny, Renderer.PLAYER_COLOR, metricsBrandonTiny);
                drawRightCenteredString(graphics, "MOVE", playerX - 5, playerY - 50, brandonTiny, Renderer.BULLET_COLOR, metricsBrandonTiny);

                drawLeftCenteredString(graphics, "SPACE", playerX + 5, playerY, brandonTiny, Renderer.PLAYER_COLOR, metricsBrandonTiny);
                drawRightCenteredString(graphics, "JUMP", playerX - 5, playerY, brandonTiny, Renderer.BULLET_COLOR, metricsBrandonTiny);

                drawLeftCenteredString(graphics, "S", playerX + 5, playerY + 50, brandonTiny, Renderer.PLAYER_COLOR, metricsBrandonTiny);
                drawRightCenteredString(graphics, "DUCK", playerX -5, playerY + 50, brandonTiny, Renderer.BULLET_COLOR, metricsBrandonTiny);
            } else {
                if (highScore != oldHighscore) {
                    oldHighscore = highScore;
                    Sound.playNoteFor(playerScoreChannel, 100, 1000);
                }
                drawCenteredString(graphics, highScore + "", (int) (Main.WIDTH / 2 - camera.getTranslateX()), Main.HEIGHT / 2 - 50, brandonBig, Renderer.SCORE_COLOR, metricsBrandonBig);
            }
        }

		if (Level.getGameState() == 2) {
    	    if (first) {
    	        first = false;
                scoreHeight = Main.HEIGHT / 2 - 50;
                TweenEngine.getInstance()
                        .tween(0, highScore, highScore * 0.1f, TweenEngine.Type.CubicEaseInOut)
                        .onTweenUpdated((value) -> {
                            writeScore = (int)value;
                            if (writeScore != oldWriteScore) {
                                oldWriteScore = writeScore;
                                Sound.playNoteFor(playerScoreChannel, 100, 1000);
                                TweenEngine.getInstance()
                                        .tween(Main.HEIGHT / 2 - 40, Main.HEIGHT / 2 - 50, 0.1f, TweenEngine.Type.SineEaseInOut)
                                        .onTweenUpdated((value2) -> scoreHeight = (int)value2)
                                        .start();
                            }

                        })
                        .start();
            }

            writeScore = Math.min(writeScore, highScore);

            drawRightCenteredString(graphics, "SCORE", Main.WIDTH / 2 + 180, Main.HEIGHT / 2 - 50, brandonSmall, Renderer.BULLET_COLOR, metricsBrandonSmall);
            drawLeftCenteredString(graphics, writeScore + "", Main.WIDTH / 2 + 200, scoreHeight, brandonSmall, Renderer.PLAYER_COLOR, metricsBrandonSmall);
            drawCenteredString(graphics, "PRESS SPACE TO PLAY AGAIN", Main.WIDTH / 2, (int) (Main.HEIGHT * 0.9f), brandonTiny, Renderer.BULLETTRAIL_GRADIENT_DARK, metricsBrandonTiny);
        }
    }

    int oldHighscore;

    int lerp(float point1, float point2, float alpha)
    {
        return (int) (point1 + alpha * (point2 - point1));
    }

    private FontMetrics getFontMetrics(Graphics g, Font font) {
        return g.getFontMetrics(font);
    }

    public void drawCenteredString(Graphics g, String text, int x, int y, Font font, Color color, FontMetrics metrics) {
        g.setColor(color);
        g.setFont(font);
        g.drawString(text, x - metrics.stringWidth(text) / 2, y + metrics.getHeight() / 4);
    }

    public void drawLeftCenteredString(Graphics g, String text, int x, int y, Font font, Color color, FontMetrics metrics) {
        g.setColor(color);
        g.setFont(font);
        g.drawString(text, x, y + metrics.getHeight() / 4);
    }

    public void drawRightCenteredString(Graphics g, String text, int x, int y, Font font, Color color, FontMetrics metrics) {
        g.setColor(color);
        g.setFont(font);
        g.drawString(text, x - metrics.stringWidth(text), y + metrics.getHeight() / 4);
    }

    private void updateEntities() {
        //remove pending entities
        for(int i = entitiesToDelete.size() - 1 ; i >= 0; i--) {
            removeEntityInternal(entitiesToDelete.get(i));
        }
        entitiesToDelete.clear();

        //add pending entities
        for(int i = 0; i < entitiesToAdd.size(); i++) {
            addEntityInternal(entitiesToAdd.get(i));
        }
        entitiesToAdd.clear();
    }

    private void updateSystems(double deltaTime) {
        totalTime += deltaTime;

        //individual entity update
        for(int i = 0; i < systemViews.length; i++) {
            for(int j = 0; j < systemViews[i].size(); j++) {
                systemMethods[i].execute(this, systemViews[i].get(j), deltaTime);
            }
        }

        //general update
        Sound.setBackgroundTempo((float) Math.max(0.7, currentTimeScale));
		bulletSystem(deltaTime * currentTimeScale * currentTimeScale);
        performCollisionDetection();
		rainSystem(deltaTime * currentTimeScale * currentTimeScale);
    }

	public void addEntity(float[] entity) {
        assert entity.length > 1;

        if(updating){
            entitiesToAdd.add(entity);
        }else{
            addEntityInternal(entity);
        }
    }

    private void addEntityInternal(float[] entity) {
        entities.add(entity);
        addEntityToViews(entity);
    }

    //adds an entity to the proper views
    private void addEntityToViews(float[] entity) {
        int entityMask = (int) entity[EntityIndex.SYSTEM_MASK.getIndex()];
        for(int i = 0; i < systemViews.length; i++){
            if((entityMask & systemBitmasks[i]) == systemBitmasks[i]) {
                systemViews[i].add(entity);
            }
        }
    }

    public void removeEntity(float[] entity) {
        if(updating){
            entitiesToDelete.add(entity);
        }else{
            removeEntityInternal(entity);
        }
    }

    private void removeEntityInternal(float[] entity) {
        removeEntityFromViews(entity);
        entities.remove(entity);
    }

    private void removeEntityFromViews(float[] entity) {
        for(int i = 0; i < systemViews.length; i++){
            systemViews[i].remove(entity);
        }
    }

    public ArrayList<float[]> getEntities() {
        return entities;
    }


    private interface SystemMethod {
        void execute(FlattenedEngine engine, float[] entity, double deltaTime);
    }

    /**
     * @param originalMask Original mask from enumeration (Or other source).
     * @param maskToCheck The mask you want to check against the original mask.
     * @return Returns <strong>true</strong> if the masks are fitting.
     */
    public static boolean isBitmaskValid(int originalMask, int maskToCheck) {
        return (originalMask & maskToCheck) == originalMask;
    }

    // ---- ENTITY SYSTEM METHODS

    //region Input System (player, camera, menu, timescale
    private void inputProcessing(float[] entity, double deltaTime) {
        //player movement
        playerControl(entity, deltaTime);

        //timescale update
        timeScaleControl(entity, deltaTime);

        //camera position update
        cameraControl(entity, deltaTime);

        //menu control stuff
        menuControl();
    }

    //player control parameters
    private float movementSpeed = 1200.0f;
    private float maxMovementSpeed = 400.0f;
    private float jumpStrength = 1600.0f;
    private float playerGravity = 2500.0f;
    private boolean isJumping = false;
    private boolean isDoubleJumping = false;
    private boolean jumpRequestValid = true;
    private boolean isJumpRequested = false;
    private boolean isCrouched = false;

    private void playerControl(float[] player, double deltaTime) {
		if (Level.getGameState() == 1 && player[EntityIndex.LIFE.getIndex()] > 0.5f) {
            if(WindowWithFlattenedECS.isKeyPressed(KeyEvent.VK_A)) {
                player[EntityIndex.VELOCITY_X.getIndex()] -= movementSpeed * deltaTime;
                player[EntityIndex.VELOCITY_X.getIndex()] = player[EntityIndex.VELOCITY_X.getIndex()] < -maxMovementSpeed ? -maxMovementSpeed : player[EntityIndex.VELOCITY_X.getIndex()];
            }

            if(WindowWithFlattenedECS.isKeyPressed(KeyEvent.VK_D)) {
                player[EntityIndex.VELOCITY_X.getIndex()] += movementSpeed * deltaTime;
                player[EntityIndex.VELOCITY_X.getIndex()] = player[EntityIndex.VELOCITY_X.getIndex()] > maxMovementSpeed ? maxMovementSpeed : player[EntityIndex.VELOCITY_X.getIndex()];
            }

            // Grounded Tween and collision sound
            if (player[EntityIndex.TRIGGER_ENTER.getIndex()] > 0.5f && !WindowWithFlattenedECS.isKeyPressed(KeyEvent.VK_S)) {
            	Sound.playNoteFor(playerCollisionChannel, 23, 1000);
            	Sound.stopNoteFor(playerCollisionChannel, 23, 1000);
                TweenEngine.getInstance()
						.tween(player, EntityIndex.EXTENT_Y.getIndex(), EntityIndex.AABB_EXTENT_Y.getIndex(), 35, 0.0f,
								TweenEngine.Type.SineEaseInOut)
						.tween(player, EntityIndex.EXTENT_Y.getIndex(), EntityIndex.AABB_EXTENT_Y.getIndex(), 40, 0.2f,
                                TweenEngine.Type.SineEaseInOut)
                        .start();

                TweenEngine.getInstance()
						.tween(player, EntityIndex.EXTENT_X.getIndex(), EntityIndex.AABB_EXTENT_X.getIndex(), 23, 0.0f,
                                TweenEngine.Type.SineEaseInOut)
						.tween(player, EntityIndex.EXTENT_X.getIndex(), EntityIndex.AABB_EXTENT_X.getIndex(), 20, 0.2f,
                                TweenEngine.Type.SineEaseInOut)
                        .start();
            }

            boolean isGrounded = false;
            if(player[EntityIndex.TRIGGER_STAY.getIndex()] > 0.5f || player[EntityIndex.TRIGGER_ENTER.getIndex()] > 0.5f) {
                isJumpRequested = false;
                isGrounded = true;
                isJumping = false;
                isDoubleJumping = false;
            }

            if(!WindowWithFlattenedECS.isKeyPressed(KeyEvent.VK_SPACE)) {
                jumpRequestValid = true;
            }

            //jump requested?
            if(WindowWithFlattenedECS.isKeyPressed(KeyEvent.VK_SPACE) && jumpRequestValid) {
            	
                jumpRequestValid = false;
                isJumpRequested = true;
            }

            //first jump
            if(isGrounded && isJumpRequested) {
            	Sound.playNoteFor(playerJumpChannel, 45, 1000);
            	Sound.stopNoteFor(playerJumpChannel, 45, 60);

                TweenEngine.getInstance()
						.tween(player, EntityIndex.EXTENT_Y.getIndex(), EntityIndex.AABB_EXTENT_Y.getIndex(), 20, 0.0f,
                                TweenEngine.Type.SineEaseInOut)
						.tween(player, EntityIndex.EXTENT_Y.getIndex(), EntityIndex.AABB_EXTENT_Y.getIndex(),40, 0.2f, TweenEngine.Type.SineEaseInOut)
                        .start();

                TweenEngine.getInstance()
						.tween(player, EntityIndex.EXTENT_X.getIndex(), EntityIndex.AABB_EXTENT_X.getIndex(), 25, 0.0f,
                                TweenEngine.Type.SineEaseInOut)
						.tween(player, EntityIndex.EXTENT_X.getIndex(), EntityIndex.AABB_EXTENT_X.getIndex(),20, 0.2f, TweenEngine.Type.SineEaseInOut)
                        .start();

                player[EntityIndex.VELOCITY_Y.getIndex()] = -jumpStrength;
                isJumping = true;
                isJumpRequested = false;
            }

            //second jump
            if((isJumping && !isDoubleJumping && isJumpRequested)||(!isJumping && !isDoubleJumping && isJumpRequested)) {
            	Sound.playNoteFor(playerJumpChannel, 50, 1000);
            	Sound.stopNoteFor(playerJumpChannel, 50, 60);

                TweenEngine.getInstance()
						.tween(player, EntityIndex.EXTENT_Y.getIndex(), EntityIndex.AABB_EXTENT_Y.getIndex(), 30, 0.0f,
                                TweenEngine.Type.SineEaseInOut)
						.tween(player, EntityIndex.EXTENT_Y.getIndex(), EntityIndex.AABB_EXTENT_Y.getIndex(),40, 0.2f, TweenEngine.Type.SineEaseInOut)
                        .start();

                TweenEngine.getInstance()
						.tween(player, EntityIndex.EXTENT_X.getIndex(), EntityIndex.AABB_EXTENT_X.getIndex(), 25, 0.0f,
                                TweenEngine.Type.SineEaseInOut)
						.tween(player, EntityIndex.EXTENT_X.getIndex(), EntityIndex.AABB_EXTENT_X.getIndex(),20, 0.2f, TweenEngine.Type.SineEaseInOut)
                        .start();

				player[EntityIndex.VELOCITY_Y.getIndex()] = -jumpStrength * 0.8f;
                isDoubleJumping = true;
                isJumpRequested = false;
            }

            //crouch

            if(WindowWithFlattenedECS.isKeyPressed(KeyEvent.VK_S) && isGrounded && !isCrouched) {
                isCrouched = true;

                TweenEngine.getInstance()
						.tween(player, EntityIndex.EXTENT_Y.getIndex(), EntityIndex.AABB_EXTENT_Y.getIndex(), 25, 0.1f,
                                TweenEngine.Type.SineEaseInOut)
                        .start();

                TweenEngine.getInstance()
						.tween(player, EntityIndex.EXTENT_X.getIndex(), EntityIndex.AABB_EXTENT_X.getIndex(), 25, 0.1f,
                                TweenEngine.Type.SineEaseInOut)
                        .start();
            }

            if(isCrouched) {
                if(!WindowWithFlattenedECS.isKeyPressed(KeyEvent.VK_S)) {
                    isCrouched = false;
                    TweenEngine.getInstance()
							.tween(player, EntityIndex.EXTENT_Y.getIndex(), EntityIndex.AABB_EXTENT_Y.getIndex(), 40,
									0.1f, TweenEngine.Type.SineEaseInOut)
                            .start();

                    TweenEngine.getInstance()
							.tween(player, EntityIndex.EXTENT_X.getIndex(), EntityIndex.AABB_EXTENT_X.getIndex(), 20,
									0.1f, TweenEngine.Type.SineEaseInOut)
                            .start();
                } else if(!isGrounded) {
                    isCrouched = false;
                }
            }

            player[EntityIndex.VELOCITY_X.getIndex()] *= player[EntityIndex.DRAG.getIndex()];
            player[EntityIndex.VELOCITY_Y.getIndex()] *= player[EntityIndex.DRAG.getIndex()];

            player[EntityIndex.VELOCITY_Y.getIndex()] += playerGravity * player[EntityIndex.GRAVITATION_INFLUENCE.getIndex()] * deltaTime;

            player[EntityIndex.POSITION_X.getIndex()] += player[EntityIndex.VELOCITY_X.getIndex()] * deltaTime;
            player[EntityIndex.POSITION_Y.getIndex()] += player[EntityIndex.VELOCITY_Y.getIndex()] * deltaTime;
		}
    }

    private void timeScaleControl(float[] player, double deltaTime) {
		final float relativeHorizontalSpeed = Math.abs(player[EntityIndex.VELOCITY_X.getIndex()] / maxMovementSpeed);
        final float value = relativeHorizontalSpeed < 0.35f ? 0.35f : relativeHorizontalSpeed;
		currentTimeScale = TweenEngine.updateEasing(TweenEngine.Type.CubicEaseInOut, value, 0.0f, 1.0f, 1.0f);
    }

    private void menuControl() {
        if(WindowWithFlattenedECS.isKeyPressed(KeyEvent.VK_ESCAPE)) {
            System.exit(42);
        }

        if(Level.getGameState() == 0) {
            emitBullets = false;
            suspendPlayer(Entities.getFirstPlayer());
            currentTimeScale = 1.0f;
            if(WindowWithFlattenedECS.isKeyPressed(KeyEvent.VK_SPACE)) {
                emitBullets = true;
                Level.setGameState(1);
                minPosX = 0;
                maxPosX = 600;
            }
        }

        if(Level.getGameState() == 2) {
            emitBullets = false;
            suspendPlayer(Entities.getFirstPlayer());
            currentTimeScale = 1.0f;
            //if(WindowWithFlattenedECS.isKeyPressed(KeyEvent.VK_N)) {
            if(WindowWithFlattenedECS.isKeyPressed(KeyEvent.VK_SPACE)) {
                emitBullets = true;
                Level.setGameState(1);
                minPosX = 0;
                maxPosX = 600;
                //Level.setGameState(0);
            }
        }
        
        // Activate/Deactivate lighting debug mode.
        if(WindowWithFlattenedECS.isKeyPressed(KeyEvent.VK_L)) {
        	DEBUG_SHADOWS = !DEBUG_SHADOWS;
        }
    }

    //camera control parameters
    private float minPosX = 0.0f;
    private float maxPosX = 600.0f;

    private float previousX = 500.0f;

    private final AffineTransform camera = new AffineTransform();

    private void cameraControl(float[] player, double deltaTime) {
        float xChange = 0.0f;
        if(player[EntityIndex.POSITION_X.getIndex()] - player[EntityIndex.EXTENT_X.getIndex()] < minPosX) {
            player[EntityIndex.POSITION_X.getIndex()] = minPosX + player[EntityIndex.EXTENT_X.getIndex()];

        } else if(player[EntityIndex.POSITION_X.getIndex()] > maxPosX && player[EntityIndex.POSITION_X.getIndex()] > previousX) {
            xChange = player[EntityIndex.POSITION_X.getIndex()] - previousX;
        }
        camera.setToTranslation(camera.getTranslateX() - xChange, camera.getTranslateY());
        minPosX += xChange;
        maxPosX += xChange;

        previousX = player[EntityIndex.POSITION_X.getIndex()];
    }

    public AffineTransform getCamera() {
        return camera;
    }
    //endregion)

    //region Lighting System

    private void lightingSystem(float[] lightSource, double deltaTime) {
		Entities.setPositionFor(lightSource, (float) (Main.WIDTH - camera.getTranslateX()), Main.HEIGHT / 6);
    }

    private Ray[] getCornerRays(Vector2 lightPosition, float[] entity) {
        // Box ray collision
        if(isBitmaskValid(EntityType.BOX_SHADOW.getEntityType(), (int) entity[EntityIndex.ENTITY_TYPE_ID.getIndex()])) {
            Vector2 min = new Vector2(
            		entity[EntityIndex.POSITION_X.getIndex()] - entity[EntityIndex.EXTENT_X.getIndex()],
                    entity[EntityIndex.POSITION_Y.getIndex()] - entity[EntityIndex.EXTENT_Y.getIndex()]);
            final float width = entity[EntityIndex.EXTENT_X.getIndex()] * 2;
            final float height = entity[EntityIndex.EXTENT_Y.getIndex()] * 2;

            final Vector2 toLeftTop = new Vector2(Math.max(min.x, -camera.getTranslateX()), Math.max(min.y, 0.0)).sub(lightPosition);
            final Ray leftTop = new Ray(lightPosition, toLeftTop);

            final Vector2 toRightTop = new Vector2(Math.min(min.x + width, -camera.getTranslateX() + Main.WIDTH), Math.max(min.y, 0.0)).sub(lightPosition);
            final Ray rightTop = new Ray(lightPosition, toRightTop);

            final Vector2 toLeftBottom = new Vector2(Math.max(min.x, -camera.getTranslateX()), Math.min(min.y + height, Main.HEIGHT)).sub(lightPosition);
            final Ray leftBottom = new Ray(lightPosition, toLeftBottom);

            final Vector2 toRightBottom = new Vector2(Math.min(min.x + width, -camera.getTranslateX() + Main.WIDTH), Math.min(min.y + height, Main.HEIGHT)).sub(lightPosition);
            final Ray rightBottom = new Ray(lightPosition, toRightBottom);

            return new Ray[] { leftTop, rightTop, leftBottom, rightBottom };
        } else if(isBitmaskValid(EntityType.SCREEN_BORDER.getEntityType(), (int) entity[EntityIndex.ENTITY_TYPE_ID.getIndex()])) {
            final Ray borderRay = new Ray(
                    new Vector2(entity[EntityIndex.BORDER_ORIGIN_X.getIndex()] - camera.getTranslateX(), entity[EntityIndex.BORDER_ORIGIN_Y.getIndex()] - camera.getTranslateY()),
                    new Vector2(entity[EntityIndex.BORDER_DIR_X.getIndex()], entity[EntityIndex.BORDER_DIR_Y.getIndex()]));

            final Vector2 toCornerPosition = new Vector2(borderRay.origin.x, borderRay.origin.y).sub(lightPosition);
            return new Ray[] { new Ray(lightPosition, toCornerPosition) };
        }
        return null;
    }

    private void intersect(Ray ray, float[] entity) {
        // Box ray collision
        if(isBitmaskValid(EntityType.BOX_SHADOW.getEntityType(), (int) entity[EntityIndex.ENTITY_TYPE_ID.getIndex()])) {
            Vector2 min = new Vector2(
            		entity[EntityIndex.POSITION_X.getIndex()] - entity[EntityIndex.EXTENT_X.getIndex()],
                    entity[EntityIndex.POSITION_Y.getIndex()] - entity[EntityIndex.EXTENT_Y.getIndex()]);
            Vector2 max = new Vector2(
            		entity[EntityIndex.POSITION_X.getIndex()] + entity[EntityIndex.EXTENT_X.getIndex()],
                    entity[EntityIndex.POSITION_Y.getIndex()] + entity[EntityIndex.EXTENT_Y.getIndex()]);

            double swap;

            double txMin = (min.x - ray.origin.x) / ray.direction.x;
            double txMax = (max.x - ray.origin.x) / ray.direction.x;

            if(txMin > txMax) {
                swap = txMin;
                txMin = txMax;
                txMax = swap;
            }

            double tyMin = (min.y - ray.origin.y) / ray.direction.y;
            double tyMax = (max.y - ray.origin.y) / ray.direction.y;

            if(tyMin > tyMax) {
                swap = tyMin;
                tyMin = tyMax;
                tyMax = swap;
            }

            if(txMin > tyMax || tyMin > txMax) {
                return;
            }

            final double tMin = (txMin > tyMin) ? txMin : tyMin;	// Choose max
            final double tMax = (txMax < tyMax) ? txMax : tyMax;	// Choose min

            final double closestDistance = (tMin < tMax) ? tMin : tMax;
            if(closestDistance < 0) {
                return;
            }
            final Vector2 hitPoint = ray.origin.copy().add(ray.direction.copy().scale(closestDistance));
            ray.updateHitInformation(hitPoint);

        } else if(isBitmaskValid(EntityType.SCREEN_BORDER.getEntityType(), (int) entity[EntityIndex.ENTITY_TYPE_ID.getIndex()])) {
            final Ray borderRay = new Ray(
            		new Vector2(entity[EntityIndex.BORDER_ORIGIN_X.getIndex()] - camera.getTranslateX(), entity[EntityIndex.BORDER_ORIGIN_Y.getIndex()] - camera.getTranslateY()),
            		new Vector2(entity[EntityIndex.BORDER_DIR_X.getIndex()], entity[EntityIndex.BORDER_DIR_Y.getIndex()]));

            final Vector2 hitPoint = ray.collideWith(borderRay, true);

            if(hitPoint != null) {
                ray.updateHitInformation(hitPoint);
            }
        }
    }

    private void calculateShadows(float[] lightEntity, Graphics2D graphics) {
    	final Vector2 lightPosition = Entities.getPositionFor(lightEntity);
    	final Path2D.Double path = new Path2D.Double();
    	addRaysOfEntities(lightPosition);
    	
    	// Check all possible intersections of the given rays
    	for(float[] entity : entities) {
    		for(Ray sortedRay : angleSortedRays) {
    			intersect(sortedRay, entity);
    		}
    	}

    	// Draw polygon with the hit infos of the rays
    	// TODO: first() -> Validate if there was a hit
    	path.moveTo(angleSortedRays.first().hitPoint.x, angleSortedRays.first().hitPoint.y);
    	for(Ray sortedRay : angleSortedRays) {
    		if(sortedRay.isValidHit()) {
    			path.lineTo(sortedRay.hitPoint.x, sortedRay.hitPoint.y);
    			
    			// Debug draw
    			if(DEBUG_SHADOWS) {
    				graphics.setColor(Color.RED);
    				graphics.drawLine((int) sortedRay.origin.x, (int) sortedRay.origin.y, (int) sortedRay.hitPoint.x, (int) sortedRay.hitPoint.y);
    				graphics.fillOval((int) sortedRay.hitPoint.x - 12, (int) sortedRay.hitPoint.y - 12, 24, 24);
    			}
    		}
    	}

        /*RadialGradientPaint rgp = new RadialGradientPaint(
                new Point2D.Float((float)(Main.WIDTH - camera.getTranslateX()) , Main.HEIGHT * 0.2f),
                Main.WIDTH,
                new float[] {0.0f, 1f},
                new Color[] {Renderer.BACKGROUND_GRADIENT_LIGHT, Renderer.BACKGROUND_GRADIENT_DARK});

        graphics.setPaint(rgp);*/

        graphics.setPaint(new GradientPaint((float)-camera.getTranslateX(), 0, Renderer.BACKGROUND_GRADIENT_DARK, (float)(Main.WIDTH -camera.getTranslateX()), 0, Renderer.BACKGROUND_GRADIENT_LIGHT));


		path.closePath();

		// TODO: clip image for background
		/*graphics.setClip(path);
		graphics.drawImage(Renderer.background, 0,0, null);
        graphics.setClip(null);*/

		if(!DEBUG_SHADOWS) {	// TODO: Remove this in finished code
			graphics.fill(path);
		}
		angleSortedRays.clear();

	}

    private void addRaysOfEntities(Vector2 lightPosition) {
		// Rays to entity corners
		for(float[] entity : entities) {
			final Ray[] cornerRays = getCornerRays(lightPosition, entity);
			if(cornerRays != null) {
				for(Ray r : cornerRays) {
					addOffsetRays(r);
				}
			}
		}
	}
	
	private void addOffsetRays(Ray originalRay) {
		final Vector2 originalDirection = originalRay.direction.copy();
		final Ray leftOffset = new Ray(originalRay.origin, originalDirection.copy().rotateBy(-0.000001));
		final Ray rightOffset = new Ray(originalRay.origin, originalDirection.copy().rotateBy(0.000001));
		
		angleSortedRays.add(leftOffset);
		angleSortedRays.add(originalRay);
		angleSortedRays.add(rightOffset);
	}
    //endregion

    //region Collision System

    //collision detection variables
    private ArrayList<float[]> staticColliders = new ArrayList<>();
    private ArrayList<float[]> dynamicColliders = new ArrayList<>();

    private void colliderSorting(float[] entity, double deltaTime) {
        if(entity[EntityIndex.COLLISION_TYPE.getIndex()] > 0.5f) {
            dynamicColliders.add(entity);
        } else {
            staticColliders.add(entity);
        }
    }

    private void performCollisionDetection() {
        for(int i = 0; i < dynamicColliders.size(); i++) {
            final float[] entity = dynamicColliders.get(i);
            for(int j = 0; j < staticColliders.size(); j++) {
                collisionCheckAABB(entity, staticColliders.get(j));
            }
        }

        for(int i= 0; i < dynamicColliders.size(); i++) {
            final float[] entity = dynamicColliders.get(i);
            for(int j = i + 1; j < dynamicColliders.size(); j++) {
                collisionCheckAABB(entity, dynamicColliders.get(j));
            }
        }
        staticColliders.clear();
        dynamicColliders.clear();
    }

    //region Collision Check AABB
    private void collisionCheckAABB(float[] entity1, float[] entity2) {
    	float xOverlap = 
    			Math.abs(
    					(entity1[EntityIndex.POSITION_X.getIndex()] + entity1[EntityIndex.AABB_CENTER_X.getIndex()]) -
    					(entity2[EntityIndex.POSITION_X.getIndex()] + entity2[EntityIndex.AABB_CENTER_X.getIndex()])) -
    					(entity1[EntityIndex.AABB_EXTENT_X.getIndex()] + entity2[EntityIndex.AABB_EXTENT_X.getIndex()]);
    	
    	if ((xOverlap) < 0) {
    		float yOverlap = Math.abs(
    				(entity1[EntityIndex.POSITION_Y.getIndex()] + entity1[EntityIndex.AABB_CENTER_Y.getIndex()]) -
    				(entity2[EntityIndex.POSITION_Y.getIndex()] + entity2[EntityIndex.AABB_CENTER_Y.getIndex()])) -
    				(entity1[EntityIndex.AABB_EXTENT_Y.getIndex()] + entity2[EntityIndex.AABB_EXTENT_Y.getIndex()]);
    		
            if ((yOverlap) < 0) {

                //if one of the entities is a raindrop splatter
                int entity1Id = (int) entity1[EntityIndex.ENTITY_TYPE_ID.getIndex()];
                int entity2Id = (int) entity2[EntityIndex.ENTITY_TYPE_ID.getIndex()];

                if(isBitmaskValid(EntityType.RAIN_DROP_SPLATTER.getEntityType(), entity1Id) && 
                		isBitmaskValid(EntityType.RAIN_DROP_SPLATTER.getEntityType(), entity2Id)) {
                	return;
                }

                //if raindrop splatter and other entity
                if(isBitmaskValid(EntityType.RAIN_DROP_SPLATTER.getEntityType(), entity1Id) && 
                		!isBitmaskValid(EntityType.RAIN_DROP_SPLATTER.getEntityType(), entity2Id)) {
                	removeEntity(entity1);
                	return;
                }

                if(isBitmaskValid(EntityType.RAIN_DROP_SPLATTER.getEntityType(), entity2Id) && 
                		!isBitmaskValid(EntityType.RAIN_DROP_SPLATTER.getEntityType(), entity1Id)) {
                	removeEntity(entity2);
                	return;
                }

                //two raindrops do not collide
                if(isBitmaskValid(EntityType.RAIN_DROP.getEntityType(), entity1Id) && 
                		isBitmaskValid(EntityType.RAIN_DROP.getEntityType(), entity2Id)) {
                	return;
                }

                //if raindrop and other entity
                if(isBitmaskValid(EntityType.RAIN_DROP.getEntityType(), entity1Id) && 
                		!isBitmaskValid(EntityType.RAIN_DROP.getEntityType(), entity2Id)) {

                    emitRainSplatterParticles(new Vector2(entity1[EntityIndex.POSITION_X.getIndex()],
                                entity1[EntityIndex.POSITION_Y.getIndex()] + entity1[EntityIndex.EXTENT_Y.getIndex()]*0.9f), 2);

                	removeEntity(entity1);
                	return;
                }

                if(isBitmaskValid(EntityType.RAIN_DROP.getEntityType(), entity2Id) && 
                		!isBitmaskValid(EntityType.RAIN_DROP.getEntityType(), entity1Id)) {

                    emitRainSplatterParticles(new Vector2(entity2[EntityIndex.POSITION_X.getIndex()],
                            entity2[EntityIndex.POSITION_Y.getIndex()] + entity2[EntityIndex.EXTENT_Y.getIndex()] * 0.9f), 2);

                    removeEntity(entity2);
                	return;
                }

                //player and bullet
                if(isBitmaskValid(EntityType.PLAYER.getEntityType(), entity2Id) &&
                        isBitmaskValid(EntityType.BULLET.getEntityType(), entity1Id) && entity2[EntityIndex.LIFE.getIndex()] > 0.5f) {
                    removeEntity(entity1);
                    deathAnimation(entity2);
                    return;
                }

                if(isBitmaskValid(EntityType.BULLET.getEntityType(), entity2Id) &&
                        isBitmaskValid(EntityType.PLAYER.getEntityType(), entity1Id) && entity1[EntityIndex.LIFE.getIndex()] > 0.5f) {
                    removeEntity(entity2);
                    deathAnimation(entity1);
                    return;
                }

                //player and environment
                if(isBitmaskValid(EntityType.PLAYER.getEntityType(), entity1Id)) {
                	resolvePlayerCollision(entity1, entity2, xOverlap, yOverlap);
                	return;
                }
                if(isBitmaskValid(EntityType.PLAYER.getEntityType(), entity2Id)) {
                	resolvePlayerCollision(entity2, entity1, xOverlap, yOverlap);
                	return;
                }
                /*float[] toDelete = entity1[EntityIndex.COLLISION_TYPE.getIndex()] > 0.5f ? entity1 : entity2;
                removeEntity(toDelete);*/
            }
        }
    }

    private void deathAnimation(float[] player) {
    	// Death sound
    	Sound.playNoteFor(playerDeathChannel, 50, 1000);
    	Sound.stopNoteFor(playerDeathChannel, 50, 1000);
    	
        player[EntityIndex.GRAVITATION_INFLUENCE.getIndex()] = 0;
        player[EntityIndex.LIFE.getIndex()] = 0;
        player[EntityIndex.VELOCITY_X.getIndex()] = 0.0f;
        player[EntityIndex.VELOCITY_Y.getIndex()] = 0.0f;
        TweenEngine.getInstance()
				.tween(player, EntityIndex.EXTENT_Y.getIndex(), EntityIndex.AABB_EXTENT_Y.getIndex(), 50, 0.1f, TweenEngine.Type.SineEaseInOut)
				.tween(player, EntityIndex.EXTENT_Y.getIndex(), EntityIndex.AABB_EXTENT_Y.getIndex(),0, 0.5f, TweenEngine.Type.SineEaseInOut)
                .notifyTweenFinished((e) -> {suspendPlayer(player); Level.setGameState(2);})
                .start();

        TweenEngine.getInstance()
				.tween(player, EntityIndex.EXTENT_X.getIndex(), EntityIndex.AABB_EXTENT_X.getIndex(), 30, 0.1f, TweenEngine.Type.SineEaseInOut)
				.tween(player, EntityIndex.EXTENT_X.getIndex(), EntityIndex.AABB_EXTENT_X.getIndex(),0, 0.5f, TweenEngine.Type.SineEaseInOut)
                .start();
    }

    private void suspendPlayer(float[] player) {
        camera.setToTranslation(0,0);
        previousX = 450;
        Entities.setPositionFor(player, 450, -1000);
        //player[EntityIndex.COLOR_A.getIndex()] = 0.0f;
        player[EntityIndex.GRAVITATION_INFLUENCE.getIndex()] = 0.0f;
        player[EntityIndex.VELOCITY_X.getIndex()] = 0.0f;
        player[EntityIndex.VELOCITY_Y.getIndex()] = 0.0f;
        player[EntityIndex.EXTENT_X.getIndex()] = 20.0f;
        player[EntityIndex.EXTENT_Y.getIndex()] = 40.0f;
        player[EntityIndex.AABB_EXTENT_X.getIndex()] = 20.0f;
        player[EntityIndex.AABB_EXTENT_Y.getIndex()] = 40.0f;
        player[EntityIndex.LIFE.getIndex()] = 1.0f;
    }
    //endregion

    //region Collision Response

    private final float tolerance = 1.1f;
    private void resolvePlayerCollision(float[] player, float[] other, float xOverlap, float yOverlap){
        boolean xResolve = (xOverlap > yOverlap) && (xOverlap-yOverlap > tolerance);
        if(xResolve) {
            float xOffset = player[EntityIndex.POSITION_X.getIndex()] < other[EntityIndex.POSITION_X.getIndex()] ? xOverlap : -xOverlap;
            player[EntityIndex.POSITION_X.getIndex()] += xOffset;
            player[EntityIndex.VELOCITY_X.getIndex()] = 0.0f;
        } else {
            float yOffset = player[EntityIndex.POSITION_Y.getIndex()] < other[EntityIndex.POSITION_Y.getIndex()] ? yOverlap : -yOverlap;
            player[EntityIndex.POSITION_Y.getIndex()] += yOffset;
            player[EntityIndex.VELOCITY_Y.getIndex()] = 0.0f;
        }
    }
    //endregion
    //endregion

    //region Movement System
    private float gravity = 3000.0f;

    private void movementSystem(float[] entity, double deltaTime) {
        final double scaledDeltaTime = deltaTime * currentTimeScale;

        entity[EntityIndex.VELOCITY_Y.getIndex()] +=  entity[EntityIndex.GRAVITATION_INFLUENCE.getIndex()] * gravity * scaledDeltaTime;

        entity[EntityIndex.POSITION_X.getIndex()] += entity[EntityIndex.VELOCITY_X.getIndex()] * scaledDeltaTime;
        entity[EntityIndex.POSITION_Y.getIndex()] += entity[EntityIndex.VELOCITY_Y.getIndex()] * scaledDeltaTime;

        entity[EntityIndex.VELOCITY_X.getIndex()] *= entity[EntityIndex.DRAG.getIndex()];
        entity[EntityIndex.VELOCITY_Y.getIndex()] *= entity[EntityIndex.DRAG.getIndex()];
    }
    //endregion

    //region Trigger System
    private boolean checkForTriggerOverlap(float[] trigger, float[] other) {
        float xOverlap =
                Math.abs(
                        (trigger[EntityIndex.POSITION_X.getIndex()] + trigger[EntityIndex.TRIGGER_POSITION_X.getIndex()]) -
                        (other[EntityIndex.POSITION_X.getIndex()] + other[EntityIndex.AABB_CENTER_X.getIndex()])) -
                        (trigger[EntityIndex.TRIGGER_EXTENT_X.getIndex()] + other[EntityIndex.AABB_EXTENT_X.getIndex()]);

        if ((xOverlap) < 0) {
            float yOverlap = Math.abs(
                    (trigger[EntityIndex.POSITION_Y.getIndex()] + trigger[EntityIndex.TRIGGER_POSITION_Y.getIndex()]) -
                            (other[EntityIndex.POSITION_Y.getIndex()] + other[EntityIndex.AABB_CENTER_Y.getIndex()])) -
                    (trigger[EntityIndex.TRIGGER_EXTENT_Y.getIndex()] + other[EntityIndex.AABB_EXTENT_Y.getIndex()]);

            if ((yOverlap) < 0) {
                return true;
            }
        }
        return false;
    }

    private void triggerSystem(float[] trigger, double deltaTime) {
        ArrayList<float[]> colliders = (trigger[EntityIndex.TRIGGER_COLLISION_TYPE.getIndex()] > 0.5f) ? dynamicColliders : staticColliders;

        if(trigger[EntityIndex.TRIGGER_COLLISION_TYPE.getIndex()] > 1.5f){
            colliders = new ArrayList<>();
            colliders.add(Entities.getFirstPlayer());
        }

        for(int i = 0; i < colliders.size(); i++){
            if(colliders.get(i)!= trigger && checkForTriggerOverlap(trigger, colliders.get(i))){
                if(trigger[EntityIndex.TRIGGER_ENTER.getIndex()] < 0.5f && trigger[EntityIndex.TRIGGER_STAY.getIndex()] < 0.5f){
                    trigger[EntityIndex.TRIGGER_ENTER.getIndex()] = 1.0f;
                    trigger[EntityIndex.TRIGGER_OBJECT_TYPE.getIndex()] = colliders.get(i)[EntityIndex.ENTITY_TYPE_ID.getIndex()];
                    return;
                }

                if (trigger[EntityIndex.TRIGGER_ENTER.getIndex()] > 0.5f && trigger[EntityIndex.TRIGGER_STAY.getIndex()] < 0.5f){
                    trigger[EntityIndex.TRIGGER_STAY.getIndex()] = 1.0f;
                    trigger[EntityIndex.TRIGGER_ENTER.getIndex()] = 0.0f;
                    trigger[EntityIndex.TRIGGER_OBJECT_TYPE.getIndex()] = colliders.get(i)[EntityIndex.ENTITY_TYPE_ID.getIndex()];
                    return;
                }

                return;
            }
        }

        if(trigger[EntityIndex.TRIGGER_ENTER.getIndex()] > 0.5f ||trigger[EntityIndex.TRIGGER_STAY.getIndex()] > 0.5f) {
            trigger[EntityIndex.TRIGGER_EXIT.getIndex()] = 1.0f;
            trigger[EntityIndex.TRIGGER_ENTER.getIndex()] = 0.0f;
            trigger[EntityIndex.TRIGGER_STAY.getIndex()] = 0.0f;
        }else if(trigger[EntityIndex.TRIGGER_EXIT.getIndex()] >0.5f){
            trigger[EntityIndex.TRIGGER_EXIT.getIndex()] = 0.0f;
            trigger[EntityIndex.TRIGGER_OBJECT_TYPE.getIndex()] = EntityType.NONE.getEntityType();
        }
    }
    //endregion

    //region Lifetime System
    private void lifetimeSystem(float[] entity, double deltaTime) {
        entity[EntityIndex.LIFE.getIndex()] -= deltaTime;
        if(entity[EntityIndex.LIFE.getIndex()] < 0.0f) {
            removeEntity(entity);
        }
    }
    //endregion

    //region Rain System

    //rain particle constants
    private double RAIN_PARTICLE_SPAWN_HEIGHT = -100.0d;
    private double RAIN_PARTICLE_SPAWN_RANGE_BEGIN = -200.0d;
    private double RAIN_PARTICLE_SPAWN_RANGE = 1680.0d;

    private static double RAIN_WINDFORCE = 0.0d;
    private static double RAIN_DOWNFORCE = 50.0d;

    private static double RAIN_SPLATTER_FORCE = 500.0d;

    private Random random = new Random();


    //control parameters
    private boolean shouldEmit = true;
    private double remainingTime = 0.0d;
    private double emitRate = 1.0f / 100.0d;

    private void rainSystem(double deltaTime) {
        if(shouldEmit){
            remainingTime += deltaTime;

            while(remainingTime > emitRate) {
                remainingTime -= emitRate;
                emitRainParticle();
            }
        }
    }

    private void emitRainParticle() {
        final Vector2 position = new Vector2(RAIN_PARTICLE_SPAWN_RANGE * random.nextFloat() + RAIN_PARTICLE_SPAWN_RANGE_BEGIN - camera.getTranslateX(), RAIN_PARTICLE_SPAWN_HEIGHT - random.nextFloat() * 50.0f - camera.getTranslateY());
        final Vector2 velocity = new Vector2(RAIN_WINDFORCE, random.nextFloat() * RAIN_DOWNFORCE);
        addEntity(Entities.createRainParticle(position, velocity));
    }

    private void emitRainSplatterParticles(Vector2 position, int amount) {
        for(int i = 0; i < amount; i++){
            final Vector2 velocity = new Vector2((-0.5f + random.nextFloat()) * RAIN_SPLATTER_FORCE, random.nextFloat() * -RAIN_SPLATTER_FORCE);
            addEntity(Entities.createSplatterParticle(position, velocity));
        }
    }
    //endregion

    //region Cleanup System
    //death zone height
    double deathZoneHeight = Main.HEIGHT + 100;
    double rainDeathZoneHeight = Main.HEIGHT - 40.0f;

    private void cleanupSystem(float[] entity, double deltaTime) {
        double tolerance = camera.getTranslateX();
        if(entity[EntityIndex.POSITION_X.getIndex()] + entity[EntityIndex.EXTENT_X.getIndex()] < -tolerance) {
            removeEntity(entity);
            return;
        }
        if(isBitmaskValid(EntityType.RAIN_DROP.getEntityType(), (int) entity[EntityIndex.ENTITY_TYPE_ID.getIndex()]) && entity[EntityIndex.POSITION_Y.getIndex()]+entity[EntityIndex.EXTENT_Y.getIndex()] > rainDeathZoneHeight){
            emitRainSplatterParticles(new Vector2(entity[EntityIndex.POSITION_X.getIndex()],
                    entity[EntityIndex.POSITION_Y.getIndex()] + entity[EntityIndex.EXTENT_Y.getIndex()]*0.9f), 2);
            removeEntity(entity);
            return;
        }

        if(entity[EntityIndex.POSITION_Y.getIndex()] > deathZoneHeight) {
            if(isBitmaskValid(EntityType.PLAYER.getEntityType(), (int) entity[EntityIndex.ENTITY_TYPE_ID.getIndex()])){
                deathAnimation(entity);
                Level.setGameState(2);
                suspendPlayer(entity);
            }else{
                removeEntity(entity);
            }
        }
    }
    //endregion

    //region Platform Movement System
    private void platformMovementSystem(float[] entity, double deltaTime) {
        float tolerance = 0.5f;

        if(entity[EntityIndex.POSITION_X.getIndex()] < entity[EntityIndex.PLATFORM_RANGE_MIN_X.getIndex()] - tolerance) {
            entity[EntityIndex.POSITION_X.getIndex()] = entity[EntityIndex.PLATFORM_RANGE_MIN_X.getIndex()];
            entity[EntityIndex.VELOCITY_X.getIndex()] = -entity[EntityIndex.VELOCITY_X.getIndex()];
        }else if(entity[EntityIndex.POSITION_X.getIndex()] > entity[EntityIndex.PLATFORM_RANGE_MAX_X.getIndex()] + tolerance) {
            entity[EntityIndex.POSITION_X.getIndex()] = entity[EntityIndex.PLATFORM_RANGE_MAX_X.getIndex()];
            entity[EntityIndex.VELOCITY_X.getIndex()] = -entity[EntityIndex.VELOCITY_X.getIndex()];
        }

        if(entity[EntityIndex.POSITION_Y.getIndex()] < entity[EntityIndex.PLATFORM_RANGE_MIN_Y.getIndex()] - tolerance) {
            entity[EntityIndex.POSITION_Y.getIndex()] = entity[EntityIndex.PLATFORM_RANGE_MIN_Y.getIndex()];
            entity[EntityIndex.VELOCITY_Y.getIndex()] = -entity[EntityIndex.VELOCITY_Y.getIndex()];
        }else if(entity[EntityIndex.POSITION_Y.getIndex()] > entity[EntityIndex.PLATFORM_RANGE_MAX_Y.getIndex()] + tolerance) {
            entity[EntityIndex.POSITION_Y.getIndex()] = entity[EntityIndex.PLATFORM_RANGE_MAX_Y.getIndex()];
            entity[EntityIndex.VELOCITY_Y.getIndex()] = -entity[EntityIndex.VELOCITY_Y.getIndex()];
        }

        //System.out.println("trigger object: "+entity[EntityIndex.TRIGGER_OBJECT_TYPE.getIndex()]);
        if(isBitmaskValid(EntityType.PLAYER.getEntityType(), (int) entity[EntityIndex.TRIGGER_OBJECT_TYPE.getIndex()])) {
            double scaledDeltaTime = deltaTime * currentTimeScale;
            float[] player = Entities.getFirstPlayer();
            player[EntityIndex.POSITION_X.getIndex()] += entity[EntityIndex.VELOCITY_X.getIndex()] * scaledDeltaTime;
            player[EntityIndex.POSITION_Y.getIndex()] += entity[EntityIndex.VELOCITY_Y.getIndex()] * scaledDeltaTime;
        }

    }
    //endregion

    //region Bullet System

    private float minHeight =  Main.HEIGHT / 5.0f;
    private float maxHeight =  Main.HEIGHT * 0.85f;

    private float initialRateOfFire = 1.8f;
    private float currentRateOfFire = 1.0f/initialRateOfFire;
    private float spawnPosX = Main.WIDTH * 1.5f;

    private float elapsedTime = 0.0f;

    private float speedDeviation = 50.0f;
    private float initialBulletSpeed = -500.f;
    private float currentBulletSpeed = initialBulletSpeed;

    private int initialWaveCount = 15;
    private int currentWaveCount = initialWaveCount;

    private float increasePercentage = 0.02f;
    private boolean emitBullets = false;


    private void bulletSystem(double deltaTime) {
        if(emitBullets) {
            elapsedTime += deltaTime;

            if(elapsedTime > currentRateOfFire) {
                elapsedTime -= currentRateOfFire;
                spawnBullet();

                currentWaveCount--;
                if(currentWaveCount<=0) {
                    increaseDifficulty();
                }
            }
        }
    }

    private void increaseDifficulty() {
        currentWaveCount = initialWaveCount;
		currentBulletSpeed = currentBulletSpeed * (1.0f + increasePercentage);
        currentRateOfFire -= currentRateOfFire * increasePercentage;
    }

    private void spawnBullet() {
        Vector2 position = new Vector2(spawnPosX - camera.getTranslateX(), minHeight+random.nextFloat() * (maxHeight - minHeight) );
		Vector2 velocity = new Vector2(-speedDeviation / 2 + currentBulletSpeed + random.nextFloat() * speedDeviation, 0);
        addEntity(Entities.createBullet(position, velocity));
    }
    //endregion
}