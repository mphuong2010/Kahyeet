import javax.swing.*;
import java.awt.event.*;

public class LoginUI extends JFrame {
    private JTextField usernameField, addressField, portField;
    private JButton loginButton;
    private Sound errorSound;

    public LoginUI() {
        setTitle("Kahyeet! Login");
        setSize(400, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);

        errorSound = new Sound("error_message.wav");

        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setBounds(20, 20, 100, 25);
        add(usernameLabel);

        usernameField = new JTextField();
        usernameField.setBounds(140, 20, 200, 25);
        add(usernameField);

        JLabel addressLabel = new JLabel("Server Address:");
        addressLabel.setBounds(20, 60, 100, 25);
        add(addressLabel);

        addressField = new JTextField("localhost");
        addressField.setBounds(140, 60, 200, 25);
        add(addressField);

        JLabel portLabel = new JLabel("Port:");
        portLabel.setBounds(20, 100, 100, 25);
        add(portLabel);

        portField = new JTextField("12345");
        portField.setBounds(140, 100, 200, 25);
        add(portField);

        loginButton = new JButton("Login");
        loginButton.setBounds(140, 150, 100, 30);
        add(loginButton);

        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText().trim();
                String address = addressField.getText().trim();
                int port;
                
                try {
                    port = Integer.parseInt(portField.getText().trim());
                } catch (NumberFormatException ex) {
                    showMessage("Invalid port number.");
                    return;
                }

                if (!username.isEmpty() && !address.isEmpty()) {
                    if (connectToClient(username, address, port))
                        dispose();
                } else {
                    showMessage("Fields cannot be empty.");
                }
            }
        });

        setVisible(true);
    }

    private boolean connectToClient(String username, String address, int port) {
        Client client = new Client(this, username, address, port);
        return client.isConnected();
    }

    public void showMessage(String message) {
        errorSound.playOnce();
        JOptionPane.showMessageDialog(this, message);
    }

    public static void main(String[] args) {
        new LoginUI();
    }
}