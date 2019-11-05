import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


class BasicFrame extends JFrame implements ActionListener {
    static final int panelWidth = 900, panelHeight = 500;
    static final int statusMove = 0, statusScale = 1, statusRotate = 2,
            statusCreate = 3, statusClip = 4;

    static int status = 0;

    private JMenuItem itemCreate, itemClip;
    private JMenuItem itemMove, itemScale, itemRotate;
    private BasicPanel basicPanel;

    void go() {
        setTitle("Polygon");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setJMenuBar(menuBar());
        basicPanel = new BasicPanel(this);
        setContentPane(basicPanel);
        pack();
        setVisible(true);
    }

    private JMenuBar menuBar() {
        JMenuBar menu = new JMenuBar();

        // JMenu -- create
        JMenu menuCreate = new JMenu(" 创 建 ");
        itemCreate = new JMenuItem(" 创 建 ");
        itemCreate.addActionListener(this);
        menuCreate.add(itemCreate);

        // JMenu -- trans
        JMenu menuTrans = new JMenu(" 变 换 ");
        itemMove = new JMenuItem("平 移");
        itemMove.addActionListener(this);
        menuTrans.add(itemMove);
        itemScale = new JMenuItem("缩 放");
        itemScale.addActionListener(this);
        menuTrans.add(itemScale);
        itemRotate = new JMenuItem("旋 转");
        itemRotate.addActionListener(this);
        menuTrans.add(itemRotate);

        // JMenu -- clip
        JMenu menuClip = new JMenu(" 裁 剪 ");
        itemClip = new JMenuItem(" 裁 剪 ");
        itemClip.addActionListener(this);
        menuClip.add(itemClip);

        // JMenuBar
        menu.add(menuCreate);
        menu.add(menuTrans);
        menu.add(menuClip);
        menu.setBorder(BorderFactory.createCompoundBorder(new LineBorder(Color.WHITE),
                new EmptyBorder(4, 1, 3, 1)));

        return menu;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object obj = e.getSource();

        if (obj.equals(itemMove)) {
            status = statusMove;
        } else if (obj.equals(itemScale)) {
            status = statusScale;
            basicPanel.getPolygonCenter();
        } else if (obj.equals(itemRotate)) {
            status = statusRotate;
            basicPanel.getPolygonCenter();
        } else if (obj.equals(itemCreate)) {
            status = statusCreate;
            basicPanel.clear(this);
        } else if (obj.equals(itemClip)) {
            status = statusClip;
            basicPanel.clearClip(this);
        }
    }
}
