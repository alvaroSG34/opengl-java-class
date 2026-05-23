package com.graphics.flappybird;

/**
 * BirdGeometry: fuente unica de geometria visual y de colision del pajaro.
 */
public final class BirdGeometry {

    private static final float BODY_WIDTH = 0.10f;
    private static final float BODY_HEIGHT = 0.10f;

    private static final float BEAK_WIDTH = 0.04f;
    private static final float BEAK_HEIGHT = 0.035f;

    private static final float EYE_WIDTH = 0.035f;
    private static final float EYE_HEIGHT = 0.035f;

    private static final float PUPIL_WIDTH = 0.015f;
    private static final float PUPIL_HEIGHT = 0.015f;

    private static final float[][] TAIL_SEGMENTS = {
        {-0.06f, -0.035f, 0.018f},
        {-0.08f, -0.014f, 0.024f},
        {-0.10f, 0.01f, 0.029f}
    };

    private static final float[][] WING_SEGMENTS = {
        {-0.015f, -0.020f, 0.028f},
        {-0.028f, -0.006f, 0.028f}
    };

    private BirdGeometry() {
    }

    public static float[][] getTailSegments() {
        return TAIL_SEGMENTS;
    }

    public static float[][] getWingSegments() {
        return WING_SEGMENTS;
    }

    public static Rect body(Bird bird) {
        return new Rect(bird.getX(), bird.getY(), BODY_WIDTH, BODY_HEIGHT);
    }

    public static Rect beak(Bird bird) {
        float x = bird.getX() + (BODY_WIDTH * 0.5f) + (BEAK_WIDTH * 0.5f);
        float y = bird.getY() + 0.01f;
        return new Rect(x, y, BEAK_WIDTH, BEAK_HEIGHT);
    }

    public static Rect eye(Bird bird) {
        float x = bird.getX() + (BODY_WIDTH * 0.5f) - (EYE_WIDTH * 0.5f) - 0.01f;
        float y = bird.getY() + 0.02f;
        return new Rect(x, y, EYE_WIDTH, EYE_HEIGHT);
    }

    public static Rect pupil(Bird bird) {
        Rect eyeRect = eye(bird);
        return new Rect(eyeRect.getX(), eyeRect.getY(), PUPIL_WIDTH, PUPIL_HEIGHT);
    }

    public static boolean collidesWithPipe(Bird bird, float pipeLeft, float pipeRight, float gapTop, float gapBottom) {
        if (intersectsPipe(body(bird), pipeLeft, pipeRight, gapTop, gapBottom)) {
            return true;
        }
        if (intersectsPipe(beak(bird), pipeLeft, pipeRight, gapTop, gapBottom)) {
            return true;
        }

        for (float[] seg : TAIL_SEGMENTS) {
            Rect tailPart = tailPart(bird, seg);
            if (intersectsPipe(tailPart, pipeLeft, pipeRight, gapTop, gapBottom)) {
                return true;
            }
        }

        return false;
    }

    private static Rect tailPart(Bird bird, float[] seg) {
        return new Rect(
                bird.getX() + seg[0],
                bird.getY() + seg[1],
                seg[2],
                seg[2]
        );
    }

    private static boolean intersectsPipe(Rect rect, float pipeLeft, float pipeRight, float gapTop, float gapBottom) {
        boolean overlapX = rect.getRight() > pipeLeft && rect.getLeft() < pipeRight;
        if (!overlapX) {
            return false;
        }
        return rect.getTop() > gapTop || rect.getBottom() < gapBottom;
    }

    public static final class Rect {

        private final float x;
        private final float y;
        private final float width;
        private final float height;

        public Rect(float x, float y, float width, float height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        public float getX() {
            return x;
        }

        public float getY() {
            return y;
        }

        public float getWidth() {
            return width;
        }

        public float getHeight() {
            return height;
        }

        public float getLeft() {
            return x - (width * 0.5f);
        }

        public float getRight() {
            return x + (width * 0.5f);
        }

        public float getTop() {
            return y + (height * 0.5f);
        }

        public float getBottom() {
            return y - (height * 0.5f);
        }
    }
}
