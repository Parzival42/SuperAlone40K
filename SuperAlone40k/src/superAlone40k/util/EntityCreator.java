package superAlone40k.util;

import superAlone40k.ecs.EntityIndex;
import superAlone40k.ecs.EntityType;

import java.awt.*;

/**
 * Created by Andy on 13.11.2017.
 */
public class EntityCreator {

    private static EntityCreator instance;
    private static int index;
    private static float[][] entityBuffer;

    private int entityTypeID;
    private int systemMask;
    private Vector2 position;
    private Vector2 extent;
    private float[] color;
    private Vector2 aabbPosition;
    private Vector2 aabbExtent;
    private double collisionType;
    private Vector2 velocity;
    private double gravitationInfluence;
    private double drag;
    private Vector2 borderOrigin;
    private Vector2 borderDirection;
    private Vector2 triggerPosition;
    private Vector2 triggerExtent;
    private double triggerCollisionType;
    private double lifetime;

    static {
        entityBuffer = new float[2][EntityIndex.values().length];
        index = 0;
    }

    public static EntityCreator getInstance() {
        if(instance == null) {
            instance = new EntityCreator();
        }

        return instance;
    }

    private EntityCreator() {
        clear();
    }

    public EntityCreator setEntityTypeID(int entityTypeID) {
        this.entityTypeID = entityTypeID;
        return this;
    }

    public EntityCreator setSystemMask(int systemMask) {
        this.systemMask = systemMask;
        return this;
    }

    public EntityCreator setPosition(Vector2 position) {
        return setPosition(position.x, position.y);
    }

    public EntityCreator setPosition(double x, double y) {
        position.set(x, y);
        return this;
    }

    public EntityCreator setExtent(Vector2 extent) {
        return setExtent(extent.x, extent.y);
    }

    public EntityCreator setExtent(double x, double y) {
        extent.set(x, y);
        return this;
    }

    public EntityCreator setColor(Color color) {
		return setColor(color.getRed() / 255.0d, color.getGreen() / 255.0d, color.getBlue() / 255.0d,
				color.getAlpha() / 255.0d);
    }

    public EntityCreator setColor(double r, double g, double b, double a) {
        color[0] = (float) r;
        color[1] = (float) g;
        color[2] = (float) b;
        color[3] = (float) a;
        return this;
    }

    public EntityCreator setAABBPosition(Vector2 aabbPosition) {
        return setAABBPosition(aabbPosition.x, aabbPosition.y);
    }

    public EntityCreator setAABBPosition(double x, double y) {
        aabbPosition.set(x,y);
        return this;
    }

    public EntityCreator setAABBExtent(Vector2 aabbExtent) {
        return setAABBExtent(aabbExtent.x, aabbExtent.y);
    }

    public EntityCreator setAABBExtent(double x, double y) {
        aabbExtent.set(x,y);
        return this;
    }

    public EntityCreator setCollisionType(double collisionType) {
        this.collisionType = collisionType;
        return this;
    }

    public EntityCreator setVelocity(Vector2 velocity) {
        return setVelocity(velocity.x, velocity.y);
    }

    public EntityCreator setVelocity(double x, double y) {
        velocity.set(x,y);
        return this;
    }

    public EntityCreator setGravitationInfluence(double gravitationInfluence) {
        this.gravitationInfluence = gravitationInfluence;
        return this;
    }

    public EntityCreator setDrag(double drag) {
        this.drag = drag;
        return this;
    }

    public EntityCreator setBorderOrigin(Vector2 borderOrigin) {
        return setBorderOrigin(borderOrigin.x, borderOrigin.y);
    }

    public EntityCreator setBorderOrigin(double x, double y) {
        borderOrigin.set(x,y);
        return this;
    }

    public EntityCreator setBorderDirection(Vector2 borderDirection) {
        return setBorderDirection(borderDirection.x, borderDirection.y);
    }

    public EntityCreator setBorderDirection(double x, double y) {
        borderDirection.set(x,y);
        return this;
    }

    public EntityCreator setTriggerPosition(Vector2 triggerPosition) {
        return setTriggerPosition(triggerPosition.x, triggerPosition.y);
    }

    public EntityCreator setTriggerPosition(double x, double y) {
        triggerPosition.set(x,y);
        return this;
    }

