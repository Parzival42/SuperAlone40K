package superAlone40k.window;

import superAlone40k.ecs.EntityType;
import superAlone40k.ecs.FlattenedEngine;
import superAlone40k.ecs.SystemBitmask;
import superAlone40k.particleSystem.*;
import superAlone40k.renderer.Renderer;
import superAlone40k.util.EntityCreator;
import superAlone40k.util.Vector2;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferStrategy;
import java.util.ArrayList;
import java.util.Random;

public class WindowWithFlattenedECS extends JFrame implements KeyListener {

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
	private static boolean[] keys = new boolean[KeyEvent.KEY_LAST + 1];
	private static boolean[] keysDown = new boolean[KeyEvent.KEY_LAST + 1];
	private static boolean[] keysUp = new boolean[KeyEvent.KEY_LAST + 1];

    //ecs
    private FlattenedEngine engine;

    //rain particles
    private static RainParticleSystem rainParticleSystem;

    //time scale
    private static float timeScale = 1.0f;
    private static float uneasedTimeScale = 1.0f;

    public static void setTimeScale(float newTimeScale){
        uneasedTimeScale = newTimeScale;
    }

    public static RainParticleSystem getRainParticleSystem(){
        return rainParticleSystem;
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

        canvas.createBufferStrategy(2);
        bufferStrategy = canvas.getBufferStrategy();



        renderer = new Renderer();

        engine = new FlattenedEngine();
        rainParticleSystem = new RainParticleSystem(engine, -100,  0, 1280);

        //setUpTestEntities();
        createSampleFloorEntities();
        createSampleBulletEntities();
        engine.addEntity(createPlayerEntity());
        
		// Left top to Bottom left
        engine.addEntity(createScreenBorder(new Vector2(0, 0), new Vector2(0, 1)));
        
        // Bottom left to Bottom right
        engine.addEntity(createScreenBorder(new Vector2(0, height), new Vector2(1, 0)));
        
        // Bottom right to Top right
        engine.addEntity(createScreenBorder(new Vector2(width, height), new Vector2(0, -1)));
        
        // Top right to Top left
        engine.addEntity(createScreenBorder(new Vector2(width, 0), new Vector2(-1, 0)));
        
        engine.addEntity(createLight());
    }

    private void createSampleBulletEntities() {
        Random random = new Random();
        Vector2 extent = new Vector2(15,5);

        for(int i = 0; i < 20; i++){

            float[] bullet = EntityCreator.getInstance()
                    .setEntityTypeID(EntityType.BULLET.getEntityType() | EntityType.BOX_SHADOW.getEntityType())
                    .setSystemMask(SystemBitmask.COLLIDER_SORTING.getSystemMask() | SystemBitmask.MOVEMENT_SYSTEM.getSystemMask())
                    .setPosition(new Vector2(1500 + i*200, random.nextFloat() * 720))
                    .setExtent(extent)
                    .setColor(new Color(1.0f, 1.0f, 1.0f,1.0f))
                    .setAABBExtent(extent)
                    .setCollisionType(1.0f)
                    .setVelocity(new Vector2(-500,0))
                    .setDrag(1.0f)
                    .create();

            engine.addEntity(bullet);
        }
    }

    private float[] createLight() {
        float[] light = EntityCreator.getInstance()
                .setEntityTypeID(EntityType.LIGHT.getEntityType())
                .setSystemMask(SystemBitmask.LIGHT_SYSTEM.getSystemMask())
                .setPosition(new Vector2(-100, 100))
                .create();
        return light;
    }

    private float[] createScreenBorder(Vector2 origin, Vector2 direction) {
        float[] screenBorder = EntityCreator.getInstance()
                .setEntityTypeID(EntityType.SCREEN_BORDER.getEntityType())
                .setBorderOrigin(origin)
                .setBorderDirection(direction)
                .create();

        return screenBorder;
    }

    private float[] createPlayerEntity(){
        Vector2 extent = new Vector2(20,40);

        float[] player = EntityCreator.getInstance()
                .setEntityTypeID(EntityType.BOX_SHADOW.getEntityType() | EntityType.PLAYER.getEntityType())
                .setSystemMask(SystemBitmask.INPUT.getSystemMask() | SystemBitmask.COLLIDER_SORTING.getSystemMask() |SystemBitmask.TRIGGER_SYSTEM.getSystemMask())
                .setPosition(new Vector2(500,650))
                .setExtent(extent)
                .setColor(new Color(218/255.0f, 94/255.0f, 92/255.0f, 1.0f))
                .setAABBExtent(extent)
                .setCollisionType(1.0f)
                .setGravitationInfluence(1.0f)
                .setDrag(0.95f)
                .setTriggerPosition(new Vector2(0, extent.y))
                .setTriggerExtent(new Vector2(10,5))
                .setTriggerCollisionType(0.0f)
                .create();

        return player;
    }

    private void createSampleFloorEntities() {
        Color platformColor = new Color(24/255.0f, 32/255.0f, 44/255.0f, 1.0f);
        Vector2 extent = new Vector2(1500, 15);

        float[] mainFloor = EntityCreator.getInstance()
                .setEntityTypeID(EntityType.BOX_SHADOW.getEntityType())
                .setSystemMask(SystemBitmask.COLLIDER_SORTING.getSystemMask())
                .setPosition(new Vector2(0, canvas.getHeight() -15))
                .setExtent(extent)
                .setColor(platformColor)
                .setAABBExtent(extent)
                .setCollisionType(0.0f)
                .create();

        engine.addEntity(mainFloor);

        Vector2 platformExtent = new Vector2(100,10);

        for(int j = 0; j < 3; j++) {
            float[] platform = EntityCreator.getInstance()
                    .setEntityTypeID(EntityType.BOX_SHADOW.getEntityType())
                    .setSystemMask(SystemBitmask.COLLIDER_SORTING.getSystemMask())
                    .setPosition(new Vector2((175.0f) + j * 450.0f, canvas.getHeight() -305))
                    .setExtent(platformExtent)
                    .setColor(platformColor)
                    .setAABBExtent(platformExtent)
                    .setCollisionType(0.0f)
                    .create();

            engine.addEntity(platform);
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

        rainParticleSystem.emit(75);

        while (running) {
            preUpdateTime = System.nanoTime();
            deltaTime = (preUpdateTime - lastTime) / 1e9;
            lastTime = preUpdateTime;

            timeScale = quadEase(uneasedTimeScale);
            update(deltaTime);


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
        rainParticleSystem.update(deltaTime*timeScale*timeScale);


        if(elapsedTime >= 1.0f){
            setTitle("FPS: "+frames+ "   |   Engine update time (ms): "+ ((System.nanoTime()-preUpdateTime)/1e6) + "   |   Entity count: "+engine.getEntities().size());
            elapsedTime = 0.0f;
            frames = 0;
        }
    }

    private void render(){
        Graphics2D g = beginRenderUpdate();
            //render background
            renderer.renderBackground(g);

            engine.render(g);
            
            //render entities
            ArrayList<float[]> entities = engine.getEntities();
            for(int i = 0; i < entities.size(); i++){
                renderer.renderEntity(g, entities.get(i));
            }

//            renderer.renderTestLight(g);

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

    private float quadEase(float t){
		return t < .5 ? 2 * t * t : -1 + (4 - 2 * t) * t;
    }
}
