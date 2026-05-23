package com.graphics.flappybird;

/**
 * Bird: representa al pajaro del juego. Maneja posicion, velocidad y fisica.
 */
public class Bird {

    // Constantes de fisica.
    private static final float GRAVEDAD = -1.9f;
    private static final float IMPULSO_SALTO = 0.85f;
    private static final float VELOCIDAD_MAX_CAIDA = -1.8f;
    private static final float FLAP_BURST_DURATION = 0.36f;
    private static final float FLAP_FREQ = 38.0f;
    private static final float FLAP_REST = 0.0f;

    private static final float BIRD_ANCHO = 0.10f;
    private static final float BIRD_ALTO = 0.10f;

    private static final float TILT_MAX = (float) Math.toRadians(25.0);
    private static final float TILT_K = 0.6f;
    private static final float TILT_SMOOTH = 10.0f;

    private final float birdX;

    private float birdY;
    private float birdVelY;
    private float tilt;

    private float flapTime;
    private float flapBurst;

    /**
     * Constructor: inicializa el pajaro en el centro vertical.
     */
    public Bird(float x) {
        this.birdX = x;
        this.birdY = 0.0f;
        this.birdVelY = 0.0f;
    }

    public Bird() {
        this(-0.45f);
    }

    /**
     * Actualiza la fisica del pajaro (gravedad, velocidad, posicion).
     *
     * @param dt delta time en segundos
     */
    public void update(float dt) {
        birdVelY += GRAVEDAD * dt;

        if (birdVelY < VELOCIDAD_MAX_CAIDA) {
            birdVelY = VELOCIDAD_MAX_CAIDA;
        }

        birdY += birdVelY * dt;

        if (flapBurst > 0.0f) {
            flapTime += dt;
            flapBurst -= dt;
            if (flapBurst < 0.0f) {
                flapBurst = 0.0f;
            }
        } else {
            flapTime = 0.0f;
        }

        float target = birdVelY * TILT_K;
        if (target > TILT_MAX) {
            target = TILT_MAX;
        } else if (target < -TILT_MAX) {
            target = -TILT_MAX;
        }

        float alpha = 1.0f - (float) Math.exp(-TILT_SMOOTH * dt);
        tilt += (target - tilt) * alpha;
    }

    /**
     * El pajaro salta: impulso ascendente.
     */
    public void jump() {
        birdVelY = IMPULSO_SALTO;
        flapTime = 0.0f;
        flapBurst = FLAP_BURST_DURATION;
    }

    /**
     * Resetea el pajaro a posicion inicial.
     */
    public void reset() {
        birdY = 0.0f;
        birdVelY = 0.0f;
        tilt = 0.0f;
        flapTime = 0.0f;
        flapBurst = 0.0f;
    }

    public float getX() {
        return birdX;
    }

    public float getY() {
        return birdY;
    }

    /**
     * Calcula el borde superior del pajaro en NDC.
     */
    public float getTop() {
        return birdY + (BIRD_ALTO * 0.5f);
    }

    /**
     * Calcula el borde inferior del pajaro en NDC.
     */
    public float getBottom() {
        return birdY - (BIRD_ALTO * 0.5f);
    }

    public float getFlap() {
        if (flapBurst <= 0.0f) {
            return FLAP_REST;
        }

        float burstFactor = flapBurst / FLAP_BURST_DURATION;
        float wave = 0.5f + 0.5f * (float) Math.sin(flapTime * FLAP_FREQ);
        return FLAP_REST + burstFactor * (wave - FLAP_REST);
    }

    public float getInclinationRadians() {
        return tilt;
    }
}
