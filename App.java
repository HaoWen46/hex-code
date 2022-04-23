package javahex.hex;

import javafx.application.Application;

import javafx.event.ActionEvent;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;


public class App extends Application {
    //按鍵大小、棋盤大小
    static final int KEY_SIZE = 10, BOARD_SIZE = 11;
    static Stage stage;
    static Group g;
    static Scene scene;
    static MyButton[][] buttons = new MyButton[BOARD_SIZE + 2][BOARD_SIZE + 2];
    static int[][] board = new int[BOARD_SIZE][BOARD_SIZE];
    static Button swapButton;
    static int round = 0;
    static int[] pos = new int[2];
    static Polygon[][] keyBackground = new Polygon[BOARD_SIZE + 2][BOARD_SIZE + 2];

    public static void reinitialize(){
        buttons = new MyButton[BOARD_SIZE + 2][BOARD_SIZE + 2];
        keyBackground = new Polygon[BOARD_SIZE + 2][BOARD_SIZE + 2];
        round = 0;
        pos = new int[2];
    }
    public static class MyButton extends Button{
        MyButton(Polygon keyBackground){
            //按鈕風格設定
            this.getStylesheets().add(getClass().getResource("normalButton.css").toExternalForm());
            //按鈕形狀設定成六邊形
            this.setShape(keyBackground);
        }
        //觸發事件
        public void enableOnClick(int i, int j) {
            this.setOnAction((ActionEvent e) -> {
                swapButton.setDisable(round != 0);
                String c = (round % 2 == 0)? "RED" : "BLUE";
                System.out.printf("[%d, %d], %s\n", i, j, c);
                pos[0] = i;
                pos[1] = j;
                switch (round % 2) {
                    case 0 -> {
                        keyBackground[i][j].setFill(Color.RED);
                        board[i - 1][j - 1] = 1;
                    }
                    case 1 -> {
                        keyBackground[i][j].setFill(Color.BLUE);
                        board[i - 1][j - 1] = -1;
                    }
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
                double x = Math.cos(Math.PI/3.0*q+Math.PI/6) * KEY_SIZE;
                double y = Math.sin(Math.PI/3.0*q+Math.PI/6) * KEY_SIZE;
                points[q*2] = x ;
                points[q*2+1] = y ;
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

    public void drawDefault(){
        if (round == 0) {
            g = new Group();
            for (int i = 0; i < BOARD_SIZE + 2; i++) {
                HBox hBox;
                //每一列是一個hbox
                hBox = new HBox();

                for (int j = 0; j < BOARD_SIZE + 2; j++) {
                    //填入邊界六邊形
                    StackPane stack = new StackPane();
                    if (i == 0 && j == 0 || i == BOARD_SIZE + 1 && j == BOARD_SIZE + 1) {
                        keyBackground[i][j] = new Hexagon("white");
                        buttons[i][j] = new MyButton(keyBackground[i][j]);
                    } else if (j == 0 || j == BOARD_SIZE + 1) {
                        keyBackground[i][j] = new Hexagon("blue");
                        buttons[i][j] = new MyButton(keyBackground[i][j]);
                    } else if (i == 0 || i == BOARD_SIZE + 1) {
                        keyBackground[i][j] = new Hexagon("red");
                        buttons[i][j] = new MyButton(keyBackground[i][j]);
                    } else {
                        keyBackground[i][j] = new Hexagon("gray");
                        buttons[i][j] = new MyButton(keyBackground[i][j]);
                        buttons[i][j].enableOnClick(i, j);
                    }
                    stack.getChildren().addAll(keyBackground[i][j], buttons[i][j]);
                    hBox.getChildren().add(stack);
                }
                hBox.relocate(Math.pow(3.0, 1 / 2) / 2 * KEY_SIZE * i + i * 4, 3.0 / 2 * KEY_SIZE * i);
                g.getChildren().add(hBox);
            }
            scene = new Scene(g,3, 3);
            stage.setScene(scene);
        }
        swapButton = new Button("SWAP");
        swapButton.relocate(BOARD_SIZE*KEY_SIZE*1.5, BOARD_SIZE*KEY_SIZE*1.9);
        swapButton.setOnAction((ActionEvent e) -> {
            System.out.println("swapped");
            keyBackground[pos[0]][pos[1]].setFill(Color.LIGHTGRAY);
            keyBackground[pos[1]][pos[0]].setFill(Color.BLUE);
            buttons[pos[1]][pos[0]].setOnAction(null);
            buttons[pos[0]][pos[1]].enableOnClick(pos[0], pos[1]);
            swapButton.setDisable(true);
            round++;
        });
        swapButton.setDisable(true);
        g.getChildren().add(swapButton);
        //投降按鈕
        Button concedeButton = new Button("CONCEDE");
        concedeButton.relocate(BOARD_SIZE*KEY_SIZE*1.5, BOARD_SIZE*KEY_SIZE*2.1);
        concedeButton.setOnAction((ActionEvent e) -> {
            switch (round % 2) {
                case 0 -> System.out.println("Red concede\nBlue Win");
                case 1 -> System.out.println("Blue concede\nRed Win");
            }
            reinitialize();
            drawDefault();
        });
        //勝負判斷
        //
        g.getChildren().add(concedeButton);



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
