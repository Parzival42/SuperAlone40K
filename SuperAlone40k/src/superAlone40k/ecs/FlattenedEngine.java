package superAlone40k.ecs;

import superAlone40k.window.WindowWithFlattenedECS;

import java.awt.event.KeyEvent;
import java.util.ArrayList;



public class FlattenedEngine {

    private int runningID = 0;

    private ArrayList<float[]> entities = new ArrayList<>();
    private ArrayList<float[]>[] systemViews;

    private ArrayList<float[]> entitiesToAdd = new ArrayList<>();
    private ArrayList<float[]> entitiesToDelete = new ArrayList<>();

    private int[] systemBitmasks = new int[]{0b1, 0b10, 0b100, 0b1000, 0b10000};
    private SystemMethod[] systemMethods = new SystemMethod[]{FlattenedEngine::simpleHorizontalMovement,  FlattenedEngine::simpleVerticalMovement, FlattenedEngine::inputProcessing, FlattenedEngine::colliderSorting, FlattenedEngine::movementSystem};


    private boolean updating;

    private static double totalTime = 0.0d;

    private double currentTimeScale = 1.0f;


    public FlattenedEngine() {
        systemViews = new ArrayList[systemMethods.length];
        for(int i = 0; i < systemViews.length; i++){
            systemViews[i] = new ArrayList<>();
        }
    }

    public void update(double deltaTime, double timeScale){
        updating = true;

        currentTimeScale = timeScale;

        updateSystems(deltaTime);

        updateEntities();

        updating = false;
    }

    private void updateEntities(){
        //remove pending entities
        for(int i = entitiesToDelete.size()-1 ; i >= 0; i--){
            removeEntityInternal(entitiesToDelete.get(i));
        }
        entitiesToDelete.clear();

        //add pending entities
        for(int i = 0; i < entitiesToAdd.size(); i++){
            addEntityInternal(entitiesToAdd.get(i));
        }
        entitiesToAdd.clear();
    }

    private void updateSystems(double deltaTime){
        //general update
        totalTime += deltaTime;


        //individual entity iterating approach
        for(int i = 0; i < systemViews.length; i++){
            for(int j = 0; j < systemViews[i].size(); j++){
                systemMethods[i].execute(this, systemViews[i].get(j), deltaTime);
            }
        }

        performCollisionDetection();
    }

    public void addEntity(float[] entity){
        assert entity.length > 1;

        entity[0] = runningID++;
        if(updating){
            entitiesToAdd.add(entity);
        }else{
            addEntityInternal(entity);
        }
    }

    private void addEntityInternal(float[] entity){
        entities.add(entity);
        addEntityToViews(entity);
    }

    //adds an entity to the proper views
    private void addEntityToViews(float[] entity){
        int entityMask = (int) entity[1];
        for(int i = 0; i < systemViews.length; i++){
            if((entityMask & systemBitmasks[i]) == systemBitmasks[i]){
                systemViews[i].add(entity);
            }
        }
    }

    public void removeEntity(float[] entity){
        if(updating){
            entitiesToDelete.add(entity);
        }else{
            removeEntityInternal(entity);
        }
    }

    private void removeEntityInternal(float[] entity){
        removeEntityFromViews(entity);
        entities.remove(entity);
    }

    private void removeEntityFromViews(float[] entity){
        for(int i = 0; i < systemViews.length; i++){
            systemViews[i].remove(entity);
        }
    }

    public ArrayList<float[]> getEntities() {
        return entities;
    }


    private interface SystemMethod{
        void execute(FlattenedEngine engine, float[] entity, double deltaTime);
    }

    // ---- ENTITY SYSTEM METHODS


    private void simpleHorizontalMovement(float[] entity, double deltaTime){
        entity[2] = (float) (entity[2] + Math.sin((totalTime+entity[0])*3) * deltaTime * currentTimeScale * 100);
    }

    private void simpleVerticalMovement(float[] entity, double deltaTime){
        entity[3] = (float) (entity[3] + Math.sin((totalTime+entity[0])*3) * deltaTime * currentTimeScale * 100);
    }


    //inputProcessing system variables
    private float movementSpeed = 700.0f;
    private float maxMovementSpeed = 200.0f;
    private float jumpStrength = 5800.0f;
    private float maxJumpStrength = 2000.0f;
    private float playerGravity = 2000.0f;

