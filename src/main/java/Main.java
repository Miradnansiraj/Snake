import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Random;


public class Main extends Application {
    //Content holders
    private BorderPane pane;
    private Pane playArea;
    private Scene scene;

    //Timeline for movements
    private Timeline movement;
    private String input = "";

    //For viewing score
    private Text scoreText;
    private int score = 0;

    //dimension of playArea
    private final int width = 400;
    private final int height = 400;

    //The snake and the lastKnown positions to be used by body
    private ArrayList<Rectangle> snakes;
    private ArrayList<Integer> lastKnownX;
    private ArrayList<Integer> lastKnownY;

    //The food
    private static Circle food;

    @Override
    public void start(Stage primaryStage) {
        pane = new BorderPane();

        //top
        pane.setTop(initTop());

        //centre
        pane.setCenter(initCentre());

        //movement animation
        setMovement();

        //scene
        scene = new Scene(pane, width, height+25);
        scene.setOnKeyPressed(event -> input = event.getCode().toString());

        //stage
        primaryStage.setScene(scene);
        primaryStage.setTitle("Snake");
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    private HBox initTop()
    {
        Button start, reset;
        scoreText = new Text("Score: " + score);

        //start button
        start = new Button("Start");
        start.setOnAction(event -> {
            if(start.getText().equals("Start"))
            {
                pane.requestFocus();
                start.setText("Stop");
                movement.play();
                input = "";
            }
            else
            {
                start.setText("Start");
                movement.stop();
                //Respawn food
                spawnFood();
            }
        });

        //reset button
        reset = new Button("Reset");
        reset.setOnAction(event -> {
            pane.getChildren().removeAll();

            //reset the score to 0
            score = 0;
            setScore();
            start.setText("Start");

            //Reset the snake
            input = "";
            for (int i = 1; i < snakes.size(); i++) {
                snakes.remove(i);
            }
            lastKnownX.clear();
            lastKnownY.clear();
            snakes.get(0).setX(width/2.0);
            lastKnownX.add(width/2);
            snakes.get(0).setY(height/2.0);
            lastKnownY.add(height/2);

            //Respawn food
            spawnFood();
        });

        //HBox
        HBox hBox = new HBox();
        hBox.setSpacing(100);
        hBox.setMaxHeight(25);
        hBox.getChildren().addAll(scoreText, start, reset);
        hBox.setBackground(new Background(new BackgroundFill(Color.web("#8c8c88"), CornerRadii.EMPTY, Insets.EMPTY)));
        hBox.setMinHeight(25);
        return hBox;
    }

    private Pane initCentre()
    {
        playArea = new Pane();

        //initialize snake
        snakes = new ArrayList<>();
        lastKnownX = new ArrayList<>();
        lastKnownY = new ArrayList<>();
        snakes.add(new Rectangle(width/2.0, height/2.0, width/40.0, height/40.0));
        System.out.println(snakes);
        lastKnownX.add( width/2);
        lastKnownY.add( height/2);

        //initialize food
        Random r = new Random();
        int x = (r.nextInt(20)*20); // values from 0 to 19 inclusively
        int y = (r.nextInt(20)*20);
        System.out.println(x + " " + y);
        food = new Circle(x+5, y+5, 5);
        setFoodAnimation();

        //Pane
        playArea.requestFocus();
        playArea.setMaxHeight(400);
        playArea.setMaxWidth(400);
        playArea.setBackground(new Background(new BackgroundFill(Color.web("#6ba651"), CornerRadii.EMPTY, Insets.EMPTY)));
        playArea.getChildren().addAll(snakes.get(0), food);
        return playArea;
    }

    private void setMovement()
    {
        //The complex movement algorithms for the snake
        movement = new Timeline(new KeyFrame(Duration.millis(1000/5), (ActionEvent event) -> {
            //The right side movement
            if(input.equals(KeyCode.D.toString())  || input.equals(KeyCode.RIGHT.toString()))
            {
                for (int i = 0; i < snakes.size(); i++) {
                    if(snakes.get(0).getX() == width-10)
                        movement.stop();
                    if(i==0)
                        snakes.get(0).setX(snakes.get(0).getX() + 10);
                    else {
                        snakes.get(i).setX(lastKnownX.get(i - 1));
                        snakes.get(i).setY(lastKnownY.get(i - 1));
                    }
                }
                lastKnownY();
                lastKnownX();
            }
            if(input.equals(KeyCode.A.toString())  || input.equals(KeyCode.LEFT.toString()))
            {
                for (int i = 0; i < snakes.size(); i++) {
                    if(snakes.get(0).getX() == 0)
                        movement.stop();
                    if(i==0)
                        snakes.get(0).setX(snakes.get(0).getX() - 10);
                    else {
                        snakes.get(i).setX(lastKnownX.get(i - 1));
                        snakes.get(i).setY(lastKnownY.get(i - 1));
                    }
                }
                lastKnownY();
                lastKnownX();
            }
            if(input.equals(KeyCode.W.toString())  || input.equals(KeyCode.UP.toString()))
            {
                for (int i = 0; i < snakes.size(); i++) {
                    if(snakes.get(0).getY() == 0)
                        movement.stop();
                    if(i==0)
                        snakes.get(0).setY(snakes.get(0).getY() - 10);
                    else {
                        snakes.get(i).setX(lastKnownX.get(i - 1));
                        snakes.get(i).setY(lastKnownY.get(i - 1));
                    }
                }
                lastKnownY();
                lastKnownX();
            }
            if(input.equals(KeyCode.S.toString()) || input.equals(KeyCode.DOWN.toString()))
            {
                for (int i = 0; i < snakes.size(); i++) {
                    if(snakes.get(0).getY() == 0)
                        movement.stop();
                    if(i==0)
                        snakes.get(0).setY(snakes.get(0).getY() + 10);
                    else {
                        snakes.get(i).setX(lastKnownX.get(i - 1));
                        snakes.get(i).setY(lastKnownY.get(i - 1));
                    }
                }
                lastKnownY();
                lastKnownX();
            }

            if(food.getCenterX() == snakes.get(0).getX() && food.getCenterY() == snakes.get(0).getY()
                || food.getCenterX() == snakes.get(0).getX()+5 && food.getCenterY() == snakes.get(0).getY()+5) {
                foodEaten();
            }
            setScore();
        }));
        movement.setAutoReverse(false);
        movement.setCycleCount(Timeline.INDEFINITE);
    }


    public static void main(String[] args) {
        launch(args);
    }

    //utility functions
    private void setScore()
    {
        scoreText.setText("Score: " + score);
    }

    private void spawnFood()
    {
        Random r = new Random();
        int x = (r.nextInt(20)*20); // values from 0 to 19 inclusively
        int y = (r.nextInt(20)*20);
        for (Rectangle snake : snakes) {
            if (snake.getX() == x)
                while (x == snake.getX()) {
                    x = (r.nextInt(20) * 20);
                }
            if (snake.getY() == y)
                while (y == snake.getY()) {
                    y = (r.nextInt(20) * 20);
                }
        }
        System.out.println(x + " " + y);
        food.setCenterX(x+5);
        food.setCenterY(y+5);
    }

    private void setFoodAnimation()
    {
        KeyFrame keyFrame = new KeyFrame(Duration.ZERO, new KeyValue(food.opacityProperty(), 0));
        KeyFrame keyFrame2 = new KeyFrame(new Duration(2000), new KeyValue(food.opacityProperty(), 1));
        Timeline blink  = new Timeline(900);
        blink.setCycleCount(Timeline.INDEFINITE);
        blink.setAutoReverse(true);
        blink.getKeyFrames().addAll(keyFrame, keyFrame2);
        blink.play();

        Timeline shrinkExpand = new Timeline(900);
        KeyFrame shrink = new KeyFrame(Duration.ZERO, new KeyValue(food.radiusProperty(), 1));
        KeyFrame expand = new KeyFrame(new Duration(2000), new KeyValue(food.radiusProperty(), 5));
        shrinkExpand.setCycleCount(Timeline.INDEFINITE);
        shrinkExpand.setAutoReverse(true);
        shrinkExpand.getKeyFrames().addAll(shrink, expand);
        shrinkExpand.play();
    }

    private void foodEaten()
    {
        System.out.println("Ate food");
        score++;
        snakes.add(new Rectangle(snakes.get(snakes.size()-1).getX(), snakes.get(snakes.size()-1).getY(),
                snakes.get(snakes.size()-1).getWidth(), snakes.get(snakes.size()-1).getHeight()));
        lastKnownX.add((int) snakes.get(snakes.size()-1).getX());
        lastKnownY.add((int) snakes.get(snakes.size()-1).getY());
        playArea.getChildren().addAll(snakes.get(snakes.size()-1));
        spawnFood();
    }

    private void lastKnownX()
    {
        for (int i = 0; i < snakes.size(); i++) {
            lastKnownX.set(i, (int) snakes.get(i).getX());
        }
        System.out.println("X: " + lastKnownX);
    }

    private void lastKnownY()
    {
        for (int i = 0; i < snakes.size(); i++) {
            lastKnownY.set(i, (int) snakes.get(i).getY());
        }
        System.out.println("Y: " + lastKnownY);
    }
}
