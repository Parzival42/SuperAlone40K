package superAlone40k.util;

import superAlone40k.Main;
import superAlone40k.ecs.EntityIndex;
import superAlone40k.ecs.EntityType;
import superAlone40k.ecs.FlattenedEngine;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.Random;

public class Level {
    private Random random = new Random();
    private Canvas canvas;

    private float movement = 0.0f;

    // 0 - fixed platforms - 1 nothing

    // 0 - nothing, 1 - floor, 2 floor with static platform, 3

    private final int WATER = 0;
    private final int WATER_HOR_PLATFORM = 1;
    private final int WATER_VER_PLATFORM = 2;
    private final int FLOOR = 3;
    private final int FLOOR_PLATFORM = 4;

    private int[] sectorProbability = new int[]{30, 30, 20, 35, 30};
    private int totalPropability = 0;

    private float currentSectorPosition = 0.0f;
    private float minSectorWidth = 200;
    private float cameraOffset;

    private int lastSector = -1;

    private int cellHeight;
    private int cellAmount = 24;

    private int windowWidth;
    private int windowHeight;

    //game state
    // 0 - menu, 1 - game, 2 - score
    private static int gameState = 0;
    private static boolean gameStateChanged = true;

    private float[] seaTopEntity;
    private float[] seaBottomEntity;

    public Level(FlattenedEngine engine, Canvas canvas){
        this.canvas = canvas;

        windowWidth = canvas.getWidth();
        windowHeight = canvas.getHeight();

        cameraOffset = -3.0f * windowWidth;
        cellHeight = windowHeight/cellAmount;

        init(engine);
    }

    public static int getGameState(){
        return gameState;
    }

    public static void setGameState(int newGameState){
        if(!gameStateChanged){
            gameState = newGameState;
            gameStateChanged = true;
        }
    }

    private void init(FlattenedEngine engine) {
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

        seaBottomEntity = Entities.createSeaPartBottom(new Vector2(0,Main.HEIGHT - 20), new Vector2(Main.WIDTH, 20));
        engine.addEntity(seaBottomEntity);

        seaTopEntity = Entities.createSeaPartTop(new Vector2(0,Main.HEIGHT - 39), new Vector2(Main.WIDTH + 10, 2));
        engine.addEntity(seaTopEntity);


        for(int i = 0; i < sectorProbability.length; i++){
            totalPropability+= sectorProbability[i];
        }
    }

    public void update(FlattenedEngine engine, AffineTransform camera){
        if(gameState == 1){
            if(gameStateChanged){
                Entities.getFirstPlayer()[EntityIndex.GRAVITATION_INFLUENCE.getIndex()] = 1.0f;
                Entities.setPositionFor(Entities.getFirstPlayer(), 400, -1000);
            }

            Entities.setPositionFor(seaTopEntity, (float) -camera.getTranslateX(), seaTopEntity[EntityIndex.POSITION_Y.getIndex()]);
            Entities.setPositionFor(seaBottomEntity, (float) - camera.getTranslateX(), seaBottomEntity[EntityIndex.POSITION_Y.getIndex()]);
            float cameraX = (float) camera.getTranslateX() + cameraOffset;

            movement = -cameraX - currentSectorPosition;
            if(movement > minSectorWidth){
                generateNextSector(engine);
            }

        }else if(gameState == 2){
            if(gameStateChanged){
                Entities.setPositionFor(seaTopEntity, 0, seaTopEntity[EntityIndex.POSITION_Y.getIndex()]);
                Entities.setPositionFor(seaBottomEntity, 0, seaBottomEntity[EntityIndex.POSITION_Y.getIndex()]);
                currentSectorPosition = -3.0f * windowWidth;

            }
            for(int i = 0; i < engine.getEntities().size(); i++){
                if(FlattenedEngine.isBitmaskValid(EntityType.PLATFORM.getEntityType(), (int) engine.getEntities().get(i)[EntityIndex.ENTITY_TYPE_ID.getIndex()]) ||
                        FlattenedEngine.isBitmaskValid(EntityType.BULLET.getEntityType(), (int) engine.getEntities().get(i)[EntityIndex.ENTITY_TYPE_ID.getIndex()]) ){
                    engine.removeEntity(engine.getEntities().get(i));
                }
            }

        }
        gameStateChanged = false;
    }

    private void generateNextSector(FlattenedEngine engine) {
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

        if(currentSectorPosition < Main.WIDTH/2){
            index = FLOOR;
        }else{
            index = correctIndex(index);
        }

        switch(index){
            case WATER: createWaterSector(engine); break;
            case WATER_HOR_PLATFORM: createWaterHorPlatformSector(engine); break;
            case WATER_VER_PLATFORM: createWaterVerPlatformSector(engine); break;
            case FLOOR: createFloor(engine); break;
            case FLOOR_PLATFORM: createFloorPlatformSector(engine); break;
        }

        lastSector = index;
    }

