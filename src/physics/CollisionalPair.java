package physics;

import exceptions.ImpossiblePairException;
import geometry.intersections.IntersectionalPair;
import geometry.objects.Segment;
import geometry.objects.Triangle;
import geometry.objects3D.Line3D;
import geometry.objects3D.Plane3D;
import geometry.objects3D.Point3D;
import geometry.objects3D.Vector3D;
import limiters.Collisional;
import physical_objects.GravityPlate;
import physical_objects.PhysicalPolyhedron;
import physical_objects.PhysicalSphere;
import physical_objects.Wall;
import utils.Pair;
import utils.Tools;
import utils.TripleMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Класс, обрабатывающий коллизию между объектам {@link limiters.Collisional}
 */

public final class CollisionalPair<FirstThingType extends Collisional, SecondThingType extends Collisional> {
    private final FirstThingType firstThing;

    private final SecondThingType secondThing;
    private final static TripleMap<Class, Class, Collider> methodsMap;


    /**
     *Конструктор, принимающиий предметы, коллизию которых нужно обработать
     * @param firstThing объект {@link limiters.Collisional}, коллизию которого с другим объектом нужно обработать
     * @param secondThing объект {@link limiters.Collisional}, коллизию с которым нужно обработать
     * @throws ImpossiblePairException исключение в случае попытки обработать коллизии стены со стеной
     */
    public CollisionalPair(FirstThingType firstThing, SecondThingType secondThing) throws ImpossiblePairException {
        this.firstThing = firstThing;
        this.secondThing = secondThing;
        if (firstThing instanceof Wall && secondThing instanceof Wall)
            throw new ImpossiblePairException("Trying to collide wall with wall");
    }


    /*Статический иницализатор, создающий таблицу переходов, содержащую методы для обработки коллизии конкретных пар объектов
     */

    static {
        methodsMap = new TripleMap<>();

        methodsMap.addFirstKey(PhysicalSphere.class);
        methodsMap.addFirstKey(PhysicalPolyhedron.class);
        methodsMap.addFirstKey(Wall.class);
        methodsMap.addFirstKey(GravityPlate.class);


        methodsMap.putByFirstKey(PhysicalSphere.class, PhysicalSphere.class, CollisionalPair::sphereToSphere);
        methodsMap.putByFirstKey(PhysicalSphere.class, Wall.class, CollisionalPair::sphereToWall);
        methodsMap.putByFirstKey(PhysicalSphere.class, GravityPlate.class, CollisionalPair::sphereToWall);
        methodsMap.putByFirstKey(PhysicalSphere.class, PhysicalPolyhedron.class, CollisionalPair::sphereToPolyhedron);

        methodsMap.putByFirstKey(Wall.class, PhysicalSphere.class, CollisionalPair::sphereToWall);
        methodsMap.putByFirstKey(Wall.class, PhysicalPolyhedron.class, CollisionalPair::polyhedronToWall);
        methodsMap.putByFirstKey(GravityPlate.class, PhysicalPolyhedron.class, CollisionalPair::polyhedronToWall);
        methodsMap.putByFirstKey(GravityPlate.class, PhysicalPolyhedron.class, CollisionalPair::polyhedronToWall);

        methodsMap.putByFirstKey(PhysicalPolyhedron.class, Wall.class, CollisionalPair::polyhedronToWall);
        methodsMap.putByFirstKey(PhysicalPolyhedron.class, GravityPlate.class, CollisionalPair::polyhedronToWall);
        methodsMap.putByFirstKey(PhysicalPolyhedron.class, PhysicalSphere.class, CollisionalPair::sphereToPolyhedron);
        methodsMap.putByFirstKey(PhysicalPolyhedron.class, PhysicalPolyhedron.class, CollisionalPair::polyhedronToPolyhedron);
    }

    /**
     * Метод, распределяющий все коллизии между двумя объектами типа {@link limiters.Collisional}, вызывает метод непосредественно для определеннтй пары
     */

