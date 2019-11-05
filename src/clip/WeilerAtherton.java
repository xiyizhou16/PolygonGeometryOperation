package clip;

import polygon.PolygonBasic;
import polygon.PolygonDraw;

import java.util.ArrayList;

public class WeilerAtherton {
    private static int flagRaw = 0, flagCross = 1, flagEnter = 2, flagOut = 3;

    private class waPoint {
        boolean isMain, isVisited;
        int xVal, yVal, flag;
        waPoint pre, next, map;

        waPoint(int x, int y, int flag, boolean isMain) {
            xVal = x;
            yVal = y;
            this.flag = flag;
            this.isMain = isMain;
            map = null;
            pre = null;
            next = null;
            isVisited = false;
        }
    }

    private class waPointList {
        waPoint head = null;
        waPoint tail = null;

        void addRawPoint(PolygonBasic.point p, boolean isMain) {
            waPoint cNode = new waPoint(p.xInt(), p.yInt(), flagRaw, isMain);
            if (head == null) {
                head = cNode;
                tail = cNode;
            } else {
                cNode.pre = tail;
                tail.next = cNode;
                tail = cNode;
            }
        }

        void insertCrossPoint(PolygonBasic.point startPoint, waPoint crossPoint) {
            waPoint cNode = head;
            while (cNode.next != null) {
                if (startPoint.xInt() == cNode.xVal && startPoint.yInt() == cNode.yVal) {
                    int xDistance = Math.abs(cNode.xVal - crossPoint.xVal);
                    int yDistance = Math.abs(cNode.yVal - crossPoint.yVal);
                    waPoint tempNode = cNode.next;
                    while (tempNode != null) {
                        if (Math.abs(cNode.xVal - tempNode.xVal) > xDistance ||
                                Math.abs(cNode.yVal - tempNode.yVal) > yDistance) {
                            tempNode.pre.next = crossPoint;
                            crossPoint.pre = tempNode.pre;
                            tempNode.pre = crossPoint;
                            crossPoint.next = tempNode;
                            break;
                        }
                        tempNode = tempNode.next;
                    }
                    break;
                }
                cNode = cNode.next;
            }
        }

        waPoint getCrossPoint(PolygonBasic clipPolygon, boolean[] isInnerClip) {
            waPoint cNode = head;
            while (cNode.next != null) {
                if (cNode.flag != flagRaw && !cNode.isVisited) {
                    waPoint tempNode = cNode;
                    int count = 0;
                    while (tempNode.flag != flagRaw) {
                        tempNode = tempNode.next;
                        count += 1;
                    }
                    if (waPointInClipPolygon(tempNode, clipPolygon, isInnerClip)) {
                        cNode.flag = count % 2 == 1 ? flagEnter : flagOut;
                    } else {
                        cNode.flag = count % 2 == 1 ? flagOut : flagEnter;
                    }
                    cNode.isVisited = true;
                    return cNode;
                }
                cNode = cNode.next;
            }
            return null;
        }

        private boolean waPointInClipPolygon(waPoint rawPoint, PolygonBasic clipPolygon, boolean[] isInnerClip) {
            boolean inPolygon = true;
            for (int i = 0; i < clipPolygon.polygonList.size(); i++) {
                if (waPointInCircle(rawPoint, clipPolygon.polygonList.get(i))) {
                    if (isInnerClip[i]) {
                        inPolygon = false;
                        break;
                    }
                } else {
                    if (!isInnerClip[i]) {
                        inPolygon = false;
                        break;
                    }
                }
            }
            return inPolygon;
        }

        private boolean waPointInCircle(waPoint p, ArrayList<PolygonBasic.point> circle) {
            int i, j;
            boolean result = false;
            for (i = 0, j = circle.size() - 1; i < circle.size(); j = i++) {
                if ((circle.get(i).yInt() > p.yVal) != (circle.get(j).yInt() > p.yVal) &&
                        p.xVal < (circle.get(j).xInt() - circle.get(i).xInt()) * (p.yVal - circle.get(i).yInt())
                                / (circle.get(j).yInt() - circle.get(i).yInt()) + circle.get(i).xInt()) {
                    result = !result;
                }
            }
            return result;
        }

