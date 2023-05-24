package mines;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Random;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;


public class Board extends JPanel {
	private static final long serialVersionUID = 6195235521361212179L;
	// Constantes pour les différentes images
	private final int NUM_IMAGES = 13;
    private final int CELL_SIZE = 15;

 // Constantes pour représenter l'état des cellules
    private final int COVER_FOR_CELL = 10;
    private final int MARK_FOR_CELL = 10;
    private final int EMPTY_CELL = 0;
    private final int MINE_CELL = 9;
    private final int COVERED_MINE_CELL = MINE_CELL + COVER_FOR_CELL;
    private final int MARKED_MINE_CELL = COVERED_MINE_CELL + MARK_FOR_CELL;

 // Constantes pour les différentes images à afficher
    private final int DRAW_MINE = 9;
    private final int DRAW_COVER = 10;
    private final int DRAW_MARK = 11;
    private final int DRAW_WRONG_MARK = 12;

    private int[] field;
    private boolean inGame;
    private int mines_left;
    private Image[] img;
    private int mines = 40;
    private int rows = 16;
    private int cols = 16;
    private int all_cells;
    private JLabel statusbar;


    public Board(JLabel statusbar) {

        this.statusbar = statusbar;
        
        // Charger les images
        img = new Image[NUM_IMAGES];

        for (int i = 0; i < NUM_IMAGES; i++) {
			img[i] =
                    (new ImageIcon(getClass().getClassLoader().getResource((i)
            			    + ".gif"))).getImage();
        }

        setDoubleBuffered(true);

        addMouseListener(new MinesAdapter());
        newGame();
    }
    
///////////////////////////////////////////////////////// CBN

    public void newGame() {
        Random random = new Random();
        int current_col;
        int i = 0;
        int position = 0;
        int cell = 0;

        inGame = true;
        mines_left = mines;
        all_cells = rows * cols;
        field = new int[all_cells];
        
     // Initialiser toutes les cellules à l'état "couverte"
        for (i = 0; i < all_cells; i++)
            field[i] = COVER_FOR_CELL;

        statusbar.setText(Integer.toString(mines_left));

        i = 0;
     // Placer les mines aléatoirement sur le terrain
        while (i < mines) {
            position = (int) (all_cells * random.nextDouble());

            if (field[position] != COVERED_MINE_CELL) {
                field[position] = COVERED_MINE_CELL;
                i++;

                current_col = position % cols;

                // Mettre à jour les cellules adjacentes pour indiquer la présence de mines
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        int adjCell = position + dx * cols + dy;

                        if (adjCell >= 0 && adjCell < all_cells &&
                            field[adjCell] != COVERED_MINE_CELL) {
                            field[adjCell] += 1;
                        }
                    }
                }
            }
        }
    }


///////////////////////////////////// CBN
  
    public void find_empty_cells(int j) {
        int current_col = j % cols;
        int cell;

        // Définition des positions relatives des cellules à vérifier
        int[] positions = {
            -cols - 1, -1, cols - 1,  // Haut gauche, Gauche, Bas gauche
            -cols, cols,            // Haut, Bas
            -cols + 1, cols + 1, 1   // Haut droite, Bas droite, Droite
        };

        for (int position : positions) {
            cell = j + position;

            // Vérifier les conditions pour traiter la cellule
            if (cell >= 0 && cell < all_cells && field[cell] > MINE_CELL) {
                field[cell] -= COVER_FOR_CELL;
                if (field[cell] == EMPTY_CELL) {
                    find_empty_cells(cell);
                }
            }
        }
    }

//////////////////////////////////////// Machi CBN
    
//  afficher le terrain de jeu
    public void paint(Graphics g) {

        int cell = 0;
        int uncover = 0;


        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {

                cell = field[(i * cols) + j];

                if (inGame && cell == MINE_CELL)
                    inGame = false;

                if (!inGame) {
                    if (cell == COVERED_MINE_CELL) {
                        cell = DRAW_MINE;
                    } else if (cell == MARKED_MINE_CELL) {
                        cell = DRAW_MARK;
                    } else if (cell > COVERED_MINE_CELL) {
                        cell = DRAW_WRONG_MARK;
                    } else if (cell > MINE_CELL) {
                        cell = DRAW_COVER;
                    }


                } else {
                    if (cell > COVERED_MINE_CELL)
                        cell = DRAW_MARK;
                    else if (cell > MINE_CELL) {
                        cell = DRAW_COVER;
                        uncover++;
                    }
                }

                g.drawImage(img[cell], (j * CELL_SIZE),
                    (i * CELL_SIZE), this);
            }
        }


        if (uncover == 0 && inGame) {
            inGame = false;
            statusbar.setText("Game won");
        } else if (!inGame)
            statusbar.setText("Game lost");
    }



////////////////////////////////// CBN
    class MinesAdapter extends MouseAdapter {
        private int cCol;
        private int cRow;

        public void mousePressed(MouseEvent e) {
            int x = e.getX();
            int y = e.getY();

            cCol = x / CELL_SIZE;
            cRow = y / CELL_SIZE;

            if (!inGame) {
                newGame();
                repaint();
                return;
            }

            if (isWithinBounds(x, y)) {
                if (e.getButton() == MouseEvent.BUTTON3) {
                    handleRightClick();
                } else {
                    handleLeftClick();
                }

                repaint();
            }
        }

        private boolean isWithinBounds(int x, int y) {
            return x < cols * CELL_SIZE && y < rows * CELL_SIZE;
        }

        private void handleRightClick() {
            int cell = field[(cRow * cols) + cCol];
            if (cell > MINE_CELL) {
                if (cell <= COVERED_MINE_CELL) {
                    markCell();
                } else {
                    unmarkCell();
                }
            }
        }

        private void markCell() {
            if (mines_left > 0) {
                field[(cRow * cols) + cCol] += MARK_FOR_CELL;
                mines_left--;
                statusbar.setText(Integer.toString(mines_left));
            } else {
                statusbar.setText("No marks left");
            }
        }

        private void unmarkCell() {
            field[(cRow * cols) + cCol] -= MARK_FOR_CELL;
            mines_left++;
            statusbar.setText(Integer.toString(mines_left));
        }

        private void handleLeftClick() {
            int cell = field[(cRow * cols) + cCol];
            if (cell > COVERED_MINE_CELL) {
                return;
            }

            if (cell > MINE_CELL && cell < MARKED_MINE_CELL) {
                uncoverCell();
            }
        }

        private void uncoverCell() {
            field[(cRow * cols) + cCol] -= COVER_FOR_CELL;
            if (field[(cRow * cols) + cCol] == MINE_CELL) {
                inGame = false;
            }
            if (field[(cRow * cols) + cCol] == EMPTY_CELL) {
                find_empty_cells((cRow * cols) + cCol);
            }
        }
    }



    ///////////////////////////:


}
