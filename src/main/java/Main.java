import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontSmoothingType;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Random;


public class Main extends Application {
    //Content holders
    private BorderPane borderPane;
    private AnchorPane anchorPane;
    private Pane pane;
    private Scene scene;

    //Buttons
    private Button start, reset;
    private boolean isDead = false;
    private boolean isStopped = false;

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
    //dimension of top menu
    private final int menuHeight = 28;

    //The snake and the lastKnown positions to be used by bodies
    private ArrayList<Rectangle> snakes;
    private ArrayList<Integer> lastKnownX;
    private ArrayList<Integer> lastKnownY;
    //Death animation timeline
    private Timeline deathBlink;
    private KeyFrame snakeDisappear, snakeAppear;

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
        scene = new Scene(borderPane, width, height+menuHeight);
        scene.getStylesheets().add("main.css");
        scene.setOnKeyPressed(event -> input = event.getCode().toString());

        //stage
        primaryStage.setScene(scene);
        primaryStage.setTitle("Snake");
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    private AnchorPane initTop()
    {
        //Displays score on menu
        scoreText = new Text("Score: " + score);
        scoreText.setFontSmoothingType(FontSmoothingType.LCD);
        scoreText.setFont(Font.font("Verdana", FontWeight.BOLD, 20));

        //start button
        start = new Button("Start");
        start.setMaxHeight(menuHeight-3);
        start.setOnAction(event -> {
            if(start.getText().equals("Start"))
            {
                if(isDead)
                {
                    //Starting the game after death
                    resetBoard();
                    isDead = false;
                }

                else if(isStopped)
                {
                    //Starting the game after stop
                    resetBoard();
                    isStopped = false;
                }
                //Initial start
                borderPane.requestFocus();
                start.setText("Stop");
                movement.play();
                input = "";
                prevInput = "";
            }
            else
            {
                if(isDead)
                {
                    resetBoard();
                    isDead = false;
                }
                start.setText("Start");
                movement.stop();
                isStopped = true;
            }
        });

        //reset button
        reset = new Button("Reset");
        reset.setMaxHeight(menuHeight-3);
        reset.setOnAction(event -> resetBoard());

        //HBox
        HBox hBox = new HBox();
        hBox.setSpacing(3);
        hBox.setMaxHeight(menuHeight);
        hBox.getChildren().addAll(start, reset);
        hBox.setMinHeight(menuHeight);

        HBox hBox2 = new HBox();
        hBox2.setSpacing(3);
        hBox2.setMaxHeight(menuHeight);
        hBox2.getChildren().addAll(scoreText);
        hBox2.setMinHeight(menuHeight);
        hBox2.setAlignment(Pos.CENTER);

        anchorPane = new AnchorPane(hBox2, hBox);
        anchorPane.setMaxHeight(menuHeight);
        anchorPane.setMinHeight(menuHeight);
        anchorPane.getStyleClass().add("top-menu");
        //Set score on Left
        AnchorPane.setLeftAnchor(hBox2, 2.0);
        //Set buttons on Right
        AnchorPane.setRightAnchor(hBox, 2.0);
        return anchorPane;
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
                //Check if youre pressing Right after Left or by itself
                //to prevent snake moving in opposite direction
                if(!(prevInput.equals(KeyCode.A.toString()) || prevInput.equals(KeyCode.LEFT.toString())))
                {
                    for (int i = 0; i < snakes.size(); i++) {
                        //the algorithm of snake movement doesnt work when theres only 1 body
                        //so theres another algorithm for single snake body
                        if(snakes.size()==1) {
                            if(snakes.get(0).getX() == width-10)
                            {
                                System.out.println("Died at: " + snakes.get(0).getX() + ", " + snakes.get(0).getY());
                                movement.stop();
                                deathAnimation();
                            }
                            else
                                snakes.get(0).setX(snakes.get(0).getX() + 10);
                        }
                        else
                        {
                            if(snakes.get(0).getX() == width)
                            {
                                System.out.println("Died at: " + snakes.get(0).getX() + ", " + snakes.get(0).getY());
                                //Fix snake position after death
                                for (int j = 0; j < snakes.size(); j++) {
                                    if(j==0)
                                        snakes.get(0).setX(snakes.get(0).getX() - 10);
                                    else {
                                        System.out.println("X before dying " + lastKnownX);
                                        snakes.get(j).setX(lastKnownX.get(j));
                                        snakes.get(j).setY(lastKnownY.get(j));
                                    }
                                }
                                movement.stop();
                                deathAnimation();
                                break;
                            }
                            if(i==0)
                                snakes.get(0).setX(snakes.get(0).getX() + 10);
                            else {
                                snakes.get(i).setX(lastKnownX.get(i - 1));
                                snakes.get(i).setY(lastKnownY.get(i - 1));
                            }
                        }
                    }
                    lastKnownY();
                    lastKnownX();
                    prevInput = input;
                }
                //Keeps snake moving after pressing opposite key
                else if((prevInput.equals(KeyCode.A.toString()) || prevInput.equals(KeyCode.LEFT.toString())))
                {
                    for (int i = 0; i < snakes.size(); i++) {
                        //the algorithm of snake movement doesnt work when theres only 1 body
                        //so theres another algorithm for single snake body
                        if(snakes.size()==1) {
                            if(snakes.get(0).getX() == 0)
                            {
                                System.out.println("Died at: " + snakes.get(0).getX() + ", " + snakes.get(0).getY());
                                movement.stop();
                                deathAnimation();
                            }
                            else
                                snakes.get(0).setX(snakes.get(0).getX() - 10);
                        }
                        else
                        {
                            if(snakes.get(0).getX() == -10)
                            {
                                System.out.println("Died at: " + snakes.get(0).getX() + ", " + snakes.get(0).getY());
                                //Fix snake position after death
                                for (int j = 0; j < snakes.size(); j++) {
                                    if(j==0)
                                        snakes.get(0).setX(snakes.get(0).getX() + 10);
                                    else {
                                        System.out.println("X before dying " + lastKnownX);
                                        snakes.get(j).setX(lastKnownX.get(j));
                                        snakes.get(j).setY(lastKnownY.get(j));
                                    }
                                }
                                movement.stop();
                                deathAnimation();
                                break;
                            }
                            if(i==0)
                                snakes.get(0).setX(snakes.get(0).getX() - 10);
                            else {
                                snakes.get(i).setX(lastKnownX.get(i - 1));
                                snakes.get(i).setY(lastKnownY.get(i - 1));
                            }
                        }
                    }
                    lastKnownY();
                    lastKnownX();
                }
            }
            //Left side
            if(input.equals(KeyCode.A.toString())  || input.equals(KeyCode.LEFT.toString()))
            {
                //Check if youre pressing Left after Right or by itself
                //to prevent snake moving in opposite direction
                if(!(prevInput.equals(KeyCode.D.toString()) || prevInput.equals(KeyCode.RIGHT.toString())))
                {
                    for (int i = 0; i < snakes.size(); i++) {
                        //the algorithm of snake movement doesnt work when theres only 1 body
                        //so theres another algorithm for single snake body
                        if(snakes.size()==1) {
                            if(snakes.get(0).getX() == 0)
                            {
                                System.out.println("Died at: " + snakes.get(0).getX() + ", " + snakes.get(0).getY());
                                movement.stop();
                                deathAnimation();
                            }
                            else
                                snakes.get(0).setX(snakes.get(0).getX() - 10);
                        }
                        else
                        {
                            if(snakes.get(0).getX() == -10)
                            {
                                System.out.println("Died at: " + snakes.get(0).getX() + ", " + snakes.get(0).getY());
                                //Fix snake position after death
                                for (int j = 0; j < snakes.size(); j++) {
                                    if(j==0)
                                        snakes.get(0).setX(snakes.get(0).getX() + 10);
                                    else {
                                        System.out.println("X before dying " + lastKnownX);
                                        snakes.get(j).setX(lastKnownX.get(j));
                                        snakes.get(j).setY(lastKnownY.get(j));
                                    }
                                }
                                movement.stop();
                                deathAnimation();
                                break;
                            }
                            if(i==0)
                                snakes.get(0).setX(snakes.get(0).getX() - 10);
                            else {
                                snakes.get(i).setX(lastKnownX.get(i - 1));
                                snakes.get(i).setY(lastKnownY.get(i - 1));
                            }
                        }
                }
                    lastKnownY();
                    lastKnownX();
                    prevInput = input;
                }
                //Keeps snake moving after pressing opposite key
                else if((prevInput.equals(KeyCode.D.toString()) || prevInput.equals(KeyCode.RIGHT.toString())))
                {
                    for (int i = 0; i < snakes.size(); i++) {
                        //the algorithm of snake movement doesnt work when theres only 1 body
                        //so theres another algorithm for single snake body
                        if(snakes.size()==1) {
                            if(snakes.get(0).getX() == width-10)
                            {
                                System.out.println("Died at: " + snakes.get(0).getX() + ", " + snakes.get(0).getY());
                                movement.stop();
                                deathAnimation();
                            }
                            else
                                snakes.get(0).setX(snakes.get(0).getX() + 10);
                        }
                        else
                        {
                            if(snakes.get(0).getX() == width)
                            {
                                System.out.println("Died at: " + snakes.get(0).getX() + ", " + snakes.get(0).getY());
                                //Fix snake position after death
                                for (int j = 0; j < snakes.size(); j++) {
                                    if(j==0)
                                        snakes.get(0).setX(snakes.get(0).getX() - 10);
                                    else {
                                        System.out.println("X before dying " + lastKnownX);
                                        snakes.get(j).setX(lastKnownX.get(j));
                                        snakes.get(j).setY(lastKnownY.get(j));
                                    }
                                }
                                movement.stop();
                                deathAnimation();
                                break;
                            }
                            if(i==0)
                                snakes.get(0).setX(snakes.get(0).getX() + 10);
                            else {
                                snakes.get(i).setX(lastKnownX.get(i - 1));
                                snakes.get(i).setY(lastKnownY.get(i - 1));
                            }
                        }
                    }
                    lastKnownY();
                    lastKnownX();
                }
            }
            //Up side
            if(input.equals(KeyCode.W.toString())  || input.equals(KeyCode.UP.toString()))
            {
                //Check if youre pressing Up after Down or by itself
                //to prevent snake moving in opposite direction
                if(!(prevInput.equals(KeyCode.S.toString()) || prevInput.equals(KeyCode.DOWN.toString()))) {
                    for (int i = 0; i < snakes.size(); i++) {
                        //the algorithm of snake movement doesnt work when theres only 1 body
                        //so theres another algorithm for single snake body
                        if(snakes.size()==1)
                        {
                            if(snakes.get(0).getY() == 0)
                            {
                                System.out.println("Died at: " + snakes.get(0).getX() + ", " + snakes.get(0).getY());
                                movement.stop();
                                deathAnimation();
                            }
                            else
                                snakes.get(0).setY(snakes.get(0).getY() - 10);
                        }
                        else
                        {
                            if(snakes.get(0).getY() == -10)
                            {
                                System.out.println("Died at: " + snakes.get(0).getX() + ", " + snakes.get(0).getY());
                                //Fix snake position after death
                                for (int j = 0; j < snakes.size(); j++) {
                                    if(j==0)
                                        snakes.get(0).setY(snakes.get(0).getY() + 10);
                                    else {
                                        System.out.println("Y before dying " + lastKnownY);
                                        snakes.get(j).setX(lastKnownX.get(j));
                                        snakes.get(j).setY(lastKnownY.get(j));
                                    }
                                }
                                movement.stop();
                                deathAnimation();
                                break;
                            }
                            if(i==0)
                                snakes.get(0).setY(snakes.get(0).getY() - 10);
                            else {
                                snakes.get(i).setX(lastKnownX.get(i - 1));
                                snakes.get(i).setY(lastKnownY.get(i - 1));
                            }
                        }
                    }
                    lastKnownY();
                    lastKnownX();
                    prevInput = input;
                }
                //Keeps snake moving after pressing opposite key
                else if(prevInput.equals(KeyCode.S.toString()) || prevInput.equals(KeyCode.DOWN.toString())) {
                    for (int i = 0; i < snakes.size(); i++) {
                        //the algorithm of snake movement doesnt work when theres only 1 body
                        //so theres another algorithm for single snake body
                        if(snakes.size()==1) {
                            if(snakes.get(0).getY() == height-10)
                            {
                                System.out.println("Died at: " + snakes.get(0).getX() + ", " + snakes.get(0).getY());
                                movement.stop();
                                deathAnimation();
                            }
                            else
                                snakes.get(0).setY(snakes.get(0).getY() + 10);
                        }
                        else
                        {
                            if(snakes.get(0).getY() == height)
                            {
                                System.out.println("Died at: " + snakes.get(0).getX() + ", " + snakes.get(0).getY());
                                //Fix snake position after death
                                for (int j = 0; j < snakes.size(); j++) {
                                    if(j==0)
                                        snakes.get(0).setY(snakes.get(0).getY() - 10);
                                    else {
                                        System.out.println("Y before dying " + lastKnownY);
                                        snakes.get(j).setX(lastKnownX.get(j));
                                        snakes.get(j).setY(lastKnownY.get(j));
                                    }
                                }
                                movement.stop();
                                deathAnimation();
                                break;
                            }
                            if(i==0)
                                snakes.get(0).setY(snakes.get(0).getY() + 10);
                            else {
                                snakes.get(i).setX(lastKnownX.get(i - 1));
                                snakes.get(i).setY(lastKnownY.get(i - 1));
                            }
                        }
                    }
                    lastKnownY();
                    lastKnownX();
                }
            }
            //Down side
            if(input.equals(KeyCode.S.toString()) || input.equals(KeyCode.DOWN.toString()))
            {
                //Check if youre pressing Down after Up or by itself
                //to prevent snake moving in opposite direction
                if(!(prevInput.equals(KeyCode.W.toString()) || prevInput.equals(KeyCode.UP.toString()))) {
                    for (int i = 0; i < snakes.size(); i++) {
                        //the algorithm of snake movement doesnt work when theres only 1 body
                        //so theres another algorithm for single snake body
                        if(snakes.size()==1) {
                            if(snakes.get(0).getY() == height-10)
                            {
                                System.out.println("Died at: " + snakes.get(0).getX() + ", " + snakes.get(0).getY());
                                movement.stop();
                                deathAnimation();
                            }
                            else
                                snakes.get(0).setY(snakes.get(0).getY() + 10);
                        }
                        else
                        {
                            if(snakes.get(0).getY() == height)
                            {
                                System.out.println("Died at: " + snakes.get(0).getX() + ", " + snakes.get(0).getY());
                                //Fix snake position after death
                                for (int j = 0; j < snakes.size(); j++) {
                                    if(j==0)
                                        snakes.get(0).setY(snakes.get(0).getY() - 10);
                                    else {
                                        System.out.println("Y before dying " + lastKnownY);
                                        snakes.get(j).setX(lastKnownX.get(j));
                                        snakes.get(j).setY(lastKnownY.get(j));
                                    }
                                }
                                movement.stop();
                                deathAnimation();
                                break;
                            }
                            if(i==0)
                                snakes.get(0).setY(snakes.get(0).getY() + 10);
                            else {
                                snakes.get(i).setX(lastKnownX.get(i - 1));
                                snakes.get(i).setY(lastKnownY.get(i - 1));
                            }
                        }
                    }
                    lastKnownY();
                    lastKnownX();
                    prevInput = input;
                }
                //Keeps snake moving after pressing opposite key
                else if(prevInput.equals(KeyCode.W.toString()) || prevInput.equals(KeyCode.UP.toString())) {
                    for (int i = 0; i < snakes.size(); i++) {
                        //the algorithm of snake movement doesnt work when theres only 1 body
                        //so theres another algorithm for single snake body
                        if(snakes.size()==1) {
                            if(snakes.get(0).getY() == 0)
                            {
                                System.out.println("Died at: " + snakes.get(0).getX() + ", " + snakes.get(0).getY());
                                movement.stop();
                                deathAnimation();
                            }
                            else
                                snakes.get(0).setY(snakes.get(0).getY() - 10);
                        }
                        else
                        {
                            if(snakes.get(0).getY() == -10)
                            {
                                System.out.println("Died at: " + snakes.get(0).getX() + ", " + snakes.get(0).getY());
                                //Fix snake position after death
                                for (int j = 0; j < snakes.size(); j++) {
                                    if(j==0)
                                        snakes.get(0).setY(snakes.get(0).getY() + 10);
                                    else {
                                        System.out.println("Y before dying " + lastKnownY);
                                        snakes.get(j).setX(lastKnownX.get(j));
                                        snakes.get(j).setY(lastKnownY.get(j));
                                    }
                                }
                                movement.stop();
                                deathAnimation();
                                break;
                            }
                            if(i==0)
                                snakes.get(0).setY(snakes.get(0).getY() - 10);
                            else {
                                snakes.get(i).setX(lastKnownX.get(i - 1));
                                snakes.get(i).setY(lastKnownY.get(i - 1));
                            }
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
        for (Rectangle snake : snakes) {
            snakeDisappear = new KeyFrame(Duration.ZERO, new KeyValue(snake.opacityProperty(), 0));
            snakeAppear = new KeyFrame(new Duration(1000 / 8.0), new KeyValue(snake.opacityProperty(), 1));
            deathBlink = new Timeline(1000 / 8.0);
            deathBlink.setCycleCount(Timeline.INDEFINITE);
            deathBlink.setAutoReverse(true);
            deathBlink.getKeyFrames().addAll(snakeDisappear, snakeAppear);
            deathBlink.play();
        }
        isDead = true;
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

    private void resetBoard()
    {
        //reset the score to 0
        score = 0;
        setScore();
        start.setText("Start");

        //Reset the snake
        input = "";
        prevInput = "";
        pane.getChildren().clear();
        //deathBlink.stop();
        snakes.clear();
        lastKnownX.clear();
        lastKnownY.clear();
        //create the initial snake at [200, 200]
        initSnake();

        //initialize food
        spawnFood();
        setFoodAnimation();
        pane.getChildren().addAll(food);
    }
}
