package superAlone40k.renderer;

import superAlone40k.Main;
import superAlone40k.ecs.EntityIndex;
import superAlone40k.ecs.EntityType;
import superAlone40k.util.Entities;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;


public class Renderer {
	private static final int PRE_CALCULATED_RAINDROP_HEIGHT = 30;

	//For the new look see: https://tinyurl.com/y9alxmfq

	//Color constants
	public static Color PLAYER_COLOR = hsvToRgb(5,77,95, 100); //new Color(242, 71, 56, 255);
	public static Color BULLET_COLOR = hsvToRgb(0,0,100, 100); //new Color(255, 255, 255, 255);
	public static Color OBSTACLE_COLOR = hsvToRgb(0,0,0, 100); //new Color(0, 6, 13, 255);
	public static Color WATER_COLOR = hsvToRgb(196,100,89, 38); //new Color(0, 136, 204, 102);
	public static Color SCORE_COLOR = hsvToRgb(0,0,100, 5); //new Color(255, 255, 255, 13);
	public static Color BACKGROUND_GRADIENT_LIGHT = hsvToRgb(200,100,48, 100); //new Color(0, 82, 122, 255);
	public static Color BACKGROUND_GRADIENT_DARK = hsvToRgb(213,100,8, 100); //new Color(0, 31, 46, 255);
	public static Color SHADOW_GRADIENT_LIGHT = hsvToRgb(200,100,35, 100); //new Color(0, 60, 89, 255);
	public static Color SHADOW_GRADIENT_DARK = hsvToRgb(213,100,8, 100); //new Color(0, 31, 46, 255);
	public static Color BULLETTRAIL_GRADIENT_LIGHT = hsvToRgb(0,0,100, 0); //new Color(255, 255, 255, 255);
	public static Color BULLETTRAIL_GRADIENT_DARK = hsvToRgb(0,0,100, 70); //new Color(255, 255, 255, 0);
	public static Color RAIN_GRADIENT_LIGHT = hsvToRgb(0,0,100, 10); //new Color(255, 255, 255, 10);
	public static Color RAIN_GRADIENT_DARK = hsvToRgb(0,0,100, 0); //new Color(255, 255, 255, 0);
    
    // Pre-calculated raindrop
    private final BufferedImage rainDrop;

    private final BufferedImage background;
    
    private final AffineTransform camera;
    
    public Renderer(AffineTransform camera) {
    	this.camera = camera;
    	
    	rainDrop = createGradientImage(
    			(int) Entities.RAIN_PARTICLE_WIDTH,
    			PRE_CALCULATED_RAINDROP_HEIGHT, 
    			new GradientPaint(0, 0, Renderer.RAIN_GRADIENT_DARK, 0, PRE_CALCULATED_RAINDROP_HEIGHT, Renderer.RAIN_GRADIENT_LIGHT, true));

    	background = createGradientImage(
				Main.WIDTH,
				Main.HEIGHT,
				new GradientPaint(0, 0, Renderer.SHADOW_GRADIENT_DARK, Main.WIDTH, 0, Renderer.SHADOW_GRADIENT_LIGHT, true));

	}

    public void renderBackground(Graphics2D g) {
    	//g.drawImage(background, 0,0,null);
        /*g.setColor(Color.BLACK);
		g.fillRect(0, 0, 1280, 720);*/
		
		//Vector2 playerPosition = Entities.getPositionFor(Entities.getFirstPlayer()).sub(new Vector2(background.getWidth() * 0.5 - camera.getTranslateX(), background.getHeight() * 0.5));
		
		g.drawImage(background, 0, 0, null);
    }


    public void renderEntity(Graphics2D g, AffineTransform camera, float[] entity) {
        if((((int) entity[EntityIndex.ENTITY_TYPE_ID.getIndex()]) & EntityType.RAIN_DROP.getEntityType()) == EntityType.RAIN_DROP.getEntityType()){
            renderRainDropEntity(g, camera, entity);
            return;
        }

		g.setColor(new Color(entity[EntityIndex.COLOR_R.getIndex()], entity[EntityIndex.COLOR_G.getIndex()],
				entity[EntityIndex.COLOR_B.getIndex()], entity[EntityIndex.COLOR_A.getIndex()]));

        g.setTransform(camera);
		g.fillRoundRect((int) (entity[EntityIndex.POSITION_X.getIndex()] - entity[EntityIndex.EXTENT_X.getIndex()]),
				(int) (entity[EntityIndex.POSITION_Y.getIndex()] - entity[EntityIndex.EXTENT_Y.getIndex()]),
				(int) (2 * entity[EntityIndex.EXTENT_X.getIndex()]),
				(int) (2 * entity[EntityIndex.EXTENT_Y.getIndex()]), 10, 10);
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

    private static Color hsvToRgb(float hue, float saturation, float brightness, float opacity) {
    	Color rgb = Color.getHSBColor(hue / 360.0f, saturation / 100.0f, brightness / 100.0f);
		return new Color(rgb.getRed()/255.0f, rgb.getGreen()/255.0f, rgb.getBlue()/255.0f, opacity/100.0f);
	}
}