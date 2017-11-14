package superAlone40k.particleSystem;

import superAlone40k.ecs.EntityIndex;
import superAlone40k.ecs.EntityType;
import superAlone40k.ecs.FlattenedEngine;
import superAlone40k.ecs.SystemBitmask;
import superAlone40k.util.EntityCreator;
import superAlone40k.util.Vector2;

import java.awt.*;
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
        Vector2 extent = new Vector2(2,2);

        float[] entity = EntityCreator.getInstance()
                .setEntityTypeID(EntityType.RAIN_DROP_SPLATTER.getEntityType())
                .setSystemMask(SystemBitmask.MOVEMENT_SYSTEM.getSystemMask() | SystemBitmask.COLLIDER_SORTING.getSystemMask())
                .setPosition(new Vector2(positionX, positionY))
                .setExtent(extent)
                .setColor(new Color(89 / 255.0f, 106 / 255.0f, 128/ 255.0f, 0.12f + random.nextFloat() * 0.19f))
                .setAABBPosition(new Vector2(0, 0))
                .setAABBExtent(extent)
                .setCollisionType(1.0d)
                .setVelocity(new Vector2(random.nextFloat() * emitForce - emitForce / 2, random.nextFloat() * -emitForce))
                .setGravitationInfluence(1.0f)
                .setDrag(0.975f)
                .create();

        engine.addEntity(entity);
    }
}
