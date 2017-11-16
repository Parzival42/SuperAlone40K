package superAlone40k;

import superAlone40k.window.WindowWithFlattenedECS;

public class Main {

    public static void main(String[] args) {
        WindowWithFlattenedECS window = new WindowWithFlattenedECS("SuperAlone40k", 1280, 720);
        window.start(60);
    }

}
