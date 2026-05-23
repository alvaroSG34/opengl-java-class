package com.graphics.flappybird;

import org.lwjgl.glfw.GLFW;

/**
 * InputManager: Gestiona entrada del usuario (teclado).
 */
public class InputManager {

    private long window;
    private boolean prevSpace;
    private boolean prevR;
    private boolean prevW;

    /**
     * Constructor: asocia la ventana para leer entrada.
     */
    public InputManager(long window) {
        this.window = window;
        this.prevSpace = false;
        this.prevR = false;
        this.prevW = false;
    }

    /**
     * Procesa entrada y devuelve acciones a realizar.
     *
     * @return InputAction con las acciones detectadas
     */
    public InputAction processInput() {
        InputAction action = new InputAction();

        // ESC: cerrar ventana.
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_ESCAPE) == GLFW.GLFW_PRESS) {
            GLFW.glfwSetWindowShouldClose(window, true);
        }

        // SPACE: saltar o empezar juego.
        boolean spaceAhora = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_SPACE) == GLFW.GLFW_PRESS;
        if (spaceAhora && !prevSpace) {
            action.spacePressed = true;
        }
        prevSpace = spaceAhora;

        // R: resetear (solo en game over).
        boolean rAhora = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_R) == GLFW.GLFW_PRESS;
        if (rAhora && !prevR) {
            action.rPressed = true;
        }
        prevR = rAhora;

        // W: saltar jugador 1
        boolean wAhora = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_W) == GLFW.GLFW_PRESS;
        if (wAhora && !prevW) {
            action.wPressed = true;
        }
        prevW = wAhora;

        return action;
    }

    /**
     * Clase auxiliar para representar las acciones detectadas.
     */
    public static class InputAction {

        public boolean spacePressed = false;
        public boolean rPressed = false;
        public boolean wPressed = false;
    }
}
