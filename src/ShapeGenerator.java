package sample;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Rectangle;

// Only for animation purposes only

public abstract class ShapeGenerator {
    String shapeType;
    Color color;
    ShapeGenerator(String shapeType,Color color)
    {
        this.shapeType = shapeType;
        this.color = color;
    }
}

class CIRCLE extends ShapeGenerator
{
    private double radius;
    CIRCLE(Color color, double radius)
    {
        super("Circle",color);
        this.radius = radius;
    }
    public Circle getCircle()
    {
        return new Circle(radius,color);
    }
}

class SQUARE extends ShapeGenerator
{
    private double height,width;
    SQUARE(double height,double width,Color color)
    {
        super("Square",color);
        this.height = height;
        this.width = width;
    }
    public Rectangle getSquare()
    {
        Rectangle rectangle = new Rectangle();
        rectangle.setStroke(color);
        rectangle.setFill(color);
        rectangle.setWidth(width);
        rectangle.setHeight(height);
        return rectangle;
    }

}

class PLUS extends ShapeGenerator
{
    private double height,width;
    PLUS(double height,double width,Color color)
    {
        super("Plus",color);
        this.height = height;
        this.width = width;
    }
    public Polyline getPlus()
    {
        Polyline plus = new Polyline();
        Double[] points = {width/2,0d,width/2,height,width/2,height/2,width,height/2,0d,height/2};
        plus.getPoints().addAll(points);
        plus.setStroke(color);
        plus.setStrokeWidth(4);
        return plus;
    }
}

class TRIANGLE extends ShapeGenerator
{
    private double height,base;
    TRIANGLE(double height,double base,Color color)
    {
        super("Triangle",color);
        this.height = height;
        this.base = base;
    }
    public Polygon getTriangle()
    {
        Polygon triangle = new Polygon();
        Double[] points = {base/2,0d,0d,height,base,height};
        triangle.getPoints().addAll(points);
        triangle.setFill(color);
        triangle.setStroke(color);
        return triangle;
    }
}

class CROSS extends ShapeGenerator
{
    private double height,width;
    CROSS(double height,double width,Color color)
    {
        super("Cross",color);
        this.height = height;
        this.width = width;
    }
    public Polyline getCross()
    {
        Polyline cross = new Polyline();
        Double[] points = {0d,0d,width,height,width/2,height/2,0d,height,width,0d};
        cross.getPoints().addAll(points);
        cross.setStroke(color);
        cross.setStrokeWidth(4);
        return cross;
    }
}



