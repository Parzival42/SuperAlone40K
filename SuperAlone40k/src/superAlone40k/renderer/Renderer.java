package superAlone40k.renderer;

import superAlone40k.ecs.EntityIndex;
import superAlone40k.ecs.EntityType;

import java.awt.*;


public class Renderer {


    Color backgroundColor = new Color(42,57, 76);


    //rain gradient test
    GradientPaint rainPaint;


    public Renderer(){}

    public void renderBackground(Graphics2D g){
        g.setColor(backgroundColor);
        //g.setBackground(backgroundColor);
        g.fillRect(0,0,1280,720);
    }


    public void renderEntity(Graphics2D g, float[] entity){
        if((((int)entity[EntityIndex.ENTITY_TYPE_ID.getIndex()]) & EntityType.RAIN_DROP.getEntityType()) == EntityType.RAIN_DROP.getEntityType()){
            renderRainDropEntity(g, entity);
            return;
        }
        g.setColor(new Color(entity[6], entity[7], entity[8], entity[9]));
		g.fillRect((int) (entity[2] - entity[4]), (int) (entity[3] - entity[5]), (int) (2 * entity[4]), (int) (2 * entity[5]));
    }

    private void renderRainDropEntity(Graphics2D g, float[] entity){
		rainPaint = new GradientPaint(entity[EntityIndex.POSITION_X.getIndex()],
				entity[EntityIndex.POSITION_Y.getIndex()] + entity[EntityIndex.EXTENT_Y.getIndex()],
				new Color(entity[EntityIndex.COLOR_R.getIndex()], entity[EntityIndex.COLOR_G.getIndex()],
						entity[EntityIndex.COLOR_B.getIndex()], entity[EntityIndex.COLOR_A.getIndex()]),
				entity[EntityIndex.POSITION_X.getIndex()], entity[EntityIndex.POSITION_Y.getIndex()],
				new Color(0, 0, 0, 0));

		
        g.setPaint(rainPaint);
		g.fillRect((int) (entity[2] - entity[4]), (int) (entity[3] - entity[5]), (int) (2 * entity[4]),
				(int) (2 * entity[5]));
    }
}