package superAlone40k;

import superAlone40k.window.WindowWithFlattenedECS;

public class Main {

    public static void main(String[] args){

        float[] entity = new float[]{0x4097, 21,213,12313.0f,13123,1231312,1,312,31231312,313};
        float systemMask = 0x4096;
        int sMask = Float.floatToIntBits(systemMask);
        if((Float.floatToRawIntBits(entity[0]) & sMask) == sMask){
            System.out.println("Float bitmask conversion worked!");
        }else{
            System.out.println("Didnt work");
        }


        /*Window window = new Window("test", 1280, 720);
        window.start(120);*/

        WindowWithFlattenedECS window2 = new WindowWithFlattenedECS("test", 1280, 720);
        window2.start(120);



    }
}
