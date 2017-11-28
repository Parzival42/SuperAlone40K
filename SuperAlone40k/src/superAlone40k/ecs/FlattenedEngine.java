package superAlone40k.ecs;

import superAlone40k.util.Easing;
import superAlone40k.util.Ray;
import superAlone40k.util.Tween;
import superAlone40k.util.Vector2;
import superAlone40k.window.WindowWithFlattenedECS;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

public class FlattenedEngine {
    private ArrayList<float[]> entities = new ArrayList<>();
    private ArrayList<float[]>[] systemViews;

    private ArrayList<float[]> entitiesToAdd = new ArrayList<>();
    private ArrayList<float[]> entitiesToDelete = new ArrayList<>();

    private int[] systemBitmasks = new int[] { SystemBitmask.HORIZONTAL_MOVEMENT.getSystemMask(), SystemBitmask.VERTICAL_MOVEMENT.getSystemMask(), SystemBitmask.INPUT.getSystemMask(), SystemBitmask.COLLIDER_SORTING.getSystemMask(), SystemBitmask.MOVEMENT_SYSTEM.getSystemMask(), SystemBitmask.LIGHT_SYSTEM.getSystemMask(), SystemBitmask.TRIGGER_SYSTEM.getSystemMask(), SystemBitmask.LIFETIME_SYSTEM.getSystemMask(), SystemBitmask.CHECKPOINT_SYSTEM.getSystemMask() };
    private SystemMethod[] systemMethods = new SystemMethod[]{FlattenedEngine::simpleHorizontalMovement,  FlattenedEngine::simpleVerticalMovement, FlattenedEngine::inputProcessing, FlattenedEngine::colliderSorting, FlattenedEngine::movementSystem, FlattenedEngine::lightingSystem, FlattenedEngine::triggerSystem, FlattenedEngine::lifetimeSystem, FlattenedEngine::checkpointSystem};

    private final TreeSet<Ray> angleSortedRays = new TreeSet<>();
    
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
    
    public void render(Graphics2D graphics) {
    	final List<float[]> lights = systemViews[5];

    	graphics.setTransform(camera);
    	for(float[] lightEntity : lights) {
    		calculateShadows(lightEntity, graphics);
    	}
    }

