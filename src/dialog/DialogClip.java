package dialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class DialogClip extends JDialog implements ActionListener {
    private JButton btnContinue;
    public boolean isContinue;

    public DialogClip(JFrame parentFrame) {
        super(parentFrame, true);
        setSize(50, 100);
        setLayout(new FlowLayout());

        isContinue = false;
        btnContinue = new JButton("继续");
        btnContinue.addActionListener(this);

        add(btnContinue);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object obj = e.getSource();

        if (obj.equals(btnContinue)) {
            isContinue = true;
            this.dispose();
        }
    }
}

