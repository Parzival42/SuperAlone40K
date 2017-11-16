package superAlone40k.util;

import java.util.ArrayList;

public class Juicer {

    public class Juice {
        private float time;
        private float beginning;
        private float end;
        private float duration;
        private boolean finished;
        public float currentValue;
        private Easing.Type easingType;

        private Juice (Easing.Type type, float beginning , float end, float duration) {
            this.beginning = beginning;
            this.end = end;
            this.duration = duration;
            this.currentValue = this.beginning;
            this.time = 0;
            this.finished = false;
            this.easingType = type;
        }

    }

    private static Juicer instance;

    ArrayList<Juice> juices = new ArrayList<>();

    public static Juicer getInstance(){
        if(instance == null){
            instance = new Juicer();
        }

        return instance;
    }

    private Juicer(){}

    public Juice addJuice(Easing.Type type, float beginning , float end, float duration) {
        Juice j = new Juice(type, beginning , end, duration);
        juices.add(j);
        return j;
    }

    public void update(double deltaTime) {
        for (int i = 0; i < juices.size(); i++) {
            Juice juice = juices.get(i);

            if (juice.finished) {
                juices.remove(i);
            }

            juice.currentValue = Easing.updateEasing(juice.easingType, juice.time, juice.beginning, juice.end, juice.duration);

            if (juice.time + deltaTime > juice.duration) {
                juice.time = juice.duration;
                juice.finished = true;
            } else {
                juice.time += deltaTime;
            }

        }
    }

}
