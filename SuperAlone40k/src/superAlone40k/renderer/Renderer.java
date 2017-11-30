package superAlone40k.renderer;

import superAlone40k.ecs.EntityIndex;
import superAlone40k.ecs.EntityType;
import superAlone40k.util.Entities;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

public class Renderer {
	private static final int PRE_CALCULATED_RAINDROP_HEIGHT = 30;
    private final Color backgroundColor = new Color(42, 57, 76);
    
    // Pre-calculated raindrop
    private final BufferedImage rainDrop;
    
    public Renderer() {
    	rainDrop = createGradientImage(
    			(int) Entities.RAIN_PARTICLE_WIDTH,
    			PRE_CALCULATED_RAINDROP_HEIGHT, 
    			new GradientPaint(0, 0, Entities.RAIN_PARTICLE_COLOR_START, 0, PRE_CALCULATED_RAINDROP_HEIGHT, Entities.RAIN_PARTICLE_COLOR_END));
    }

    public void renderBackground(Graphics2D g) {
        g.setColor(backgroundColor);
        g.fillRect(0,0,1280,720);
    }


    public void renderEntity(Graphics2D g, AffineTransform camera, float[] entity) {
        if((((int) entity[EntityIndex.ENTITY_TYPE_ID.getIndex()]) & EntityType.RAIN_DROP.getEntityType()) == EntityType.RAIN_DROP.getEntityType()){
            renderRainDropEntity(g, camera, entity);
            return;
        }

		g.setColor(new Color(entity[EntityIndex.COLOR_R.getIndex()], entity[EntityIndex.COLOR_G.getIndex()],
				entity[EntityIndex.COLOR_B.getIndex()], entity[EntityIndex.COLOR_A.getIndex()]));

        g.setTransform(camera);
		g.fillRect((int) (entity[EntityIndex.POSITION_X.getIndex()] - entity[EntityIndex.EXTENT_X.getIndex()]),
				(int) (entity[EntityIndex.POSITION_Y.getIndex()] - entity[EntityIndex.EXTENT_Y.getIndex()]),
				(int) (2 * entity[EntityIndex.EXTENT_X.getIndex()]),
				(int) (2 * entity[EntityIndex.EXTENT_Y.getIndex()]));
    }

    private void renderRainDropEntity(Graphics2D g, AffineTransform camera, float[] entity) {
		final double scaleX = entity[EntityIndex.EXTENT_X.getIndex()] * 2.0;
    	final double scaleY = entity[EntityIndex.EXTENT_Y.getIndex()] * 2.0;
    	final double positionX = entity[EntityIndex.POSITION_X.getIndex()] - (scaleX * 0.5);
    	final double positionY = entity[EntityIndex.POSITION_Y.getIndex()] - (scaleY * 0.5);
    	
    	final AffineTransform transformation = AffineTransform.getTranslateInstance(positionX, positionY);
    	transformation.concatenate(AffineTransform.getScaleInstance(1.0, scaleY / rainDrop.getHeight()));
    	
    	g.drawImage(rainDrop, transformation, null);
    }
    
    public static BufferedImage createGradientImage(int width, int height, GradientPaint gradient) {
    	final BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    	final Graphics2D g = image.createGraphics();
    	g.setPaint(gradient);
    	g.fillRect(0, 0, width, height);
    	return image;
    }
}