package simulator;

public class Wall {

    public Point start;
    public Point end;
    public boolean isCurveD;
    public boolean isCurveU;

    public Wall(Point start, Point end) {
        this.start = start;
        this.end = end;
        this.isCurveD = false;
        this.isCurveU = false;
    }

    public Point getStart() {
        return start;
    }

    public Point getEnd() {
        return end;
    }


}