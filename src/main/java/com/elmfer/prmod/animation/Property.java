package com.elmfer.prmod.animation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

public class Property {

    protected final List<Keyframe> keyframeList = new ArrayList<Keyframe>();
    private final String name;

    protected Keyframe currentKeyframe = new Keyframe(0.0D, 0.0D);
    protected Keyframe nextKeyframe = new Keyframe(1.0D, 0.0D);
    protected Timeline host = null;

    private double value;

    private static final Comparator<Keyframe> compare = new Comparator<Keyframe>() {

        @Override
        public int compare(Keyframe o1, Keyframe o2) {

            if (o1.getFracTimeStamp() < o2.getFracTimeStamp())
                return -1;
            if (o1.getFracTimeStamp() > o2.getFracTimeStamp())
                return 1;

            return 0;
        }

    };

    public Property(String name) {

        this.name = name;
    }

    public Property(String name, double value) {

        this.name = name;

        addKeyframes(new Keyframe(0.5D, value));
    }

    public Property(String name, double startingValue, double endingValue) {

        this.name = name;

        addKeyframes(new Keyframe(0.0D, startingValue), new Keyframe(1.0D, endingValue));

    }

    public Property(String name, double startingValue, double endingValue, Function<Double, Double> easingFunc) {

        this.name = name;

        addKeyframes(new Keyframe(0.0D, startingValue, easingFunc), new Keyframe(1.0D, endingValue));
    }

    public Property(String name, double startingValue, double endingValue, Easing easingEnum) {

        this.name = name;

        addKeyframes(new Keyframe(0.0D, startingValue, easingEnum.getFunction()), new Keyframe(1.0D, endingValue));
    }

    public void update(double fracTime) {

        if (keyframeList.size() == 0) {

            return;
        }

        getKeyframes(fracTime);
    }

    public double getValue() {

        if (host != null)
            update(host.getFracTime());
        else
            update(0.0);

        double k2v = (double) nextKeyframe.getValue();
        double k1v = (double) currentKeyframe.getValue();
        double k2t = nextKeyframe.getFracTimeStamp();
        double k1t = currentKeyframe.getFracTimeStamp();

        double partialFracTime = (host.getFracTime() - k1t) / (k2t - k1t);
        value = (double) ((k2v - k1v) * currentKeyframe.getValueShader().apply(partialFracTime) + k1v);

        return value;
    }

    public String getName() {

        return name;
    }

    public void addKeyframes(Keyframe... keyframes) {

        for (Keyframe keyframe : keyframes) {

            validateKeyframe(keyframe);
        }

        sortKeyframes();
    }

    public void addKeyframes(Collection<Keyframe> keyframes) {

        for (Keyframe keyframe : keyframes) {

            validateKeyframe(keyframe);
        }

        sortKeyframes();
    }

    private void getKeyframes(double fracTime) {

        if (!keyframeList.isEmpty()) {

            for (int i = 0; i < keyframeList.size(); i++) {

                if (keyframeList.size() - 1 == i || keyframeList.size() == 1) {

                    currentKeyframe = keyframeList.get(i);
                    nextKeyframe = new Keyframe(2.0D, currentKeyframe.getValue());
                    break;
                } else if (keyframeList.get(i).getFracTimeStamp() <= fracTime
                        && fracTime < keyframeList.get(i + 1).getFracTimeStamp()) {

                    currentKeyframe = keyframeList.get(i);
                    nextKeyframe = keyframeList.get(i + 1);
                    break;
                } else if (fracTime < keyframeList.get(i).getFracTimeStamp()) {

                    currentKeyframe = new Keyframe(0.0D, keyframeList.get(0).getValue());
                    nextKeyframe = keyframeList.get(0);
                    break;
                } else {

                    currentKeyframe = new Keyframe(0.0D, 0.0D);
                    nextKeyframe = new Keyframe(1.0D, 0.0D);
                }

            }
        }
    }

    private void validateKeyframe(Keyframe keyframe) {

        if (keyframeList.size() == 0) {

            keyframeList.add(keyframe);
        } else {

            for (int i = 0; i < keyframeList.size(); i++) {

                if (keyframeList.get(i).getFracTimeStamp() == keyframe.getFracTimeStamp()) {

                    keyframeList.set(i, keyframe);
                } else {

                    keyframeList.add(keyframe);
                }
            }
        }
    }

    private void sortKeyframes() {

        keyframeList.sort(compare);
    }

}
