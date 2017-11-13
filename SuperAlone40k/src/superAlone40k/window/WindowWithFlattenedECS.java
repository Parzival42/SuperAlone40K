package superAlone40k.window;

import superAlone40k.ecs.EntityIndex;
import superAlone40k.ecs.FlattenedEngine;
import superAlone40k.particleSystem.*;
import superAlone40k.renderer.Renderer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferStrategy;
import java.util.ArrayList;
import java.util.Random;

public class WindowWithFlattenedECS extends JFrame implements KeyListener{

    //game loop
    private boolean running = true;
    private long targetFrameTimeNano = (long) 1e9 / 60;

    //FPS measurement
    private int frames = 0;
    private double elapsedTime = 0.0f;

    //render
    private Canvas canvas;
    private BufferStrategy bufferStrategy;
    private Renderer renderer;

    //input
    private static boolean[] keys = new boolean[KeyEvent.KEY_LAST+1];

    //ecs
    private FlattenedEngine engine;

    //player entity
    private float[] player;

    //particle system
    private static SimpleParticleSystem simpleParticleSystem;

    //rain particles
    private RainParticleSystem rainParticleSystem;

    //time scale
    private static float timeScale = 1.0f;
    private static float uneasedTimeScale = 1.0f;

    public static void setTimeScale(float newTimeScale){
        uneasedTimeScale = newTimeScale;
    }

    public static SimpleParticleSystem getSimpleParticleSystem(){
        return simpleParticleSystem;
    }

    public WindowWithFlattenedECS(String name, int width, int height){
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setFocusable(true);
        setResizable(false);
        setName(name);

        canvas = new Canvas();
        canvas.setPreferredSize(new Dimension(width, height));
        canvas.addKeyListener(this);
        add(canvas);

        pack();
        setVisible(true);

        canvas.createBufferStrategy(2);
        bufferStrategy = canvas.getBufferStrategy();



        renderer = new Renderer();

        engine = new FlattenedEngine();
        simpleParticleSystem = new SimpleParticleSystem(engine, 450, 500);
        rainParticleSystem = new RainParticleSystem(engine, -100,  0, 1280);

        //setUpTestEntities();
        createSampleFloorEntities();
        createSampleBulletEntities();
        player = createPlayerEntity();
        engine.addEntity(player);
    }

    private void createSampleBulletEntities() {
        Random random = new Random();

        for(int i = 0; i < 20; i++){
            float[] entity = new float[19];

            //mask - input system, collider,
            //00001100
            entity[EntityIndex.SYSTEM_MASK.getIndex()] = 120;

            //pos
            entity[EntityIndex.POSITION_X.getIndex()] = 1500 + i*200;
            entity[EntityIndex.POSITION_Y.getIndex()] = random.nextFloat() * 720;

            //extent
            entity[EntityIndex.EXTENT_X.getIndex()] = 15;
            entity[EntityIndex.EXTENT_Y.getIndex()] = 5;

            //color
            entity[EntityIndex.COLOR_R.getIndex()] = 1.0f;
            entity[EntityIndex.COLOR_G.getIndex()] = 1.0f;
            entity[EntityIndex.COLOR_B.getIndex()] = 1.0f;
            entity[EntityIndex.COLOR_A.getIndex()] = 1.0f;

            //aabb box center
            entity[EntityIndex.AABB_CENTER_X.getIndex()] = 0;
            entity[EntityIndex.AABB_CENTER_Y.getIndex()] = 0;

            //aabb box extent
            entity[EntityIndex.AABB_EXTENT_X.getIndex()] = entity[4];
            entity[EntityIndex.AABB_EXTENT_Y.getIndex()] = entity[5]-5;

            //aabb dynamic vs static
            //player -> dynamic -> 1
            entity[EntityIndex.COLLISION_TYPE.getIndex()] = 1.0f;

            //velocity
            entity[EntityIndex.VELOCITY_X.getIndex()] = -500.0f;
            entity[EntityIndex.VELOCITY_Y.getIndex()] = 0.0f;

            entity[EntityIndex.GRAVITATION_INFLUENCE.getIndex()] = 0.0f;

            //drag
            entity[EntityIndex.DRAG.getIndex()] = 1.0f;

            engine.addEntity(entity);
        }
    }


    private float[] createPlayerEntity(){
        float[] entity = new float[19];

        //mask - input system, collider,
        //00001100
        entity[1] = 12;

        //pos
        entity[2] = 500;
        entity[3] = 650;

        //extent
        entity[4] = 20;
        entity[5] = 40;

        //color
        entity[6] = 218/255.0f;
        entity[7] = 94/255.0f;
        entity[8] = 92/255.0f;
        entity[9] = 1.0f;

        //aabb box center
        entity[10] = 0;
        entity[11] = 0;

        //aabb box extent
        entity[12] = entity[4];
        entity[13] = entity[5];

        //aabb dynamic vs static
        //player -> dynamic -> 1
        entity[14] = 1.0f;

        //velocity
        entity[15] = 0.0f;
        entity[16] = 0.0f;

        //gravity
        entity[17] = 1.0f;

        //drag
        entity[18] = 0.975f;


        return entity;
    }

