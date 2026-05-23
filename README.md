# Flappy Bird en Java con OpenGL

Mini-juego estilo **Flappy Bird** desarrollado en Java usando **LWJGL**, **GLFW** y **OpenGL**.

El proyecto incluye:

- Ventana y renderizado 2D con OpenGL.
- Pajaro controlable por teclado.
- Tuberias con colisiones.
- Puntaje y pantalla de estado.
- Sonidos para salto, punto, muerte y game over.
- Texturas y fuentes incluidas en el proyecto.

## Requisitos

- Java 17 o superior.
- Maven 3.9 o superior.
- Windows o macOS con soporte para OpenGL.

## Compilar

Desde la carpeta donde esta el `pom.xml`:

```bash
mvn compile
```

## Ejecutar

```bash
mvn compile exec:exec -DmainClass=com.graphics.flappybird.AppFlappyBird
```

## Controles

- `W`: saltar / iniciar / reiniciar.
- `SPACE`: saltar / iniciar / reiniciar.
- `R`: reiniciar despues de perder.
- `ESC`: cerrar la ventana.

## Recursos

Los recursos del juego estan en:

- `src/main/resources/textures/`
- `src/main/resources/sfx/`
- `src/main/java/com/graphics/flappybird/`

## Clase principal

La entrada del juego es:

```text
com.graphics.flappybird.AppFlappyBird
```

Si Maven indica que no encuentra el `pom.xml`, ejecuta los comandos desde la carpeta raiz del proyecto.
