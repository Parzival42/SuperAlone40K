package superAlone40k.particleSystem;

import superAlone40k.ecs.EntityIndex;
import superAlone40k.ecs.EntityType;
import superAlone40k.ecs.FlattenedEngine;
import superAlone40k.ecs.SystemBitmask;
import superAlone40k.util.EntityCreator;
import superAlone40k.util.Vector2;

import java.awt.*;
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
    private float emitRate = 1.0f / 10.0f;

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
        this.emitRate = 1.0f / emitRate;
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
        Vector2 extent = new Vector2(3,45 + random.nextFloat() * 50.0f);

        float[] entity = EntityCreator.getInstance()
                .setEntityTypeID(EntityType.RAIN_DROP.getEntityType())
                .setSystemMask(SystemBitmask.COLLIDER_SORTING.getSystemMask() | SystemBitmask.MOVEMENT_SYSTEM.getSystemMask())
                .setPosition(new Vector2(range * random.nextFloat() + rangeBegin, height - random.nextFloat() * 50.0f))
                .setExtent(extent)
                .setColor(new Color(89 / 255.0f, 106 / 255.0f, 126 / 255.0f, 0.12f + random.nextFloat() * 0.12f))
                .setAABBPosition(new Vector2(0, 0))
                .setAABBExtent(extent)
                .setCollisionType(1.0d)
                .setVelocity(new Vector2(windForce, random.nextFloat() * downForce))
                .setGravitationInfluence(1.0f)
                .setDrag(0.975f)
                .create();

        engine.addEntity(entity);
    }
}