    public EntityCreator setTriggerExtent(Vector2 triggerExtent) {
        return setTriggerExtent(triggerExtent.x, triggerExtent.y);
    }

    public EntityCreator setTriggerExtent(double x, double y) {
        triggerExtent.set(x, y);
        return this;
    }

    public EntityCreator setTriggerCollisionType(double collisionType) {
        this.triggerCollisionType = collisionType;
        return this;
    }

    public EntityCreator setLifetime(double lifetime) {
        this.lifetime = lifetime;
        return this;
    }

    public float[] create() {
        float[] entity = new float[EntityIndex.values().length];

        entity[EntityIndex.ENTITY_TYPE_ID.getIndex()] = entityTypeID;
        entity[EntityIndex.SYSTEM_MASK.getIndex()] = systemMask;
        Entities.setPositionFor(entity, (float) position.x, (float) position.y);
//        entity[EntityIndex.POSITION_X.getIndex()] = (float) position.x;
//        entity[EntityIndex.POSITION_Y.getIndex()] = (float) position.y;
        entity[EntityIndex.EXTENT_X.getIndex()] = (float) extent.x;
        entity[EntityIndex.EXTENT_Y.getIndex()] = (float) extent.y;
        entity[EntityIndex.COLOR_R.getIndex()] = color[0];
        entity[EntityIndex.COLOR_G.getIndex()] = color[1];
        entity[EntityIndex.COLOR_B.getIndex()] = color[2];
        entity[EntityIndex.COLOR_A.getIndex()] = color[3];
        entity[EntityIndex.AABB_CENTER_X.getIndex()] = (float) aabbPosition.x;
        entity[EntityIndex.AABB_CENTER_Y.getIndex()] = (float) aabbPosition.y;
        entity[EntityIndex.AABB_EXTENT_X.getIndex()] = (float) aabbExtent.x;
        entity[EntityIndex.AABB_EXTENT_Y.getIndex()] = (float) aabbExtent.y;
        entity[EntityIndex.COLLISION_TYPE.getIndex()] = (float) collisionType;
        entity[EntityIndex.VELOCITY_X.getIndex()] = (float) velocity.x;
        entity[EntityIndex.VELOCITY_Y.getIndex()] = (float) velocity.y;
        entity[EntityIndex.GRAVITATION_INFLUENCE.getIndex()] = (float) gravitationInfluence;
        entity[EntityIndex.DRAG.getIndex()] = (float) drag;
        entity[EntityIndex.BORDER_ORIGIN_X.getIndex()] = (float) borderOrigin.x;
        entity[EntityIndex.BORDER_ORIGIN_Y.getIndex()] = (float) borderOrigin.y;
        entity[EntityIndex.BORDER_DIR_X.getIndex()] = (float) borderDirection.x;
        entity[EntityIndex.BORDER_DIR_Y.getIndex()] = (float) borderDirection.y;
        entity[EntityIndex.TRIGGER_POSITION_X.getIndex()] = (float) triggerPosition.x;
        entity[EntityIndex.TRIGGER_POSITION_Y.getIndex()] = (float) triggerPosition.y;
        entity[EntityIndex.TRIGGER_EXTENT_X.getIndex()] = (float) triggerExtent.x;
        entity[EntityIndex.TRIGGER_EXTENT_Y.getIndex()] = (float) triggerExtent.y;
        entity[EntityIndex.TRIGGER_COLLISION_TYPE.getIndex()] = (float) triggerCollisionType;
        entity[EntityIndex.TRIGGER_OBJECT_TYPE.getIndex()] = EntityType.NONE.getEntityType();
        entity[EntityIndex.LIFETIME.getIndex()] = (float)lifetime;

        clear();
        return entity;
    }

    private void clear(){
        entityTypeID = -1;
        systemMask = 0b0;
        position = new Vector2();
        extent = new Vector2();
        color = new float[4];
        aabbPosition = new Vector2();
        aabbExtent = new Vector2();
        collisionType = 0.0d;
        velocity = new Vector2();
        gravitationInfluence = 0.0d;
        drag = 0.0d;
        borderOrigin = new Vector2();
        borderDirection = new Vector2();
        triggerPosition = new Vector2();
        triggerExtent = new Vector2();
        triggerCollisionType = 0.0f;
        lifetime = 0.0f;
    }
}