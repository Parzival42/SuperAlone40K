package superAlone40k;

import superAlone40k.window.WindowWithFlattenedECS;

public class Main {
	public static final int WIDTH = 1280;
	public static final int HEIGHT = 720;
	
    public static void main(String[] args) {
        WindowWithFlattenedECS window = new WindowWithFlattenedECS("SuperAlone40k", WIDTH, HEIGHT);
        window.start(60);
    }
}