        ArrayList<ArrayList<waPoint>> getNoIntersectionCircleList() {
            waPoint circleStartNode = head;
            ArrayList<ArrayList<waPoint>> waCircleList = new ArrayList<>();

            while (circleStartNode != null) {
                waPoint cNode = circleStartNode.next;
                boolean noIntersection = true;

                while (cNode.map != circleStartNode) {
                    if (cNode.flag != flagRaw) {
                        noIntersection = false;
                        break;
                    }
                    cNode = cNode.next;
                }

                if (noIntersection) {
                    ArrayList<waPoint> waCircle = new ArrayList<>();
                    while (circleStartNode.map == null) {
                        waCircle.add(circleStartNode);
                        circleStartNode = circleStartNode.next;
                    }
                    waCircleList.add(waCircle);
                } else {
                    circleStartNode = cNode;
                }

                while (circleStartNode.map == null || circleStartNode.flag != flagRaw) {
                    circleStartNode = circleStartNode.next;
                }
                circleStartNode = circleStartNode.next;
            }

            return waCircleList;
        }
    }

    public PolygonDraw clip(PolygonDraw mainPolygon, PolygonBasic clipPolygon) {
        if (mainPolygon.polygonList.size() == 0) {
            return new PolygonDraw();
        }

        // 内圈外圈判断
        boolean[] isInnerMain = new boolean[mainPolygon.polygonList.size()];
        boolean[] isInnerClip = new boolean[clipPolygon.polygonList.size()];
        circlePropertyJudge(mainPolygon.polygonList, isInnerMain);
        circlePropertyJudge(clipPolygon.polygonList, isInnerClip);

        // 旋转顺序判断，外圈顺时针置为 false， 内圈逆时针置为 false
        boolean[] reverseMain = new boolean[mainPolygon.polygonList.size()];
        boolean[] reverseClip = new boolean[clipPolygon.polygonList.size()];

        for (int i = 0; i < mainPolygon.polygonList.size(); i++) {
            reverseMain[i] = isClockwise(mainPolygon.polygonList.get(i)) == isInnerMain[i];
        }
        for (int i = 0; i < clipPolygon.polygonList.size(); i++) {
            reverseClip[i] = isClockwise(clipPolygon.polygonList.get(i)) == isInnerClip[i];
        }

        // 填充 waPointList
        waPointList mainList = new waPointList();
        waPointList clipList = new waPointList();
        waPointListInit(mainList, mainPolygon.polygonList, reverseMain, true);
        waPointListInit(clipList, clipPolygon.polygonList, reverseClip, false);

        for (int mainListIndex = 0; mainListIndex < mainPolygon.polygonList.size(); mainListIndex++) {
            ArrayList<PolygonBasic.point> mainPointList = mainPolygon.polygonList.get(mainListIndex);
            for (int mainPointIndex = 0; mainPointIndex < mainPointList.size(); mainPointIndex++) {
                int mainEndIndex = mainPointIndex + 1 < mainPointList.size() ? mainPointIndex + 1 : 0;

                for (int clipListIndex = 0; clipListIndex < clipPolygon.polygonList.size(); clipListIndex++) {
                    ArrayList<PolygonBasic.point> clipPointList = clipPolygon.polygonList.get(clipListIndex);
                    for (int clipPointIndex = 0; clipPointIndex < clipPointList.size(); clipPointIndex++) {
                        int clipEndIndex = clipPointIndex + 1 < clipPointList.size() ? clipPointIndex + 1 : 0;

                        waPoint crossPointMain = getPOI(mainPointList.get(mainPointIndex), mainPointList.get(mainEndIndex),
                                clipPointList.get(clipPointIndex), clipPointList.get(clipEndIndex));

                        if (crossPointMain != null) {
                            crossPointMain.isMain = true;
                            waPoint crossPointClip = new waPoint(crossPointMain.xVal, crossPointMain.yVal, flagCross, false);
                            crossPointMain.map = crossPointClip;
                            crossPointClip.map = crossPointMain;

                            int mainIndex = reverseMain[mainListIndex] ? mainEndIndex : mainPointIndex;
                            int clipIndex = reverseClip[clipListIndex] ? clipEndIndex : clipPointIndex;
                            mainList.insertCrossPoint(mainPointList.get(mainIndex), crossPointMain);
                            clipList.insertCrossPoint(clipPointList.get(clipIndex), crossPointClip);
                        }
                    }
                }
            }
        }

        // 新建多边形顶点表
        PolygonDraw newMainPolygon = new PolygonDraw();
        waPoint waStartPoint = mainList.getCrossPoint(clipPolygon, isInnerClip);

        while (waStartPoint != null) {
            newMainPolygon.init(waStartPoint.xVal, waStartPoint.yVal);

            waPoint waCrossPoint = waStartPoint;
            while (true) {
                waCrossPoint.map.flag = waCrossPoint.flag;
                if ((waCrossPoint.flag == flagEnter && !waCrossPoint.isMain) ||
                        (waCrossPoint.flag == flagOut && waCrossPoint.isMain)) {
                    waCrossPoint = waCrossPoint.map;
                    waCrossPoint.isVisited = true;
                }

                waPoint waRawPoint = waCrossPoint.next;
                while (waRawPoint.flag == flagRaw) {
                    newMainPolygon.init(waRawPoint.xVal, waRawPoint.yVal);
                    waRawPoint.isVisited = true;
                    if (waRawPoint.map != null) {
                        waRawPoint.map.isVisited = true;
                        waRawPoint = waRawPoint.map.next;
                    } else {
                        waRawPoint = waRawPoint.next;
                    }
                }
                waRawPoint.flag = waCrossPoint.flag == flagEnter ? flagOut : flagEnter;
                waCrossPoint = waRawPoint;
                waCrossPoint.isVisited = true;
                newMainPolygon.init(waCrossPoint.xVal, waCrossPoint.yVal);

                if ((waCrossPoint.xVal == waStartPoint.xVal) && (waCrossPoint.yVal == waStartPoint.yVal)) {
                    break;
                }
            }
            waStartPoint = mainList.getCrossPoint(clipPolygon, isInnerClip);
        }

        // 裁剪多边形 与 主多边形 部分或全部边框无交点处理 —— 未完成
        ArrayList<ArrayList<waPoint>> mainNICircleList = mainList.getNoIntersectionCircleList();
        ArrayList<ArrayList<waPoint>> clipNICircleList = clipList.getNoIntersectionCircleList();

        if (newMainPolygon.polygonList.size() != 0) {
            PolygonBasic.point polygonPoint = newMainPolygon.polygonList.get(0).get(0);
            for (ArrayList<waPoint> mainNICircle : mainNICircleList) {
                if (!pointInWACircle(polygonPoint.xInt(), polygonPoint.yInt(), mainNICircle)) {
                    for (int i = 0; i <= mainNICircle.size(); i++) {
                        waPoint tempPoint = mainNICircle.get(i % mainNICircle.size());
                        newMainPolygon.init(tempPoint.xVal, tempPoint.yVal);
                    }
                }
            }
            for (ArrayList<waPoint> clipNICircle : clipNICircleList) {
                if (!pointInWACircle(polygonPoint.xInt(), polygonPoint.yInt(), clipNICircle)) {
                    for (int i = 0; i <= clipNICircle.size(); i++) {
                        waPoint tempPoint = clipNICircle.get(i % clipNICircle.size());
                        newMainPolygon.init(tempPoint.xVal, tempPoint.yVal);
                    }
                }
            }
        } else {

        }


        newMainPolygon.isFinished = true;
        return newMainPolygon;
    }

