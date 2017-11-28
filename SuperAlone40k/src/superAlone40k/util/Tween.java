package superAlone40k.util;

import java.util.ArrayList;

public class Tween {

    public class TweenObject {
        private ArrayList<float[]> entities;
        private int entityIndex;
        private int indexToTween;
        private float time;
        private float beginning;
        private float end;
        private float duration;
        private boolean finished;
        public float currentValue;
        private Easing.Type easingType;
        private boolean permanent;

        private TweenObject(ArrayList<float[]> entities, int entityIndex, int indexToTween, float end, float duration, Easing.Type type, boolean permanent) {
            this.entities = entities;
            this.entityIndex = entityIndex;
            this.indexToTween = indexToTween;
            this.beginning = entities.get(entityIndex)[indexToTween];
            this.end = end;
            this.duration = duration;
            this.currentValue = this.beginning;
            this.time = 0;
            this.finished = false;
            this.easingType = type;
            this.permanent = permanent;
        }

    }

    private static Tween instance;

    ArrayList<TweenObject> tweenObjects = new ArrayList<>();

    public static Tween getInstance(){
        if(instance == null){
            instance = new Tween();
        }

        return instance;
    }

    private Tween(){}

    public void add(ArrayList<float[]> entities, int entityIndex, int indexToTween, float end, float duration, Easing.Type type, boolean permanent) {
        TweenObject j = new TweenObject(entities, entityIndex, indexToTween, end , duration, type, permanent);
        tweenObjects.add(j);
    }

    public void update(double deltaTime) {
        for (int i = 0; i < tweenObjects.size(); i++) {
            TweenObject tweenObject = tweenObjects.get(i);

            if (tweenObject.finished) {
                if (!tweenObject.permanent) {
                    tweenObject.entities.get(tweenObject.entityIndex)[tweenObject.indexToTween] = tweenObject.beginning;
                }
                tweenObjects.remove(i);
            }

            tweenObject.entities.get(tweenObject.entityIndex)[tweenObject.indexToTween] = Easing.updateEasing(tweenObject.easingType, tweenObject.time, tweenObject.beginning, tweenObject.end, tweenObject.duration);

            if (tweenObject.time + deltaTime > tweenObject.duration) {
                tweenObject.time = tweenObject.duration;
                tweenObject.finished = true;
            } else {
                tweenObject.time += deltaTime;
            }

        }
    }

}
