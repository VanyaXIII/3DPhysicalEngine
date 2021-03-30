package geometry.intersections;

import geometry.objects3D.Point3D;

public class PolyhedronToPlaneIntersection extends AbstractIntersection {
    
    private Point3D collisionPoint;
    private Point3D pointOfPolygon;
    private double value;

    public PolyhedronToPlaneIntersection(boolean areIntersected) {
        super(areIntersected);
    }

    public PolyhedronToPlaneIntersection(boolean areIntersected, Point3D collisionPoint, Point3D pointOfPolygon, double value){
        super(areIntersected);
        this.collisionPoint = collisionPoint;
        this.pointOfPolygon = pointOfPolygon;
        this.value = value;
    }

    public double getValue() {
        return value;
    }

    public Point3D getPointOfPolygon() {
        return pointOfPolygon;
    }

    public Point3D getCollisionPoint() {
        return collisionPoint;
    }
}
