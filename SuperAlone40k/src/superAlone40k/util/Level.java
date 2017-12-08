package superAlone40k.util;

import superAlone40k.ecs.FlattenedEngine;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.Random;

public class Level {
    private Random random = new Random();
    private FlattenedEngine engine;
    private Canvas canvas;

    private float movement = 0.0f;


    // 0 - fixed platforms - 1 nothing
    private int[] sectorProbability = new int[]{15,5};
    private int totalPropability = 0;

    private float currentSectorPosition = 0.0f;
    private float minSectorWidth = 250;
    private float cameraOffset;

    private ArrayList<Integer> indexHistory = new ArrayList<>();
    private ArrayList<Float> sectorWidthHistory = new ArrayList<>();

    private int cellHeight;
    private int cellAmount = 24;

    private int windowWidth;
    private int windowHeight;

    public Level(FlattenedEngine engine, Canvas canvas){
        this.engine = engine;
        this.canvas = canvas;

        windowWidth = canvas.getWidth();
        windowHeight = canvas.getHeight();

        cameraOffset = -3.0f * windowWidth;
        cellHeight = windowHeight/cellAmount;

        init();
    }

    private void init() {

        //createCheckpoint();
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

        //engine.addEntity(Entities.createMovingPlatform(new Vector2(400.0f, 500.0f), new Vector2(50,10), new Vector2(200.0f,0.0f), new Vector2(200.0f,500.0f), new Vector2(600.0f, 500.0f)));


        for(int i = 0; i < sectorProbability.length; i++){
            totalPropability+= sectorProbability[i];
        }
    }

    public void update(FlattenedEngine engine, AffineTransform camera, double deltaTime){
        float cameraX = (float) camera.getTranslateX() + cameraOffset;

        movement = -cameraX - currentSectorPosition;
        if(movement > minSectorWidth){
            generateNextSector(engine);
            refineSectors(engine);
        }


        //TODO: do useful stuff
    }




    private void refineSectors(FlattenedEngine engine) {



    }

    private void generateNextSector(FlattenedEngine engine) {
        float sectorWidth = 250 + random.nextFloat() * 50;


        int value = (int) (random.nextFloat() * totalPropability);

        int index = 0;
        boolean indexMatched = false;
        while(!indexMatched){
            if(value < sectorProbability[index]){
                indexMatched = true;
            }else{
                value -= sectorProbability[index];
                index++;
            }
        }

        //System.out.println("Index: " + index);

        switch(index){
            case 0: createPlatformSector(engine, sectorWidth); break;
            case 1:
                int count = 0;
                for(int i = 0; i < indexHistory.size(); i++){
                    if(indexHistory.get(i) == 1){
                        count++;
                    }
                }

                if(count > 3){
                    System.out.println("Index change requested");
                    createPlatformSector(engine, sectorWidth);
                    index = 0;
                    addMovingPlatform(new Vector2(currentSectorPosition- (3*sectorWidth),500), new Vector2(currentSectorPosition, 500));
                }
                break;

            default: break;
        }


        if(indexHistory.size() > 3){
            indexHistory.remove(0);
            sectorWidthHistory.remove(0);
        }
        indexHistory.add(index);
        sectorWidthHistory.add(sectorWidth);

        currentSectorPosition += sectorWidth;
    }

    private void addMovingPlatform(Vector2 min, Vector2 max){
        engine.addEntity(Entities.createMovingPlatform(min, new Vector2(75,15), new Vector2(200,0), min, max));
    }


    private void createPlatformSector(FlattenedEngine engine, float sectorWidth){
        int step = Math.min(Math.round((windowHeight*0.9f+random.nextFloat()*windowHeight*0.2f)/ cellHeight)-1, cellAmount-1);
        int height = (cellAmount - step) * cellHeight;
        engine.addEntity(Entities.createPlatform(new Vector2(currentSectorPosition+sectorWidth/2.0f,(step * cellHeight )+  (height/2.0f)), new Vector2(sectorWidth/2.0f+1.5f, height)));
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


}
