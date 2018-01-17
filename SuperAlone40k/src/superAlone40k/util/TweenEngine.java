package superAlone40k.util;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class TweenEngine {

    public interface TweenListener {
        public void tweenAction(float value);
    }

    public class TweenObject {
        private float[] entity;
        private boolean set;
        private boolean delay;
        private int indexToTween;
        private int boundingBoxToTween;
        private float time;
        private float beginning;
        private float value;
        private float end;
        private float duration;
        private boolean finished;
        private Type easingType;

        private TweenObject nextTweenObject;
        private TweenObject prevTweenObject;
        private TweenObject head;

        private TweenListener actionListener;
        private TweenListener updateListener;

        TweenObject() {
            this.head = this;
        }

        private TweenObject (TweenObject head, TweenObject prev) {
            this.head = head;
            this.prevTweenObject = prev;
        }

        public TweenObject tween(float[] entity, int indexToTween, int boundingBoxToTween, float tweenTo, float duration, Type type) {
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

        public TweenObject tween(float tweenFrom, float tweenTo, float duration, Type type) {
            this.set = true;
            this.beginning = tweenFrom;
            this.end = tweenTo;
            this.duration = duration;
            this.easingType = type;
            nextTweenObject = new TweenObject(this.head, this);
            return nextTweenObject;
        }

        /*public TweenObject delay(float duration) {
            this.set = true;
            this.delay = true;
            this.duration = duration;
            nextTweenObject = new TweenObject(this.head, this);
            return nextTweenObject;
        }

        public TweenObject reverse() {
            return this.tween(prevTweenObject.entity, prevTweenObject.indexToTween, prevTweenObject.boundingBoxToTween, prevTweenObject.beginning, prevTweenObject.duration, prevTweenObject.easingType);
        }*/

        public TweenObject notifyTweenFinished(TweenListener actionListener) {
            this.prevTweenObject.actionListener = actionListener;
            return this;
        }

        public TweenObject onTweenUpdated(TweenListener updateListener) {
            this.prevTweenObject.updateListener = updateListener;
            return this;
        }

        private void tweenFinished () {
            if (actionListener != null) {
                this.actionListener.tweenAction(0);
            }
        }

        private void tweenUpdated() {
            if (updateListener != null) {
                this.updateListener.tweenAction(value);
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

    public TweenObject tween(float[] entity, int indexToTween, int boundingBoxToTween, float tweenTo, float duration, Type type) {
        return new TweenObject().tween(entity, indexToTween, boundingBoxToTween, tweenTo , duration, type);
    }

    public TweenObject tween(float tweenFrom, float tweenTo, float duration, Type type) {
        return new TweenObject().tween(tweenFrom, tweenTo , duration, type);
    }

    /*public TweenObject delay (float duration){
        return new TweenObject().delay(duration);
    }*/

    public void update(double deltaTime) {

        for (int i = 0; i < tweenObjects.size(); i++) {
            TweenObject tweenObject = tweenObjects.get(i);

            if (!tweenObject.delay) {
                if (tweenObject.entity == null) {
                    tweenObject.value = updateEasing(tweenObject.easingType, tweenObject.time, tweenObject.beginning, tweenObject.end, tweenObject.duration);
                } else {
                    tweenObject.entity[tweenObject.indexToTween] = updateEasing(tweenObject.easingType, tweenObject.time, tweenObject.beginning, tweenObject.end, tweenObject.duration);
                    if (tweenObject.boundingBoxToTween != -1) {
                        tweenObject.entity[tweenObject.boundingBoxToTween] = tweenObject.entity[tweenObject.indexToTween];
                    }
                }
            }

            if (tweenObject.time + deltaTime > tweenObject.duration) {
                if (!tweenObject.delay) {
                    if (tweenObject.entity == null) {
                        tweenObject.value =  tweenObject.end;
                    } else {
                        tweenObject.entity[tweenObject.indexToTween] = tweenObject.end;
                    }
                }
                tweenObject.time = tweenObject.duration;
                tweenObject.finished = true;
            } else {
                tweenObject.time += deltaTime;
            }

            tweenObject.tweenUpdated();

            if (tweenObject.finished) {

                if (!tweenObject.delay) {

                    if (tweenObject.entity == null) {
                        tweenObject.value =  tweenObject.end;
                    } else {
                        tweenObject.entity[tweenObject.indexToTween] = tweenObject.end;
                        if (tweenObject.boundingBoxToTween != -1) {
                            tweenObject.entity[tweenObject.boundingBoxToTween] = tweenObject.end;
                        }
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

    public enum Type {
        /*Linear,
        SineEaseIn,
        SineEaseOut,*/
        SineEaseInOut,
        /*CubicEaseIn,
        CubicEaseOut,*/
        CubicEaseInOut
        /*BackEaseIn,
        BackEaseOut,
        BackEaseInOut,
        ElasticEaseIn,
        ElasticEaseOut,
        ElasticEaseInOut
        /*BounceEaseIn,
        BounceEaseOut,
        BounceEaseInOut*/
    }

    public static float updateEasing(Type type, float time, float beginning , float end, float duration) {

        float value;

        switch (type) {
            /*case Linear:
                value = linearEaseInOut(time, beginning , end - beginning, duration);
                break;
            case SineEaseIn:
                value = sineEaseIn(time, beginning , end - beginning, duration);
                break;
            case SineEaseOut:
                value = sineEaseOut(time, beginning , end - beginning, duration);
                break;*/
            case SineEaseInOut:
                value = sineEaseInOut(time, beginning , end - beginning, duration);
                break;
            /*case CubicEaseIn:
                value = cubicEaseIn(time, beginning , end - beginning, duration);
                break;
            case CubicEaseOut:
                value = cubicEaseOut(time, beginning , end - beginning, duration);
                break;*/
            case CubicEaseInOut:
                value = cubicEaseInOut(time, beginning , end - beginning, duration);
                break;
            /*case BackEaseIn:
                value = backEaseIn(time, beginning , end - beginning, duration);
                break;
            case BackEaseOut:
                value = backEaseOut(time, beginning , end - beginning, duration);
                break;
            case BackEaseInOut:
                value = backEaseInOut(time, beginning , end - beginning, duration);
                break;
            case ElasticEaseIn:
                value = elasticEaseIn(time, beginning , end - beginning, duration);
                break;
            case ElasticEaseOut:
                value = elasticEaseOut(time, beginning , end - beginning, duration);
                break;
            case ElasticEaseInOut:
                value = elasticEaseInOut(time, beginning , end - beginning, duration);
                break;
            case BounceEaseIn:
                value = bounceEaseIn(time, beginning , end - beginning, duration);
                break;
            case BounceEaseOut:
                value = bounceEaseOut(time, beginning , end - beginning, duration);
                break;
            case BounceEaseInOut:
                value = bounceEaseInOut(time, beginning , end - beginning, duration);
                break;*/
            default:
                value = 0;
                break;
        }

        return value;
    }

    // LINEAR

    /*private static float linearEaseInOut (float time,float beginning , float change, float duration) {
        return change*time/duration + beginning;
    }

    // SINE

    private static float  sineEaseIn(float time,float beginning , float change, float duration) {
        return -change * (float)Math.cos(time/duration * (Math.PI/2)) + change + beginning;
    }

    private static float  sineEaseOut(float time,float beginning , float change, float duration) {
        return change * (float)Math.sin(time/duration * (Math.PI/2)) + beginning;
    }*/

    private static float  sineEaseInOut(float time,float beginning , float change, float duration) {
        return -change/2 * ((float)Math.cos(Math.PI*time/duration) - 1) + beginning;
    }

    // CUBIC

    /*private static float cubicEaseIn (float time,float beginning , float change, float duration) {
        return change*(time/=duration)*time*time + beginning;
    }

    private static float cubicEaseOut (float time,float beginning , float change, float duration) {
        return change*((time=time/duration-1)*time*time + 1) + beginning;
    }*/

    private static float cubicEaseInOut (float time,float beginning , float change, float duration) {
        if ((time/=duration/2) < 1) return change/2*time*time*time + beginning;
        return change/2*((time-=2)*time*time + 2) + beginning;
    }

    // BACK

    /*private static float  backEaseIn(float time,float beginning , float change, float duration) {
        float s = 1.70158f;
        return change*(time/=duration)*time*((s+1)*time - s) + beginning;
    }

    private static float  backEaseOut(float time,float beginning , float change, float duration) {
        float s = 1.70158f;
        return change*((time=time/duration-1)*time*((s+1)*time + s) + 1) + beginning;
    }

    private static float  backEaseInOut(float time,float beginning , float change, float duration) {
        float s = 1.70158f;
        if ((time/=duration/2) < 1) return change/2*(time*time*(((s*=(1.525f))+1)*time - s)) + beginning;
        return change/2*((time-=2)*time*(((s*=(1.525f))+1)*time + s) + 2) + beginning;
    }

    // ELASTIC

    private static float  elasticEaseIn(float time,float beginning , float change, float duration) {
        if (time==0) return beginning;  if ((time/=duration)==1) return beginning+change;
        float p=duration*.3f;
        float a=change;
        float s=p/4;
        return -(a*(float)Math.pow(2,10*(time-=1)) * (float)Math.sin( (time*duration-s)*(2*(float)Math.PI)/p )) + beginning;
    }

    private static float  elasticEaseOut(float time,float beginning , float change, float duration) {
        if (time==0) return beginning;  if ((time/=duration)==1) return beginning+change;
        float p=duration*.3f;
        float a=change;
        float s=p/4;
        return (a*(float)Math.pow(2,-10*time) * (float)Math.sin( (time*duration-s)*(2*(float)Math.PI)/p ) + change + beginning);
    }

    private static float  elasticEaseInOut(float time,float beginning , float change, float duration) {
        if (time==0) return beginning;  if ((time/=duration/2)==2) return beginning+change;
        float p=duration*(.3f*1.5f);
        float a=change;
        float s=p/4;
        if (time < 1) return -.5f*(a*(float)Math.pow(2,10*(time-=1)) * (float)Math.sin( (time*duration-s)*(2*(float)Math.PI)/p )) + beginning;
        return a*(float)Math.pow(2,-10*(time-=1)) * (float)Math.sin( (time*duration-s)*(2*(float)Math.PI)/p )*.5f + change + beginning;
    }

    // BOUNCE

    private static float  bounceEaseIn(float time,float beginning , float change, float duration) {
        return change - bounceEaseOut (duration-time, 0, change, duration) + beginning;
    }

    private static float  bounceEaseOut(float time,float beginning , float change, float duration) {
        if ((time/=duration) < (1/2.75f)) {
            return change*(7.5625f*time*time) + beginning;
        } else if (time < (2/2.75f)) {
            return change*(7.5625f*(time-=(1.5f/2.75f))*time + .75f) + beginning;
        } else if (time < (2.5/2.75)) {
            return change*(7.5625f*(time-=(2.25f/2.75f))*time + .9375f) + beginning;
        } else {
            return change*(7.5625f*(time-=(2.625f/2.75f))*time + .984375f) + beginning;
        }
    }

    private static float  bounceEaseInOut(float time,float beginning , float change, float duration) {
        if (time < duration/2) return bounceEaseIn (time*2, 0, change, duration) * .5f + beginning;
        else return bounceEaseOut (time*2-duration, 0, change, duration) * .5f + change*.5f + beginning;
    }*/

}