package superAlone40k.window;

import superAlone40k.ecs.FlattenedEngine;
import superAlone40k.renderer.Renderer;
import superAlone40k.util.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferStrategy;
import java.util.ArrayList;

@SuppressWarnings("serial")
public class WindowWithFlattenedECS extends JFrame implements KeyListener {

    //game loop
    private boolean running = true;
    private long targetFrameTimeNano = (long) 1e9 / 60;

    //measurement
    private int frames = 0;
    private double elapsedTime = 0.0d;
    private double engineUpdateTime = 0.0d;
    private double renderTime;

    //render
    private Canvas canvas;
    private BufferStrategy bufferStrategy;
    private Renderer renderer;

    //input
	private static boolean[] keys = new boolean[KeyEvent.KEY_LAST + 1];

    //ecs
    private FlattenedEngine engine;
    private Level level;

    //time scale
    private static float timeScale = 1.0f;
    private static float uneasedTimeScale = 1.0f;


    public static void setTimeScale(float newTimeScale){
        uneasedTimeScale = newTimeScale;
    }

    public WindowWithFlattenedECS(String name, int width, int height){
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setFocusable(true);
        setResizable(false);
        setTitle(name);

        canvas = new Canvas();
        canvas.setPreferredSize(new Dimension(width, height));
        canvas.addKeyListener(this);
        add(canvas);

        pack();
        setVisible(true);
        setLocationRelativeTo(null);
        canvas.requestFocus();

        canvas.createBufferStrategy(2);
        bufferStrategy = canvas.getBufferStrategy();

        engine = new FlattenedEngine();
        renderer = new Renderer(engine.getCamera());
        level = new Level(engine, canvas);
    }



    public void start(int targetFrameRate){
        targetFrameTimeNano = (long) 1e9 / targetFrameRate;
        loop();
    }

    private void loop() {
        long lastTime = System.nanoTime();
        long preUpdateTime;
        long postUpdateTime;
        long sleepTime;

        double deltaTime;

        while (running) {
            preUpdateTime = System.nanoTime();
            deltaTime = (preUpdateTime - lastTime) / 1e9;
            lastTime = preUpdateTime;

            //FPS
            frames++;
            elapsedTime += deltaTime;

            timeScale = quadEase(uneasedTimeScale);

            //update
            long updateStartTime = System.nanoTime();
            update(deltaTime);
            engineUpdateTime = ((System.nanoTime()-updateStartTime)/1e6);

            //render
            updateStartTime = System.nanoTime();
            render();
            renderTime = ((System.nanoTime()-updateStartTime)/1e6);

            //measurement
            if(elapsedTime >= 1.0f){
                setTitle("FPS: "+frames+"   |   Engine Update Time: "+engineUpdateTime+"   |   Render time: "+renderTime+ "   |   Entity count: "+engine.getEntities().size());
                engineUpdateTime = ((System.nanoTime()-preUpdateTime)/1e6);
                elapsedTime = 0.0f;
                frames = 0;
            }

            postUpdateTime = System.nanoTime();
            sleepTime = targetFrameTimeNano - (postUpdateTime - preUpdateTime);
            if (sleepTime <= 0) continue;
            try {
                Thread.sleep((long) (sleepTime / 1e6), (int) (sleepTime % 1e6));
                //Thread.yield();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public void stop(){
        running = false;
    }

    private void update(double deltaTime){
        level.update(engine, engine.getCamera(), deltaTime);
        engine.update(deltaTime, timeScale);

        TweenEngine.getInstance().update(deltaTime);
    }

    private void render(){
        Graphics2D g = beginRenderUpdate();
            //render background
            renderer.renderBackground(g);

            engine.render(g);
            
            //render entities
            ArrayList<float[]> entities = engine.getEntities();
            for(int i = 0; i < entities.size(); i++){
                renderer.renderEntity(g, engine.getCamera(), entities.get(i));
            }

        endRenderUpdate(g);
    }


    private Graphics2D beginRenderUpdate(){
        Graphics2D g = (Graphics2D) bufferStrategy.getDrawGraphics();
        g.setColor(Color.DARK_GRAY);
        g.fillRect(0,0, canvas.getWidth(), canvas.getHeight());
        g.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        return g;
    }

    private void endRenderUpdate(Graphics2D g){
        g.dispose();
        bufferStrategy.show();
    }

    //----INPUT---

    public static boolean isKeyPressed(int key){
        return keys[key];
    }

    @Override
    public void keyTyped(KeyEvent e) {
    	e.consume();
        //pff
    }

    @Override
    public void keyPressed(KeyEvent e) {
        keys[e.getKeyCode()] = true;
        e.consume();
    }

    @Override
    public void keyReleased(KeyEvent e) {
        keys[e.getKeyCode()] = false;
        e.consume();
    }

    private float quadEase(float t){
		return t < .5 ? 2 * t * t : -1 + (4 - 2 * t) * t;
    }
}
