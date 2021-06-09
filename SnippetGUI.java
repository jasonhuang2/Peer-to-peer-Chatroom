package registry;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class SnippetGUI implements ActionListener{  

    final int MAX_CHARS = 20;
    JFrame frame;

    public String inputSnip;
    private String snippetLog;
    private JPanel snipPanel;
    private JPanel textPanel;
    private JLabel snipLabel;
    private JLabel textLabel1;
    private JLabel textLabel2;
    private JEditorPane snipEditPane;
    private JScrollPane snipLogPane;
    private JTextArea snipTextArea;
    private JTextField textField;
    private JButton sendButton;

    public void startGUI() {  
        frame = new JFrame("SnippetGUI");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(640, 640);

        snipPanel = new JPanel();
        snipLabel = new JLabel("Snippet Log");

        snipEditPane = new JEditorPane();
        snipEditPane.setEditable(false);

        snipLogPane = new JScrollPane(snipEditPane);
        snipLogPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        snipLogPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        snipLogPane.setPreferredSize(new Dimension(380, 480));
        snipLogPane.setMinimumSize(new Dimension(125, 180));
        snipPanel.add(snipLogPane);
        snipTextArea = new JTextArea();

        textPanel = new JPanel();
        textLabel1 = new JLabel("Enter snippet");
        textLabel2 = new JLabel("You entered: ");
        textField = new JTextField(MAX_CHARS);
        sendButton = new JButton("Send"); 
        sendButton.addActionListener(this);

        textPanel.add(textLabel1);
        textPanel.add(textField);
        textPanel.add(sendButton);
        textPanel.add(textLabel2);
                
        frame.getContentPane().add(BorderLayout.NORTH, snipLabel);
        frame.getContentPane().add(BorderLayout.CENTER, snipPanel);
        frame.getContentPane().add(BorderLayout.SOUTH, textPanel);
        frame.setVisible(true);
    }

    public void actionPerformed(ActionEvent e) {   
        // set text of test label to field text
        inputSnip = textField.getText();
        textLabel2.setText("You entered: " + inputSnip);
        textField.setText(" ");

        // then we must send the input snip to be added to the SnippetList

    }

    public void updateSnippetLog() {
        snipTextArea.setText(snippetLog);
        snipLogPane.setViewportView(snipTextArea);
    }

    public void closeGUI() {
        frame.dispose();
    }

    public void setSnippetLog (String snippetLog) {
        this.snippetLog = snippetLog;
    }

    public String getInputSnip () {
        return inputSnip;
    }

}  