package com.graphics.flappybird;

import java.io.IOException;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;

/**
 * AppFlappyBird: mini-juego estilo Flappy Bird con OpenGL 2D.
 */
public class AppFlappyBird {

    private static final int ANCHO = 1920;
    private static final int ALTO = 1080;

    private static final float OFFSCREEN_Y = -1.2f;
    private static final float[] P1_COLOR = {0.98f, 0.85f, 0.20f};
    private static final float[] P2_COLOR = {0.18f, 0.70f, 0.25f};

    private long window;
    private Bird bird1;
    private Bird bird2;
    private Pipe pipe;
    private Renderer renderer;
    private InputManager inputManager;
    private GameState gameState;
    private TextRenderer hudTextRenderer;
    private TextRenderer screenTextRenderer;
    private HudScreenRenderer hudScreenRenderer;
    private SoundManager sound;
    private boolean wasGameOver;
    private float worldScroll;

    public void run() {
        init();
        loop();
        cleanup();
    }

    private void init() {
        initWindow();
        initAudio();
        initRenderers();
        initGameObjects();
        updateWindowTitle();
    }

    private void initWindow() {
        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("No se pudo iniciar GLFW");
        }

        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, GLFW.GLFW_TRUE);

        window = GLFW.glfwCreateWindow(ANCHO, ALTO, "Flappy Bird OpenGL", 0, 0);
        if (window == 0) {
            throw new RuntimeException("No se pudo crear la ventana");
        }

        GLFW.glfwMakeContextCurrent(window);
        GLFW.glfwSwapInterval(1);
        GLFW.glfwShowWindow(window);

        GL.createCapabilities();
    }

    private void initAudio() {
        try {
            sound = new SoundManager();
            sound.init();
        } catch (IOException | UnsupportedAudioFileException | LineUnavailableException e) {
            throw new RuntimeException("Error al cargar audio", e);
        }
    }

    private void initRenderers() {
        renderer = new Renderer();
        renderer.init();

        try {
            hudTextRenderer = new TextRenderer();
            hudTextRenderer.init("COMICATE.TTF", 48);
            screenTextRenderer = new TextRenderer();
            screenTextRenderer.init("stocky.ttf", 56);
        } catch (IOException e) {
            throw new RuntimeException("Error al cargar la fuente", e);
        }
    }

    private void initGameObjects() {
        bird1 = new Bird(-0.55f);
        bird2 = new Bird(-0.35f);
        pipe = new Pipe();
        inputManager = new InputManager(window);
        gameState = new GameState();
        hudScreenRenderer = new HudScreenRenderer(renderer, screenTextRenderer, ANCHO, ALTO);
        worldScroll = 0.0f;
    }

    private void loop() {
        float ultimoTiempo = (float) GLFW.glfwGetTime();

        while (!GLFW.glfwWindowShouldClose(window)) {
            float ahora = (float) GLFW.glfwGetTime();
            float dt = ahora - ultimoTiempo;
            ultimoTiempo = ahora;
            if (dt > 0.033f) {
                dt = 0.033f;
            }

            InputManager.InputAction action = inputManager.processInput();
            processGameInput(action);

            if (gameState.isStarted()) {
                updatePlayers(dt);
            }

            if (gameState.isRunning()) {
                runGameplayFrame(dt, ahora);
                updateWindowTitle();
            }

            handleGameOverTransitions();
            render();

            GLFW.glfwSwapBuffers(window);
            GLFW.glfwPollEvents();
        }
    }

    private void runGameplayFrame(float dt, float nowSeconds) {
        int puntajeMaximo = Math.max(gameState.getPuntaje1(), gameState.getPuntaje2());
        Pipe.UpdateResult updateResult = pipe.step(dt, bird1, bird2, puntajeMaximo);
        GameState.FrameOutcome frameOutcome = gameState.applyFrameOutcome(updateResult, bird1, bird2, nowSeconds);
        playFrameAudio(frameOutcome);
        worldScroll += pipe.getVelocidadActual() * dt;
    }

    private void playFrameAudio(GameState.FrameOutcome frameOutcome) {
        if (frameOutcome.hasAnyDeath()) {
            sound.playDeath();
        }
        if (frameOutcome.hasScored()) {
            sound.playPoint();
        }
    }

    private void updatePlayers(float dt) {
        updatePlayerIfVisible(bird1, 1, dt);
        updatePlayerIfVisible(bird2, 2, dt);
    }

    private void updatePlayerIfVisible(Bird bird, int playerId, float dt) {
        if (isPlayerVisible(bird, gameState.isPlayerAlive(playerId))) {
            bird.update(dt);
        }
    }

    private void handleGameOverTransitions() {
        if (gameState.isGameOver() && !wasGameOver) {
            sound.playGameOver();
        }
        wasGameOver = gameState.isGameOver();
    }

    private boolean isPlayerVisible(Bird bird, boolean alive) {
        return alive || bird.getBottom() > OFFSCREEN_Y;
    }

    private void processGameInput(InputManager.InputAction action) {
        if (handleJumpAction(action.wPressed, 1, bird1)) {
            return;
        }

        if (handleJumpAction(action.spacePressed, 2, bird2)) {
            return;
        }

        if (action.rPressed && gameState.isGameOver()) {
            resetGame();
        }
    }

    private boolean handleJumpAction(boolean pressed, int playerId, Bird bird) {
        if (!pressed) {
            return false;
        }

        if (gameState.isGameOver()) {
            resetGame();
            return true;
        }

        if (!gameState.isStarted()) {
            gameState.start();
        }

        if (gameState.isPlayerAlive(playerId)) {
            bird.jump();
            sound.playJump();
        }

        return false;
    }

    private void resetGame() {
        gameState.reset();
        bird1.reset();
        bird2.reset();
        pipe.reset();
        worldScroll = 0.0f;
        updateWindowTitle();
    }

    private void render() {
        float now = (float) GLFW.glfwGetTime();
        renderer.beginRender();
        renderer.setTime(now);
        renderer.setWobble(0.0f);
        renderer.drawBackground(now, worldScroll);

        renderer.drawPipes(pipe.getSegmentosRender());

        if (shouldRenderPlayer(1, bird1, now)) {
            renderer.drawBird(bird1, P1_COLOR);
        }
        if (shouldRenderPlayer(2, bird2, now)) {
            renderer.drawBird(bird2, P2_COLOR);
        }

        if (!gameState.isStarted()) {
            hudScreenRenderer.drawStartScreen();
        } else if (gameState.isGameOver()) {
            hudScreenRenderer.drawGameOverScreen(gameState.getPuntaje1(), gameState.getPuntaje2());
        } else {
            hudTextRenderer.drawHud(gameState.getPuntaje1(), gameState.getPuntaje2(), pipe.getVelocidadActual(), ANCHO, ALTO);
        }

        renderer.endRender();
    }

    private boolean shouldRenderPlayer(int playerId, Bird bird, float nowSeconds) {
        boolean alive = gameState.isPlayerAlive(playerId);
        if (alive) {
            return true;
        }
        if (gameState.shouldRenderDeadPlayerBlink(playerId, nowSeconds)) {
            return true;
        }
        return isPlayerVisible(bird, false);
    }

    private void updateWindowTitle() {
        GLFW.glfwSetWindowTitle(window, gameState.buildWindowTitle(pipe.getVelocidadActual()));
    }

    private void cleanup() {
        renderer.cleanup();
        hudTextRenderer.cleanup();
        screenTextRenderer.cleanup();
        sound.cleanup();
        GLFW.glfwDestroyWindow(window);
        GLFW.glfwTerminate();
    }

    public static void main(String[] args) {
        new AppFlappyBird().run();
    }
}
