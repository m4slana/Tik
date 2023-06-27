import java.util.Scanner;
import java.io.*;

// plansza
abstract class Board {
    protected char[][] board;

    public Board() {
        board = new char[3][3];
        initializeBoard();
    }

    protected void initializeBoard() {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                board[row][col] = '-';
            }
        }
    }

    public abstract void displayBoard();

    public boolean hasPlayerWon(char symbol) {
        return false;
    }

    public abstract boolean isGameOver();

    public abstract boolean isValidMove(int row, int col);

    public abstract void makeMove(int row, int col, char symbol);
}

//kółko i krzyżyk
class TicTacToeBoard extends Board {
    @Override
    public void displayBoard() {
        System.out.println("---------");
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                System.out.print(board[row][col] + " ");
            }
            System.out.println();
        }
        System.out.println("---------");
    }

    @Override
    public boolean hasPlayerWon(char symbol) {
        for (int i = 0; i < 3; i++) {
            if ((board[i][0] == symbol && board[i][1] == symbol && board[i][2] == symbol) ||
                    (board[0][i] == symbol && board[1][i] == symbol && board[2][i] == symbol)) {
                return true;
            }
        }
        return (board[0][0] == symbol && board[1][1] == symbol && board[2][2] == symbol) ||
                (board[0][2] == symbol && board[1][1] == symbol && board[2][0] == symbol);
    }

    @Override
    public boolean isGameOver() {
        return hasPlayerWon('X') || hasPlayerWon('O') || isBoardFull();
    }

    private boolean isBoardFull() {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                if (board[row][col] == '-') {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public boolean isValidMove(int row, int col) {
        return row >= 0 && row < 3 && col >= 0 && col < 3 && board[row][col] == '-';
    }

    @Override
    public void makeMove(int row, int col, char symbol) {
        board[row][col] = symbol;
    }
}

// operacje CRUD)
interface GameDataManagement {
    void saveResult(String result);
    void displayResults();
}

// bazy danych w pliku



class DatabaseManager implements GameDataManagement {
    @Override
    public void saveResult(String result) {
        String filePath = "results.txt";
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath, true))) {
            writer.println(result);
        } catch (IOException e) {
            System.out.println("Błąd zapisu do pliku: " + e.getMessage());
        }
    }

    @Override
    public void displayResults() {
        String filePath = "results.txt";
        System.out.println("Wyniki z bazy danych:");
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            int count = 1;
            while ((line = reader.readLine()) != null) {
                System.out.println("- Wynik " + count + ": " + line);
                count++;
            }
        } catch (IOException e) {
            System.out.println("Błąd odczytu pliku: " + e.getMessage());
        }
    }
}



// gracz
abstract class Player {
    protected char symbol;

    public Player(char symbol) {
        this.symbol = symbol;
    }

    public abstract void makeMove(Board board);
}

// konkuter
class ComputerPlayer extends Player {
    public ComputerPlayer(char symbol) {
        super(symbol);
    }

    @Override
    public void makeMove(Board board) {
        System.out.println("Ruch gracza " + symbol);

        // czy komputer wygra naxt ruch
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                if (board.isValidMove(row, col)) {
                    board.makeMove(row, col, symbol);
                    if (board.hasPlayerWon(symbol)) {
                        return; // wygrana
                    }
                    board.makeMove(row, col, '-'); // cofnij ruch
                }
            }
        }

        // czy gracz wygra next ruch
        char opponentSymbol = (symbol == 'X') ? 'O' : 'X';
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                if (board.isValidMove(row, col)) {
                    board.makeMove(row, col, opponentSymbol);
                    if (board.hasPlayerWon(opponentSymbol)) {
                        board.makeMove(row, col, symbol); // zablokuj
                        return;
                    }
                    board.makeMove(row, col, '-'); // cofnij ruch
                }
            }
        }

        // ruch bez wygranej i bez bloku
        while (true) {
            int row = (int) (Math.random() * 3);
            int col = (int) (Math.random() * 3);
            if (board.isValidMove(row, col)) {
                board.makeMove(row, col, symbol);
                return;
            }
        }
    }
}


// gra w kółko  krzyzyk
class TicTacToeGame {
    private Board board;
    private Scanner scanner;
    private Player player1;
    private Player player2;
    private GameDataManagement dataManager;

    public TicTacToeGame() {
        board = new TicTacToeBoard();
        scanner = new Scanner(System.in);
        player1 = new Player('X') {
            @Override
            public void makeMove(Board board) {
                System.out.println("Gracz X, podaj rząd (0-2):");
                int row = scanner.nextInt();

                System.out.println("Gracz X, podaj kolumnę (0-2):");
                int col = scanner.nextInt();

                if (board.isValidMove(row, col)) {
                    board.makeMove(row, col, symbol);
                } else {
                    System.out.println("Nieprawidłowy ruch. Spróbuj ponownie.");
                    makeMove(board);
                }
            }
        };
        player2 = new ComputerPlayer('O');
        dataManager = new DatabaseManager();
    }

    public void playGame() {
        System.out.println("Gra w kółko i krzyżyk");
        board.displayBoard();

        while (!board.isGameOver()) {
            player1.makeMove(board);
            board.displayBoard();

            if (board.isGameOver()) {
                break;
            }

            player2.makeMove(board);
            board.displayBoard();
        }

        if (board.hasPlayerWon('X')) {
            System.out.println("Gracz X wygrał!");
            dataManager.saveResult("Wygrana gracza X");
        } else if (board.hasPlayerWon('O')) {
            System.out.println("Gracz O wygrał!");
            dataManager.saveResult("Wygrana gracza O");
        } else {
            System.out.println("Remis!");
            dataManager.saveResult("Remis");
        }

        dataManager.displayResults();
    }
}

//główna
public class TicTacToeMain {
    public static void main(String[] args) {
        TicTacToeGame game = new TicTacToeGame();
        game.playGame();
    }
}
