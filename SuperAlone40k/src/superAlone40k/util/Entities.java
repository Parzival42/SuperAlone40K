package superAlone40k.util;

import superAlone40k.ecs.EntityIndex;
import superAlone40k.ecs.EntityType;
import superAlone40k.ecs.SystemBitmask;
import superAlone40k.renderer.Renderer;

import java.util.ArrayList;
import java.util.SplittableRandom;

public class Entities {
    private final static SplittableRandom random = new SplittableRandom();

    //rain particle constants
    public static double RAIN_PARTICLE_WIDTH = 3.0d;
    public static double RAIN_PARTICLE_BASE_HEIGHT = 45.0d;
    public static double RAIN_PARTICLE_HEIGHT_VARIANCE = 50.0d;

    //rain splatter particle constants
    public static Vector2 RAIN_SPLATTER_EXTENT = new Vector2(2.0d, 2.0d);

    // List of players in the game (Should be normally max. 1).
    private static ArrayList<float[]> PLAYERS = new ArrayList<>();

    //region Player
    /**
     * Creates a player entity and adds it to the player register.
     */
    public static float[] createPlayer() {
        final Vector2 extent = new Vector2(20, 40);

        float[] player = EntityCreator.getInstance()
                .setEntityTypeID(EntityType.BOX_SHADOW.getEntityType() | EntityType.PLAYER.getEntityType())
                .setSystemMask(SystemBitmask.INPUT.getSystemMask() | SystemBitmask.COLLIDER_SORTING.getSystemMask() | SystemBitmask.TRIGGER_SYSTEM.getSystemMask() | SystemBitmask.CLEANUP_SYSTEM.getSystemMask())
                .setPosition(new Vector2(400,-1000))
                .setExtent(extent)
                .setColor(Renderer.PLAYER_COLOR)
                .setAABBExtent(extent)
                .setCollisionType(1.0f)
                .setGravitationInfluence(1.0f)
                .setDrag(0.95f)
                .setTriggerPosition(new Vector2(0, extent.y))
                .setTriggerExtent(new Vector2(19,3))
                .setTriggerCollisionType(0.0f)
                .setLifetime(1.0f)
                .create();
        
        PLAYERS.add(player);
        return player;
    }

    public static float[] createSeaPartTop(Vector2 position, Vector2 extent) {
        float[] entity = EntityCreator.getInstance()
                .setPosition(position)
                .setExtent(extent)
                .setColor(Renderer.BULLET_COLOR)
                .create();
        return entity;
    }

    public static float[] createSeaPartBottom(Vector2 position, Vector2 extent) {
        float[] entity = EntityCreator.getInstance()
                .setPosition(position)
                .setExtent(extent)
                .setColor(Renderer.WATER_COLOR)
                .create();

        return entity;
    }

    public static float[] createSeaPartBottomTop(Vector2 position, Vector2 extent) {
        float[] entity = EntityCreator.getInstance()
                .setPosition(position)
                .setExtent(extent)
                .setColor(Renderer.WATER_COLOR/*_2*/)
                .create();

        return entity;
    }
    
    /**
     * @return Returns <strong>true</strong> if a player is already in the game, <strong>false</strong> otherwise.
     */
    public static boolean isPlayerInGame() {
    	return PLAYERS.size() > 0 ? true : false;
    }
    
    /**
     * Returns the first player in the player-register. If no player is found
     * <strong>null</strong> is returned. 
     */
    public static float[] getFirstPlayer() {
    	if(isPlayerInGame()) {
    		return PLAYERS.get(0);
    	}
    	return null;
    }
    
    //endregion

    //region Screen Border
    public static float[] createScreenBorder(Vector2 origin, Vector2 direction) {
        float[] screenBorder = EntityCreator.getInstance()
                .setEntityTypeID(EntityType.SCREEN_BORDER.getEntityType())
                .setBorderOrigin(origin)
                .setBorderDirection(direction)
                .create();

        return screenBorder;
    }
    //endregion

    //region Light
    public static float[] createLight() {
        float[] light = EntityCreator.getInstance()
                .setEntityTypeID(EntityType.LIGHT.getEntityType())
                .setSystemMask(SystemBitmask.LIGHT_SYSTEM.getSystemMask())
                .setPosition(new Vector2(-100, 100))
                .create();

        return light;
    }
    //endregion

    //region Rain Particle
    public static float[] createRainParticle(Vector2 position, Vector2 velocity){
        Vector2 extent = new Vector2(RAIN_PARTICLE_WIDTH, RAIN_PARTICLE_BASE_HEIGHT + random.nextDouble() * RAIN_PARTICLE_HEIGHT_VARIANCE);

        final float[] entity = EntityCreator.getInstance()
                .setEntityTypeID(EntityType.RAIN_DROP.getEntityType())
                .setSystemMask(SystemBitmask.COLLIDER_SORTING.getSystemMask() | SystemBitmask.MOVEMENT_SYSTEM.getSystemMask() | SystemBitmask.CLEANUP_SYSTEM.getSystemMask())
                .setPosition(position)
                .setExtent(extent)
                .setColor(Renderer.RAIN_GRADIENT_LIGHT)
                .setAABBPosition(new Vector2(0, extent.y/2.0f))
                .setAABBExtent(new Vector2(extent.x, extent.y/2.0f))
                .setCollisionType(1.0d)
                .setVelocity(velocity)
                .setGravitationInfluence(1.0f)
                .setDrag(0.975f)
                .create();

        return entity;
    }
    //endregion

