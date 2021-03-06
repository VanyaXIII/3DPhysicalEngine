package physics;


import exceptions.ImpossiblePairException;
import geometry.intersections.IntersectionalPair;
import geometry.objects.Triangle;
import geometry.intersections.PolyhedronToPlaneIntersection;
import geometry.intersections.SphereToPlaneIntersection;
import geometry.intersections.SpheresIntersection;
import physical_objects.PhysicalPolyhedron;
import physical_objects.PhysicalSphere;
import physical_objects.Wall;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;

/**
 * Обработчик физики
 */
public class PhysicsHandler {

    private final ArrayList<PhysicalSphere> spheres;
    private final ArrayList<Wall> walls;
    private final ArrayList<PhysicalPolyhedron> polyhedrons;
    private final int depth;

    /**
     * Конструктор по пространству, физику в котором надо рассчитать и глубине просчета
     * @param space пространство
     * @param depth глубина просчета
     */
    PhysicsHandler(Space space, int depth) {
        spheres = space.getSpheres();
        walls = space.getWalls();
        polyhedrons = space.getPolyhedrons();
        this.depth = depth;
    }

    /**
     * Метод, обрабатывающий всю физику <b>depth</b> раз
     * @throws InterruptedException исключение в случае прерывания потока
     */
    public void update() throws InterruptedException {
        for (int i = 0; i< depth; i++)
            handlePhysics();
    }

    /**
     * Метод, обрабатывающий физику
     * @throws InterruptedException исключение в случае прерывания потока
     * @throws ConcurrentModificationException исключение в случае изменения коллекции при ее итерации
     */
    private void handlePhysics() throws InterruptedException, ConcurrentModificationException {
        Thread sphereThread = new Thread(() -> {
            for (int i = 0; i < spheres.size() - 1; i++) {
                for (int j = i + 1; j < spheres.size(); j++) {
                    synchronized (spheres.get(i)) {
                        synchronized (spheres.get(j)) {
                            try {
                                if (new IntersectionalPair<>(spheres.get(i), spheres.get(j)).areIntersected()) {
                                    new CollisionalPair<>(spheres.get(i), spheres.get(j)).collide();
                                }
                                SpheresIntersection spherePair = new IntersectionalPair<>(spheres.get(i), spheres.get(j)).getSpheresIntersection();
                                if (spherePair.areIntersected) {
                                    spheres.get(i).pullFromSphere(spherePair);
                                }
                            } catch (ImpossiblePairException e) {
                                e.printStackTrace();
                            }

                        }
                    }
                }
            }
            spheres.forEach(sphere -> {
                for (Wall wall : walls) {
                    try {
                        for (Triangle triangle : wall.getTriangles()) {
                            if (new IntersectionalPair<>(sphere, triangle).areIntersected()) {
                                new CollisionalPair<>(sphere, wall).collide();
                            }
                            SphereToPlaneIntersection pair = new IntersectionalPair<>(sphere, triangle).getSphereToPlaneIntersection();
                            if (pair.areIntersected)
                                sphere.pullFromPlane(pair);
                        }
                    } catch (ImpossiblePairException e) {
                        e.printStackTrace();
                    }
                }
            });
        });

        Thread polyhedronThread = new Thread(() -> {

            for (int i = 0; i < polyhedrons.size() - 1; i++) {
                for (int j = i + 1; j < polyhedrons.size(); j++) {
                    synchronized (polyhedrons.get(i)) {
                        synchronized (polyhedrons.get(j)) {
                            try {
                                try {
                                    if (new IntersectionalPair<>(polyhedrons.get(i), polyhedrons.get(j)).areIntersected())
                                        new CollisionalPair<>(polyhedrons.get(i), polyhedrons.get(j)).collide();
                                }
                                catch (Exception ignored){}
                                for (Triangle triangle : polyhedrons.get(i).getTriangles(false)){
                                    PolyhedronToPlaneIntersection pair = new IntersectionalPair<>(polyhedrons.get(j), triangle).getPolyhedronToPlaneIntersection();
                                    if (pair.areIntersected)
                                        polyhedrons.get(j).pullFromPlane(pair);
                                }
                                for (Triangle triangle : polyhedrons.get(j).getTriangles(false)){
                                    PolyhedronToPlaneIntersection pair = new IntersectionalPair<>(polyhedrons.get(i), triangle).getPolyhedronToPlaneIntersection();
                                    if (pair.areIntersected)
                                        polyhedrons.get(i).pullFromPlane(pair);
                                }

                            } catch (ImpossiblePairException e) {
                                e.printStackTrace();
                            }

                        }
                    }
                }
            }


            polyhedrons.forEach(polyhedron -> {
            for (Wall wall : walls) {
                try {
                    for (Triangle triangle : wall.getTriangles()) {
                        if (new IntersectionalPair<>(polyhedron, triangle).areIntersected()) {
                            new CollisionalPair<>(polyhedron, wall).collide();
                        }
                        PolyhedronToPlaneIntersection pair = new IntersectionalPair<>(polyhedron, triangle).getPolyhedronToPlaneIntersection();
                        if (pair.areIntersected)
                            polyhedron.pullFromPlane(pair);
                    }
                }
                catch (Exception ignored){}
            }
            for (PhysicalSphere sphere : spheres) {
                try {
                        if (new IntersectionalPair<>(polyhedron, sphere).areIntersected()) {
                            new CollisionalPair<>(polyhedron, sphere).collide();
                        }
                    for(Triangle triangle : polyhedron.getTriangles(false)) {
                        SphereToPlaneIntersection pair = new IntersectionalPair<>(sphere, triangle).getSphereToPlaneIntersection();
                        if (pair.areIntersected) {
//                            sphere.pullFromPlane(pair);
                        }
                    }
                } catch (ImpossiblePairException e) {
                    e.printStackTrace();
                }
            }
        });});

        sphereThread.start();
        polyhedronThread.start();
        sphereThread.join();
        polyhedronThread.join();

//        System.out.println("Физика: " + ((System.nanoTime() - time1) / 1000000.0));

        sphereThread = new Thread(() -> {
            synchronized (spheres) {
                spheres.forEach(PhysicalSphere::update);
            }
        });

        polyhedronThread = new Thread(() -> {
            synchronized (polyhedrons) {
                polyhedrons.forEach(PhysicalPolyhedron::update);
            }
        });

        sphereThread.start();
        polyhedronThread.start();
        polyhedronThread.join();
        sphereThread.join();

    }


}

