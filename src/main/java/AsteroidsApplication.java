import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Polygon;
import javafx.stage.Stage;
import javafx.scene.text.Text;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class AsteroidsApplication extends Application {

    public static int WIDTH = 300;
    public static int HEIGHT = 200;

    @Override
    public void start(Stage stage) throws Exception {
        Pane pane = new Pane();
        Button button = new Button("Retry");

        AtomicInteger points = new AtomicInteger();

        Text text = new Text(10, 20, "Points: 0");
        Text finalText = new Text(110, 80, "GAME OVER");

        pane.getChildren().add(text);

        pane.setPrefSize(WIDTH, HEIGHT);

        Ship ship = new Ship(WIDTH/2, HEIGHT/2);
        List<Projectile> projectiles = new ArrayList<>();
        List<Asteroid> asteroids = new ArrayList<Asteroid>();
        for(int i = 0; i < 5; i++){
            Random rnd = new Random();
            Asteroid asteroid = new Asteroid(rnd.nextInt(WIDTH/3), rnd.nextInt(HEIGHT));
            asteroids.add(asteroid);
        }

        pane.getChildren().add(ship.getCharacter());
        asteroids.forEach(asteroid -> pane.getChildren().add(asteroid.getCharacter()));

        Scene scene = new Scene(pane);
        stage.setTitle("Asteroids!");
        stage.setScene(scene);
        stage.show();

        Map<KeyCode, Boolean> pressedKeys = new HashMap<>();

        scene.setOnKeyPressed(event -> {
            pressedKeys.put(event.getCode(), Boolean.TRUE);
        });

        scene.setOnKeyReleased(event -> {
            pressedKeys.put(event.getCode(), Boolean.FALSE);
        });

        Point2D movement = new Point2D(1, 0);

        new AnimationTimer() {

            @Override
            public void handle(long now) {
                if (pressedKeys.getOrDefault(KeyCode.LEFT, false)) {
                    ship.turnLeft();
                }
                if (pressedKeys.getOrDefault(KeyCode.RIGHT, false)) {
                    ship.turnRight();
                }
                if (pressedKeys.getOrDefault(KeyCode.UP, false)) {
                    ship.accelerate();
                }
                ship.move();
                asteroids.forEach(asteroid -> asteroid.move());
                projectiles.forEach(projectile -> projectile.move());

                List<Asteroid> aEliminar = new ArrayList<>();

                asteroids.forEach(asteroid -> {
                    if (ship.collide(asteroid)) {
                        aEliminar.add(asteroid);
                    }
                });

                if (!aEliminar.isEmpty()) {
                    this.stop();

                    // Limpieza
                    asteroids.forEach(a -> pane.getChildren().remove(a.getCharacter()));
                    asteroids.clear();
                    projectiles.forEach(p -> pane.getChildren().remove(p.getCharacter()));
                    projectiles.clear();
                    pane.getChildren().remove(ship.getCharacter());

                    // UI
                    text.setTranslateX(100);
                    text.setTranslateY(80);
                    if (!pane.getChildren().contains(button)) {
                        pane.getChildren().addAll(finalText, button);
                    }
                    button.setTranslateX(110);
                    button.setTranslateY(110);

                    button.setOnAction(event -> {
                        pane.getChildren().removeAll(finalText, button);
                        points.set(0);
                        text.setText("Points: 0");
                        text.setTranslateX(0);
                        text.setTranslateY(0);

                        ship.setMovement(new Point2D(0, 0));
                        ship.getCharacter().setRotate(0);
                        ship.getCharacter().setTranslateX(WIDTH / 2);
                        ship.getCharacter().setTranslateY(HEIGHT / 2);
                        pane.getChildren().add(ship.getCharacter());

                        for (int i = 0; i < 5; i++) {
                            Asteroid a = new Asteroid(new Random().nextInt(WIDTH / 3), new Random().nextInt(HEIGHT));
                            asteroids.add(a);
                            pane.getChildren().add(a.getCharacter());
                        }
                        this.start();
                    });
                }

                asteroids.forEach(asteroid-> {
                    if (ship.collide(asteroid)) {
                        pane.getChildren().remove(ship.getCharacter());
                        asteroids.forEach(a -> pane.getChildren().remove(a.getCharacter()));
                        asteroids.clear();
                        projectiles.forEach(p -> pane.getChildren().remove(p.getCharacter()));
                        projectiles.clear();
                        stop();
                        text.setTranslateX(100);
                        text.setTranslateY(80);
                        pane.getChildren().add(finalText);
                        pane.getChildren().add(button);
                        button.setTranslateX(110);
                        button.setTranslateY(110);
                        button.setOnAction(event -> {
                            pane.getChildren().remove(finalText);
                            pane.getChildren().remove(button);
                            pane.getChildren().remove(text);
                            points.set(0);
                            ship.getCharacter().setTranslateX(WIDTH/2);
                            ship.getCharacter().setTranslateY(HEIGHT/2);
                            ship.setMovement(new Point2D(0, 0));
                            ship.getCharacter().setRotate(0);
                            pane.getChildren().add(ship.getCharacter());
                            for(int i = 0; i < 5; i++){
                                Asteroid asteroid1 = new Asteroid(new Random().nextInt(WIDTH / 3), new Random().nextInt(HEIGHT));
                                asteroids.add(asteroid1);
                                pane.getChildren().add(asteroid1.getCharacter());
                            }
                            this.start();
                        });
                    }
                });
                if(pressedKeys.getOrDefault(KeyCode.SPACE, false) && projectiles.size() < 3){
                    Projectile projectile = new Projectile((int) ship.getCharacter().getTranslateX(), (int) ship.getCharacter().getTranslateY());
                    projectile.getCharacter().setRotate(ship.getCharacter().getRotate());
                    projectiles.add(projectile);

                    projectile.accelerate();
                    projectile.setMovement(projectile.getMovement().normalize().multiply(3));

                    pane.getChildren().add(projectile.getCharacter());
                }

                projectiles.forEach(projectile -> {
                    asteroids.forEach(asteroid -> {
                        if(projectile.collide(asteroid)) {
                            projectile.setAlive(false);
                            asteroid.setAlive(false);
                        }
                    });

                    if(!projectile.isAlive()) {
                        text.setText("Points: " + points.addAndGet(1000));
                    }
                });

                projectiles.stream()
                        .filter(projectile -> !projectile.isAlive())
                        .forEach(projectile -> pane.getChildren().remove(projectile.getCharacter()));
                projectiles.removeAll(projectiles.stream()
                        .filter(projectile -> !projectile.isAlive())
                        .collect(Collectors.toList()));

                asteroids.stream()
                        .filter(asteroid -> !asteroid.isAlive())
                        .forEach(asteroid -> pane.getChildren().remove(asteroid.getCharacter()));
                asteroids.removeAll(asteroids.stream()
                        .filter(asteroid -> !asteroid.isAlive())
                        .collect(Collectors.toList()));

                if(Math.random() < 0.005) {
                    Asteroid asteroid = new Asteroid(WIDTH, HEIGHT);
                    if(!asteroid.collide(ship)) {
                        asteroids.add(asteroid);
                        pane.getChildren().add(asteroid.getCharacter());
                    }
                }
            }
        }.start();
    }

    public static void start (String[] args){
        launch(args);
    }
}