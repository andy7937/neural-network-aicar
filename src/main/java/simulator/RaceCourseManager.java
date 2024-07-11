package simulator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RaceCourseManager {

    private List<Wall> raceCourse;
    private List<Point> barriers;
    private int radius = 180;

    public void initializeRaceCourse(List<Wall> raceCourse) {
        // Initialize race course walls and barriers
    }

    public Wall createWall(Point start, Point end) {
        Wall wall = new Wall(start, end);
        return wall;
    }

    public Wall createCurveD(Point start) {
        Wall wall = new Wall(start, null);
        wall.isCurveD = true;
        return wall;
    }

    public Wall createCurvedU(Point start) {
        Wall wall = new Wall(start, null);
        wall.isCurveU = true;
        return wall;
    }

    public void initializeRaceCourse() {

        raceCourse = new ArrayList<>(); // Initialize the raceCourse list

        // wall behind start
        raceCourse.add(createWall(new Point(30, 60), new Point(200, 60)));

        // first straight line
        raceCourse.add(createWall(new Point(30, 60), new Point(30, 800)));
        raceCourse.add(createWall(new Point(200, 60), new Point(200, 800)));

        // first curve up (from cars perspective)
        raceCourse.add(createCurvedU(new Point(30 + radius, 800)));

        // wall from end of curveU striaght up
        raceCourse.add(createWall(new Point(30 + radius, 800), new Point(30 + radius, 200)));
        raceCourse.add(createWall(new Point(210 + radius, 800), new Point(210 + radius, 200)));

        // small walls between curveU and curveD
        raceCourse.add(createWall(new Point(40 + radius, 600), new Point(100 + radius, 550)));
        raceCourse.add(createWall(new Point(200 + radius, 450), new Point(140 + radius, 400)));


        // second curve down (from cars perspective)
        raceCourse.add(createCurveD(new Point(30 + radius + radius, 200)));

        // wall from end of curveD tilted straight right
        raceCourse.add(createWall(new Point(210 + radius, 200), new Point(450 + radius, 500)));
        raceCourse.add(createWall(new Point(390 + radius , 200), new Point(630 + radius, 500)));

        // wall from tilted straight right to tiled straight left
        raceCourse.add(createWall(new Point(450 + radius, 500), new Point(210 + radius, 800)));
        raceCourse.add(createWall(new Point(630 + radius , 500), new Point(390 + radius, 800)));


        // second curve up (from cars perspective)
        raceCourse.add(createCurvedU(new Point(30 + radius * 3, 800)));

        // from second curve, getting smaller wall
        raceCourse.add(createWall(new Point(30 + radius * 4, 800), new Point(30 + radius * 4 + 120, 500)));

        // opening tunnel from tight wall
        raceCourse.add(createWall(new Point(30 + radius * 4 + 120, 500), new Point(30 + radius * 4 + 360, 200)));

        // small walls in the tunnel

        // first row of small walls
        raceCourse.add(createWall(new Point(30 + radius * 4 + 10, 350), new Point(30 + radius * 4 + 30, 350)));
        raceCourse.add(createWall(new Point(30 + radius * 4 + 80, 350), new Point(30 + radius * 4 + 100, 350)));
        raceCourse.add(createWall(new Point(30 + radius * 4 + 150, 350), new Point(30 + radius * 4 + 170, 350)));


        // second row of small walls
        raceCourse.add(createWall(new Point(30 + radius * 3 + 40, 250), new Point(30 + radius * 3 + 60, 250)));
        raceCourse.add(createWall(new Point(30 + radius * 4, 250), new Point(30 + radius * 4 + 20, 250)));
        raceCourse.add(createWall(new Point(30 + radius * 4 + 160, 250), new Point(30 + radius * 4 + 180, 250)));
        raceCourse.add(createWall(new Point(30 + radius * 5 + 120, 250), new Point(30 + radius * 5 + 140, 250)));


        raceCourse.add(createCurveD(new Point(radius * 4 + 30, 200)));
        raceCourse.add(createCurveD(new Point(radius * 6 + 30, 200)));



        // Initialize the barriers list
        initBarrier();

    }

    public void initBarrier() {
        barriers = new ArrayList<>();
    
        for (Wall wall : raceCourse) {
            Point start = wall.getStart();
            Point end = wall.getEnd();
    
            // wall is curved downwards
            if (wall.isCurveD) {
    
                // Iterate through angles to create points on the semi-circle
                for (int angle = 180; angle <= 360; angle++) {
                    int x = start.x + (int) (radius * Math.cos(Math.toRadians(angle)));
                    int y = start.y + (int) (radius * Math.sin(Math.toRadians(angle)));
                    barriers.add(new Point(x, y));
                }
            } else if (wall.isCurveU) {    
                // Iterate through angles to create points on the semi-circle
                for (int angle = 0; angle <= 180; angle++) {
                    int x = start.x + (int) (radius * Math.cos(Math.toRadians(angle)));
                    int y = start.y + (int) (radius * Math.sin(Math.toRadians(angle)));
                    barriers.add(new Point(x, y));
                }
            } else {
                // wall is a straight line
                for (int t = 0; t <= 100; t++) {
                    int x = start.x + t * (end.x - start.x) / 100;
                    int y = start.y + t * (end.y - start.y) / 100;
                    barriers.add(new Point(x, y));
                }
            }
        }
    }
}
