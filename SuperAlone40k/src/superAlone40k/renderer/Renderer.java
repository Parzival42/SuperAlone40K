package superAlone40k.renderer;

import java.awt.*;
import java.awt.geom.Point2D;

public class Renderer {

     //vignette gradient paint stuff
    float[] distance = {0.0f, 1.0f};
    Color[] colors = {new Color(0.1f,0.1f,0.1f,0.0f), new Color(0.2f, 0.2f, 0.2f, 0.85f)};
    RadialGradientPaint radialGradientPaint = new RadialGradientPaint(new Point2D.Float(640, 360), 500, distance, colors);


    Color backgroundColor = new Color(42,57, 76);


    //rain gradient test
    GradientPaint rainPaint;


    public Renderer(){}

    public void renderBackground(Graphics2D g){
        g.setColor(backgroundColor);
        g.fillRect(0,0,1280,720);
    }

    public void renderTestLight(Graphics2D g){

        //GradientPaint gradientPaint = new GradientPaint(1280,0, new Color(1.0f,1.0f, 1.0f, 0.15f), 100,500, new Color(0.0f,0.0f,0.0f,0.0f));

        Polygon poly = new Polygon();

        poly.addPoint(0, 0);
        poly.addPoint(0,405);
        poly.addPoint(175, 407);

        poly.addPoint(175, 425);

        poly.addPoint(0, 495);
        poly.addPoint(0, 610);



        poly.addPoint(425, 407);
        poly.addPoint(625, 407);

        poly.addPoint(625, 425);


        poly.addPoint(200,692);
        poly.addPoint(580,692);
        //poly.addPoint(1280,0);

        poly.addPoint(875,407);
        poly.addPoint(1075,407);
        poly.addPoint(1075,425);

        poly.addPoint(940,692);
        poly.addPoint(1280,692);

        poly.addPoint(1280,0);

        g.setColor(new Color(1.0f, 1.0f,1.0f, 0.15f));
        g.fill(poly);


    }

    public void renderVignette(Graphics2D g){

        g.setPaint(radialGradientPaint);

        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.65f));
        g.fillRect(0,0,1280,720);

    }

    public void renderEntity(Graphics2D g, float[] entity){
        if(((int) entity[1]) == 24){
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