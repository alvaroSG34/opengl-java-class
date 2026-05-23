package com.graphics.flappybird;

/**
 * GameState: gestiona el estado del juego.
 */
public class GameState {

    private int puntaje1;
    private int puntaje2;
    private boolean alive1;
    private boolean alive2;
    private boolean started;
    private boolean gameOver;
    private float lastDeathTimeP1;
    private float lastDeathTimeP2;

    private static final float DEATH_BLINK_DURATION = 0.95f;
    private static final float DEATH_BLINK_INTERVAL = 0.10f;

    /**
     * Resultado semantico de aplicar un frame al estado del juego.
     */
    public static class FrameOutcome {

        private final boolean anyDeath;
        private final boolean scored;

        public FrameOutcome(boolean anyDeath, boolean scored) {
            this.anyDeath = anyDeath;
            this.scored = scored;
        }

        public boolean hasAnyDeath() {
            return anyDeath;
        }

        public boolean hasScored() {
            return scored;
        }
    }

    /**
     * Constructor: inicializa el estado del juego.
     */
    public GameState() {
        reset();
    }

    /**
     * Resetea el estado del juego.
     */
    public void reset() {
        puntaje1 = 0;
        puntaje2 = 0;
        started = false;
        alive1 = true;
        alive2 = true;
        gameOver = false;
        lastDeathTimeP1 = -1.0f;
        lastDeathTimeP2 = -1.0f;
    }

    /**
     * Inicia el juego.
     */
    public void start() {
        this.started = true;
        this.gameOver = false;
    }

    public void killPlayer(int playerId) {
        killPlayer(playerId, -1.0f);
    }

    public void killPlayer(int playerId, float nowSeconds) {
        if (playerId == 1) {
            if (alive1 && nowSeconds >= 0.0f) {
                lastDeathTimeP1 = nowSeconds;
            }
            alive1 = false;
        }
        if (playerId == 2) {
            if (alive2 && nowSeconds >= 0.0f) {
                lastDeathTimeP2 = nowSeconds;
            }
            alive2 = false;
        }
        if (!alive1 && !alive2) {
            gameOver = true;
        }
    }

    /**
     * Aplica el resultado del frame para muertes y puntos de ambos jugadores.
     */
    public FrameOutcome applyFrameOutcome(Pipe.UpdateResult updateResult, Bird bird1, Bird bird2, float nowSeconds) {
        boolean p1AliveBefore = alive1;
        boolean p2AliveBefore = alive2;

        if (alive1 && (isOutOfBounds(bird1) || updateResult.isAnyCollisionP1())) {
            killPlayer(1, nowSeconds);
        }

        if (alive2 && (isOutOfBounds(bird2) || updateResult.isAnyCollisionP2())) {
            killPlayer(2, nowSeconds);
        }

        boolean anyDeath = (p1AliveBefore && !alive1) || (p2AliveBefore && !alive2);

        boolean scored = false;
        if (alive1 && updateResult.getPointsP1() > 0) {
            addPoints(1, updateResult.getPointsP1());
            scored = true;
        }
        if (alive2 && updateResult.getPointsP2() > 0) {
            addPoints(2, updateResult.getPointsP2());
            scored = true;
        }

        return new FrameOutcome(anyDeath, scored);
    }

    public boolean isPlayerAlive(int playerId) {
        if (playerId == 1) {
            return alive1;
        }
        if (playerId == 2) {
            return alive2;
        }
        return false;
    }

    public boolean shouldRenderDeadPlayerBlink(int playerId, float nowSeconds) {
        if (isPlayerAlive(playerId)) {
            return true;
        }

        float deathTime = playerId == 1 ? lastDeathTimeP1 : lastDeathTimeP2;
        if (deathTime < 0.0f) {
            return false;
        }

        float elapsed = nowSeconds - deathTime;
        if (elapsed < 0.0f || elapsed > DEATH_BLINK_DURATION) {
            return false;
        }

        int phase = (int) (elapsed / DEATH_BLINK_INTERVAL);
        return phase % 2 == 0;
    }

    /**
     * Suma puntos.
     */
    public void addPoints(int playerId, int points) {
        if (playerId == 1) {
            puntaje1 += points;
        } else if (playerId == 2) {
            puntaje2 += points;
        }
    }

    private boolean isOutOfBounds(Bird bird) {
        return bird.getTop() >= 1.0f || bird.getBottom() <= -1.0f;
    }

    public int getPuntaje1() {
        return puntaje1;
    }

    public int getPuntaje2() {
        return puntaje2;
    }

    public boolean isStarted() {
        return started;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public boolean isAlive1() {
        return alive1;
    }

    public boolean isAlive2() {
        return alive2;
    }

    /**
     * Verifica si el juego esta activo (corriendo).
     */
    public boolean isRunning() {
        return started && (alive1 || alive2);
    }

    /**
     * Retorna mensaje de estado para el titulo de la ventana.
     */
    public String getStatusMessage() {
        if (!started) {
            return " | W o SPACE para empezar";
        } else if (gameOver) {
            return " | GAME OVER - W o SPACE o R para reiniciar";
        } else {
            return "";
        }
    }

    public String buildWindowTitle(float velocidadActual) {
        return "P1: " + getPuntaje1()
                + " | P2: " + getPuntaje2()
                + " | V: " + String.format(java.util.Locale.US, "%.2f", velocidadActual)
                + getStatusMessage();
    }
}
