package Question_2;

import javax.swing.JFrame;

public class VirusSimulation {

    public static void main(String[] args) {
        JFrame frame = new JFrame("Mobile Phone Virus Simulation");
        frame.setSize(1000, 1000);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Panel panel = new Panel(frame);
        frame.getContentPane().add(panel);
        frame.setVisible(true);
    }
}