    private void createSampleFloorEntities() {

        float[] entity = new float[15];

        //mask - static collider
        //00001000
        entity[1] = 8;

        //pos
        entity[2] = 0.0f;
        entity[3] = canvas.getHeight() - 15.0f;

        //extent
        entity[4] = 1500.0f;
        entity[5] = 15.0f;

        //color
        entity[6] = 24/255.0f;
        entity[7] = 32/255.0f;
        entity[8] = 44/255.0f;
        entity[9] = 1.0f;


        //aabb box center
        entity[10] = 0;
        entity[11] = 0;

        //aabb box extent
        entity[12] = entity[4];
        entity[13] = entity[5];

        //aabb dynamic vs static
        //platform -> static -> 0
        entity[14] = 0.0f;


        engine.addEntity(entity);


        for(int j = 0; j < 3; j++){
            float[] entityPlatform = new float[15];

            //mask - static collider
            //00001000
            entityPlatform[1] = 8;

            //pos
            entityPlatform[2] = (75.0f) + j*450.0f;
            entityPlatform[3] = canvas.getHeight() - 305.0f;

            //extent
            entityPlatform[4] = 100.0f;
            entityPlatform[5] = 10.0f;

            //color
            entityPlatform[6] = 24/255.0f;
            entityPlatform[7] = 32/255.0f;
            entityPlatform[8] = 44/255.0f;
            entityPlatform[9] = 1.0f;

            //aabb box center
            entityPlatform[10] = 0;
            entityPlatform[11] = 0;

            //aabb box extent
            entityPlatform[12] = entityPlatform[4];
            entityPlatform[13] = entityPlatform[5];

            //aabb dynamic vs static
            //platform -> static -> 0
            entityPlatform[14] = 0.0f;

            engine.addEntity(entityPlatform);
        }



    }

    private void setUpTestEntities(){
        for(int i = 1; i < 11; i++) {
            float[] entity = new float[10];

            //mask
            //00000011
            entity[1] = 3;

            //pos
            entity[2] = 1000.0f;
            entity[3] = i*40.0f;

            //extent
            entity[4] = 5.0f;
            entity[5] = 5.0f;

            //color
            entity[6] = 0.5f;
            entity[7] = 0.8f;
            entity[8] = 1.0f;
            entity[9] = 1.0f;


            engine.addEntity(entity);
        }
    }



    public void start(int targetFrameRate){
        targetFrameTimeNano = (long) 1e9 / targetFrameRate;
        loop();
    }

    public void start(){
        start(60);
    }

    private void loop() {
        long lastTime = System.nanoTime();
        long preUpdateTime;
        long postUpdateTime;
        long sleepTime;

        double deltaTime;

        simpleParticleSystem.emit(50,5);
        rainParticleSystem.emit(100);

        while (running) {
            preUpdateTime = System.nanoTime();
            deltaTime = (preUpdateTime - lastTime) / 1e9;
            lastTime = preUpdateTime;

            timeScale = quartEase(uneasedTimeScale);
            update(deltaTime);
            //simpleParticleSystem.update(deltaTime);
            rainParticleSystem.update(deltaTime*timeScale);

            render();

            postUpdateTime = System.nanoTime();
            sleepTime = targetFrameTimeNano - (postUpdateTime - preUpdateTime);
            if (sleepTime <= 0) continue;
            try {
                Thread.sleep((long) (sleepTime / 1e6), (int) (sleepTime % 1e6));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public void stop(){
        running = false;
    }

    private void update(double deltaTime){

        //FPS
        frames++;
        elapsedTime += deltaTime;

        //engine update
        long preUpdateTime = System.nanoTime();
        engine.update(deltaTime, timeScale);


        if(elapsedTime>=1.0f){
            System.out.println("FPS: "+frames);
            elapsedTime = 0.0f;
            frames = 0;

            System.out.println("Engine update time in nanoseconds: "+ (System.nanoTime()-preUpdateTime));
        }

        System.out.println("Entities: "+engine.getEntities().size());
    }

    private void render(){
        Graphics2D g = beginRenderUpdate();

            //render background
            renderer.renderBackground(g);

            //render entities
            ArrayList<float[]> entities = engine.getEntities();
            for(int i = 0; i < entities.size(); i++){
                renderer.renderEntity(g, entities.get(i));
            }

            renderer.renderTestLight(g);

            //renderer.renderVignette(g);

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
        //pff
    }

    @Override
    public void keyPressed(KeyEvent e) {
        keys[e.getKeyCode()] = true;
    }

    @Override
    public void keyReleased(KeyEvent e) {
        keys[e.getKeyCode()] = false;
    }

    private float quartEase(float t){
        //return t<.5 ? 8*t*t*t*t : 1-8*(--t)*t*t*t;
        return t<.5 ? 2*t*t : -1+(4-2*t)*t;
        //return t;
    }
}
