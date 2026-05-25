import javax.swing.*;
import java.awt.*;

// Clase hereda de JPanel para actuar como el lienzo gráfico del laberinto
public class LaberintoGrafico extends JPanel {

    // Atributo que representa el estado del laberinto
    private int[][] laberinto;

    // Atributo define el tamaño en píxeles de cada celda
    private int TAM = 60;

    // Atributo contador global de llamadas recursivas
    private int  llamadas   = 0;

    // Atributo contador global de retrocesos ejecutados en el camino
    private int  retrocesos = 0;

    // Atributo guarda el tiempo inicial en nanosegundos para la medición
    private long inicio;

    // Atributo guarda el tiempo final en nanosegundos de la ejecución
    private long fin;

    // Atributo registra la profundidad máxima de la recursión alcanzada
    private int profundidad = 0;

    // Atributo registra el número de celdas únicas exploradas
    private int nodos       = 0;

    // Atributo Identificador del reto seleccionado
    private int tipoRetoActivo = 0;

    // Método  gestiona los diálogos y lanza la aplicación
    public static void main(String[] args) {

        String[] tamanos = {
                "1 - Matriz 5x5",
                "2 - Matriz 10x10",
                "3 - Matriz 20x20"
        };

        String tamanoElegido = (String) JOptionPane.showInputDialog(
                null,
                "Selecciona el tamaño de la matriz:",
                "Paso 1 - Tamaño del laberinto",
                JOptionPane.PLAIN_MESSAGE,
                null,
                tamanos,
                tamanos[0]
        );

        if (tamanoElegido == null) return;

        int indiceTamano = 0;
        for (int i = 0; i < tamanos.length; i++) {
            if (tamanos[i].equals(tamanoElegido)) { indiceTamano = i; break; }
        }

        String[] retos = {
                "RETO 1 - Medir impacto del tamaño",
                "RETO 2 - Antes (Arriba, Derecha, Abajo, Izquierda)",
                "RETO 2 - Después (Derecha primero)",
                "RETO 3 - Laberinto sin solución",
                "RETO 4 - Visualización avanzada",
                "RETO 5 - Heurística Manhattan"
        };

        String retoElegido = (String) JOptionPane.showInputDialog(
                null,
                "Selecciona el reto a aplicar:",
                "Paso 2 - Tipo de reto",
                JOptionPane.PLAIN_MESSAGE,
                null,
                retos,
                retos[0]
        );

        if (retoElegido == null) return;

        int indiceReto = 0;
        for (int i = 0; i < retos.length; i++) {
            if (retos[i].equals(retoElegido)) { indiceReto = i; break; }
        }

        // Constructor implícito: Inicializa una nueva instancia del lienzo gráfico
        LaberintoGrafico panel = new LaberintoGrafico();
        panel.tipoRetoActivo = indiceReto;

        JFrame ventana = new JFrame("Backtracking  |  " + tamanoElegido + "  |  " + retoElegido);
        ventana.setLayout(new BorderLayout());
        ventana.add(panel, BorderLayout.CENTER);
        ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        if (indiceReto == 3) {
            if (indiceTamano == 0) {
                panel.laberinto = panel.getSinSolucion5x5();
                panel.TAM = 80;
            } else if (indiceTamano == 1) {
                panel.laberinto = panel.getSinSolucion10x10();
                panel.TAM = 50;
            } else {
                panel.laberinto = panel.getSinSolucion20x20();
                panel.TAM = 35;
            }
        } else {
            if (indiceTamano == 0) {
                panel.laberinto = panel.get5x5();
                panel.TAM = 80;
            } else if (indiceTamano == 1) {
                panel.laberinto = panel.get10x10();
                panel.TAM = 50;
            } else {
                panel.laberinto = panel.get20x20();
                panel.TAM = 35;
            }
        }

        ventana.pack();
        ventana.setLocationRelativeTo(null);
        ventana.setVisible(true);

        final int retoFinal = indiceReto;
        new Thread(() -> {

            panel.llamadas    = 0;
            panel.retrocesos  = 0;
            panel.profundidad = 0;
            panel.nodos       = 0;

            panel.inicio = System.nanoTime();
            boolean solucion;

            if (retoFinal == 5) {
                solucion = panel.resolverHeuristico(0, 0);
            } else {
                solucion = panel.resolver(0, 0);
            }

            panel.fin = System.nanoTime();
            panel.repaint();

            String res = (solucion ? "Solución encontrada" : "Sin solución")
                    + "\nLlamadas recursivas : " + panel.llamadas
                    + "\nRetrocesos          : " + panel.retrocesos
                    + "\nTiempo (ms)         : " + String.format("%.3f", (panel.fin - panel.inicio) / 1_000_000.0);

            if (retoFinal == 4) {
                res += "\nProfundidad máxima  : " + panel.profundidad
                        + "\nNodos explorados    : " + panel.nodos;
            }

            System.out.println("=== " + tamanoElegido + " | " + retoElegido + " ===");
            System.out.println(res);

        }).start();
    }

