package javahex.hex;

import javafx.application.Application;

import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Stack;


public class App extends Application {
    //按鍵大小、棋盤大小
    static final int KEY_SIZE = 10, BOARD_SIZE = 11;
    static Stage stage;
    static Group group;
    static Scene scene;
    static Polygon[][] keyBackground = new Polygon[BOARD_SIZE + 2][BOARD_SIZE + 2];
    static MyButton[][] buttons = new MyButton[BOARD_SIZE + 2][BOARD_SIZE + 2];
    static int[][] board = new int[BOARD_SIZE + 2][BOARD_SIZE + 2];
    static int round = 0;
    static Button swapButton, concedeButton;
    static ArrayList<int[]> records = new ArrayList<>();

    public static void reinitialize(){
        buttons = new MyButton[BOARD_SIZE + 2][BOARD_SIZE + 2];
        keyBackground = new Polygon[BOARD_SIZE + 2][BOARD_SIZE + 2];
        board = new int[BOARD_SIZE + 2][BOARD_SIZE + 2];
        round = 0;
        records = new ArrayList<>();
    }
    public static class MyButton extends Button{
        MyButton(Polygon keyBackground){
            //按鈕風格設定
            this.getStylesheets().add(Objects.requireNonNull(getClass().getResource("normalButton.css")).toExternalForm());
            //按鈕形狀設定成六邊形
            this.setShape(keyBackground);
        }
        //觸發事件
        public void enableOnClick(int i, int j) {
            this.setOnAction(actionEvent -> {
                swapButton.setDisable(round != 0);
                System.out.printf("[%d, %d], %s\n", i, j, (round % 2 == 0)? "RED" : "BLUE");
                records.add(new int[]{i, j});
                if (round % 2 == 0) {
                    keyBackground[i][j].setFill(Color.RED);
                    board[i][j] = 1;
                    if (winner("red")) winningAction("Red");
                }
                else {
                    keyBackground[i][j].setFill(Color.BLUE);
                    board[i][j] = -1;
                    if (winner("blue")) winningAction("Blue");
                }

                buttons[i][j].setOnAction(null);
                round++;
                stage.show();
            });
        }
    }

    public static class Hexagon extends Polygon{
        Hexagon(String newColor){
            //按鈕背景的多邊形
            Double[] points = new Double[12];
            for (int q = 0; q < 6; q++){
                //使用極座標參數式畫多邊形
                points[q*2] = Math.cos(Math.PI/3.0*q+Math.PI/6) * KEY_SIZE;
                points[q*2+1] = Math.sin(Math.PI/3.0*q+Math.PI/6) * KEY_SIZE;
            }
            this.getPoints().addAll(points);
            //填色
            switch (newColor) {
                case "gray" -> this.setFill(Color.LIGHTGRAY);
                case "blue" -> this.setFill(Color.BLUE);
                case "red" -> this.setFill(Color.RED);
                default -> this.setFill(Color.WHITE);
            }
        }
    }

