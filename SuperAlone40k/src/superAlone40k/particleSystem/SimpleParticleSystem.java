package superAlone40k.particleSystem;

import superAlone40k.ecs.FlattenedEngine;

import java.util.Random;

public class SimpleParticleSystem {

    //engine reference
    private FlattenedEngine engine;

    //emitter position
    private int positionX;
    private int positionY;

    //emit settings
    private float emitRate = 1.0f/10.0f;
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
        this.emitRate = 1.0f/emitRate;
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
        float[] entity = new float[19];

        //mask - , collider, movement
        //00011100
        entity[1] = 56;

        //pos
        entity[2] = positionX;
        entity[3] = positionY-10;

        //extent
        entity[4] = 2;
        entity[5] = 2;

        //color
        entity[6] = 89/255.0f;
        entity[7] = 106/255.0f;
        entity[8] = 128/255.0f;
        entity[9] = 0.09f + random.nextFloat() * 0.09f;

        //aabb box center
        entity[10] = 0;
        entity[11] = 0;

        //aabb box extent
        entity[12] = entity[4];
        entity[13] = entity[5];

        //aabb dynamic vs static
        //player -> dynamic -> 1
        entity[14] = 1.0f;

        //velocity
        entity[15] = random.nextFloat() * emitForce - emitForce/2;
        entity[16] = random.nextFloat() * -emitForce;
        entity[17] = 1.0f;
        entity[18] = 0.975f;

        engine.addEntity(entity);
    }
}
