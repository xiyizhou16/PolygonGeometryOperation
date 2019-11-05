import clip.WeilerAtherton;
import dialog.DialogClip;
import polygon.PolygonBasic;
import polygon.PolygonDraw;
import dialog.DialogDraw;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;


public class BasicPanel extends JPanel implements MouseListener, MouseMotionListener, KeyListener {
    private PolygonDraw polygonDraw;
    private PolygonBasic polygonClip;
    private DialogDraw dialogDraw;
    private DialogClip dialogClip;
    private WeilerAtherton weilerAthertonClip;
    private int mouseX, mouseY;

    BasicPanel(JFrame frame) {
        setLayout(null);
        setPreferredSize(new Dimension(BasicFrame.panelWidth, BasicFrame.panelHeight));
        setOpaque(false);

        addMouseListener(this);
        addMouseMotionListener(this);
        addKeyListener(this);

        setFocusable(true);

        polygonDraw = new PolygonDraw();
        dialogDraw = new DialogDraw(frame);
        dialogDraw.setVisible(false);

        polygonClip = new PolygonBasic();
        dialogClip = new DialogClip(frame);
        dialogClip.setVisible(false);

        weilerAthertonClip = new WeilerAtherton();
    }

    void clear(JFrame frame) {
        polygonDraw = new PolygonDraw();
        dialogDraw = new DialogDraw(frame);
        dialogDraw.setVisible(false);
        polygonClip = new PolygonBasic();
        dialogClip = new DialogClip(frame);
        dialogClip.setVisible(false);
        repaint();
    }

    void clearClip(JFrame frame) {
        polygonClip = new PolygonBasic();
        dialogClip = new DialogClip(frame);
        dialogClip.setVisible(false);
        repaint();
    }

    void getPolygonCenter() {
        polygonDraw.getCenter();
    }

    public void paint(Graphics g) {
        super.paint(g);

        if (polygonDraw.isFinished) {
            polygonDraw.drawWhenFinished(g, dialogDraw.fillColor, dialogDraw.edgeColor);
        }
        if (BasicFrame.status == BasicFrame.statusCreate) {
            polygonDraw.drawWhenInit(g);
        } else if (BasicFrame.status == BasicFrame.statusClip) {
            polygonClip.drawWhenInit(g);
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (BasicFrame.status == BasicFrame.statusCreate) {
            polygonDraw.init(e.getX(), e.getY());
            repaint();

            if (polygonDraw.isCorrect) {
                dialogDraw.setVisible(true);
                if (!dialogDraw.isContinue) {
                    polygonDraw.isFinished = true;
                    BasicFrame.status = BasicFrame.statusMove;
                }
                dialogDraw.isContinue = false;
                repaint();
            }
        } else if (BasicFrame.status == BasicFrame.statusClip) {
            polygonClip.init(e.getX(), e.getY());
            repaint();

            if (polygonClip.isCorrect) {
                dialogClip.setVisible(true);
                if (!dialogClip.isContinue) {
                    polygonDraw = weilerAthertonClip.clip(polygonDraw, polygonClip);
                    polygonClip = new PolygonBasic();
                }
                dialogClip.isContinue = false;
                repaint();
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }
    @Override
    public void mouseEntered(MouseEvent e) {
    }
    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (BasicFrame.status == BasicFrame.statusMove) {
            int deltaX = e.getX() - mouseX, deltaY = e.getY() - mouseY;
            polygonDraw.move(deltaX, deltaY);
            repaint();
        }
        mouseX = e.getX();
        mouseY = e.getY();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }
    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (BasicFrame.status == BasicFrame.statusScale) {
            if(e.getKeyCode() == KeyEvent.VK_MINUS){
                polygonDraw.scale(false);
            }
            else if(e.getKeyCode() == KeyEvent.VK_EQUALS){
                polygonDraw.scale(true);
            }
            repaint();
        } else if (BasicFrame.status == BasicFrame.statusRotate) {
            if(e.getKeyCode() == KeyEvent.VK_UP){
                polygonDraw.rotate(false);
            }
            else if(e.getKeyCode() == KeyEvent.VK_DOWN){
                polygonDraw.rotate(true);
            }
            repaint();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }
}