    private int correctIndex(int index){
        int newIndex = index;

        if(index == WATER || index == WATER_HOR_PLATFORM){
            if(lastSector == WATER || lastSector == WATER_HOR_PLATFORM) {
                return random.nextFloat() < 0.7f ? FLOOR : FLOOR_PLATFORM;
            }
        }

        if(index == WATER_VER_PLATFORM){
            if(lastSector == WATER){
                return random.nextFloat() < 0.4f ? FLOOR : FLOOR_PLATFORM;
            }
        }

        if(index == FLOOR || index == FLOOR_PLATFORM){
            if(lastSector == FLOOR || lastSector == FLOOR_PLATFORM){
                if(random.nextFloat() > 0.6f){
                    return random.nextFloat() > 0.4f ? random.nextFloat() > 0.7f ? WATER : WATER_HOR_PLATFORM : WATER_VER_PLATFORM;
                }
            }
        }


        return newIndex;
    }

    private void createWaterSector(FlattenedEngine engine){
        int segments = 1+random.nextInt(2);
        float[] segmentDimensions = generateSegmentDimenions(segments);
        getSegmentPositions(segmentDimensions);
        lastSector = WATER;
    }

    private float[] getSegmentPositions(float[] segmentDimensions){
        float[] segmentPositions = new float[segmentDimensions.length];
        float currentSum = 0.0f;
        for(int i = 0; i < segmentPositions.length; i++){
            segmentPositions[i] = currentSectorPosition + currentSum;
            currentSum += segmentDimensions[i];
        }

        for(int i = 0; i < segmentDimensions.length; i++){
            currentSectorPosition+=segmentDimensions[i];
        }
        return segmentPositions;
    }


    private void createWaterHorPlatformSector(FlattenedEngine engine){
        int segments = 2+random.nextInt(2);
        float[] segmentDimensions = generateSegmentDimenions(segments);
        float[] segmentPositions = getSegmentPositions(segmentDimensions);

        int step = random.nextFloat() > 0.5f ? 14 : 19;
        Vector2 min = new Vector2(segmentPositions[0] + segmentDimensions[0]/2.0f,step*cellHeight);
        Vector2 max = new Vector2(segmentPositions[segments-1] + segmentDimensions[segments-1]/2.0f,step*cellHeight);
        engine.addEntity(Entities.createMovingPlatform(min, new Vector2(40.0f +random.nextFloat()*20.0f, 15), new Vector2(200+random.nextFloat()*50.0f,0), min, max));

        lastSector = WATER_HOR_PLATFORM;
    }

    private void createWaterVerPlatformSector(FlattenedEngine engine){
        int segments = 2+random.nextInt(2);
        float[] segmentDimensions = generateSegmentDimenions(segments);
        float[] segmentPositions = getSegmentPositions(segmentDimensions);

        int index = segments == 2 ? 0 : 1;
        Vector2 min = new Vector2(segmentPositions[index] + segmentDimensions[index]/2.0f,12*cellHeight);
        Vector2 max = new Vector2(segmentPositions[index] + segmentDimensions[index]/2.0f,21*cellHeight);
        engine.addEntity(Entities.createMovingPlatform(min, new Vector2(40.0f +random.nextFloat()*20.0f, 15), new Vector2(0,200+random.nextFloat()*50.0f), min, max));

        lastSector = WATER_VER_PLATFORM;
    }

    private void createFloor(FlattenedEngine engine){
        int segments = 2+random.nextInt(2);
        float[] segmentDimensions = generateSegmentDimenions(segments);
        float[] segmentPositions = getSegmentPositions(segmentDimensions);

        int step = Math.min(Math.round((windowHeight*0.9f+random.nextFloat()*windowHeight*0.3f)/ cellHeight)-1, cellAmount-1);
        int height = (cellAmount - step) * cellHeight;

        for(int i = 0; i < segments; i++){
            engine.addEntity(Entities.createPlatform(new Vector2(segmentPositions[i]+segmentDimensions[i]/2.0f,(step * cellHeight )+  (height/2.0f)), new Vector2(segmentDimensions[i]/2.0f+3.0f, height)));
        }

        lastSector = FLOOR;
    }

    private void createFloorPlatformSector(FlattenedEngine engine){
        int segments = 2+random.nextInt(3);
        float[] segmentDimensions = generateSegmentDimenions(segments);
        float[] segmentPositions = getSegmentPositions(segmentDimensions);

        int step = Math.min(Math.round((windowHeight*0.9f+random.nextFloat()*windowHeight*0.3f)/ cellHeight)-1, cellAmount-1);
        int height = (cellAmount - step) * cellHeight;

        for(int i = 0; i < segments; i++){
            engine.addEntity(Entities.createPlatform(new Vector2(segmentPositions[i]+segmentDimensions[i]/2.0f,(step * cellHeight )+  (height/2.0f)), new Vector2(segmentDimensions[i]/2.0f+3.0f, height)));
        }

        int platforms = random.nextInt(segments+1);

        for(int i = 0; i < platforms; i++){
            step = random.nextFloat() > 0.5f ? 11 : 16;

            engine.addEntity(Entities.createPlatform(new Vector2(segmentPositions[i]+segmentDimensions[i]/2.0f,(step * cellHeight )+  10), new Vector2(40+random.nextFloat()*40.0f, 10)));
        }

        lastSector = FLOOR;
    }

    private float[] generateSegmentDimenions(int amount){
        float[] segmentDimensions = new float[amount];
        for(int i = 0; i < amount; i++){
            segmentDimensions[i]=  minSectorWidth+random.nextFloat()*50.0f;
        }
        return segmentDimensions;
    }

}