    //region Rain Splatter Particle
    public static float[] createSplatterParticle(Vector2 position, Vector2 velocity){
        float[] entity = EntityCreator.getInstance()
                .setEntityTypeID(EntityType.RAIN_DROP_SPLATTER.getEntityType())
                .setSystemMask(SystemBitmask.MOVEMENT_SYSTEM.getSystemMask() | SystemBitmask.COLLIDER_SORTING.getSystemMask() | SystemBitmask.CLEANUP_SYSTEM.getSystemMask())
                .setPosition(position)
                .setExtent(RAIN_SPLATTER_EXTENT)
                .setColor(Renderer.RAIN_GRADIENT_LIGHT)
                .setAABBPosition(new Vector2(0, 0))
                .setAABBExtent(RAIN_SPLATTER_EXTENT)
                .setCollisionType(1.0d)
                .setVelocity(velocity)
                .setGravitationInfluence(1.0f)
                .setDrag(0.975f)
                .create();

        return entity;
    }
    //endregion

    //region Platform
    public static float[] createPlatform(Vector2 position, Vector2 extent){
        float[] entity = EntityCreator.getInstance()
                .setEntityTypeID(EntityType.BOX_SHADOW.getEntityType() | EntityType.PLATFORM.getEntityType())
                .setSystemMask(SystemBitmask.COLLIDER_SORTING.getSystemMask() | SystemBitmask.CLEANUP_SYSTEM.getSystemMask())
                .setPosition(position)
                .setExtent(extent)
                .setColor(Renderer.OBSTACLE_COLOR)
                .setAABBExtent(extent)
                .setCollisionType(0.0f)
                .create();

        return entity;
    }
    //endregion

    //region Bullet
    public static float[] createBullet(Vector2 position, Vector2 velocity){
        final Vector2 extent = new Vector2(15, 5);

        float[] entity = EntityCreator.getInstance()
                .setEntityTypeID(EntityType.BULLET.getEntityType() | EntityType.BOX_SHADOW.getEntityType())
                .setSystemMask(SystemBitmask.COLLIDER_SORTING.getSystemMask() | SystemBitmask.MOVEMENT_SYSTEM.getSystemMask() | SystemBitmask.CLEANUP_SYSTEM.getSystemMask())
                .setPosition(position)
                .setExtent(extent)
                .setColor(Renderer.BULLET_COLOR)
                .setAABBExtent(extent)
                .setCollisionType(1.0f)
                .setVelocity(velocity)
                .setDrag(1.0f)
                .setLifetime(10.0f + random.nextDouble() * 10.0f)
                .create();

        return entity;
    }
    //endregion

    //region moving platforms
    public static float[] createMovingPlatform(Vector2 position, Vector2 extent, Vector2 velocity, Vector2 movementRangeMin, Vector2 movementRangeMax){
        float[] entity = EntityCreator.getInstance()
                .setEntityTypeID(EntityType.BOX_SHADOW.getEntityType() | EntityType.PLATFORM.getEntityType())
                .setSystemMask(SystemBitmask.COLLIDER_SORTING.getSystemMask() | SystemBitmask.PLATFORM_MOVEMENT_SYSTEM.getSystemMask() | SystemBitmask.MOVEMENT_SYSTEM.getSystemMask() | SystemBitmask.TRIGGER_SYSTEM.getSystemMask() | SystemBitmask.CLEANUP_SYSTEM.getSystemMask())
                .setPosition(position)
                .setExtent(extent)
                .setColor(Renderer.OBSTACLE_COLOR)
                .setAABBExtent(extent)
                .setCollisionType(0.0f)
                .setTriggerPosition(new Vector2(0.0f, -extent.y/2.0f))
                .setTriggerExtent(extent)
                .setTriggerCollisionType(2.0f) //triggered only by player entity
                .setVelocity(velocity)
                .setDrag(1.0f)
                .setPlatformRangeMin(movementRangeMin)
                .setPlatformRangeMax(movementRangeMax)
                .create();

        return entity;
    }
    //endregion

    //region Entity specific util methods
    /**
     * Sets the position for the given entity.
     */
    public static void setPositionFor(final float[] entity, float x, float y) {
    	entity[EntityIndex.POSITION_X.getIndex()] = x;
    	entity[EntityIndex.POSITION_Y.getIndex()] = y;
    }
    
    /**
     * Returns the position of the given entity as {@link Vector2}.
     */
    public static Vector2 getPositionFor(final float[] entity) {
    	return new Vector2(entity[EntityIndex.POSITION_X.getIndex()], entity[EntityIndex.POSITION_Y.getIndex()]);
    }
    //endregion
}