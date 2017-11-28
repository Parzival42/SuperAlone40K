package superAlone40k.util;

import java.util.ArrayList;

public class TweenEngine {

    public class TweenObject {
        private float[] entity;
        private int indexToTween;
        private float time;
        private float beginning;
        private float end;
        private float duration;
        private boolean finished;
        public float currentValue;
        private Easing.Type easingType;
        private boolean permanent;

        private TweenObject(float[] entity, int indexToTween, float tweenTo, float duration, Easing.Type type, boolean permanent) {
            this.entity = entity;
            this.indexToTween = indexToTween;
            this.beginning = entity[indexToTween];
            this.end = tweenTo;
            this.duration = duration;
            this.currentValue = this.beginning;
            this.time = 0;
            this.finished = false;
            this.easingType = type;
            this.permanent = permanent;
        }

    }

    private static TweenEngine instance;

    ArrayList<TweenObject> tweenObjects = new ArrayList<>();

    public static TweenEngine getInstance(){
        if(instance == null){
            instance = new TweenEngine();
        }

        return instance;
    }

    private TweenEngine(){}

    public void tween(float[] entity, int indexToTween, float end, float duration, Easing.Type type, boolean permanent) {
        TweenObject j = new TweenObject(entity, indexToTween, end , duration, type, permanent);
        tweenObjects.add(j);
    }

    public void update(double deltaTime) {
        for (int i = 0; i < tweenObjects.size(); i++) {
            TweenObject tweenObject = tweenObjects.get(i);

            tweenObject.entity[tweenObject.indexToTween] = Easing.updateEasing(tweenObject.easingType, tweenObject.time, tweenObject.beginning, tweenObject.end, tweenObject.duration);

            if (tweenObject.time + deltaTime > tweenObject.duration) {
                tweenObject.entity[tweenObject.indexToTween] = tweenObject.end;
                tweenObject.time = tweenObject.duration;
                tweenObject.finished = true;
            } else {
                tweenObject.time += deltaTime;
            }

            if (tweenObject.finished) {
                if (!tweenObject.permanent) {
                    tweenObject.entity[tweenObject.indexToTween] = tweenObject.beginning;
                }
                tweenObjects.remove(i);
            }
        }
    }

}