    public static void drawDefault(){
        group = new Group();
        for (int i = 0; i < BOARD_SIZE + 2; i++) {
            HBox hBox;
            //每一列是一個hBox
            hBox = new HBox();

            for (int j = 0; j < BOARD_SIZE + 2; j++) {
                //填入邊界六邊形
                StackPane stack = new StackPane();
                String newColor = ((i == 0 || i == BOARD_SIZE + 1) && i == j) ? "white" :
                                (j == 0 || j == BOARD_SIZE + 1) ? "blue" :
                                (i == 0 || i == BOARD_SIZE + 1) ? "red" : "gray";

                keyBackground[i][j] = new Hexagon(newColor);
                buttons[i][j] = new MyButton(keyBackground[i][j]);
                if (newColor.equals("gray")) buttons[i][j].enableOnClick(i, j);

                stack.getChildren().addAll(keyBackground[i][j], buttons[i][j]);
                hBox.getChildren().add(stack);
            }
            hBox.relocate((Math.pow(3d, 1/2) / 2 * KEY_SIZE + 4) * i, 1.5 * KEY_SIZE * i);
            group.getChildren().add(hBox);
        }
        scene = new Scene(group,3, 3);
        stage.setScene(scene);

        swapButton = new Button("SWAP");
        swapButton.relocate(BOARD_SIZE*KEY_SIZE*1.5, BOARD_SIZE*KEY_SIZE*1.9);
        swapButton.setOnAction(actionEvent -> {
            System.out.println("swapped");
            int[] r = records.get(records.size() - 1);
            board[r[0]][r[1]] = 0;
            board[r[1]][r[0]] = -1;
            keyBackground[r[0]][r[1]].setFill(Color.LIGHTGRAY);
            keyBackground[r[1]][r[0]].setFill(Color.BLUE);
            buttons[r[0]][r[1]].enableOnClick(r[0], r[1]);
            buttons[r[1]][r[0]].setOnAction(null);
            swapButton.setDisable(true);
            round++;
        });
        swapButton.setDisable(true);
        group.getChildren().add(swapButton);

        //投降按鈕
        concedeButton = new Button("CONCEDE");
        concedeButton.relocate(BOARD_SIZE*KEY_SIZE*1.5, BOARD_SIZE*KEY_SIZE*2.1);
        concedeButton.setOnAction(actionEvent-> {
            if (round == 0) {
                System.out.println("action failed");
            }
            else {
                switch (round % 2) {
                    case 0 -> System.out.println("Red concede\nBlue Win");
                    case 1 -> System.out.println("Blue concede\nRed Win");
                }
                reinitialize();
                drawDefault();
            }
        });
        group.getChildren().add(concedeButton);

        stage.show();
    }

    //勝負判斷
    public static boolean winner(String player) {
        boolean[][] visited = new boolean[BOARD_SIZE + 2][BOARD_SIZE + 2];
        Stack<int[]> stack = new Stack<>();
        int x = 1, y = 1, color = (player.equals("red")) ? 1 : -1;
        // 將n的處理一般化
        for (int t = 1; t <= BOARD_SIZE; t++) { //原本沒判定邊最後一排
            if (board[x][y] == color) {
                visited[x][y] = true;
                stack.push(new int[]{x, y});
            }
            switch (player) {
                case "red" -> y++;
                case "blue" -> x++;
            }
        }
        if (stack.empty()) return false;
        int[] curr = new int[]{stack.peek()[0], stack.peek()[1]};
        // check for the presence of neighbors and DFS
        do {
            // return true if having reached the bottom row or the rightmost column
            if (curr[(player.equals("red")) ? 0 : 1] == BOARD_SIZE) return true;
            boolean hasNeighbor = false;

            for (int i = 0; i < 6; i++) {
                int A = (i == 0 || i == 1) ? -1 : (i == 2 || i == 5) ? 0 : 1;
                int B = (i == 4 || i == 5) ? -1 : (i == 0 || i == 3) ? 0 : 1;
                if (!visited[curr[0] + A][curr[1] + B] && board[curr[0] + A][curr[1] + B] == color) {
                    hasNeighbor = true;
                    visited[curr[0] + A][curr[1] + B] = true;
                    stack.push(new int[]{curr[0] + A, curr[1] + B});
                    break;
                }
            }
            if (!hasNeighbor) {
                if (stack.size() <= 1) break;
                // move back to the previous coordinate if it has no neighbor
                stack.pop();
            }

            curr = new int[]{stack.peek()[0], stack.peek()[1]};
        } while (!stack.empty());

        return false;
    }

    public static void winningAction(String player) {
        System.out.printf("%s is the winner!\n", player);
        for (int i = 1; i < BOARD_SIZE + 1; i++) {
            for (int j = 1; j < BOARD_SIZE + 1; j++) {
                buttons[i][j].setOnAction(null);
            }
        }
        concedeButton.setOnAction(actionEvent -> {
            System.out.println("Game Over! cannot proceed to concede");
        });
        addRestartButton();
    }


    // add a restart button once there is a winner
    public static void addRestartButton() {
        Button restart = new Button("Restart");
        restart.relocate(BOARD_SIZE*KEY_SIZE*0.4, BOARD_SIZE*KEY_SIZE*1.9);
        restart.setOnAction(actionEvent -> {
            reinitialize();
            drawDefault();
        });
        group.getChildren().add(restart);
        stage.show();
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        stage = primaryStage;
        stage.setTitle("HEX");
        stage.setWidth(370);
        stage.setHeight(300);
        drawDefault();
    }
    public static void main(String[] args) {launch();}
}