    private void updateEntities(){
        //remove pending entities
        for(int i = entitiesToDelete.size() - 1 ; i >= 0; i--){
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
        int entityMask = (int) entity[EntityIndex.SYSTEM_MASK.getIndex()];
        for(int i = 0; i < systemViews.length; i++){
            if((entityMask & systemBitmasks[i]) == systemBitmasks[i]) {
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

    /**
     * @param originalMask Original mask from enumeration (Or other source).
     * @param maskToCheck The mask you want to check against the original mask.
     * @return Returns <strong>true</strong> if the masks are fitting.
     */
    public static boolean isBitmaskValid(int originalMask, int maskToCheck) {
        return (originalMask & maskToCheck) == originalMask;
    }

    // ---- ENTITY SYSTEM METHODS

    //region Horizontal and Vertical Movement Systems
    private void simpleHorizontalMovement(float[] entity, double deltaTime){
		entity[EntityIndex.POSITION_X.getIndex()] = (float) (entity[2]
				+ Math.sin((totalTime + entity[3]) * 3) * deltaTime  * 100);
    }

    private void simpleVerticalMovement(float[] entity, double deltaTime){
		entity[EntityIndex.POSITION_Y.getIndex()] = (float) (entity[3]
				+ Math.sin((totalTime + entity[2]) * 3) * deltaTime  * 100);
    }
    //endregion

    //region Input System (player, camera, menu, timescale
    private void inputProcessing(float[] entity, double deltaTime){
        //player movement
        playerControl(entity, deltaTime);

        //timescale update
        timeScaleControl(entity, deltaTime);

        //camera position update
        cameraControl(entity, deltaTime);

        //menu control stuff
        menuControl();
    }

    //player control parameters
    private float movementSpeed = 1000.0f;
    private float maxMovementSpeed = 350.0f;
    private float jumpStrength = 1200.0f;
    private float maxJumpStrength = 700.0f;
    private float playerGravity = 2000.0f;
    private boolean isJumping = false;
    private boolean isDoubleJumping = false;
    private boolean jumpRequestValid = true;
    private boolean isJumpRequested = false;

    private void playerControl(float[] player, double deltaTime){
        if(WindowWithFlattenedECS.isKeyPressed(KeyEvent.VK_A)){
            player[EntityIndex.VELOCITY_X.getIndex()] -= movementSpeed * deltaTime;
            player[EntityIndex.VELOCITY_X.getIndex()] = player[EntityIndex.VELOCITY_X.getIndex()] < -maxMovementSpeed ? -maxMovementSpeed : player[EntityIndex.VELOCITY_X.getIndex()];
        }

        if(WindowWithFlattenedECS.isKeyPressed(KeyEvent.VK_D)){
            player[EntityIndex.VELOCITY_X.getIndex()] += movementSpeed * deltaTime;
            player[EntityIndex.VELOCITY_X.getIndex()] = player[EntityIndex.VELOCITY_X.getIndex()] > maxMovementSpeed ? maxMovementSpeed : player[EntityIndex.VELOCITY_X.getIndex()];
        }

        boolean isGrounded = false;
        if(Math.abs(player[EntityIndex.TRIGGER_STAY.getIndex()]) > 0.5f){
            isGrounded = true;
            isJumping = false;
            isDoubleJumping = false;
        }

        if(!WindowWithFlattenedECS.isKeyPressed(KeyEvent.VK_SPACE)){
            jumpRequestValid = true;
        }

        //jump requested?
        if(WindowWithFlattenedECS.isKeyPressed(KeyEvent.VK_SPACE) && jumpRequestValid){
            jumpRequestValid = false;
            isJumpRequested = true;
        }

        //first jump
        if(isGrounded && isJumpRequested){
            //TODO: uncomment to see easeing in action
           /* ArrayList<float[]> playerList = new ArrayList<>();
            playerList.add(player);

            Tween.getInstance().add(playerList, 0, EntityIndex.EXTENT_X.getIndex(), 40, 0.3f, Easing.Type.ElasticEaseOut, false);
            Tween.getInstance().add(playerList, 0, EntityIndex.EXTENT_Y.getIndex(), 80, 0.3f, Easing.Type.ElasticEaseOut, false);

            Tween.getInstance().add(playerList, 0, EntityIndex.AABB_EXTENT_X.getIndex(), 40, 0.3f, Easing.Type.ElasticEaseOut, false);
            Tween.getInstance().add(playerList, 0, EntityIndex.AABB_EXTENT_Y.getIndex(), 80, 0.3f, Easing.Type.ElasticEaseOut, false);*/

            player[EntityIndex.VELOCITY_Y.getIndex()] = -jumpStrength;
            isJumping = true;
            isJumpRequested = false;
        }

        //second jump
        if(isJumping && !isDoubleJumping && isJumpRequested){

            //TODO: uncomment to see easeing in action
            /*ArrayList<float[]> playerList = new ArrayList<>();
            playerList.add(player);

            Tween.getInstance().add(playerList, 0, EntityIndex.EXTENT_X.getIndex(), 20, 0.3f, Easing.Type.ElasticEaseIn, false);
            Tween.getInstance().add(playerList, 0, EntityIndex.EXTENT_Y.getIndex(), 40, 0.3f, Easing.Type.ElasticEaseIn, false);

            Tween.getInstance().add(playerList, 0, EntityIndex.AABB_EXTENT_X.getIndex(), 20, 0.3f, Easing.Type.ElasticEaseIn, false);
            Tween.getInstance().add(playerList, 0, EntityIndex.AABB_EXTENT_Y.getIndex(), 40, 0.3f, Easing.Type.ElasticEaseIn, false);*/

            player[EntityIndex.VELOCITY_Y.getIndex()] = -jumpStrength;
            isDoubleJumping = true;
            isJumpRequested = false;
        }

        player[EntityIndex.VELOCITY_X.getIndex()] *= player[EntityIndex.DRAG.getIndex()];
        player[EntityIndex.VELOCITY_Y.getIndex()] *= player[EntityIndex.DRAG.getIndex()];

        player[EntityIndex.VELOCITY_Y.getIndex()] += playerGravity * deltaTime;

        player[EntityIndex.POSITION_X.getIndex()] += player[EntityIndex.VELOCITY_X.getIndex()] * deltaTime;
        player[EntityIndex.POSITION_Y.getIndex()] += player[EntityIndex.VELOCITY_Y.getIndex()] * deltaTime;
    }

    private void timeScaleControl(float[] player, double deltaTime){
        float relativeHorizontalSpeed = Math.abs(player[EntityIndex.VELOCITY_X.getIndex()]/maxMovementSpeed);
        float relativeVerticalSpeed = Math.abs(player[EntityIndex.VELOCITY_Y.getIndex()]/maxJumpStrength);

        float timeScale = relativeHorizontalSpeed < relativeVerticalSpeed ? relativeVerticalSpeed : relativeHorizontalSpeed;
        WindowWithFlattenedECS.setTimeScale(timeScale < 0.25f ? 0.25f : timeScale);
    }

    private void menuControl(){
        if(WindowWithFlattenedECS.isKeyPressed(KeyEvent.VK_ESCAPE)) {
            System.exit(42);
        }
    }

    //camera control parameters
    private float minPosX = 200.0f;
    private float maxPosX = 1000.0f;

    private AffineTransform camera = new AffineTransform();

    private void cameraControl(float[] player, double deltaTime){
        float xChange = 0.0f;
        if(player[EntityIndex.POSITION_X.getIndex()] < minPosX && player[EntityIndex.VELOCITY_X.getIndex()] < 0.05f){
            xChange = (float) (player[EntityIndex.VELOCITY_X.getIndex()] * deltaTime);

        }else if(player[EntityIndex.POSITION_X.getIndex()]> maxPosX && player[EntityIndex.VELOCITY_X.getIndex()] > 0.05f){
            xChange = (float) (player[EntityIndex.VELOCITY_X.getIndex()]*deltaTime);
        }

        camera.setToTranslation(camera.getTranslateX()-xChange, camera.getTranslateY());
        minPosX += xChange;
        maxPosX += xChange;
    }

    public AffineTransform getCamera(){
        return camera;
    }
    //endregion

    //region Lighting System

    private void lightingSystem(float[] entity, double deltaTime) {
        //entity[EntityIndex.POSITION_X.getIndex()] = (float) (-camera.getTranslateX()+100);
        //entity[EntityIndex.POSITION_Y.getIndex()] = (float) (-camera.getTranslateY()+100);
    }

    private Ray[] getCornerRays(Vector2 lightPosition, float[] entity) {
        // Box ray collision
        if(isBitmaskValid(EntityType.BOX_SHADOW.getEntityType(), (int) entity[EntityIndex.ENTITY_TYPE_ID.getIndex()])) {
            Vector2 min = new Vector2(
                    entity[EntityIndex.POSITION_X.getIndex()] - entity[EntityIndex.EXTENT_X.getIndex()],
                    entity[EntityIndex.POSITION_Y.getIndex()] - entity[EntityIndex.EXTENT_Y.getIndex()]);
            final float width = entity[EntityIndex.EXTENT_X.getIndex()] * 2;
            final float height = entity[EntityIndex.EXTENT_Y.getIndex()] * 2;

            final Vector2 toLeftTop = new Vector2(min.x, min.y).sub(lightPosition);
            final Ray leftTop = new Ray(lightPosition, toLeftTop);

            final Vector2 toRightTop = new Vector2(min.x + width, min.y).sub(lightPosition);
            final Ray rightTop = new Ray(lightPosition, toRightTop);

            final Vector2 toLeftBottom = new Vector2(min.x, min.y + height).sub(lightPosition);
            final Ray leftBottom = new Ray(lightPosition, toLeftBottom);

            final Vector2 toRightBottom = new Vector2(min.x + width, min.y + height).sub(lightPosition);
            final Ray rightBottom = new Ray(lightPosition, toRightBottom);

            return new Ray[] { leftTop, rightTop, leftBottom, rightBottom };
        } else if(isBitmaskValid(EntityType.SCREEN_BORDER.getEntityType(), (int) entity[EntityIndex.ENTITY_TYPE_ID.getIndex()])) {
            final Ray borderRay = new Ray(
                    new Vector2(entity[EntityIndex.BORDER_ORIGIN_X.getIndex()]-camera.getTranslateX(), entity[EntityIndex.BORDER_ORIGIN_Y.getIndex()]-camera.getTranslateY()),
                    new Vector2(entity[EntityIndex.BORDER_DIR_X.getIndex()], entity[EntityIndex.BORDER_DIR_Y.getIndex()]));

            final Vector2 toCornerPosition = new Vector2(borderRay.origin.x, borderRay.origin.y).sub(lightPosition);
            return new Ray[] { new Ray(lightPosition, toCornerPosition) };
        }
        return null;
    }

    private void intersect(Ray ray, float[] entity) {
        // Box ray collision
        if(isBitmaskValid(EntityType.BOX_SHADOW.getEntityType(), (int) entity[EntityIndex.ENTITY_TYPE_ID.getIndex()])) {
            Vector2 min = new Vector2(
                    entity[EntityIndex.POSITION_X.getIndex()] - entity[EntityIndex.EXTENT_X.getIndex()],
                    entity[EntityIndex.POSITION_Y.getIndex()] - entity[EntityIndex.EXTENT_Y.getIndex()]);
            Vector2 max = new Vector2(
                    entity[EntityIndex.POSITION_X.getIndex()] + entity[EntityIndex.EXTENT_X.getIndex()],
                    entity[EntityIndex.POSITION_Y.getIndex()] + entity[EntityIndex.EXTENT_Y.getIndex()]);

            double swap;

            double txMin = (min.x - ray.origin.x) / ray.direction.x;
            double txMax = (max.x - ray.origin.x) / ray.direction.x;

            if(txMin > txMax) {
                swap = txMin;
                txMin = txMax;
                txMax = swap;
            }

            double tyMin = (min.y - ray.origin.y) / ray.direction.y;
            double tyMax = (max.y - ray.origin.y) / ray.direction.y;

            if(tyMin > tyMax) {
                swap = tyMin;
                tyMin = tyMax;
                tyMax = swap;
            }

            if(txMin > tyMax || tyMin > txMax) {
                return;
            }

            final double tMin = (txMin > tyMin) ? txMin : tyMin;	// Choose max
            final double tMax = (txMax < tyMax) ? txMax : tyMax;	// Choose min

            final double closestDistance = (tMin < tMax) ? tMin : tMax;
            if(closestDistance < 0) {
                return;
            }
            final Vector2 hitPoint = ray.origin.copy().add(ray.direction.copy().scale(closestDistance));
            ray.updateHitInformation(hitPoint);

        } else if(isBitmaskValid(EntityType.SCREEN_BORDER.getEntityType(), (int) entity[EntityIndex.ENTITY_TYPE_ID.getIndex()])) {
            final Ray borderRay = new Ray(
                    new Vector2(entity[EntityIndex.BORDER_ORIGIN_X.getIndex()]-camera.getTranslateX(), entity[EntityIndex.BORDER_ORIGIN_Y.getIndex()]-camera.getTranslateY()),
                    new Vector2(entity[EntityIndex.BORDER_DIR_X.getIndex()], entity[EntityIndex.BORDER_DIR_Y.getIndex()]));

            final Vector2 hitPoint = ray.collideWith(borderRay, true);

            if(hitPoint != null) {
                ray.updateHitInformation(hitPoint);
            }
        }
    }

    private void calculateShadows(float[] lightEntity, Graphics2D graphics) {
    	final Vector2 lightPosition = new Vector2(lightEntity[EntityIndex.POSITION_X.getIndex()], lightEntity[EntityIndex.POSITION_Y.getIndex()]);
    	final Path2D.Double path = new Path2D.Double();
    	addRaysOfEntities(lightPosition);
    	
    	// Check all possible intersections of the given rays
    	for(float[] entity : entities) {
    		for(Ray sortedRay : angleSortedRays) {
    			intersect(sortedRay, entity);
    		}
    	}

    	// Draw polygon with the hit infos of the rays
    	// TODO: first() -> Validate if there was a hit
    	path.moveTo(angleSortedRays.first().hitPoint.x, angleSortedRays.first().hitPoint.y);
    	for(Ray sortedRay : angleSortedRays) {
    		if(sortedRay.isValidHit()) {
    			path.lineTo(sortedRay.hitPoint.x, sortedRay.hitPoint.y);
    			
    			// Debug draw
//				graphics.setColor(Color.RED);
//				graphics.drawLine((int) sortedRay.origin.x, (int) sortedRay.origin.y, (int) sortedRay.hitPoint.x, (int) sortedRay.hitPoint.y);
//				graphics.fillOval((int) sortedRay.hitPoint.x - 12, (int) sortedRay.hitPoint.y - 12, 24, 24);
    		}
    	}
    	
    	graphics.setColor(new Color(49, 65, 88));
		path.closePath();
		graphics.fill(path);
		angleSortedRays.clear();
	}

    private void addRaysOfEntities(Vector2 lightPosition) {
		// Rays to entity corners
		for(float[] entity : entities) {
			final Ray[] cornerRays = getCornerRays(lightPosition, entity);
			if(cornerRays != null) {
				for(Ray r : cornerRays) {
					addOffsetRays(r);
				}
			}
		}
	}
	
	private void addOffsetRays(Ray originalRay) {
		final Vector2 originalDirection = originalRay.direction.copy();
		final Ray leftOffset = new Ray(originalRay.origin, originalDirection.copy().rotateBy(-0.000001));
		final Ray rightOffset = new Ray(originalRay.origin, originalDirection.copy().rotateBy(0.000001));
		
		angleSortedRays.add(leftOffset);
		angleSortedRays.add(originalRay);
		angleSortedRays.add(rightOffset);
	}
    //endregion

    //region Collision System

    //collision detection variables
    private ArrayList<float[]> staticColliders = new ArrayList<>();
    private ArrayList<float[]> dynamicColliders = new ArrayList<>();

    private void colliderSorting(float[] entity, double deltaTime){
        if(entity[EntityIndex.COLLISION_TYPE.getIndex()] > 0.5f){
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
            for(int j = i + 1; j < dynamicColliders.size(); j++){
                collisionCheckAABB(entity, dynamicColliders.get(j));
            }
        }

        staticColliders.clear();
        dynamicColliders.clear();
    }

    //region Collision Check AABB

    //death zone height
    double deathZoneHeight = 1500;

    private void collisionCheckAABB(float[] entity1, float[] entity2) {
    	float xOverlap = 
    			Math.abs(
    					(entity1[EntityIndex.POSITION_X.getIndex()] + entity1[EntityIndex.AABB_CENTER_X.getIndex()]) -
    					(entity2[EntityIndex.POSITION_X.getIndex()] + entity2[EntityIndex.AABB_CENTER_X.getIndex()])) -
    					(entity1[EntityIndex.AABB_EXTENT_X.getIndex()] + entity2[EntityIndex.AABB_EXTENT_X.getIndex()]);
    	
    	if ((xOverlap) < 0) {
    		float yOverlap = Math.abs(
    				(entity1[EntityIndex.POSITION_Y.getIndex()] + entity1[EntityIndex.AABB_CENTER_Y.getIndex()]) -
    				(entity2[EntityIndex.POSITION_Y.getIndex()] + entity2[EntityIndex.AABB_CENTER_Y.getIndex()])) -
    				(entity1[EntityIndex.AABB_EXTENT_Y.getIndex()] + entity2[EntityIndex.AABB_EXTENT_Y.getIndex()]);
    		
            if ((yOverlap) < 0) {

                //if one of the entities is a raindrop
                int entity1Id = (int) entity1[EntityIndex.ENTITY_TYPE_ID.getIndex()];
                int entity2Id = (int) entity2[EntityIndex.ENTITY_TYPE_ID.getIndex()];

                if(isBitmaskValid(EntityType.RAIN_DROP_SPLATTER.getEntityType(), entity1Id) && 
                		isBitmaskValid(EntityType.RAIN_DROP_SPLATTER.getEntityType(), entity2Id)) {
                	return;
                }

                //if raindrop splatter and other entity
                if(isBitmaskValid(EntityType.RAIN_DROP_SPLATTER.getEntityType(), entity1Id) && 
                		!isBitmaskValid(EntityType.RAIN_DROP_SPLATTER.getEntityType(), entity2Id)) {
                	removeEntity(entity1);
                	return;
                }

                if(isBitmaskValid(EntityType.RAIN_DROP_SPLATTER.getEntityType(), entity2Id) && 
                		!isBitmaskValid(EntityType.RAIN_DROP_SPLATTER.getEntityType(), entity1Id)) {
                	removeEntity(entity2);
                	return;
                }

                //two raindrops do not collide
                if(isBitmaskValid(EntityType.RAIN_DROP.getEntityType(), entity1Id) && 
                		isBitmaskValid(EntityType.RAIN_DROP.getEntityType(), entity2Id)) {
                	return;
                }

                //if raindrop and other entity
                if(isBitmaskValid(EntityType.RAIN_DROP.getEntityType(), entity1Id) && 
                		!isBitmaskValid(EntityType.RAIN_DROP.getEntityType(), entity2Id)) {
                	WindowWithFlattenedECS.getRainParticleSystem().burstEmit(
                			(int) (entity1[EntityIndex.POSITION_X.getIndex()]),
                			(int) (entity1[EntityIndex.POSITION_Y.getIndex()] + entity1[EntityIndex.EXTENT_Y.getIndex()]*0.9f), 2);
                	removeEntity(entity1);
                	return;
                }

                if(isBitmaskValid(EntityType.RAIN_DROP.getEntityType(), entity2Id) && 
                		!isBitmaskValid(EntityType.RAIN_DROP.getEntityType(), entity1Id)) {
                	WindowWithFlattenedECS.getRainParticleSystem().burstEmit(
                			(int) (entity2[EntityIndex.POSITION_X.getIndex()]),
                			(int) (entity2[EntityIndex.POSITION_Y.getIndex()] + entity2[EntityIndex.EXTENT_Y.getIndex()]*0.9f), 2);
                	removeEntity(entity2);
                	return;
                }

                //player and bullet
                if(isBitmaskValid(EntityType.PLAYER.getEntityType(), entity2Id) &&
                        isBitmaskValid(EntityType.BULLET.getEntityType(), entity1Id)) {
                    removeEntity(entity1);
                    //removeEntity(entity2);
                    respawn(entity2);
                    System.out.println("Game Over");
                    return;
                }

                if(isBitmaskValid(EntityType.BULLET.getEntityType(), entity2Id) &&
                        isBitmaskValid(EntityType.PLAYER.getEntityType(), entity1Id)) {
                    //removeEntity(entity1);
                    removeEntity(entity2);
                    respawn(entity1);
                    System.out.println("Game Over");
                    return;
                }

                //player and environment
                if(isBitmaskValid(EntityType.PLAYER.getEntityType(), entity1Id)) {
                	resolvePlayerCollision(entity1, entity2, xOverlap, yOverlap);
                	return;
                }
                if(isBitmaskValid(EntityType.PLAYER.getEntityType(), entity2Id)) {
                	resolvePlayerCollision(entity2, entity1, xOverlap, yOverlap);
                	return;
                }

                float[] toDelete = entity1[EntityIndex.COLLISION_TYPE.getIndex()] > 0.5f ? entity1 : entity2;
                removeEntity(toDelete);
            }
        }else if(entity1[EntityIndex.POSITION_Y.getIndex()] > deathZoneHeight){
    	    removeEntity(entity1);
        }
    }
    //endregion

    //region Collision Response
    private void resolvePlayerCollision(float[] player, float[] other, float xOverlap, float yOverlap){
        if(xOverlap > yOverlap){
            float xOffset = player[EntityIndex.POSITION_X.getIndex()] < other[EntityIndex.POSITION_X.getIndex()] ? xOverlap : -xOverlap;
            player[EntityIndex.POSITION_X.getIndex()] += xOffset;
            player[EntityIndex.VELOCITY_X.getIndex()] = 0.0f;
        }else{
            float yOffset = player[EntityIndex.POSITION_Y.getIndex()] < other[EntityIndex.POSITION_Y.getIndex()] ? yOverlap : -yOverlap;
            player[EntityIndex.POSITION_Y.getIndex()] += yOffset;
            player[EntityIndex.VELOCITY_Y.getIndex()] = 0.0f;
        }
    }

    private void respawn(float[] player){
        camera.setToTranslation(0.0d,0.0d);
        player[EntityIndex.POSITION_X.getIndex()] = 250;
        player[EntityIndex.POSITION_Y.getIndex()] = 650;
        minPosX = 200.0f;
        maxPosX = 1000.0f;
    }
    //endregion
    //endregion

    //region Movement System
    private float gravity = 3000.0f;

    private void movementSystem(float[] entity, double deltaTime){
        final double scaledDeltaTime = deltaTime * currentTimeScale;

        entity[EntityIndex.VELOCITY_Y.getIndex()] +=  entity[EntityIndex.GRAVITATION_INFLUENCE.getIndex()] * gravity * scaledDeltaTime;

        entity[EntityIndex.POSITION_X.getIndex()] += entity[EntityIndex.VELOCITY_X.getIndex()] * scaledDeltaTime;
        entity[EntityIndex.POSITION_Y.getIndex()] += entity[EntityIndex.VELOCITY_Y.getIndex()] * scaledDeltaTime;

        entity[EntityIndex.VELOCITY_X.getIndex()] *= entity[EntityIndex.DRAG.getIndex()];
        entity[EntityIndex.VELOCITY_Y.getIndex()] *= entity[EntityIndex.DRAG.getIndex()];
    }
    //endregion

    //region Trigger System
    private boolean checkForTriggerOverlap(float[] trigger, float[] other){
        float xOverlap =
                Math.abs(
                        (trigger[EntityIndex.POSITION_X.getIndex()] + trigger[EntityIndex.TRIGGER_POSITION_X.getIndex()]) -
                        (other[EntityIndex.POSITION_X.getIndex()] + other[EntityIndex.AABB_CENTER_X.getIndex()])) -
                        (trigger[EntityIndex.TRIGGER_EXTENT_X.getIndex()] + other[EntityIndex.AABB_EXTENT_X.getIndex()]);

        if ((xOverlap) < 0) {
            float yOverlap = Math.abs(
                    (trigger[EntityIndex.POSITION_Y.getIndex()] + trigger[EntityIndex.TRIGGER_POSITION_Y.getIndex()]) -
                            (other[EntityIndex.POSITION_Y.getIndex()] + other[EntityIndex.AABB_CENTER_Y.getIndex()])) -
                    (trigger[EntityIndex.TRIGGER_EXTENT_Y.getIndex()] + other[EntityIndex.AABB_EXTENT_Y.getIndex()]);

            if ((yOverlap) < 0) {
                return true;
            }
        }
        return false;
    }

    private void triggerSystem(float[] trigger, double deltaTime){
        ArrayList<float[]> colliders = trigger[EntityIndex.TRIGGER_COLLISION_TYPE.getIndex()] > 0.5f ? dynamicColliders : staticColliders;

        for(int i = 0; i < colliders.size(); i++){
            if(colliders.get(i)!= trigger && checkForTriggerOverlap(trigger, colliders.get(i))){
                if(trigger[EntityIndex.TRIGGER_ENTER.getIndex()] < 0.5f && trigger[EntityIndex.TRIGGER_STAY.getIndex()] < 0.5f){
                    trigger[EntityIndex.TRIGGER_ENTER.getIndex()] = 1.0f;
                    trigger[EntityIndex.TRIGGER_OBJECT_TYPE.getIndex()] = colliders.get(i)[EntityIndex.ENTITY_TYPE_ID.getIndex()];
                    return;
                }

                if (trigger[EntityIndex.TRIGGER_ENTER.getIndex()] > 0.5f && trigger[EntityIndex.TRIGGER_STAY.getIndex()] < 0.5f){
                    trigger[EntityIndex.TRIGGER_STAY.getIndex()] = 1.0f;
                    trigger[EntityIndex.TRIGGER_ENTER.getIndex()] = 0.0f;
                    trigger[EntityIndex.TRIGGER_OBJECT_TYPE.getIndex()] = colliders.get(i)[EntityIndex.ENTITY_TYPE_ID.getIndex()];
                    return;
                }

                return;
            }
        }

        if(trigger[EntityIndex.TRIGGER_ENTER.getIndex()] > 0.5f ||trigger[EntityIndex.TRIGGER_STAY.getIndex()] > 0.5f){
            trigger[EntityIndex.TRIGGER_EXIT.getIndex()] = 1.0f;
            trigger[EntityIndex.TRIGGER_ENTER.getIndex()] = 0.0f;
            trigger[EntityIndex.TRIGGER_STAY.getIndex()] = 0.0f;
        }else if(trigger[EntityIndex.TRIGGER_EXIT.getIndex()] >0.5f){
            trigger[EntityIndex.TRIGGER_EXIT.getIndex()] = 0.0f;
            trigger[EntityIndex.TRIGGER_OBJECT_TYPE.getIndex()] = EntityType.NONE.getEntityType();
        }
    }
    //endregion

    //region Lifetime System
    private void lifetimeSystem(float[] entity, double deltaTime){
        entity[EntityIndex.LIFETIME.getIndex()] -= deltaTime;
        if(entity[EntityIndex.LIFETIME.getIndex()] < 0.0f){
            removeEntity(entity);
        }
    }
    //endregion

    //region Checkpoint System
    private void checkpointSystem(float[] entity, double deltaTime){
        if(isBitmaskValid(EntityType.PLAYER.getEntityType(), (int) entity[EntityIndex.TRIGGER_OBJECT_TYPE.getIndex()])){
            System.out.println("Game Won!!! ");
        }
    }
    //endregion


}
