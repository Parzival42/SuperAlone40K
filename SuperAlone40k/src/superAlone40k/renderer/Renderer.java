package superAlone40k.renderer;

import superAlone40k.Main;
import superAlone40k.ecs.EntityIndex;
import superAlone40k.ecs.EntityType;
import superAlone40k.util.Entities;
import superAlone40k.util.Vector2;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;


public class Renderer {
	private static final int PRE_CALCULATED_RAINDROP_HEIGHT = 30;
    private final Color backgroundColor = new Color(42, 57, 76);
    
    // Pre-calculated raindrop
    private final BufferedImage rainDrop;
    
    private final BufferedImage backgroundRadial;
    
    private final AffineTransform camera;
    
    public Renderer(AffineTransform camera) {
    	this.camera = camera;
    	
    	rainDrop = createGradientImage(
    			(int) Entities.RAIN_PARTICLE_WIDTH,
    			PRE_CALCULATED_RAINDROP_HEIGHT, 
    			new GradientPaint(0, 0, Entities.RAIN_PARTICLE_COLOR_START, 0, PRE_CALCULATED_RAINDROP_HEIGHT, Entities.RAIN_PARTICLE_COLOR_END));
    	
    	final int radialWidth = (int) (Main.WIDTH * 2.5);
    	final int radialHeight = (int) (Main.HEIGHT * 2.5);
    	final RadialGradientPaint radialGradient = new RadialGradientPaint(
    			new Point2D.Float(radialWidth / 2, radialHeight / 2),
    			650f,
    			new float[] {0.2f, 0.9f},
    			new Color[] {new Color(37, 51, 67), new Color(46, 61, 83)});
    	
    	backgroundRadial = createGradientImage(radialWidth, radialHeight, radialGradient);
    }

    public void renderBackground(Graphics2D g) {
        g.setColor(backgroundColor);
		g.fillRect(0, 0, 1280, 720);
		
		Vector2 playerPosition = Entities.getPositionFor(Entities.getFirstPlayer())
				.sub(new Vector2(backgroundRadial.getWidth() * 0.5 - camera.getTranslateX(), backgroundRadial.getHeight() * 0.5));
		
		g.drawImage(backgroundRadial, (int) playerPosition.x, (int) playerPosition.y, null);
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
    
    public static BufferedImage createGradientImage(int width, int height, Paint gradient) {
    	final BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    	final Graphics2D g = image.createGraphics();
    	g.setPaint(gradient);
    	g.fillRect(0, 0, width, height);
    	return image;
    }
}