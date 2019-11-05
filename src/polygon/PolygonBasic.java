package polygon;

import java.awt.*;
import java.util.ArrayList;

public class PolygonBasic {
    public ArrayList<ArrayList<point>> polygonList;
    public boolean isCorrect = true;
    public boolean isFinished = false;

    public class point {
        double xVal, yVal;

        point(int x, int y) {
            xVal = (double) x;
            yVal = (double) y;
        }

        public int xInt() {
            return (int) xVal;
        }

        public int yInt() {
            return (int) yVal;
        }
    }

    public PolygonBasic() {
        polygonList = new ArrayList<>();
    }

    public void init(int xVal, int yVal) {
        int delta = 5;
        if (isCorrect) {
            ArrayList<point> pointList = new ArrayList<>();
            pointList.add(new point(xVal, yVal));
            polygonList.add(pointList);
            isCorrect = false;
        } else {
            point startPoint = polygonList.get(polygonList.size() - 1).get(0);
            if (Math.abs(xVal - startPoint.xVal) + Math.abs(yVal - startPoint.yVal) < delta) {
                isCorrect = true;
            } else {
                ArrayList<point> cList = polygonList.get(polygonList.size() - 1);
                cList.add(new point(xVal, yVal));
            }
        }
    }

    public void drawWhenInit(Graphics graphics) {
        Graphics2D graphics2D = (Graphics2D) graphics;
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        graphics.setColor(Color.BLACK);
        for (int i = 0; i < polygonList.size() - 1; i++) {
            ArrayList<point> pointList = polygonList.get(i);
            for (int j = 0; j < pointList.size() - 1; j++) {
                graphics2D.drawLine(pointList.get(j).xInt(), pointList.get(j).yInt(),
                        pointList.get(j + 1).xInt(), pointList.get(j + 1).yInt());
            }
            graphics2D.drawLine(pointList.get(pointList.size() - 1).xInt(), pointList.get(pointList.size() - 1).yInt(),
                    pointList.get(0).xInt(), pointList.get(0).yInt());
        }

        if (polygonList.size() == 0) {
            return;
        }

        ArrayList<point> cList = polygonList.get(polygonList.size() - 1);
        if (cList.size() == 1) {
            graphics2D.fillOval(cList.get(0).xInt(), cList.get(0).yInt(), 2, 2);
        } else {
            for (int j = 0; j < cList.size() - 1; j++) {
                graphics2D.drawLine(cList.get(j).xInt(), cList.get(j).yInt(),
                        cList.get(j + 1).xInt(), cList.get(j + 1).yInt());
            }
            if (isCorrect) {
                graphics2D.drawLine(cList.get(cList.size() - 1).xInt(), cList.get(cList.size() - 1).yInt(),
                        cList.get(0).xInt(), cList.get(0).yInt());
            }
        }
    }
}