    public void collide() {
        methodsMap.getElement(firstThing.getClass(), secondThing.getClass()).collide(firstThing, secondThing);
    }

    /**Метод, обрабатывающий коллизию между двумя многогранниками
     * @param thing1 многогранник 1
     * @param thing2 многогранник 2
     */

    private static void polyhedronToPolyhedron(Collisional thing1, Collisional thing2) {
        PhysicalPolyhedron polyhedron1 = (PhysicalPolyhedron) thing1;
        PhysicalPolyhedron polyhedron2 = (PhysicalPolyhedron) thing2;

        Plane3D edgePlane = null;
        Point3D collisionPoint = null;
        Map<Plane3D, Point3D> params = new HashMap<>();
        ArrayList<Boolean> bTFs = new ArrayList<>();
        boolean bTF = true;
        boolean vInters = true;

        {

            for (Point3D point : polyhedron1.getPoints(true))
                for (Triangle triangle : polyhedron2.getTriangles(true))
                    if (triangle.isIntersectedWithSegment(new Segment(point, polyhedron1.getPositionOfCentre(true)))) {
//                        collisionPoint = point;
//                        edgePlane = triangle.getPlane();
                        params.put(triangle.getPlane(), point);
                        bTFs.add(true);
                    }

            for (Point3D point : polyhedron2.getPoints(true))
                for (Triangle triangle : polyhedron1.getTriangles(true))
                    if (triangle.isIntersectedWithSegment(new Segment(point, polyhedron2.getPositionOfCentre(true)))) {
//                        edgePlane = triangle.getPlane();
//                        collisionPoint = point;
                        params.put(triangle.getPlane(), point);
                        bTFs.add(false);
                    }
        }

        if (params.size() == 0) {
            vInters = false;
            {
                for (Segment segment : polyhedron1.getSegments(true))
                    for (Triangle triangle : polyhedron2.getTriangles(true))
                        if (triangle.isIntersectedWithSegment(segment)) {
//                            edgePlane = triangle.getPlane();
//                            collisionPoint = segment.getIntersection(triangle.getPlane()).get();
                            params.put(triangle.getPlane(), segment.getIntersection(triangle.getPlane()).get());
                            bTFs.add(true);
                        }

                for (Segment segment : polyhedron2.getSegments(true))
                    for (Triangle triangle : polyhedron1.getTriangles(true))
                        if (triangle.isIntersectedWithSegment(segment)) {
//                            edgePlane = triangle.getPlane();
//                            collisionPoint = segment.getIntersection(triangle.getPlane()).get();
                            params.put(triangle.getPlane(), segment.getIntersection(triangle.getPlane()).get());
                            bTFs.add(false);
                        }
            }
        }

        int i = 0;

        for (Map.Entry<Plane3D, Point3D> param : params.entrySet()) {
            Vector3D axisX = param.getKey().vector.normalize();
            Point3D collisionPoint1 = null;
            Point3D collisionPoint2 = null;

            if (!vInters) {
                collisionPoint2 = param.getValue();
                collisionPoint1 = param.getValue();
            }

            if (bTFs.get(i) && vInters) {
                collisionPoint1 = param.getValue();
                collisionPoint2 = param.getKey().getIntersection(new Line3D(polyhedron2.getPositionOfCentre(true), collisionPoint1)).get();
            } else if(vInters) {
                collisionPoint2 = param.getValue();
                collisionPoint1 = param.getKey().getIntersection(new Line3D(polyhedron1.getPositionOfCentre(true), collisionPoint2)).get();
            }

            Vector3D r1 = new Vector3D(polyhedron1.getPositionOfCentre(true), collisionPoint1);
            Vector3D r2 = new Vector3D(polyhedron2.getPositionOfCentre(true), collisionPoint2);
            Vector3D vel1 = polyhedron1.getVelOfPoint(collisionPoint1, true);
            Vector3D vel2 = polyhedron2.getVelOfPoint(collisionPoint2, true);
            Vector3D cVel1 = polyhedron1.getV();
            Vector3D cVel2 = polyhedron2.getV();

            Point3D pos1 = polyhedron1.getPositionOfCentre(true);
            Point3D pos2 = polyhedron2.getPositionOfCentre(true);

            Plane3D plane1 = new Plane3D(collisionPoint1, axisX.addToPoint(collisionPoint1), pos1);
            Plane3D plane2 = new Plane3D(collisionPoint2, axisX.addToPoint(collisionPoint2), pos2);


            final double k = Tools.countAverage(polyhedron1.getMaterial().coefOfReduction, polyhedron2.getMaterial().coefOfReduction);
            final double fr = Tools.countAverage(polyhedron1.getMaterial().coefOfFriction, polyhedron2.getMaterial().coefOfFriction);
            final double m1 = polyhedron1.getM();
            final double m2 = polyhedron2.getM();
            double J1 = polyhedron1.getJ(new Line3D(polyhedron1.getPositionOfCentre(true), plane1.vector), true);
            double J2 = polyhedron2.getJ(new Line3D(polyhedron2.getPositionOfCentre(true), plane2.vector), true);
            final double ratio = m1 / m2;
            double ry1 = r1.subtract(axisX.multiply(axisX.scalarProduct(r1))).getLength();
            double ry2 = r2.subtract(axisX.multiply(axisX.scalarProduct(r2))).getLength();

            double v2x = vel2.scalarProduct(axisX);
            double v1x = vel1.scalarProduct(axisX);
            double v1cx = cVel1.scalarProduct(axisX);
            double v2cx = cVel2.scalarProduct(axisX);
            double w1x = polyhedron1.getRotationVelOfPoint(collisionPoint1, true).scalarProduct(axisX) / ry1;
            double w2x = polyhedron2.getRotationVelOfPoint(collisionPoint2, true).scalarProduct(axisX) / ry2;

            double fw1x = (-k * (v1x - v2x) + v2cx - v1cx + (ratio + 1) * J1 * w1x / (m1 * ry1) + w2x * ry2 + J1 * ry2 * ry2 * w1x / (J2 * ry1)) /
                    ((ratio + 1) * J1 / (m1 * ry1) + ry1 + J1 * ry2 * ry2 / (J2 * ry1));

            double s = J1 * (fw1x - w1x) / ry1;

            Vector3D v1 = vel1.subtract(axisX.multiply(axisX.scalarProduct(vel1)));
            Vector3D v2 = vel2.subtract(axisX.multiply(axisX.scalarProduct(vel2)));

            Vector3D relativeVel1 = v1.subtract(v2);
            Vector3D relativeVel2 = v2.subtract(v1);

            Plane3D frictionPlane1 = new Plane3D(pos1, r1.addToPoint(pos1), v1.addToPoint(pos1));
            Plane3D frictionPlane2 = new Plane3D(pos2, r2.addToPoint(pos2), v2.addToPoint(pos2));

            J1 = polyhedron1.getJ(new Line3D(pos1, frictionPlane1.vector), true);
            J2 = polyhedron1.getJ(new Line3D(pos2, frictionPlane2.vector), true);
            double frictionRad1 = r1.subtract(relativeVel2.normalize().multiply(relativeVel2.normalize().scalarProduct(r1))).getLength();
            double frictionRad2 = r2.subtract(relativeVel1.normalize().multiply(relativeVel1.normalize().scalarProduct(r2))).getLength();


            Vector3D polyhedron1Friction1 = relativeVel1.multiply(-1d).multiply(s * fr);
            Vector3D polyhedron1Friction2 = relativeVel1.multiply(-1d / (1d / m1 + 1d / m2 + frictionRad2 * frictionRad2 / J2 + frictionRad1 * frictionRad1 / J1));

            if (polyhedron1Friction1.getLength() < polyhedron1Friction2.getLength()){
//                polyhedron1.getImpulses().add(new Pair<>(polyhedron1Friction1, collisionPoint1));
//                polyhedron2.getImpulses().add(new Pair<>(polyhedron1Friction1.multiply(-1d),collisionPoint2));
                polyhedron1.applyImpulse(polyhedron1Friction1, collisionPoint1, true);
                polyhedron2.applyImpulse(polyhedron1Friction1.multiply(-1d),collisionPoint2, true);
            }
            else{
//                polyhedron1.getImpulses().add(new Pair<>(polyhedron1Friction2, collisionPoint1));
//                polyhedron2.getImpulses().add(new Pair<>(polyhedron1Friction2.multiply(-1d),collisionPoint2));
                polyhedron1.applyImpulse(polyhedron1Friction2, collisionPoint1, true);
                polyhedron2.applyImpulse(polyhedron1Friction2.multiply(-1d),collisionPoint2, true);
            }

//            polyhedron1.getImpulses().add(new Pair<>(axisX.multiply(s), collisionPoint1));
//            polyhedron2.getImpulses().add(new Pair<>(axisX.multiply(-s), collisionPoint2));

            polyhedron1.applyImpulse(axisX.multiply(s), collisionPoint1, true);
            polyhedron2.applyImpulse(axisX.multiply(-s), collisionPoint2, true);

            i++;
        }


    }

