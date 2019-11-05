package dialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class DialogDraw extends JDialog implements ActionListener {
    private JButton btnEdgeColor, btnFillColor, btnContinue;
    public Color edgeColor, fillColor;
    public boolean isContinue;

    public DialogDraw(JFrame parentFrame) {
        super(parentFrame, true);
        setSize(150, 100);
        setLayout(new FlowLayout());

        edgeColor = new Color(0, 0, 255);
        btnEdgeColor = new JButton("选择边的颜色");
        btnEdgeColor.addActionListener(this);

        fillColor = new Color(0, 0, 0);
        btnFillColor = new JButton("选择填充颜色");
        btnFillColor.addActionListener(this);

        isContinue = false;
        btnContinue = new JButton("继续");
        btnContinue.addActionListener(this);

        add(btnFillColor);
        add(btnEdgeColor);
        add(btnContinue);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object obj = e.getSource();

        if (obj.equals(btnEdgeColor)) {
            edgeColor = JColorChooser.showDialog(null, "", edgeColor);
            if (edgeColor == null) {
                edgeColor = new Color(0, 0, 255);
            }
        } else if (obj.equals(btnFillColor)) {
            fillColor = JColorChooser.showDialog(null, "", fillColor);
            if (fillColor == null) {
                fillColor = new Color(0, 0, 0);
            }
        } else if (obj.equals(btnContinue)) {
            isContinue = true;
            this.dispose();
        }
    }
}

