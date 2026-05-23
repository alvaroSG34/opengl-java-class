```mermaid
flowchart TD
    A[AppFlappyBird.main] --> B[new AppFlappyBird().run]
    B --> C[init]

    subgraph INIT[Inicializacion]
        C --> C1[glfwInit]
        C1 --> C2[window hints OpenGL 3.3 core]
        C2 --> C3[glfwCreateWindow 900x700]
        C3 --> C4[glfwMakeContextCurrent]
        C4 --> C5[glfwSwapInterval 1 VSync]
        C5 --> C6[GL.createCapabilities]
        C6 --> C7[renderer.init shaders y quad]
        C7 --> C8[crear bird1 bird2 pipe inputManager gameState]
        C8 --> C9[updateWindowTitle estado inicial]
    end

    C9 --> D[loop]

    subgraph LOOP[Bucle principal por frame]
        D --> E[leer tiempo actual]
        E --> F[dt = ahora - ultimoTiempo]
        F --> G[clamp dt max 0.033]

        G --> H[inputManager.processInput]
        H --> H1[ESC cierra ventana]
        H --> H2[SPACE edge press]
        H --> H3[W edge press]
        H --> H4[R edge press]

        H4 --> I[processGameInput]

        subgraph INPUTMAP[Mapeo de acciones]
            I --> I1{W presionado}
            I1 -->|si gameOver| I2[resetGame]
            I1 -->|si no started| I3[gameState.start]
            I1 -->|si alive1| I4[bird1.jump]

            I --> I5{SPACE presionado}
            I5 -->|si gameOver| I6[resetGame]
            I5 -->|si no started| I7[gameState.start]
            I5 -->|si alive2| I8[bird2.jump]

            I --> I9{R presionado y gameOver}
            I9 -->|si| I10[resetGame]
        end

        I10 --> J[calcular visibilidad pajaros]
        J --> J1[p1Visible = alive1 || bird1.bottom > -1.2]
        J --> J2[p2Visible = alive2 || bird2.bottom > -1.2]

        J2 --> K{gameState.started}
        K -->|si| K1[bird1.update dt si p1Visible]
        K -->|si| K2[bird2.update dt si p2Visible]
        K -->|no| K3[sin update de fisica]

        K1 --> L{gameState.running}
        K2 --> L
        K3 --> L

        subgraph RULES[Reglas activas de juego]
            L -->|si| M[colisiones jugador 1 si alive1]
            M --> M1{top >= 1 o bottom <= -1}
            M1 -->|si| M2[killPlayer 1]
            M --> M3{pipe.checkCollision bird1}
            M3 -->|si| M4[killPlayer 1]

            L -->|si| N[colisiones jugador 2 si alive2]
            N --> N1{top >= 1 o bottom <= -1}
            N1 -->|si| N2[killPlayer 2]
            N --> N3{pipe.checkCollision bird2}
            N3 -->|si| N4[killPlayer 2]

            N4 --> O[pipe.update dt x1 x2]
            M4 --> O

            O --> O1[timerSpawn += dt]
            O1 --> O2{timer >= 1.5}
            O2 -->|si| O3[spawn tuberia x=1.2 gap aleatorio]
            O2 -->|no| O4[continuar]
            O3 --> O5[mover tuberias x -= vel*dt]
            O4 --> O5
            O5 --> O6[puntuar por jugador al pasar tuberia]
            O6 --> O7[remover tuberias fuera de pantalla]
            O7 --> O8[retorna puntos p1 p2]

            O8 --> P[sumar puntos si jugador sigue alive]
            P --> Q[updateWindowTitle puntajes y estado]
        end

        L -->|no| R[omitir reglas de colision y scoring]

        Q --> S[render]
        R --> S

        subgraph RENDER[Pipeline de render]
            S --> S1[renderer.beginRender clear color]
            S1 --> S2[renderer.setTime glfwGetTime]
            S2 --> S3[draw pipes desde getSegmentosRender]
            S3 --> S4[drawBird p1 si visible]
            S4 --> S5[drawBird p2 si visible]
            S5 --> S6[si gameOver draw overlay]
            S6 --> S7[renderer.endRender]
        end

        S7 --> T[glfwSwapBuffers]
        T --> U[glfwPollEvents]
        U --> V{windowShouldClose?}
        V -->|no| E
    end

    V -->|si| W[cleanup]

    subgraph CLEANUP[Liberacion de recursos]
        W --> W1[renderer.cleanup]
        W1 --> W2[glfwDestroyWindow]
        W2 --> W3[glfwTerminate]
    end
```