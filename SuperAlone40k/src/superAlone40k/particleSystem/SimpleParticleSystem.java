package superAlone40k.particleSystem;

import superAlone40k.ecs.EntityIndex;
import superAlone40k.ecs.EntityType;
import superAlone40k.ecs.FlattenedEngine;
import superAlone40k.ecs.SystemBitmask;

import java.util.Random;

public class SimpleParticleSystem {

    //engine reference
    private FlattenedEngine engine;

    //emitter position
    private int positionX;
    private int positionY;

    //emit settings
    private float emitRate = 1.0f / 10.0f;
    private float emitTime = 1.0f;
    private float emitForce = 500.0f;

    //current time;
    private double remainingTime = 0.0f;
    private Random random = new Random();

    public SimpleParticleSystem(FlattenedEngine engine, int positionX, int positionY){
        this.engine = engine;
        this.positionX = positionX;
        this.positionY = positionY;
    }

    public void emit(int emitRate, int emitTime){
        this.emitRate = 1.0f / emitRate;
        this.emitTime = emitTime;
    }

    public void burstEmit(int positionX, int positionY, int amount){
        this.positionX = positionX;
        this.positionY = positionY;

        for(int i = 0; i < amount; i++){
            emitParticle();
        }
    }

    public void update(double deltaTime){
        if(emitTime > 0.0f){
            emitTime -= deltaTime;
            remainingTime += deltaTime;

            while(remainingTime > emitRate){
                remainingTime -= emitRate;
                emitParticle();
            }
        }
    }

    private void emitParticle(){
        float[] entity = new float[EntityIndex.values().length];

        entity[EntityIndex.ENTITY_TYPE_ID.getIndex()] = EntityType.RAIN_DROP_SPLATTER.getEntityType();
        entity[EntityIndex.SYSTEM_MASK.getIndex()] = SystemBitmask.MOVEMENT_SYSTEM.getSystemMask() | SystemBitmask.COLLIDER_SORTING.getSystemMask();

        //pos
        entity[EntityIndex.POSITION_X.getIndex()] = positionX;
        entity[EntityIndex.POSITION_Y.getIndex()] = positionY - 10;

        //extent
        entity[EntityIndex.EXTENT_X.getIndex()] = 2;
        entity[EntityIndex.EXTENT_Y.getIndex()] = 2;

        //color
        entity[EntityIndex.COLOR_R.getIndex()] = 89 / 255.0f;
        entity[EntityIndex.COLOR_G.getIndex()] = 106 / 255.0f;
        entity[EntityIndex.COLOR_B.getIndex()] = 128 / 255.0f;
        entity[EntityIndex.COLOR_A.getIndex()] = 0.09f + random.nextFloat() * 0.09f;

        //aabb box center
        entity[EntityIndex.AABB_CENTER_X.getIndex()] = 0;
        entity[EntityIndex.AABB_CENTER_Y.getIndex()] = 0;

        //aabb box extent
        entity[EntityIndex.AABB_EXTENT_X.getIndex()] = entity[EntityIndex.EXTENT_X.getIndex()];
        entity[EntityIndex.AABB_EXTENT_Y.getIndex()] = entity[EntityIndex.EXTENT_Y.getIndex()];

        //aabb dynamic vs static
        //player -> dynamic -> 1
        entity[EntityIndex.COLLISION_TYPE.getIndex()] = 1.0f;

        //velocity
        entity[EntityIndex.VELOCITY_X.getIndex()] = random.nextFloat() * emitForce - emitForce / 2;
        entity[EntityIndex.VELOCITY_Y.getIndex()] = random.nextFloat() * -emitForce;

        //gravitation influence
        entity[EntityIndex.GRAVITATION_INFLUENCE.getIndex()] = 1.0f;

        //drag
        entity[EntityIndex.DRAG.getIndex()] = 0.975f;

        engine.addEntity(entity);
    }
}