    // Método gráfico de las celdas del laberinto
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (laberinto == null) return;

        int filas = laberinto.length;
        int cols  = laberinto[0].length;

        for (int fila = 0; fila < filas; fila++) {
            for (int col = 0; col < cols; col++) {
                switch (laberinto[fila][col]) {
                    case 0: g.setColor(Color.WHITE);             break;
                    case 1: g.setColor(Color.BLACK);             break;
                    case 2: g.setColor(new Color(30, 120, 255)); break;
                    case 9: g.setColor(new Color(50, 200, 80));  break;
                    case 5: g.setColor(new Color(220, 60, 60));  break;
                }
                g.fillRect(col * TAM, fila * TAM, TAM, TAM);
                g.setColor(Color.GRAY);
                g.drawRect(col * TAM, fila * TAM, TAM, TAM);
            }
        }
    }

    // Método  dimensiones del panel ajustadas al laberinto
    @Override
    public Dimension getPreferredSize() {
        if (laberinto == null) return new Dimension(400, 400);
        return new Dimension(laberinto[0].length * TAM + 2, laberinto.length * TAM);
    }

    // Método  algoritmo en la celda inicial (0,0)
    public boolean resolver(int fila, int col) {
        return resolver(fila, col, 0);
    }

    // Método  búsqueda sistemática por backtracking usando condicionales
    private boolean resolver(int fila, int col, int prof) {
        repaint();
        dormir();

        llamadas++;
        if (prof > profundidad) profundidad = prof;

        if (fila < 0 || col < 0 ||
                fila >= laberinto.length ||
                col >= laberinto[0].length) {
            return false;
        }

        if (laberinto[fila][col] == 1 ||
                laberinto[fila][col] == 9 ||
                laberinto[fila][col] == 5) {
            return false;
        }

        if (laberinto[fila][col] == 2) {
            return true;
        }

        laberinto[fila][col] = 9;
        nodos++;
        repaint();
        dormir();

        if (tipoRetoActivo == 2) {
            if (resolver(fila, col + 1, prof + 1)) return true;
            if (resolver(fila + 1, col, prof + 1)) return true;
            if (resolver(fila - 1, col, prof + 1)) return true;
            if (resolver(fila, col - 1, prof + 1)) return true;
        }
        else {
            if (resolver(fila - 1, col, prof + 1)) return true;
            if (resolver(fila, col + 1, prof + 1)) return true;
            if (resolver(fila + 1, col, prof + 1)) return true;
            if (resolver(fila, col - 1, prof + 1)) return true;
        }

        laberinto[fila][col] = 5;
        retrocesos++;
        repaint();
        dormir();

        return false;
    }

    // Método optimizado busqueda de las coordenadas de la meta
    public boolean resolverHeuristico(int fila, int col) {
        int filaMeta = 0, colMeta = 0;
        for (int f = 0; f < laberinto.length; f++)
            for (int c = 0; c < laberinto[0].length; c++)
                if (laberinto[f][c] == 2) { filaMeta = f; colMeta = c; }

        return resolverHeuristico(fila, col, 0, filaMeta, colMeta);
    }

    // Método que implementa backtracking Heuristica Manhattan
    private boolean resolverHeuristico(int fila, int col, int prof, int filaMeta, int colMeta) {
        repaint();
        dormir();

        llamadas++;
        if (prof > profundidad) profundidad = prof;

        if (fila < 0 || col < 0 ||
                fila >= laberinto.length ||
                col >= laberinto[0].length) {
            return false;
        }

        if (laberinto[fila][col] == 1 ||
                laberinto[fila][col] == 9 ||
                laberinto[fila][col] == 5) {
            return false;
        }

        if (laberinto[fila][col] == 2) {
            return true;
        }

        laberinto[fila][col] = 9;
        nodos++;
        repaint();
        dormir();

        int[][] dirs = {{-1,0},{0,1},{1,0},{0,-1}};
        for (int i = 0; i < dirs.length - 1; i++) {
            for (int j = 0; j < dirs.length - 1 - i; j++) {
                int distA = Math.abs((fila + dirs[j][0])   - filaMeta)
                        + Math.abs((col  + dirs[j][1])   - colMeta);
                int distB = Math.abs((fila + dirs[j+1][0]) - filaMeta)
                        + Math.abs((col  + dirs[j+1][1]) - colMeta);
                if (distA > distB) {
                    int[] tmp  = dirs[j];
                    dirs[j]    = dirs[j+1];
                    dirs[j+1]  = tmp;
                }
            }
        }

        for (int[] dir : dirs) {
            if (resolverHeuristico(fila + dir[0], col + dir[1], prof + 1, filaMeta, colMeta))
                return true;
        }

        laberinto[fila][col] = 5;
        retrocesos++;
        repaint();
        dormir();

        return false;
    }

    // Método que detiene la ejecución unos milisegundos para permitir la animación
    public void dormir() {
        try {
            Thread.sleep(150);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // Método getter que proporciona la matriz con solución de tamaño 5x5
    public int[][] get5x5() {
        return new int[][] {
                {0, 1, 0, 0, 0},
                {0, 1, 0, 1, 0},
                {0, 0, 0, 1, 0},
                {1, 1, 0, 1, 0},
                {0, 0, 0, 0, 2}
        };
    }

    // Método getter que proporciona la matriz con solución de tamaño 10x10
    public int[][] get10x10() {
        return new int[][] {
                {0, 1, 0, 0, 0, 0, 1, 0, 0, 0},
                {0, 1, 0, 1, 1, 0, 1, 0, 1, 0},
                {0, 0, 0, 0, 1, 0, 0, 0, 1, 0},
                {1, 1, 1, 0, 1, 1, 1, 0, 1, 0},
                {0, 0, 0, 0, 0, 0, 1, 0, 0, 0},
                {0, 1, 1, 1, 1, 0, 1, 1, 1, 0},
                {0, 0, 0, 1, 0, 0, 0, 0, 1, 0},
                {1, 1, 0, 1, 0, 1, 1, 0, 1, 0},
                {0, 0, 0, 0, 0, 0, 1, 0, 0, 0},
                {0, 1, 1, 1, 1, 0, 1, 1, 1, 2}
        };
    }

    // Método getter que proporciona la matriz  con solución de tamaño 20x20
    public int[][] get20x20() {
        return new int[][] {
                {0,1,0,0,0,0,1,0,0,0,0,1,0,0,0,0,1,0,0,0},
                {0,1,0,1,1,0,1,0,1,1,0,1,0,1,1,0,1,0,1,0},
                {0,0,0,0,1,0,0,0,0,1,0,0,0,0,1,0,0,0,1,0},
                {1,1,1,0,1,1,1,1,0,1,1,1,1,0,1,1,1,0,1,0},
                {0,0,0,0,0,0,0,1,0,0,0,0,1,0,0,0,1,0,0,0},
                {0,1,1,1,1,0,0,1,1,1,1,0,1,1,1,0,1,1,1,0},
                {0,0,0,1,0,0,1,0,0,0,1,0,0,0,1,0,0,0,1,0},
                {1,1,0,1,0,1,1,1,1,0,1,1,1,0,1,1,1,0,1,0},
                {0,0,0,0,0,0,0,0,1,0,0,0,1,0,0,0,1,0,0,0},
                {0,1,1,1,1,1,1,0,1,1,1,0,1,1,1,0,1,1,1,0},
                {0,0,0,0,0,0,1,0,0,0,1,0,0,0,1,0,0,0,1,0},
                {1,1,1,1,1,0,1,1,1,0,1,1,1,0,1,1,1,0,1,0},
                {0,0,0,0,1,0,0,0,1,0,0,0,1,0,0,0,1,0,0,0},
                {0,1,1,0,1,1,1,0,1,1,1,0,1,1,1,0,1,1,1,0},
                {0,0,1,0,0,0,1,0,0,0,1,0,0,0,1,0,0,0,1,0},
                {1,0,1,1,1,0,1,1,1,0,1,1,1,0,1,1,1,0,1,0},
                {1,0,0,0,1,0,0,0,1,0,0,0,1,0,0,0,1,0,0,0},
                {1,1,1,0,1,1,1,0,1,1,1,0,1,1,1,0,1,1,1,0},
                {0,0,0,0,0,0,1,0,0,0,1,0,0,0,1,0,0,0,1,0},
                {0,1,1,1,1,0,1,1,1,1,1,1,1,1,1,1,1,1,1,2}
        };
    }

    // Método getter que proporciona la matriz 5x5 sin salida para pruebas de estrés recursivo
    public int[][] getSinSolucion5x5() {
        return new int[][] {
                {0, 0, 0, 0, 0},
                {0, 1, 0, 1, 0},
                {1, 1, 1, 1, 1},
                {0, 0, 0, 0, 0},
                {0, 0, 0, 0, 2}
        };
    }

    // Método getter que proporciona la matriz 10x10 sin salida para pruebas de estrés recursivo
    public int[][] getSinSolucion10x10() {
        return new int[][] {
                {0, 1, 0, 0, 0, 0, 1, 0, 0, 0},
                {0, 1, 0, 1, 1, 0, 1, 0, 1, 0},
                {0, 0, 0, 0, 1, 0, 0, 0, 1, 0},
                {0, 0, 0, 0, 0, 0, 1, 0, 0, 0},
                {1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
                {0, 1, 1, 1, 1, 0, 1, 1, 1, 0},
                {0, 0, 0, 1, 0, 0, 0, 0, 1, 0},
                {1, 1, 0, 1, 0, 1, 1, 0, 1, 0},
                {0, 0, 0, 0, 0, 0, 1, 0, 0, 0},
                {0, 1, 1, 1, 1, 0, 1, 1, 1, 2}
        };
    }

    // Método getter que proporciona la matriz 20x20 sin salida para pruebas de estrés recursivo
    public int[][] getSinSolucion20x20() {
        return new int[][] {
                {0,1,0,0,0,0,1,0,0,0,0,1,0,0,0,0,1,0,0,0},
                {0,1,0,1,1,0,1,0,1,1,0,1,0,1,1,0,1,0,1,0},
                {0,0,0,0,1,0,0,0,0,1,0,0,0,0,1,0,0,0,1,0},
                {1,1,1,0,1,1,1,1,0,1,1,1,1,0,1,1,1,0,1,0},
                {0,0,0,0,0,0,0,1,0,0,0,0,1,0,0,0,1,0,0,0},
                {0,1,1,1,1,0,0,1,1,1,1,0,1,1,1,0,1,1,1,0},
                {0,0,0,1,0,0,1,0,0,0,1,0,0,0,1,0,0,0,1,0},
                {1,1,0,1,0,1,1,1,1,0,1,1,1,0,1,1,1,0,1,0},
                {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
                {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
                {0,0,0,0,0,0,1,0,0,0,1,0,0,0,1,0,0,0,1,0},
                {1,1,1,1,1,0,1,1,1,0,1,1,1,0,1,1,1,0,1,0},
                {0,0,0,0,1,0,0,0,1,0,0,0,1,0,0,0,1,0,0,0},
                {0,1,1,0,1,1,1,0,1,1,1,0,1,1,1,0,1,1,1,0},
                {0,0,1,0,0,0,1,0,0,0,1,0,0,0,1,0,0,0,1,0},
                {1,0,1,1,1,0,1,1,1,0,1,1,1,0,1,1,1,0,1,0},
                {1,0,0,0,1,0,0,0,1,0,0,0,1,0,0,0,1,0,0,0},
                {1,1,1,0,1,1,1,0,1,1,1,0,1,1,1,0,1,1,1,0},
                {0,0,0,0,0,0,1,0,0,0,1,0,0,0,1,0,0,0,1,0},
                {0,1,1,1,1,0,1,1,1,1,1,1,1,1,1,1,1,1,1,2}
        };
    }
}