    /**Метод, обрабатывающий коллизию между сферой и многогранником.
     * @param thing1 сфера или многогранник
     * @param thing2 многогранник или сфера
     */

    private static void sphereToPolyhedron(Collisional thing1, Collisional thing2) {
        PhysicalPolyhedron polyhedron;
        PhysicalSphere sphere;

        if (thing1 instanceof PhysicalPolyhedron) {
            polyhedron = (PhysicalPolyhedron) thing1;
            sphere = (PhysicalSphere) thing2;
        } else {
            sphere = (PhysicalSphere) thing1;
            polyhedron = (PhysicalPolyhedron) thing2;
        }

//        Plane3D edgePlane = null;
        HashSet<Plane3D> planes = new HashSet<>();

        try {
            for (Triangle triangle : polyhedron.getTriangles(true))
                if (new IntersectionalPair<>(sphere, triangle).areIntersected()) {
//                    edgePlane = triangle.getPlane();
                    planes.add(triangle.getPlane());
                }
        } catch (Exception ignored) {
        }

        for (Plane3D edgePlane : planes){
            Vector3D axisX = edgePlane.vector.normalize();

            Plane3D collisionPlane = new Plane3D(sphere.getPositionOfCentre(true),
                    polyhedron.getPositionOfCentre(true),
                    axisX.addToPoint(sphere.getPositionOfCentre(true)));

            final double k = Tools.countAverage(polyhedron.getMaterial().coefOfReduction, sphere.getMaterial().coefOfReduction);
            final double fr = Tools.countAverage(polyhedron.getMaterial().coefOfFriction, sphere.getMaterial().coefOfFriction);
            final double m2 = polyhedron.getM();
            final double m1 = sphere.getM();
            final double ratio = m1 / m2;
            final double J1 = sphere.getJ();
            double J2 = polyhedron.getJ(new Line3D(polyhedron.getPositionOfCentre(true), collisionPlane.vector), true);

            Line3D collisionLine = new Line3D(sphere.getPositionOfCentre(true), axisX);
            Point3D collisionPoint2 = collisionLine.getIntersection(edgePlane).get();
            Point3D collisionPoint1 = new Vector3D(sphere.getPositionOfCentre(true), collisionPoint2)
                    .normalize()
                    .multiply(sphere.getR())
                    .addToPoint(sphere.getPositionOfCentre(true));

            Vector3D pRadV = new Vector3D(polyhedron.getPositionOfCentre(true), collisionPoint2);

            double ry = pRadV.subtract(axisX.multiply(axisX.scalarProduct(pRadV))).getLength();
            double v1cx = sphere.getV().scalarProduct(axisX);
            double v2x = polyhedron.getVelOfPoint(collisionPoint2, true).scalarProduct(axisX);
            double wr2 = polyhedron.getRotationVelOfPoint(collisionPoint2, true).scalarProduct(axisX);

            double u1cx = ((ratio - k) * v1cx + v2x * (1 + k) + wr2 + (m1 * ry * ry * v1cx) / J2) / (1d + ratio + m1 * ry * ry / J2);
            double s = m1 * (u1cx - v1cx);

            Vector3D vel1 = Tools.calcProjectionOfVectorOnPlane(sphere.getVelOfPoint(collisionPoint1, true), edgePlane);
            Vector3D vel2 = Tools.calcProjectionOfVectorOnPlane(polyhedron.getVelOfPoint(collisionPoint2, true), edgePlane);
            Vector3D relativeVel2 = vel2.subtract(vel1);

            Plane3D frictionAndRadPlane = new Plane3D(polyhedron.getPositionOfCentre(true), collisionPoint2, vel2.addToPoint(collisionPoint2));
            J2 = polyhedron.getJ(new Line3D(polyhedron.getPositionOfCentre(true), frictionAndRadPlane.vector), true);

            double r2 = pRadV.subtract(vel2.normalize().multiply(vel2.normalize().scalarProduct(pRadV))).getLength();

            Vector3D polyhedronFriction1 = relativeVel2.normalize().multiply(-1d * fr * s);
            Vector3D polyhedronFriction2 = relativeVel2.multiply(-1d / (1d / m1 + 1d / m2 + r2 * r2 / J2 + sphere.getR() * sphere.getR() / J1));

            if (polyhedronFriction1.getLength() < polyhedronFriction2.getLength()) {
                polyhedron.applyImpulse(polyhedronFriction1, collisionPoint2, true);
                sphere.applyFriction(collisionPoint1, polyhedronFriction1.multiply(-1d));
            } else {
                polyhedron.applyImpulse(polyhedronFriction2, collisionPoint2, true);
                sphere.applyFriction(collisionPoint1, polyhedronFriction2.multiply(-1d));
            }


            Vector3D sphereImpulse = axisX.multiply(s);


            sphere.applyStrikeImpulse(sphereImpulse);
            polyhedron.applyImpulse(sphereImpulse.multiply(-1d), collisionPoint2, true);


        }


    }

