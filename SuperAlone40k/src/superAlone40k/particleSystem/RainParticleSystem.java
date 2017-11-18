package superAlone40k.particleSystem;

import superAlone40k.ecs.EntityType;
import superAlone40k.ecs.FlattenedEngine;
import superAlone40k.ecs.SystemBitmask;
import superAlone40k.util.EntityCreator;
import superAlone40k.util.Vector2;

import java.awt.*;
import java.util.Random;

public class RainParticleSystem {
	private static Random RND = new Random();
	public static double PARTICLE_WIDTH = 3.0;
	public static Color PARTICLE_COLOR_START = new Color(49 / 255f, 65 / 255f, 88 / 255f);
	public static Color PARTICLE_COLOR_END = new Color(60 / 255f, 80 / 255f, 108 / 255f);
	
    //engine
    private final FlattenedEngine engine;

    //emit height
    final private float height;

    //emit range
    final private float rangeBegin;
    final private float rangeEnd;
    final private float range;

    //emit settings
    private float emitRate = 1.0f / 10.0f;

    //emit control
    private boolean shouldEmit = false;

    //initial down force
    private float downForce = 50.0f;

    //wind force
    private float windForce = 0.0f;
    
    //splatter force
    private float splatterEmitForce = 500.0f;

    //current time;
    private double remainingTime = 0.0f;

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
        final Vector2 extent = new Vector2(PARTICLE_WIDTH, 45 + RND.nextFloat() * 50.0f);

        final float[] entity = EntityCreator.getInstance()
                .setEntityTypeID(EntityType.RAIN_DROP.getEntityType())
                .setSystemMask(SystemBitmask.COLLIDER_SORTING.getSystemMask() | SystemBitmask.MOVEMENT_SYSTEM.getSystemMask())
                .setPosition(new Vector2(range * RND.nextFloat() + rangeBegin, height - RND.nextFloat() * 50.0f))
                .setExtent(extent)
                .setColor(new Color(89 / 255.0f, 106 / 255.0f, 126 / 255.0f, 0.12f + RND.nextFloat() * 0.12f))
                .setAABBPosition(new Vector2(0, extent.y/2.0f))
                .setAABBExtent(extent.x, extent.y/2.0f)
                .setCollisionType(1.0d)
                .setVelocity(new Vector2(windForce, RND.nextFloat() * downForce))
                .setGravitationInfluence(1.0f)
                .setDrag(0.975f)
                .create();

        engine.addEntity(entity);
    }

   
    public void burstEmit(int positionX, int positionY, int amount){
        for(int i = 0; i < amount; i++){
            emitSplatterParticle(positionX, positionY);
        }
    }

    private void emitSplatterParticle(int positionX, int positionY){
        final Vector2 extent = new Vector2(2, 2);

        float[] entity = EntityCreator.getInstance()
                .setEntityTypeID(EntityType.RAIN_DROP_SPLATTER.getEntityType())
                .setSystemMask(SystemBitmask.MOVEMENT_SYSTEM.getSystemMask() | SystemBitmask.COLLIDER_SORTING.getSystemMask())
                .setPosition(new Vector2(positionX, positionY))
                .setExtent(extent)
                .setColor(new Color(89 / 255.0f, 106 / 255.0f, 128 / 255.0f, 0.12f + RND.nextFloat() * 0.09f))
                .setAABBPosition(new Vector2(0, 0))
                .setAABBExtent(extent)
                .setCollisionType(1.0d)
                .setVelocity(new Vector2((-0.5f + RND.nextFloat()) * splatterEmitForce, RND.nextFloat() * -splatterEmitForce))
                .setGravitationInfluence(1.0f)
                .setDrag(0.975f)
                .create();

        engine.addEntity(entity);
    }
}