package com.graphics.flappybird;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Pipe: gestiona las tuberias del juego. Maneja spawn, movimiento, colisiones y
 * puntuacion.
 */
public class Pipe {

    // Parametros de tuberias.
    private static final float TUBERIA_ANCHO = 0.18f;
    private static final float GAP_ALTO = 0.48f;
    private static final float VELOCIDAD_TUBERIAS = 0.62f;
    private static final float TIEMPO_ENTRE_TUBERIAS = 2f;
    private static final float GAP_MIN_CENTRO = -0.45f;
    private static final float GAP_MAX_CENTRO = 0.45f;
    private static final float VELOCIDAD_POR_PUNTO = 0.03f;
    private static final float TIEMPO_ENTRE_MINIMA = 0.9f;
    private static final float VELOCIDAD_MAXIMA = 1.4f;

    private final List<Tuberia> tuberias;
    private final Random random;

    private float timerSpawn;
    private float velocidadActual;

    /**
     * Resultado de la simulacion de un frame para ambos jugadores.
     */
    public static class UpdateResult {

        private final int pointsP1;
        private final int pointsP2;
        private final boolean anyCollisionP1;
        private final boolean anyCollisionP2;

        public UpdateResult(int pointsP1, int pointsP2, boolean anyCollisionP1, boolean anyCollisionP2) {
            this.pointsP1 = pointsP1;
            this.pointsP2 = pointsP2;
            this.anyCollisionP1 = anyCollisionP1;
            this.anyCollisionP2 = anyCollisionP2;
        }

        public int getPointsP1() {
            return pointsP1;
        }

        public int getPointsP2() {
            return pointsP2;
        }

        public boolean isAnyCollisionP1() {
            return anyCollisionP1;
        }

        public boolean isAnyCollisionP2() {
            return anyCollisionP2;
        }
    }

    /**
     * Representa una tuberia en el juego.
     */
    public static class Tuberia {

        public float x;
        public float gapCentroY;
        public boolean puntuada1;
        public boolean puntuada2;

        public Tuberia(float x, float gapCentroY) {
            this.x = x;
            this.gapCentroY = gapCentroY;
            this.puntuada1 = false;
            this.puntuada2 = false;
        }
    }

    /**
     * Segmento rectangular de tuberia listo para renderizar.
     */
    public static class SegmentoRender {

        private final float x;
        private final float y;
        private final float ancho;
        private final float alto;

        public SegmentoRender(float x, float y, float ancho, float alto) {
            this.x = x;
            this.y = y;
            this.ancho = ancho;
            this.alto = alto;
        }

        public float getX() {
            return x;
        }

        public float getY() {
            return y;
        }

        public float getAncho() {
            return ancho;
        }

        public float getAlto() {
            return alto;
        }
    }

    /**
     * Constructor: inicializa el sistema de tuberias.
     */
    public Pipe() {
        this.tuberias = new ArrayList<>();
        this.random = new Random();
        this.timerSpawn = 0.0f;
    }

    public float getVelocidadActual() {
        return velocidadActual;
    }

    /**
     * Simula avance, score y colisiones de un frame para ambos jugadores.
     */
    public UpdateResult step(float dt, Bird bird1, Bird bird2, int puntajeMax) {
        int p1 = 0;
        int p2 = 0;
        boolean colisionP1 = false;
        boolean colisionP2 = false;

        velocidadActual = VELOCIDAD_TUBERIAS + (puntajeMax * VELOCIDAD_POR_PUNTO);
        if (velocidadActual > VELOCIDAD_MAXIMA) {
            velocidadActual = VELOCIDAD_MAXIMA;
        }

        float tiempoEntreTuberiasActual = TIEMPO_ENTRE_TUBERIAS * (VELOCIDAD_TUBERIAS / velocidadActual);
        if (tiempoEntreTuberiasActual < TIEMPO_ENTRE_MINIMA) {
            tiempoEntreTuberiasActual = TIEMPO_ENTRE_MINIMA;
        }

        timerSpawn += dt;
        if (timerSpawn >= tiempoEntreTuberiasActual) {
            timerSpawn = 0.0f;
            spawn();
        }

        Iterator<Tuberia> it = tuberias.iterator();
        while (it.hasNext()) {
            Tuberia t = it.next();

            t.x -= velocidadActual * dt;

            if (t.x + (TUBERIA_ANCHO * 0.5f) < bird1.getX() && !t.puntuada1) {
                t.puntuada1 = true;
                p1++;
            }
            if (t.x + (TUBERIA_ANCHO * 0.5f) < bird2.getX() && !t.puntuada2) {
                t.puntuada2 = true;
                p2++;
            }

            if (!colisionP1 && checkCollisionWithPipe(bird1, t)) {
                colisionP1 = true;
            }
            if (!colisionP2 && checkCollisionWithPipe(bird2, t)) {
                colisionP2 = true;
            }

            if (t.x + (TUBERIA_ANCHO * 0.5f) < -1.3f) {
                it.remove();
            }
        }

        return new UpdateResult(p1, p2, colisionP1, colisionP2);
    }

    /**
     * Crea una nueva tuberia con gap aleatorio.
     */
    private void spawn() {
        float gapCentro = GAP_MIN_CENTRO + random.nextFloat() * (GAP_MAX_CENTRO - GAP_MIN_CENTRO);
        tuberias.add(new Tuberia(1.2f, gapCentro));
    }

    /**
     * Verifica colision entre el pajaro y una tuberia especifica.
     */
    private boolean checkCollisionWithPipe(Bird bird, Tuberia t) {
        float pipeLeft = t.x - (TUBERIA_ANCHO * 0.5f);
        float pipeRight = t.x + (TUBERIA_ANCHO * 0.5f);
        float gapTop = t.gapCentroY + (GAP_ALTO * 0.5f);
        float gapBottom = t.gapCentroY - (GAP_ALTO * 0.5f);

        return BirdGeometry.collidesWithPipe(bird, pipeLeft, pipeRight, gapTop, gapBottom);
    }

    /**
     * Resetea el sistema de tuberias.
     */
    public void reset() {
        tuberias.clear();
        timerSpawn = 0.0f;
    }

    /**
     * Obtiene los rectangulos visibles de las tuberias.
     */
    public List<SegmentoRender> getSegmentosRender() {
        List<SegmentoRender> segmentos = new ArrayList<>();

        for (Tuberia t : tuberias) {
            float gapTop = t.gapCentroY + (GAP_ALTO * 0.5f);
            float gapBottom = t.gapCentroY - (GAP_ALTO * 0.5f);

            float altoSuperior = 1.0f - gapTop;
            if (altoSuperior > 0.0f) {
                float yCentroSup = gapTop + (altoSuperior * 0.5f);
                segmentos.add(new SegmentoRender(t.x, yCentroSup, TUBERIA_ANCHO, altoSuperior));
            }

            float altoInferior = gapBottom + 1.0f;
            if (altoInferior > 0.0f) {
                float yCentroInf = -1.0f + (altoInferior * 0.5f);
                segmentos.add(new SegmentoRender(t.x, yCentroInf, TUBERIA_ANCHO, altoInferior));
            }
        }

        return segmentos;
    }
}
