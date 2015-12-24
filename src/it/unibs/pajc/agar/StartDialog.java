package it.unibs.pajc.agar;

import javax.swing.*;
import java.awt.*;

public class StartDialog extends JDialog {

    private JTextField name = new JTextField(), ipAddr = new JTextField();
    private JButton create = new JButton("Create"), join = new JButton("Join");
    private boolean isServer = false;

    public StartDialog() {
        super((JFrame)null, "Start Options", true);

        create.addActionListener(e -> closeDialog(true));
        join.addActionListener(e -> closeDialog(false));

        name.setColumns(20);
        name.setText("DefaultName");
        ipAddr.setColumns(20);
        ipAddr.setText("127.0.0.1");
        create.getSize();
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.weightx = 1;
        c.insets = new Insets(5,10,5,10);
        add(new JLabel("Nome"), c);
        c.gridx = 1;
        add(name, c);
        c.gridx = 2;
        add(create, c);
        c.gridx = 0;
        c.gridy = 1;
        add(new JLabel("IP"), c);
        c.gridx = 1;
        add(ipAddr, c);
        c.gridx = 2;
        add(join, c);
        pack();
    }

    public boolean isServer() {
        return isServer;
    }

    public String getPlayerName() {
        return name.getText();
    }

    public String getIpAddress() {
        return ipAddr.getText();
    }

    public void closeDialog(boolean isServer) {
        this.isServer = isServer;
        this.setVisible(false);
    }
}