    private void circlePropertyJudge(ArrayList<ArrayList<PolygonBasic.point>> circleList, boolean[] isInner) {
        for (int i = 0; i < circleList.size(); i++) {
            isInner[i] = false;
            PolygonBasic.point singlePoint = circleList.get(i).get(0);
            for (int j = 0; j < circleList.size(); j++) {
                if (j != i) {
                    if (isInside(circleList.get(j), singlePoint)) {
                        isInner[i] = true;
                        break;
                    }
                }
            }
        }
    }

    private void waPointListInit(waPointList waList, ArrayList<ArrayList<PolygonBasic.point>> pList,
                                 boolean[] reverse, boolean isMain) {
        for (int i = 0; i < pList.size(); i++) {
            ArrayList<PolygonBasic.point> tempList = pList.get(i);
            if (!reverse[i]) {
                waList.addRawPoint(tempList.get(0), isMain);
                waPoint tempNode = waList.tail;
                for (int j = 1; j <= tempList.size(); j++) {
                    waList.addRawPoint(tempList.get(j % tempList.size()), isMain);
                }
                waList.tail.map = tempNode;
            } else {
                waList.addRawPoint(tempList.get(tempList.size() - 1), isMain);
                waPoint tempNode = waList.tail;
                for (int j = tempList.size() - 2; j >= -1; j--) {
                    int index = j == -1 ? tempList.size() - 1 : j;
                    waList.addRawPoint(tempList.get(index), isMain);
                }
                waList.tail.map = tempNode;
            }
        }
    }