    private void inputProcessing(float[] entity, double deltaTime){
        if(WindowWithFlattenedECS.isKeyPressed(KeyEvent.VK_A)){
            entity[15] -= movementSpeed*deltaTime;
            entity[15] = entity[15] < -maxMovementSpeed ? -maxMovementSpeed : entity[15];
        }

        if(WindowWithFlattenedECS.isKeyPressed(KeyEvent.VK_D)){
            entity[15] += movementSpeed*deltaTime;
            entity[15] = entity[15] > maxMovementSpeed ? maxMovementSpeed : entity[15];
        }

        if(WindowWithFlattenedECS.isKeyPressed(KeyEvent.VK_SPACE)){
            entity[16] -= jumpStrength*deltaTime;
            entity[16] = entity[16] > maxJumpStrength ? maxJumpStrength : entity[16];
        }

        entity[16] += playerGravity * deltaTime;

        entity[2] += entity[15] * deltaTime;
        entity[3] += (entity[16] /*+ gravity*/) * deltaTime;

        float timeScale = Math.abs(entity[15]/maxMovementSpeed);
        WindowWithFlattenedECS.setTimeScale(timeScale < 0.25f ? 0.25f : timeScale);

        //System.out.println("speed: "+entity[15]);

        entity[15] *= entity[18];
        entity[16] *= entity[18];
    }

    //collision detection variables
    private ArrayList<float[]> staticColliders = new ArrayList<>();
    private ArrayList<float[]> dynamicColliders = new ArrayList<>();

    private void colliderSorting(float[] entity, double deltaTime){
        if(entity[14]>0.5f){
            dynamicColliders.add(entity);
        }else{
            staticColliders.add(entity);
        }
    }

    private void performCollisionDetection(){
        for(int i = 0; i < dynamicColliders.size(); i++){
            float[] entity = dynamicColliders.get(i);
            for(int j = 0; j < staticColliders.size(); j++){
                collisionCheckAABB(entity, staticColliders.get(j));
            }
        }

        for(int i= 0; i < dynamicColliders.size(); i++){
            float[] entity = dynamicColliders.get(i);
            for(int j = i+1; j < dynamicColliders.size(); j++){
                collisionCheckAABB(entity, dynamicColliders.get(j));
            }
        }

        staticColliders.clear();
        dynamicColliders.clear();
    }

    private void collisionCheckAABB(float[] entity1, float[] entity2) {
        float xOverlap = Math.abs((entity1[2]+entity1[10]) - (entity2[2]+entity2[10])) - (entity1[12] + entity2[12]);
        if ((xOverlap)<0) {
            float yOverlap = Math.abs((entity1[3]+entity1[11]) - (entity2[3]+entity2[11])) - (entity1[13] + entity2[13]);
            if ((yOverlap) < 0) {

                //if one of the entities is a raindrop
                int entity1Mask = (int) entity1[1];
                int entity2Mask = (int) entity2[1];


                if(entity1Mask == 56 && entity2Mask == 56){
                    return;
                }

                //if raindrop and other entity
                if(entity1Mask == 56 && entity2Mask != 56){
                    removeEntity(entity1);
                    return;
                }

                if(entity2Mask == 56 && entity1Mask != 56){
                    removeEntity(entity2);
                    return;
                }


                //two raindrops do not collide
                if(entity1Mask == 24 && entity2Mask == 24){
                    return;
                }

                //if raindrop and other entity
                if(entity1Mask == 24 && entity2Mask != 24){
                    WindowWithFlattenedECS.getSimpleParticleSystem().burstEmit((int)(entity1[2]+entity1[4]), (int)(entity1[3]+entity1[5]), 5);
                    removeEntity(entity1);
                    return;
                }

                if(entity2Mask == 24 && entity1Mask != 24){
                    WindowWithFlattenedECS.getSimpleParticleSystem().burstEmit((int)(entity2[2]+entity2[4]), (int)(entity2[3]+entity2[5]), 5);
                    removeEntity(entity2);
                    return;
                }

                //player and environment
                if(entity1Mask == 12){
                    resolvePlayerCollision(entity1, entity2, xOverlap, yOverlap);
                    return;
                }

                if(entity2Mask == 12){
                    resolvePlayerCollision(entity2, entity1, xOverlap, yOverlap);
                    return;
                }



                float[] toDelete = entity1[14] > 0.5f ? entity1 : entity2;
                removeEntity(toDelete);
            }
        }
    }

    private void resolvePlayerCollision(float[] player, float[] other, float xOverlap, float yOverlap){
        if(xOverlap > yOverlap){
            float xOffset = player[2] < other[2] ? xOverlap : -xOverlap;
            player[2] += xOffset;
        }else{
            float yOffset = player[3] < other[3] ? yOverlap : -yOverlap;
            player[3] += yOffset;
        }
    }


    //simple movement system
    private float gravity = 3000.0f;

    private void movementSystem(float[] entity, double deltaTime){
        double scaledDeltaTime = deltaTime * currentTimeScale;

        entity[16] +=  entity[17] * gravity * scaledDeltaTime;

        entity[2] += entity[15] * scaledDeltaTime;
        entity[3] += entity[16] * scaledDeltaTime;

        entity[15] *= entity[18];
        entity[16] *= entity[18];
    }


}