    /**Метод, обрабатывающий коллизию между многогранником и стеной
     * @param thing1 многогранник или стена
     * @param thing2 стена или многогранник
     */

    private static void polyhedronToWall(Collisional thing1, Collisional thing2) {
        PhysicalPolyhedron polyhedron;
        Wall wall;

        if (thing1 instanceof PhysicalPolyhedron) {
            polyhedron = (PhysicalPolyhedron) thing1;
            wall = (Wall) thing2;
        } else {
            wall = (Wall) thing1;
            polyhedron = (PhysicalPolyhedron) thing2;
        }

        final double k = Tools.countAverage(polyhedron.getMaterial().coefOfReduction, wall.getMaterial().coefOfReduction);
        final double fr = Tools.countAverage(polyhedron.getMaterial().coefOfFriction, wall.getMaterial().coefOfFriction);
        final double m = polyhedron.getM();

        ArrayList<Point3D> collisionPoints = new ArrayList<>();


        for (Point3D point : polyhedron.getPoints(true)) {
            for (Triangle triangle : wall.getTriangles())
                if (triangle.isIntersectedWithSegment(new Segment(point, polyhedron.getPositionOfCentre(true)))) {
                    collisionPoints.add(point);
                    break;
                }
        }

        for (Point3D collisionPoint : collisionPoints) {
            Vector3D vel = polyhedron.getVelOfPoint(collisionPoint, true);

            Vector3D axisY = wall.getPlane().vector.normalize();




            Plane3D collisionPlane = new Plane3D(polyhedron.getPositionOfCentre(true),
                    collisionPoint,
                    axisY.addToPoint(collisionPoint));

            Plane3D frictionPlane = wall.getPlane();

            Vector3D vel1 = Tools.calcProjectionOfVectorOnPlane(vel, frictionPlane);


            Vector3D r = new Vector3D(polyhedron.getPositionOfCentre(true), collisionPoint);

            double J = polyhedron.getJ(new Line3D(polyhedron.getPositionOfCentre(true), collisionPlane.vector), true);
            double rx = r.subtract(axisY.multiply(r.scalarProduct(axisY))).getLength();
            double w1 = polyhedron.getRotationVelOfPoint(collisionPoint, true).scalarProduct(axisY) / rx;

            double vy = vel.scalarProduct(axisY);
            double vc = polyhedron.getV().scalarProduct(axisY);

            double w2 = (J * w1 + rx * m * (-k * vy - vc)) / (J + rx * rx * m);
            double s = J * (w2 - w1) / rx;

            Vector3D normalizedVel1 = vel1.normalize();

            double r1 = r.subtract(normalizedVel1.multiply(normalizedVel1.scalarProduct(r))).getLength();


            Plane3D frictionAndRadPlane = new Plane3D(collisionPoint,
                    r.addToPoint(collisionPoint),
                    vel1.addToPoint(collisionPoint));

            double J1 = polyhedron.getJ(new Line3D(polyhedron.getPositionOfCentre(true), frictionAndRadPlane.vector),
                    true);

            Vector3D friction1 = normalizedVel1.multiply(-1d * Math.abs(fr * s));
            Vector3D friction2 = vel1.multiply(-1d * (1d / (1d / m + r1 * r1 / J1)));

            if (friction1.getLength() > friction2.getLength())
                polyhedron.applyImpulse(friction2, collisionPoint, true);
            else
                polyhedron.applyImpulse(friction1, collisionPoint, true);


            polyhedron.applyImpulse(axisY.multiply(s), collisionPoint, true);
        }

    }


