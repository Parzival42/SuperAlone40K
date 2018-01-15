package superAlone40k.util;

import java.awt.event.ActionListener;
import java.util.ArrayList;

public class TweenEngine {

    public class TweenObject {
        private float[] entity;
        private boolean set;
        private boolean delay;
        private int indexToTween;
        private int boundingBoxToTween;
        private float time;
        private float beginning;
        private float end;
        private float duration;
        private boolean finished;
        private Easing.Type easingType;

        private TweenObject nextTweenObject;
        private TweenObject prevTweenObject;
        private TweenObject head;

        private ActionListener actionListener;

        TweenObject() {
            this.head = this;
        }

        private TweenObject (TweenObject head, TweenObject prev) {
            this.head = head;
            this.prevTweenObject = prev;
        }

        public TweenObject tween(float[] entity, int indexToTween, int boundingBoxToTween, float tweenTo, float duration, Easing.Type type) {
            this.set = true;
            this.entity = entity;
            this.indexToTween = indexToTween;
            this.boundingBoxToTween = boundingBoxToTween;
            this.beginning = entity[indexToTween];
            this.end = tweenTo;
            this.duration = duration;
            this.easingType = type;
            nextTweenObject = new TweenObject(this.head, this);
            return nextTweenObject;
        }

        public TweenObject delay(float duration) {
            this.set = true;
            this.delay = true;
            this.duration = duration;
            nextTweenObject = new TweenObject(this.head, this);
            return nextTweenObject;
        }

//        public TweenObject reverse() {
//            return this.tween(prevTweenObject.entity, prevTweenObject.indexToTween, prevTweenObject.boundingBoxToTween, prevTweenObject.beginning, prevTweenObject.duration, prevTweenObject.easingType);
//        }

        public TweenObject notifyTweenFinished(ActionListener actionListener) {
            this.prevTweenObject.actionListener = actionListener;
            return this;
        }

        private void tweenFinished () {
            if (actionListener != null) {
                this.actionListener.actionPerformed(null);
            }
        }

        public void start () {
            TweenEngine.getInstance().tweenObjects.add(this.head);
        }

    }

    private static TweenEngine instance;

    private ArrayList<TweenObject> tweenObjects = new ArrayList<>();

    public static TweenEngine getInstance(){
        if(instance == null){
            instance = new TweenEngine();
        }

        return instance;
    }

    private TweenEngine(){}

    public TweenObject tween(float[] entity, int indexToTween, int boundingBoxToTween, float tweenTo, float duration, Easing.Type type) {
        return new TweenObject().tween(entity, indexToTween, boundingBoxToTween, tweenTo , duration, type);
    }

//    public TweenObject delay (float duration){
//        return new TweenObject().delay(duration);
//    }

    public void update(double deltaTime) {

        for (int i = 0; i < tweenObjects.size(); i++) {
            TweenObject tweenObject = tweenObjects.get(i);

            if (!tweenObject.delay) {
                tweenObject.entity[tweenObject.indexToTween] = Easing.updateEasing(tweenObject.easingType, tweenObject.time, tweenObject.beginning, tweenObject.end, tweenObject.duration);
                if (tweenObject.boundingBoxToTween != -1) {
                    tweenObject.entity[tweenObject.boundingBoxToTween] = tweenObject.entity[tweenObject.indexToTween];
                }
            }

            if (tweenObject.time + deltaTime > tweenObject.duration) {
                if (!tweenObject.delay) {
                    tweenObject.entity[tweenObject.indexToTween] = tweenObject.end;
                }
                tweenObject.time = tweenObject.duration;
                tweenObject.finished = true;
            } else {
                tweenObject.time += deltaTime;
            }

            if (tweenObject.finished) {

                if (!tweenObject.delay) {
                    tweenObject.entity[tweenObject.indexToTween] = tweenObject.end;
                    if (tweenObject.boundingBoxToTween != -1) {
                        tweenObject.entity[tweenObject.boundingBoxToTween] = tweenObject.end;
                    }
                    tweenObject.nextTweenObject.beginning = tweenObject.end;
                }

                if (tweenObject.nextTweenObject.set) {
                    tweenObjects.add(tweenObject.nextTweenObject);
                }


                tweenObject.tweenFinished();
                tweenObjects.remove(i);
            }
        }
    }
}
