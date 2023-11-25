package simulator;

public class Wall {

    public Point start;
    public Point end;

    public Wall(Point start, Point end) {
        this.start = start;
        this.end = end;
    }

    public Point getStart() {
        return start;
    }

    public Point getEnd() {
        return end;
    }


}