    /**Метод, обрабатывающий коллизию между двумя сферами
     * @param thing1 сфера 1
     * @param thing2 сфера 2
     */

    private static void sphereToSphere(Collisional thing1, Collisional thing2) {
        PhysicalSphere sphere1 = (PhysicalSphere) thing1;
        PhysicalSphere sphere2 = (PhysicalSphere) thing2;

        final Point3D firstSpherePos = sphere1.getPositionOfCentre(true);
        final Point3D secondSpherePos = sphere2.getPositionOfCentre(true);
        final Vector3D axisX = new Vector3D(firstSpherePos.x - secondSpherePos.x,
                firstSpherePos.y - secondSpherePos.y, firstSpherePos.z - secondSpherePos.z);
        final double axisXLen = axisX.getLength();

        final double m1 = sphere1.getM();
        final double m2 = sphere2.getM();
        final double ratio = m1 / m2;
        final double k = Tools.countAverage(sphere1.getMaterial().coefOfReduction, sphere2.getMaterial().coefOfReduction);
        final double fr = Tools.countAverage(sphere1.getMaterial().coefOfFriction, sphere2.getMaterial().coefOfFriction);

        final double r1 = sphere1.getR();
        final double r2 = sphere2.getR();

        final double v1x = (sphere1.getV().scalarProduct(axisX) / axisXLen);
        final double v2x = (sphere2.getV().scalarProduct(axisX) / axisXLen);
        final double u1x = ((ratio - k) * v1x + (k + 1) * v2x) / (ratio + 1);
        final double u2x = ((ratio * (k + 1)) * v1x + (1 - k * ratio) * v2x) / (ratio + 1);
        final double s = (m1 * m2) / (m1 + m2) * (1f + k) * Math.abs(v1x - v2x);

        final Plane3D frictionPlane = new Plane3D(axisX, firstSpherePos);

        final Point3D collisionPoint1 = axisX.multiply(-r1 / axisXLen).addToPoint(sphere1.getPositionOfCentre(true));
        final Point3D collisionPoint2 = axisX.multiply(r2 / axisXLen).addToPoint(sphere2.getPositionOfCentre(true));

        final Vector3D vel1 = Tools.calcProjectionOfVectorOnPlane(sphere1.getVelOfPoint(collisionPoint1, true), frictionPlane);
        final Vector3D vel2 = Tools.calcProjectionOfVectorOnPlane(sphere2.getVelOfPoint(collisionPoint2, true), frictionPlane);

        Vector3D relativeVel2 = vel2.subtract(vel1);
        Vector3D relativeVel1 = vel1.subtract(vel2);

        Vector3D firstSphereFriction1 = relativeVel2.multiply(fr * s / relativeVel2.getLength());
        Vector3D firstSphereFriction2 = relativeVel2.multiply(m1 * m2 / (3.5 * (m1 + m2)));

        if (firstSphereFriction1.getLength() < firstSphereFriction2.getLength()) {
            sphere1.getFrictionImpulses().add(new Pair<>(firstSphereFriction1, collisionPoint1));
            sphere2.getFrictionImpulses().add(new Pair<>(firstSphereFriction1.multiply(-1), collisionPoint2));
        } else {
            sphere1.getFrictionImpulses().add(new Pair<>(firstSphereFriction2, collisionPoint1));
            sphere2.getFrictionImpulses().add(new Pair<>(firstSphereFriction2.multiply(-1), collisionPoint2));
        }


        sphere1.getImpulses().add(axisX.multiply(m1 * (u1x - v1x) / axisXLen));
        sphere2.getImpulses().add(axisX.multiply(m2 * (u2x - v2x) / axisXLen));
    }

