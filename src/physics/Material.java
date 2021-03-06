package physics;

import java.awt.*;

/**
 * Все доступные материалы
 */
public enum Material {

    STEEL(7900, new Color(175, 175, 175),0.5f, 0.1f),
    WOOD(500, new Color(124, 67, 11),1f, 0f),
    STONE(2500, new Color(90, 90, 90),1f, 0.5f),
    GOLD(19300, new Color(238, 198, 0),0f, 0.05f),
    LAZULI(2500, new Color(0, 34, 255), 0.9f, 0.1f),
    OSMIUM(22500, new Color(148, 157, 191),1f, 0f),
    CONSTANTIN(1000, Color.RED, 0.7f, 1f);

    public final double p;
    public transient final Color fillColor;
    public final double coefOfReduction;
    public final double coefOfFriction;

    /**
     * Констуктор, принимающий все необходимые параметры, создающий новый материал
     * @param p плотность материала
     * @param fillColor цвет материала
     * @param coefOfReduction коэффициент восстановления материала
     * @param coefOfFriction коэффициент трения материала
     */
    Material(double p, Color fillColor, double coefOfReduction, double coefOfFriction) {
        this.p = p;
        this.coefOfReduction = coefOfReduction;
        this.coefOfFriction = coefOfFriction;
        this.fillColor = fillColor;
    }
}
