import javafx.scene.shape.Polygon;

import java.util.ArrayList;
import java.util.List;

public class Projectile extends Character{

    public Projectile(int x, int y){
        super(new Polygon(2, -2, 2, 2, -2, 2, -2, -2), x, y);
    }
}
