package physical_objects;

import drawing.Drawable;
import exceptions.ImpossibleObjectException;
import geometry.objects3D.Point3D;
import geometry.objects3D.Vector3D;
import physics.Material;
import physics.Space;

import java.awt.image.ImagingOpException;

public abstract class AbstractBody implements Drawable{

    protected double x0, y0, z0;
    protected Vector3D v;
    protected Vector3D w;
    protected final Material material;
    protected final double m;
    protected final Space space;

    protected AbstractBody(Space space, double x0, double y0, double z0, Vector3D v, Vector3D w, Material material, double m) throws ImpossibleObjectException {
        this.space = space;
        this.v = v;
        this.w = w;
        this.x0 = x0;
        this.y0 = y0;
        this.z0 = z0;
        this.material = material;
        this.m = m;
        if (m <= 0d) throw new ImpossibleObjectException("Impossible object; mass is null");
    }

    public synchronized void update() {
        changeSpeed();
        x0 += v.x * space.getDT();
        y0 += v.y * space.getDT();
        z0 -= (v.z + v.z - space.getG() * space.getDT()) * space.getDT() / 2.0f;
        updateDrawingInterpretation();
    }

    private synchronized void changeSpeed() {
        v = new Vector3D(v.x, v.y, v.z + space.getG() * space.getDT());
    }

    public final synchronized Point3D getPositionOfCentre(boolean mode) {
        double m = mode ? 1.0f : 0.0f;
        return new Point3D(x0 + m * v.x * space.getDT(),
                y0 + m * v.y * space.getDT(),
                z0 - m * ((v.z + v.z + space.getG() * space.getDT()) * space.getDT() / 2.0f));
    }

    public double getM() {
        return m;
    }

    public Vector3D getV() {
        return v;
    }

    public Vector3D getW() {
        return w;
    }

    public Material getMaterial() {
        return material;
    }

    public Space getSpace() {
        return space;
    }

    public void setV(Vector3D v) {
        this.v = v;
    }

    public void setW(Vector3D w) {
        this.w = w;
    }
}