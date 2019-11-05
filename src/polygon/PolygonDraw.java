package polygon;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PolygonDraw extends PolygonBasic {
    private int yCenter, xCenter;

    public void getCenter() {
        double yMin = polygonList.get(0).get(0).yVal;
        double yMax = polygonList.get(0).get(0).yVal;
        double xMin = polygonList.get(0).get(0).xVal;
        double xMax = polygonList.get(0).get(0).xVal;
        for (ArrayList<point> pointList : polygonList) {
            for (point singlePoint : pointList) {
                yMin = yMin < singlePoint.yVal ? yMin : singlePoint.yVal;
                yMax = yMax > singlePoint.yVal ? yMax : singlePoint.yVal;
                xMin = xMin < singlePoint.xVal ? xMin : singlePoint.xVal;
                xMax = xMax > singlePoint.xVal ? xMax : singlePoint.xVal;
            }
        }
        yCenter = (int) ((yMin + yMax) / 2);
        xCenter = (int) ((xMin + xMax) / 2);
    }

    public void move(int xDelta, int yDelta) {
        for (ArrayList<point> pointList : polygonList) {
            for (point singlePoint : pointList) {
                singlePoint.xVal += xDelta;
                singlePoint.yVal += yDelta;
            }
        }
    }

    public void scale(boolean enLarge) {
        double scaleX = 0.99, scaleY = 0.99;
        if (enLarge) {
            scaleX = 1.01;
            scaleY = 1.01;
        }
        for (ArrayList<point> pointList : polygonList) {
            for (point singlePoint : pointList) {
                singlePoint.xVal = singlePoint.xVal * scaleX + (1 - scaleX) * xCenter;
                singlePoint.yVal = singlePoint.yVal * scaleY + (1 - scaleY) * yCenter;
            }
        }
    }

    public void rotate(boolean isClockwise) {
        double delta = Math.toRadians(1.0);
        if (!isClockwise) {
            delta = -delta;
        }
        for (ArrayList<point> pointList : polygonList) {
            for (point singlePoint : pointList) {
                singlePoint.xVal =
                        singlePoint.xVal * Math.cos(delta) - singlePoint.yVal * Math.sin(delta)
                                + xCenter * (1 - Math.cos(delta)) + yCenter * Math.sin(delta);
                singlePoint.yVal =
                        singlePoint.xVal * Math.sin(delta) + singlePoint.yVal * Math.cos(delta)
                                + yCenter * (1 - Math.cos(delta)) - xCenter * Math.sin(delta);
            }
        }

    }

    public void drawWhenFinished(Graphics graphics, Color fillColor, Color edgeColor) {
        if (polygonList.size() == 0) {
            return;
        }

        Graphics2D graphics2D = (Graphics2D) graphics;
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        graphics.setColor(fillColor);
        polygonFill(graphics);
        graphics.setColor(edgeColor);
        for (ArrayList<point> pointList : polygonList) {
            for (int j = 0; j < pointList.size(); j++) {
                int endIndex = j + 1 < pointList.size() ? j + 1 : 0;
                graphics2D.drawLine(pointList.get(j).xInt(), pointList.get(j).yInt(),
                        pointList.get(endIndex).xInt(), pointList.get(endIndex).yInt());
            }
        }
    }

    // 区域填充 —— 扫描线算法
    private class NETNode {
        double lowX;
        double slopeInverse;
        double highY;

        NETNode(int lowX, double slopeInverse, int highY) {
            this.lowX = (double) lowX;
            this.slopeInverse = slopeInverse;
            this.highY = (double) highY;
        }
    }

    private class NETLinkList {
        class LinkListNode {
            NETNode netNode;
            LinkListNode next = null;
            LinkListNode pre = null;
        }

        private LinkListNode head = null;

        void insertNode(NETNode node) {
            if (head == null) {
                head = new LinkListNode();
                head.netNode = node;
            } else {
                LinkListNode currentNode = head;
                LinkListNode newNode = new LinkListNode();
                newNode.netNode = node;

                while (true) {
                    if ((node.lowX < currentNode.netNode.lowX) ||
                            (node.lowX == currentNode.netNode.lowX && node.slopeInverse < currentNode.netNode.slopeInverse)) {
                        if (currentNode.pre == null) {
                            currentNode.pre = newNode;
                            newNode.next = currentNode;
                            head = newNode;
                        } else {
                            currentNode.pre.next = newNode;
                            newNode.pre = currentNode.pre;
                            currentNode.pre = newNode;
                            newNode.next = currentNode;
                        }
                        return;
                    } else {
                        if (currentNode.next == null) {
                            currentNode.next = newNode;
                            newNode.pre = currentNode;
                            return;
                        } else {
                            currentNode = currentNode.next;
                        }
                    }
                }
            }
        }

        void deleteNode(int scanY) {
            LinkListNode currentNode = head;
            while (currentNode != null) {
                if (currentNode.netNode.highY == scanY) {
                    if (currentNode.pre == null && currentNode.next == null) {
                        head = null;
                    } else if (currentNode.pre == null) {
                        head = currentNode.next;
                        currentNode.next.pre = null;
                    } else if (currentNode.next == null) {
                        currentNode.pre.next = null;
                    } else {
                        currentNode.pre.next = currentNode.next;
                        currentNode.next.pre = currentNode.pre;
                    }
                }
                currentNode = currentNode.next;
            }
        }

        void increase() {
            LinkListNode currentNode = head;
            while (currentNode != null) {
                currentNode.netNode.lowX += currentNode.netNode.slopeInverse;
                currentNode = currentNode.next;
            }
        }

        ArrayList getX() {
            ArrayList xList = new ArrayList<>();
            LinkListNode currentNode = head;
            while (currentNode != null) {
                xList.add((int) currentNode.netNode.lowX);
                currentNode = currentNode.next;
            }
            return xList;
        }
    }

    private void polygonFill(Graphics graphics) {
        Graphics2D graphics2D = (Graphics2D) graphics;
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 新边表创建
        Map<Integer, ArrayList<NETNode>> newEdgeTable = new HashMap<>();
        for (ArrayList<point> pointList : polygonList) {
            for (int j = 0; j < pointList.size(); j++) {
                int nextIndex = j + 1 < pointList.size() ? j + 1 : 0;

                point lowPoint = pointList.get(j), highPoint = pointList.get(nextIndex);
                if (lowPoint.yInt() > highPoint.yInt()) {
                    lowPoint = pointList.get(nextIndex);
                    highPoint = pointList.get(j);
                } else if (lowPoint.yInt() == highPoint.yInt()) {
                    continue;
                }

                double slopeInverse = (highPoint.xVal - lowPoint.xVal) / (highPoint.yVal - lowPoint.yVal);
                if (newEdgeTable.get(lowPoint.yInt()) == null) {
                    ArrayList<NETNode> newEdgeList = new ArrayList<>();
                    newEdgeList.add(new NETNode(lowPoint.xInt(), slopeInverse, highPoint.yInt()));
                    newEdgeTable.put(lowPoint.yInt(), newEdgeList);
                } else {
                    newEdgeTable.get(lowPoint.yInt()).add(new NETNode(lowPoint.xInt(), slopeInverse, highPoint.yInt()));
                }
            }
        }

        // 活性编表
        NETLinkList activeEdgeList = new NETLinkList();

        int scanY = polygonList.get(0).get(0).yInt();
        int scanYMax = polygonList.get(0).get(0).yInt();
        for (ArrayList<point> pointList : polygonList) {
            for (point singlePoint : pointList) {
                scanY = scanY < singlePoint.yInt() ? scanY : singlePoint.yInt();
                scanYMax = scanYMax > singlePoint.yInt() ? scanYMax : singlePoint.yInt();
            }
        }

        scanY -= 1;
        while (scanY <= scanYMax + 1) {
            ArrayList<NETNode> newEdgeList = newEdgeTable.get(scanY);
            if (newEdgeList != null) {
                for (NETNode newEdgeListNode : newEdgeList) {
                    activeEdgeList.insertNode(newEdgeListNode);
                }
            }

            ArrayList xList = activeEdgeList.getX();
            for (int i = 0; i < xList.size(); i += 2) {
                graphics2D.drawLine((int) xList.get(i), scanY, (int) xList.get(i + 1), scanY);
            }

            scanY += 1;
            activeEdgeList.deleteNode(scanY);
            activeEdgeList.increase();
        }
    }
}
