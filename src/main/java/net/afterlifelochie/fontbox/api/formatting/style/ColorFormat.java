package net.afterlifelochie.fontbox.api.formatting.style;

public class ColorFormat implements Cloneable {
    public int red;
    public int green;
    public int blue;
    public int alpha;

    public ColorFormat(int red, int green, int blue) {
        this(red, green, blue, 255);
    }

    public ColorFormat(int red, int green, int blue, int alpha) {
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;
    }

    public float redF() {
        return red / 255.0f;
    }

    public float greenF() {
        return green / 255.0f;
    }

    public float blueF() {
        return blue / 255.0f;
    }

    public float alphaF() {
        return alpha / 255.0f;
    }

    @Override
    public ColorFormat clone() {
        return new ColorFormat(red, green, blue, alpha);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ColorFormat))
            return false;
        ColorFormat that = (ColorFormat) o;
        if (that.red != red || that.green != green || that.blue != blue || that.alpha != alpha)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return String.format("ColorFormat{r=%s, g=%s, b=%s, a=%s}", red, green, blue, alpha);
    }
}