    private boolean isClockwise(ArrayList<PolygonBasic.point> pointList) {
        double flag = 0.;
        for (int i = 0; i < pointList.size(); i++) {
            int nextIndex = i + 1 < pointList.size() ? i + 1 : 0;
            flag += (pointList.get(nextIndex).yInt() + pointList.get(i).yInt()) * (pointList.get(nextIndex).xInt() - pointList.get(i).xInt());
        }
        return flag < 0;
    }

    private boolean isInside(ArrayList<PolygonBasic.point> polygon, PolygonBasic.point p) {
        boolean inside = false;
        for (int i = 0, j = polygon.size() - 1; i < polygon.size(); j = i++) {
            if ((polygon.get(i).yInt() > p.yInt()) != (polygon.get(j).yInt() > p.yInt()) &&
                    p.xInt() < (polygon.get(j).xInt() - polygon.get(i).xInt()) * (p.yInt() - polygon.get(i).yInt())
                            / (polygon.get(j).yInt() - polygon.get(i).yInt()) + polygon.get(i).xInt()) {
                inside = !inside;
            }
        }
        return inside;
    }

    private waPoint getPOI(PolygonBasic.point dStart, PolygonBasic.point dEnd,
                           PolygonBasic.point cStart, PolygonBasic.point cEnd) {
        int dDeltaX = dEnd.xInt() - dStart.xInt(), dDeltaY = dEnd.yInt() - dStart.yInt();
        int cDeltaX = cEnd.xInt() - cStart.xInt(), cDeltaY = cEnd.yInt() - cStart.yInt();
        int detVectorCross = dDeltaX * cDeltaY - dDeltaY * cDeltaX;

        if (detVectorCross == 0) {
            return null;
        }

        double d, c;
        d = (double) (-dDeltaY * (dStart.xInt() - cStart.xInt()) + dDeltaX * (dStart.yInt() - cStart.yInt())) / detVectorCross;
        c = (double) (cDeltaX * (dStart.yInt() - cStart.yInt()) - cDeltaY * (dStart.xInt() - cStart.xInt())) / detVectorCross;
        if ((d >= 0 && d <= 1) && (c >= 0 && c <= 1)) {
            int xVal = dStart.xInt() + (int) (c * dDeltaX);
            int yVal = dStart.yInt() + (int) (c * dDeltaY);
            return new waPoint(xVal, yVal, flagCross, true);
        } else {
            return null;
        }
    }

    private boolean pointInWACircle(int xVal, int yVal, ArrayList<waPoint> circle) {
        boolean inside = false;
        for (int i = 0, j = circle.size() - 1; i < circle.size(); j = i++) {
            if ((circle.get(i).yVal > yVal) != (circle.get(j).yVal > yVal) &&
                    xVal < (circle.get(j).xVal - circle.get(i).xVal) * (yVal - circle.get(i).yVal)
                            / (circle.get(j).yVal - circle.get(i).yVal) + circle.get(i).xVal) {
                inside = !inside;
            }
        }
        return inside;
    }
}
