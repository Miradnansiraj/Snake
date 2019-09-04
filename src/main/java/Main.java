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
    private BorderPane borderPane;
    private Pane pane;
    private Scene scene;

    //Timeline for movements
    private Timeline movement;
    private KeyFrame moveKeys;
    private int fpsCounter = 8;
    private String input = "";
    private String prevInput = "";

    //For viewing score
    private Text scoreText;
    private int score = 0;

    //dimension of playArea
    private final int width = 400;
    private final int height = 400;

    //The snake and the lastKnown positions to be used by bodies
    private ArrayList<Rectangle> snakes;
    private ArrayList<Integer> lastKnownX;
    private ArrayList<Integer> lastKnownY;

    //The food
    private static Circle food;
    private KeyFrame disappear, appear, shrink, expand;
    private Timeline blink, shrinkExpand;

    @Override
    public void start(Stage primaryStage) {
        borderPane = new BorderPane();

        //top
        borderPane.setTop(initTop());

        //centre
        pane = new Pane();
        //initialize snake
        snakes = new ArrayList<>();
        lastKnownX = new ArrayList<>();
        lastKnownY = new ArrayList<>();
        //initialize food
        food = new Circle(5, Color.RED);
        initCentre();

        //movement animation
        setMovement();

        //scene
        scene = new Scene(borderPane, width, height+25);
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
                borderPane.requestFocus();
                start.setText("Stop");
                movement.play();
                input = "";
                prevInput = "";
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
            //reset the score to 0
            score = 0;
            setScore();
            start.setText("Start");

            //Reset the snake
            input = "";
            prevInput = "";
            pane.getChildren().clear();
            snakes.clear();
            lastKnownX.clear();
            lastKnownY.clear();
            //create the initial snake at [200, 200]
            initSnake();

            //initialize food
            spawnFood();
            setFoodAnimation();
            pane.getChildren().addAll(food);
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

    private void initCentre()
    {
        //create the initial snake at [200, 200]
        initSnake();

        //initialize food
        spawnFood();
        setFoodAnimation();

        //Pane
        pane.requestFocus();
        pane.setMaxHeight(400);
        pane.setMaxWidth(400);
        pane.setBackground(new Background(new BackgroundFill(Color.web("#6ba651"), CornerRadii.EMPTY, Insets.EMPTY)));
        pane.getChildren().addAll(food);
        borderPane.setCenter(pane);
    }

    private void setMovement()
    {
        //The complex movement algorithms for the snake
        moveKeys = new KeyFrame(setFPS(fpsCounter), (ActionEvent event) -> {
            //Right side
            if(input.equals(KeyCode.D.toString())  || input.equals(KeyCode.RIGHT.toString()))
            {
                if(!prevInput.equals(KeyCode.A.toString()))
                {
                    for (int i = 0; i < snakes.size(); i++) {
                        if(snakes.get(0).getX() == width)
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
                    prevInput = input;
                }
                //Keeps snake moving after pressing opposite key
                if(prevInput.equals(KeyCode.A.toString()))
                {
                    for (int i = 0; i < snakes.size(); i++) {
                        if(snakes.get(0).getX() == width)
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
            }
            //Left side
            if(input.equals(KeyCode.A.toString())  || input.equals(KeyCode.LEFT.toString()))
            {
                if(!prevInput.equals(KeyCode.D.toString()))
                {
                    for (int i = 0; i < snakes.size(); i++) {
                    if(snakes.get(0).getX() == -10)
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
                    prevInput = input;
                }
                //Keeps snake moving after pressing opposite key
                if(prevInput.equals(KeyCode.D.toString()))
                {
                    for (int i = 0; i < snakes.size(); i++) {
                        if(snakes.get(0).getX() == -10)
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
            }
            //Up side
            if(input.equals(KeyCode.W.toString())  || input.equals(KeyCode.UP.toString()))
            {
                if(!prevInput.equals(KeyCode.S.toString())) {
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
                    prevInput = input;
                }
                //Keeps snake moving after pressing opposite key
                if(prevInput.equals(KeyCode.S.toString())) {
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
            }
            //Down side
            if(input.equals(KeyCode.S.toString()) || input.equals(KeyCode.DOWN.toString()))
            {
                if(!prevInput.equals(KeyCode.W.toString())) {
                    for (int i = 0; i < snakes.size(); i++) {
                        if(snakes.get(0).getY() == 400)
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
                    prevInput = input;
                }
                //Keeps snake moving after pressing opposite key
                if(prevInput.equals(KeyCode.W.toString())) {
                    for (int i = 0; i < snakes.size(); i++) {
                        if(snakes.get(0).getY() == 400)
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
            }
            //Check if it ate food
            if(food.getCenterX() == snakes.get(0).getX() && food.getCenterY() == snakes.get(0).getY()
                    || food.getCenterX() == snakes.get(0).getX()+5 && food.getCenterY() == snakes.get(0).getY()+5) {
                foodEaten();
                setScore();
            }
        });
        //The timeline
        movement = new Timeline();
        movement.setAutoReverse(false);
        movement.setCycleCount(Timeline.INDEFINITE);
        movement.getKeyFrames().addAll(moveKeys);
    }

    public static void main(String[] args) {
        launch(args);
    }

    //utility functions
    private void initSnake()
    {
        //Add initial snake at [200, 200]
        snakes.add(new Rectangle(width/2.0, height/2.0, width/40.0, height/40.0));
        System.out.println(snakes);
        lastKnownX.add( width/2);
        System.out.println("X: " + lastKnownX);
        lastKnownY.add( height/2);
        System.out.println("Y: " + lastKnownY);
        pane.getChildren().addAll(snakes.get(0));
    }

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
        System.out.println("Food: " + x + " " + y);
        food.setCenterX(x+5);
        food.setCenterY(y+5);
    }

    //animations
    private void setFoodAnimation()
    {
        disappear = new KeyFrame(Duration.ZERO, new KeyValue(food.opacityProperty(), 0));
        appear = new KeyFrame(new Duration(2000), new KeyValue(food.opacityProperty(), 1));
        blink  = new Timeline(1000/8.0);
        blink.setCycleCount(Timeline.INDEFINITE);
        blink.setAutoReverse(true);
        blink.getKeyFrames().addAll(disappear, appear);
        blink.play();

        shrinkExpand = new Timeline(1000/8.0);
        shrink = new KeyFrame(Duration.ZERO, new KeyValue(food.radiusProperty(), 1));
        expand = new KeyFrame(new Duration(2000), new KeyValue(food.radiusProperty(), 5));
        shrinkExpand.setCycleCount(Timeline.INDEFINITE);
        shrinkExpand.setAutoReverse(true);
        shrinkExpand.getKeyFrames().addAll(shrink, expand);
        shrinkExpand.play();
    }

    private void deathAnimation()
    {

    }

    private void foodEaten()
    {
        System.out.println("Ate food");
        score++;
        snakes.add(new Rectangle(snakes.get(snakes.size()-1).getX(), snakes.get(snakes.size()-1).getY(),
                snakes.get(snakes.size()-1).getWidth(), snakes.get(snakes.size()-1).getHeight()));
        lastKnownX.add((int) snakes.get(snakes.size()-1).getX());
        lastKnownY.add((int) snakes.get(snakes.size()-1).getY());
        pane.getChildren().addAll(snakes.get(snakes.size()-1));
        spawnFood();
    }

    private void lastKnownX()
    {
        for (int i = 0; i < snakes.size(); i++) {
            if(lastKnownX.get(i) == null)
                lastKnownX.add(i, (int) snakes.get(i).getX());
            else
                lastKnownX.set(i, (int) snakes.get(i).getX());
        }
        System.out.println("X: " + lastKnownX);
    }

    private void lastKnownY()
    {
        for (int i = 0; i < snakes.size(); i++) {
            if(lastKnownY.get(i) == null)
                lastKnownY.add(i, (int) snakes.get(i).getY());
            else
                lastKnownY.set(i, (int) snakes.get(i).getY());
        }
        System.out.println("Y: " + lastKnownY);
    }

    private Duration setFPS(int fps)
    {
        return Duration.millis(1000/((double) fps));
    }
}