    /**Метод, обрабатывающий коллизию между сферой и стеной
     * @param thing1 сфера или стена
     * @param thing2 стена или сфера
     */

    private static void sphereToWall(Collisional thing1, Collisional thing2) {
        PhysicalSphere sphere;
        Wall wall;

        if (thing1 instanceof PhysicalSphere) {
            sphere = (PhysicalSphere) thing1;
            wall = (Wall) thing2;
        } else {
            wall = (Wall) thing1;
            sphere = (PhysicalSphere) thing2;
        }

        final double k = Tools.countAverage(sphere.getMaterial().coefOfReduction, wall.getMaterial().coefOfReduction);
        final double fr = Tools.countAverage(sphere.getMaterial().coefOfFriction, wall.getMaterial().coefOfFriction);
        final double m = sphere.getM();

        Vector3D axisY = wall.getPlane().vector;

        if (sphere.getV().scalarProduct(axisY) > 0)
            axisY = axisY.multiply(-1);

        final double axisYLen = axisY.getLength();

        final Point3D collisionPoint = axisY.multiply(-sphere.getR() / axisYLen).addToPoint(sphere.getPositionOfCentre(true));

        final double vy = Math.abs(sphere.getV().scalarProduct(axisY) / axisY.getLength());
        final double s = (1f + k) * sphere.getM() * vy;


        Vector3D v = Tools.calcProjectionOfVectorOnPlane(sphere.getV(), wall.getPlane());
        Vector3D angularVel = sphere.getRotationVelOfPoint(collisionPoint, true);

        final Vector3D velOfCollisionPoint = v.add(angularVel);


        Vector3D frictionImpulse1 = velOfCollisionPoint.multiply(-fr * Math.abs(s) / velOfCollisionPoint.getLength());
        Vector3D frictionImpulse2 = velOfCollisionPoint.multiply(-m / 3.5d);

        if (frictionImpulse1.getLength() < frictionImpulse2.getLength()) {
            sphere.applyFriction(collisionPoint, frictionImpulse1);
        } else {
            sphere.applyFriction(collisionPoint, frictionImpulse2);
        }


        sphere.applyStrikeImpulse(axisY.multiply(s / axisYLen));


    }
}
