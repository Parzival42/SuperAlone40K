package superAlone40k.util;

import superAlone40k.ecs.EntityIndex;

import java.awt.*;

/**
 * Created by Andy on 13.11.2017.
 */
public class EntityCreator {

    private static EntityCreator instance;
    private int index;
    private float[][] entityBuffer;

    public static EntityCreator getInstance() {
        if(instance == null) {
            instance = new EntityCreator();
        }
        return instance;
    }

    private EntityCreator() {
        entityBuffer = new float[2][EntityIndex.values().length];
        index = 0;
    }

    public EntityCreator setEntityTypeID(int entityTypeID){
        entityBuffer[index][EntityIndex.ENTITY_TYPE_ID.getIndex()] = entityTypeID;
        return this;
    }

    public EntityCreator setSystemMask(int systemMask) {
        entityBuffer[index][EntityIndex.SYSTEM_MASK.getIndex()] = systemMask;
        return this;
    }

    public EntityCreator setPosition(Vector2 position) {
        entityBuffer[index][EntityIndex.POSITION_X.getIndex()] = (float) position.x;
        entityBuffer[index][EntityIndex.POSITION_Y.getIndex()] = (float) position.y;
        return this;
    }

    public EntityCreator setExtent(Vector2 extent) {
        entityBuffer[index][EntityIndex.EXTENT_X.getIndex()] = (float) extent.x;
        entityBuffer[index][EntityIndex.EXTENT_Y.getIndex()] = (float) extent.y;
        return this;
    }

    public EntityCreator setColor(Color color) {
        entityBuffer[index][EntityIndex.COLOR_R.getIndex()] = color.getRed()/255.0f;
        entityBuffer[index][EntityIndex.COLOR_G.getIndex()] = color.getGreen()/255.0f;
        entityBuffer[index][EntityIndex.COLOR_B.getIndex()] = color.getBlue()/255.0f;
        entityBuffer[index][EntityIndex.COLOR_A.getIndex()] = color.getAlpha()/255.0f;
        return this;
    }

    public EntityCreator setAABBPosition(Vector2 aabbPosition) {
        entityBuffer[index][EntityIndex.AABB_CENTER_X.getIndex()] = (float) aabbPosition.x;
        entityBuffer[index][EntityIndex.AABB_CENTER_Y.getIndex()] = (float) aabbPosition.y;
        return this;
    }

    public EntityCreator setAABBExtent(Vector2 aabbExtent) {
        entityBuffer[index][EntityIndex.AABB_EXTENT_X.getIndex()] = (float) aabbExtent.x;
        entityBuffer[index][EntityIndex.AABB_EXTENT_Y.getIndex()] = (float) aabbExtent.y;
        return this;
    }

    public EntityCreator setCollisionType(double collisionType) {
        entityBuffer[index][EntityIndex.COLLISION_TYPE.getIndex()] = (float) collisionType;
        return this;
    }

    public EntityCreator setVelocity(Vector2 velocity) {
        entityBuffer[index][EntityIndex.VELOCITY_X.getIndex()] = (float) velocity.x;
        entityBuffer[index][EntityIndex.VELOCITY_Y.getIndex()] = (float) velocity.y;
        return this;
    }

    public EntityCreator setGravitationInfluence(double gravitationInfluence) {
        entityBuffer[index][EntityIndex.GRAVITATION_INFLUENCE.getIndex()] = (float) gravitationInfluence;
        return this;
    }

    public EntityCreator setDrag(double drag) {
        entityBuffer[index][EntityIndex.DRAG.getIndex()] = (float) drag;
        return this;
    }

    public EntityCreator setBorderOrigin(Vector2 borderOrigin) {
        entityBuffer[index][EntityIndex.BORDER_ORIGIN_X.getIndex()] = (float) borderOrigin.x;
        entityBuffer[index][EntityIndex.BORDER_ORIGIN_Y.getIndex()] = (float) borderOrigin.y;
        return this;
    }

    public EntityCreator setBorderDirection(Vector2 borderDirection) {
        entityBuffer[index][EntityIndex.BORDER_DIR_X.getIndex()] = (float) borderDirection.x;
        entityBuffer[index][EntityIndex.BORDER_DIR_Y.getIndex()] = (float) borderDirection.y;
        return this;
    }

    public EntityCreator setTriggerPosition(Vector2 triggerPosition) {
        entityBuffer[index][EntityIndex.TRIGGER_POSITION_X.getIndex()] = (float) triggerPosition.x;
        entityBuffer[index][EntityIndex.TRIGGER_POSITION_Y.getIndex()] = (float) triggerPosition.y;
        return this;
    }

    public EntityCreator setTriggerExtent(Vector2 triggerExtent) {
        entityBuffer[index][EntityIndex.TRIGGER_EXTENT_X.getIndex()] = (float) triggerExtent.x;
        entityBuffer[index][EntityIndex.TRIGGER_EXTENT_Y.getIndex()] = (float) triggerExtent.y;
        return this;
    }

    public EntityCreator setTriggerCollisionType(double collisionType) {
        entityBuffer[index][EntityIndex.TRIGGER_COLLISION_TYPE.getIndex()] = (float) collisionType;
        return this;
    }

    public EntityCreator setLifetime(double lifetime) {
        entityBuffer[index][EntityIndex.LIFETIME.getIndex()] = (float) lifetime;
        return this;
    }

    public EntityCreator setPlatformRangeMin(Vector2 platformRangeMin){
        entityBuffer[index][EntityIndex.PLATFORM_RANGE_MIN_X.getIndex()] = (float) platformRangeMin.x;
        entityBuffer[index][EntityIndex.PLATFORM_RANGE_MIN_Y.getIndex()] = (float) platformRangeMin.y;
        return this;
    }

    public EntityCreator setPlatformRangeMax(Vector2 platformRangeMax){
        entityBuffer[index][EntityIndex.PLATFORM_RANGE_MAX_X.getIndex()] = (float) platformRangeMax.x;
        entityBuffer[index][EntityIndex.PLATFORM_RANGE_MAX_Y.getIndex()] = (float) platformRangeMax.y;
        return this;
    }

    public float[] create() {
        float[] entity = entityBuffer[index];
        entityBuffer[index] = new float[EntityIndex.values().length];
        index = index == 0 ? 1 : 0;
        return entity;
    }
}