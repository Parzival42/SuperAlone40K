package superAlone40k.util;

import superAlone40k.ecs.FlattenedEngine;

import java.awt.*;
import java.util.Random;

public class Level {
    private Random random = new Random();
    private FlattenedEngine engine;
    private Canvas canvas;

    public Level(FlattenedEngine engine, Canvas canvas){
        this.engine = engine;
        this.canvas = canvas;

        init();
    }

    private void init() {

        createSampleFloorEntities();
        createSampleBulletEntities();
        createCheckpoint();
        engine.addEntity(Entities.createPlayer());

        // Left top to Bottom left
        engine.addEntity(Entities.createScreenBorder(new Vector2(0, 0), new Vector2(0, 1)));

        // Bottom left to Bottom right
        engine.addEntity(Entities.createScreenBorder(new Vector2(0, canvas.getHeight()), new Vector2(1, 0)));

        // Bottom right to Top right
        engine.addEntity(Entities.createScreenBorder(new Vector2(canvas.getWidth(), canvas.getHeight()), new Vector2(0, -1)));

        // Top right to Top left
        engine.addEntity(Entities.createScreenBorder(new Vector2(canvas.getWidth(), 0), new Vector2(-1, 0)));

        engine.addEntity(Entities.createLight());

    }

    public void update(double deltaTime){
        //TODO: do useful stuff
    }


    private void createCheckpointParticles(){
        for(int i = 0; i < 10; i++){
            Vector2 position = new Vector2(2435, canvas.getHeight() - 40 - 25 * i);
            engine.addEntity(Entities.createHorizontalMovingParticle(position));
        }
    }

    private void createCheckpoint(){
        final Vector2 position = new Vector2(2450, canvas.getHeight() - 150);
        engine.addEntity(Entities.createCheckpoint(position));

        createCheckpointParticles();
    }

    private void createSampleBulletEntities() {
        for(int i = 0; i < 20; i++){
            final Vector2 position = new Vector2(1500 + i * 200, random.nextFloat() * 720);
			final Vector2 velocity = new Vector2(-500 + 100 * random.nextFloat(), 0);
            engine.addEntity(Entities.createBullet(position,velocity));
        }
    }

    private void createSampleFloorEntities() {
		final Vector2 position = new Vector2(1200, canvas.getHeight() - 15);
        final Vector2 extent = new Vector2(1500, 15);
        engine.addEntity(Entities.createPlatform(position, extent));

		final Vector2 platformExtent = new Vector2(100, 10);
		for (int j = 0; j < 6; j++) {
			Vector2 platformPosition = new Vector2((175.0f) + j * 450.0f, canvas.getHeight() - 305);
			engine.addEntity(Entities.createPlatform(platformPosition, platformExtent));
		}
    }
}
