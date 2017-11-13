package superAlone40k.particleSystem;

import superAlone40k.ecs.EntityIndex;
import superAlone40k.ecs.FlattenedEngine;
import superAlone40k.ecs.SystemBitmask;

import java.util.Random;

public class RainParticleSystem {

    //engine
    FlattenedEngine engine;

    //emit height
    private float height;

    //emit range
    private float rangeBegin;
    private float rangeEnd;
    private float range;

    //emit settings
    private float emitRate = 1.0f/10.0f;

    //emit control
    private boolean shouldEmit = false;

    //initial down force
    private float downForce = 50.0f;

    //wind force
    private float windForce = 0.0f;

    //current time;
    private double remainingTime = 0.0f;
    private Random random = new Random();

    public RainParticleSystem(FlattenedEngine engine, float height, float rangeBegin, float rangeEnd){
        this.engine = engine;
        this.height = height;
        this.rangeBegin = rangeBegin;
        this.rangeEnd = rangeEnd;

        range = rangeEnd - rangeBegin;
    }

    public void emit(int emitRate){
        this.emitRate = 1.0f/emitRate;
        this.shouldEmit = true;
    }

    public void stopEmit(){
        this.shouldEmit = false;
    }

    public void update(double deltaTime){
        if(shouldEmit){
            remainingTime += deltaTime;

            while(remainingTime > emitRate){
                remainingTime -= emitRate;
                emitParticle();
            }
        }
    }

    private void emitParticle(){
        float[] entity = new float[19];

        entity[EntityIndex.SYSTEM_MASK.getIndex()] = SystemBitmask.COLLIDER_SORTING.getSystemMask() | SystemBitmask.MOVEMENT_SYSTEM.getSystemMask();

        //pos
        entity[EntityIndex.POSITION_X.getIndex()] = range*random.nextFloat()+ rangeBegin;
        entity[EntityIndex.POSITION_Y.getIndex()] = height- random.nextFloat()*50.0f;

        //extent
        entity[EntityIndex.EXTENT_X.getIndex()] = 3;
        entity[EntityIndex.EXTENT_Y.getIndex()] = 45+random.nextFloat()*45;

        //color + alpha
        entity[EntityIndex.COLOR_R.getIndex()] = 89/255.0f;
        entity[EntityIndex.COLOR_G.getIndex()] = 106/255.0f;
        entity[EntityIndex.COLOR_B.getIndex()] = 128/255.0f;
        entity[EntityIndex.COLOR_A.getIndex()] = 0.12f + random.nextFloat() * 0.12f;

        //aabb box center
        entity[EntityIndex.AABB_CENTER_X.getIndex()] = 0;
        entity[EntityIndex.AABB_CENTER_Y.getIndex()] = 0;

        //aabb box extent
        entity[EntityIndex.AABB_EXTENT_X.getIndex()] = entity[EntityIndex.EXTENT_X.getIndex()];
        entity[EntityIndex.AABB_EXTENT_Y.getIndex()] = entity[EntityIndex.EXTENT_Y.getIndex()];

        //aabb dynamic vs static
        //raindrop -> dynamic -> 1
        entity[EntityIndex.COLLISION_TYPE.getIndex()] = 1.0f;

        //velocity
        entity[EntityIndex.VELOCITY_X.getIndex()] = windForce;
        entity[EntityIndex.VELOCITY_Y.getIndex()] = random.nextFloat() * downForce;

        //affected by gravity? > yes > 1.0f
        entity[EntityIndex.GRAVITATION_INFLUENCE.getIndex()] = 1.0f;
        entity[EntityIndex.DRAG.getIndex()] = 0.975f;

        engine.addEntity(entity);
    }
}