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
    private int[] sectorProbability = new int[]{15,50};
    private int totalPropability = 0;

    private float currentSectorPosition = 0.0f;
    private float minSectorWidth = 200;
    private float cameraOffset;

    private ArrayList<Integer> indexHistory = new ArrayList<>();

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

        seaTopEntity = Entities.createSeaPartBottom(new Vector2(0,Main.HEIGHT - 20), new Vector2(Main.WIDTH, 20));
        engine.addEntity(seaTopEntity);

        seaBottomEntity = Entities.createSeaPartTop(new Vector2(0,Main.HEIGHT - 39), new Vector2(Main.WIDTH + 10, 2));
        engine.addEntity(seaBottomEntity);


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
            createScoreScene(engine);

        }
        gameStateChanged = false;
    }

    private void createScoreScene(FlattenedEngine engine) {
        for(int i = 0; i < engine.getEntities().size(); i++){
            if(FlattenedEngine.isBitmaskValid(EntityType.PLATFORM.getEntityType(), (int) engine.getEntities().get(i)[EntityIndex.ENTITY_TYPE_ID.getIndex()]) ||
                    FlattenedEngine.isBitmaskValid(EntityType.BULLET.getEntityType(), (int) engine.getEntities().get(i)[EntityIndex.ENTITY_TYPE_ID.getIndex()]) ){
                engine.removeEntity(engine.getEntities().get(i));
                continue;
            }
        }
    }


    private void refineSectors(FlattenedEngine engine, int oneSequence) {
        if(oneSequence>2){
            Vector2 min = new Vector2(currentSectorPosition- (2.5f*minSectorWidth),canvas.getHeight()*0.7f);
            Vector2 max = new Vector2(currentSectorPosition-0.5f*minSectorWidth, canvas.getHeight()*0.7f);
            engine.addEntity(Entities.createMovingPlatform(min, new Vector2(75,15), new Vector2(200,0), min, max));
        }else if(oneSequence ==1){
            Vector2 min = new Vector2(currentSectorPosition-0.5f*minSectorWidth,canvas.getHeight()*0.4f);
            Vector2 max = new Vector2(currentSectorPosition-0.5f*minSectorWidth, canvas.getHeight()*0.8f);
            engine.addEntity(Entities.createMovingPlatform(min, new Vector2(32.5f,15), new Vector2(0,200), min, max));

        }


    }

    private void generateNextSector(FlattenedEngine engine) {
        float sectorWidth = minSectorWidth + random.nextFloat() * 50;

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

        if(currentSectorPosition < windowWidth){
            index = 0;
        }

        int oneSequence = getOneSequenceAmount();

        if(index == 0){
            createPlatformSector(engine, sectorWidth);
        }else{
            if(oneSequence > 2){
                createPlatformSector(engine, sectorWidth);
                index = 0;
            }
        }

        refineSectors(engine, oneSequence);

        indexHistory.add(index);
        if(indexHistory.size() > 3){
            indexHistory.remove(0);
        }


        currentSectorPosition += sectorWidth;
    }

    private int getOneSequenceAmount() {
        int ones = 0;
        for (int i = 0; i < indexHistory.size(); i++){
            if(indexHistory.get(i) == 1){
                ones++;
            }else{
                ones = 0;
            }
        }

        return ones;
    }


    private void createPlatformSector(FlattenedEngine engine, float sectorWidth){
        int step = Math.min(Math.round((windowHeight*0.9f+random.nextFloat()*windowHeight*0.2f)/ cellHeight)-1, cellAmount-1);
        int height = (cellAmount - step) * cellHeight;
        engine.addEntity(Entities.createPlatform(new Vector2(currentSectorPosition+sectorWidth/2.0f,(step * cellHeight )+  (height/2.0f)), new Vector2(sectorWidth/2.0f+1.5f, height)));
    }
}
