package it.unibs.pajc.agar;

import javax.swing.*;
import java.awt.*;
import java.util.prefs.Preferences;

class StartDialog extends JDialog {

    private final JTextField name = new JTextField();
    private final JTextField ipAddress = new JTextField();
    private final JButton create = new JButton("Create");
    private final JButton join = new JButton("Join");
    private boolean isServer = false;
    private boolean closed = true;

    public StartDialog() {
        super((JFrame)null, "Start Options", true);

        create.addActionListener(e -> closeDialog(true));
        join.addActionListener(e -> closeDialog(false));

        Preferences pref = Preferences.userRoot().node(this.getClass().getName());
        name.setColumns(20);
        name.setText(pref.get("UserName", "Default"));
        ipAddress.setColumns(20);
        ipAddress.setText(pref.get("IpAddress", "127.0.0.1"));
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
        add(ipAddress, c);
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
        return ipAddress.getText();
    }

    private void closeDialog(boolean isServer) {
        this.closed = false;
        this.isServer = isServer;
        this.setVisible(false);
        Preferences preferences = Preferences.userRoot().node(this.getClass().getName());
        preferences.put("UserName", getPlayerName());
        preferences.put("IpAddress", getIpAddress());
    }

    public boolean wasClosed() {
        return closed;
    }
}
