package superAlone40k.util;

public class Easing {

    public enum Type {
        Linear,
        SineEaseIn,
        SineEaseOut,
        SineEaseInOut,
        CubicEaseIn,
        CubicEaseOut,
        CubicEaseInOut,
        BackEaseIn,
        BackEaseOut,
        BackEaseInOut,
        ElasticEaseIn,
        ElasticEaseOut,
        ElasticEaseInOut,
        BounceEaseIn,
        BounceEaseOut,
        BounceEaseInOut
    }

    public static float updateEasing(Type type, float time,float beginning , float end, float duration) {

        float value = 0;

        switch (type) {
            case Linear:
                value = linearEaseInOut(time, beginning , end - beginning, duration);
                break;
            case SineEaseIn:
                value = sineEaseIn(time, beginning , end - beginning, duration);
                break;
            case SineEaseOut:
                value = sineEaseOut(time, beginning , end - beginning, duration);
                break;
            case SineEaseInOut:
                value = sineEaseInOut(time, beginning , end - beginning, duration);
                break;
            case CubicEaseIn:
                value = cubicEaseIn(time, beginning , end - beginning, duration);
                break;
            case CubicEaseOut:
                value = cubicEaseOut(time, beginning , end - beginning, duration);
                break;
            case CubicEaseInOut:
                value = cubicEaseInOut(time, beginning , end - beginning, duration);
                break;
            case BackEaseIn:
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
                break;
            default:
                value = 0;
                break;
        }

        return value;
    }

    // LINEAR

    public static float linearEaseInOut (float time,float beginning , float change, float duration) {
        return change*time/duration + beginning;
    }

    // SINE

    public static float  sineEaseIn(float time,float beginning , float change, float duration) {
        return -change * (float)Math.cos(time/duration * (Math.PI/2)) + change + beginning;
    }

    public static float  sineEaseOut(float time,float beginning , float change, float duration) {
        return change * (float)Math.sin(time/duration * (Math.PI/2)) + beginning;
    }

    public static float  sineEaseInOut(float time,float beginning , float change, float duration) {
        return -change/2 * ((float)Math.cos(Math.PI*time/duration) - 1) + beginning;
    }

    // CUBIC

    public static float cubicEaseIn (float time,float beginning , float change, float duration) {
        return change*(time/=duration)*time*time + beginning;
    }

    public static float cubicEaseOut (float time,float beginning , float change, float duration) {
        return change*((time=time/duration-1)*time*time + 1) + beginning;
    }

    public static float cubicEaseInOut (float time,float beginning , float change, float duration) {
        if ((time/=duration/2) < 1) return change/2*time*time*time + beginning;
        return change/2*((time-=2)*time*time + 2) + beginning;
    }

    // BACK

    public static float  backEaseIn(float time,float beginning , float change, float duration) {
        float s = 1.70158f;
        return change*(time/=duration)*time*((s+1)*time - s) + beginning;
    }

    public static float  backEaseOut(float time,float beginning , float change, float duration) {
        float s = 1.70158f;
        return change*((time=time/duration-1)*time*((s+1)*time + s) + 1) + beginning;
    }

    public static float  backEaseInOut(float time,float beginning , float change, float duration) {
        float s = 1.70158f;
        if ((time/=duration/2) < 1) return change/2*(time*time*(((s*=(1.525f))+1)*time - s)) + beginning;
        return change/2*((time-=2)*time*(((s*=(1.525f))+1)*time + s) + 2) + beginning;
    }

    // ELASTIC

    public static float  elasticEaseIn(float time,float beginning , float change, float duration) {
        if (time==0) return beginning;  if ((time/=duration)==1) return beginning+change;
        float p=duration*.3f;
        float a=change;
        float s=p/4;
        return -(a*(float)Math.pow(2,10*(time-=1)) * (float)Math.sin( (time*duration-s)*(2*(float)Math.PI)/p )) + beginning;
    }

    public static float  elasticEaseOut(float time,float beginning , float change, float duration) {
        if (time==0) return beginning;  if ((time/=duration)==1) return beginning+change;
        float p=duration*.3f;
        float a=change;
        float s=p/4;
        return (a*(float)Math.pow(2,-10*time) * (float)Math.sin( (time*duration-s)*(2*(float)Math.PI)/p ) + change + beginning);
    }

    public static float  elasticEaseInOut(float time,float beginning , float change, float duration) {
        if (time==0) return beginning;  if ((time/=duration/2)==2) return beginning+change;
        float p=duration*(.3f*1.5f);
        float a=change;
        float s=p/4;
        if (time < 1) return -.5f*(a*(float)Math.pow(2,10*(time-=1)) * (float)Math.sin( (time*duration-s)*(2*(float)Math.PI)/p )) + beginning;
        return a*(float)Math.pow(2,-10*(time-=1)) * (float)Math.sin( (time*duration-s)*(2*(float)Math.PI)/p )*.5f + change + beginning;
    }

    // BOUNCE

    public static float  bounceEaseIn(float time,float beginning , float change, float duration) {
        return change - bounceEaseOut (duration-time, 0, change, duration) + beginning;
    }

    public static float  bounceEaseOut(float time,float beginning , float change, float duration) {
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

    public static float  bounceEaseInOut(float time,float beginning , float change, float duration) {
        if (time < duration/2) return bounceEaseIn (time*2, 0, change, duration) * .5f + beginning;
        else return bounceEaseOut (time*2-duration, 0, change, duration) * .5f + change*.5f + beginning;
    }

}
