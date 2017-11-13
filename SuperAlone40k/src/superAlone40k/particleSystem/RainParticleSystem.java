package superAlone40k.particleSystem;

import superAlone40k.ecs.FlattenedEngine;

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

        //mask - , collider, movement
        //00011000
        entity[1] = 24;

        //pos
        entity[2] = range*random.nextFloat()+ rangeBegin;
        entity[3] = height- random.nextFloat()*50.0f;

        //extent
        entity[4] = 3;
        entity[5] = 45+random.nextFloat()*45;

        //color + alpha
        entity[6] = 89/255.0f;
        entity[7] = 106/255.0f;
        entity[8] = 128/255.0f;
        entity[9] = 0.12f + random.nextFloat() * 0.12f;

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
        entity[15] = windForce;
        entity[16] = random.nextFloat() * downForce;

        //affected by gravity? > yes > 1.0f
        entity[17] = 1.0f;
        entity[18] = 0.975f;

        engine.addEntity(entity);
    }






}
