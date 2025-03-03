package Question_2;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class Panel extends JPanel implements KeyListener, ComponentListener {

    JFrame frame;
    RepairShop repairShop;
    List<Phone> phones = new ArrayList<>();

    public Panel(JFrame frame) {
        this.frame = frame;
        this.addKeyListener(this);
        this.addComponentListener(this);
        this.setFocusable(true);
        this.requestFocusInWindow();
    
        // Initialise repair shop
        repairShop = new RepairShop();
    
        // Start a background thread for updates
        Thread updateThread = new Thread(() -> {
            while (true) {
                try {
                    updatePhones(); // Update the phone states
                    repaint(); // Repaint the panel
                    Thread.sleep(500); // Delay (similar to Timer)
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break; // Exit thread if interrupted
                }
            }
        });
        updateThread.setDaemon(true); // Allow program to exit when JFrame is closed
        updateThread.start();
    }
    

    //initial state
    private boolean repairShopOpen = true;

    public void makeRepairShopAvailable() {
        repairShopOpen = true;
    }

    private void updatePhones() {

        spreadInfection();

        //check for phones moving to repair
        boolean isAnyPhoneMovingToRepair = false;
        for (Phone phone : phones) {
            if (phone.getPhoneState() == Phone.PhoneState.MOVING_TO_REPAIR) {
                isAnyPhoneMovingToRepair = true;
                break;
            }
        }
        //if no phones moving to repair
        if (!isAnyPhoneMovingToRepair && repairShopOpen) {
            // Synchronize on the shared resource
            synchronized (phones) { 
                for (Phone phone : phones) {
                    if (phone.getPhoneState() == Phone.PhoneState.INFECTED) {
                        // Move to repair
                        phone.setPhoneState(Phone.PhoneState.MOVING_TO_REPAIR);
                        // Close shop
                        repairShopOpen = false;
                        break; // Exit loop after setting one phone to repair
                    }
                }
            }
        }

        //remove phones from arraylist if dead
        Iterator<Phone> iterator = phones.iterator();
        while (iterator.hasNext()) {
            Phone phone = iterator.next();
            if (!phone.alivePhone()) {
                iterator.remove();
            }
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int screenMargin = 10;

        //phone colour
        for (Phone phone : phones) {
            switch (phone.getPhoneState()) {
                case HEALTHY:
                    g.setColor(Color.GREEN);
                    break;
                case MOVING_TO_REPAIR:
                    g.setColor(Color.YELLOW);
                    break;
                case INFECTED:
                    g.setColor(Color.RED);
                    break;
            }
            //phone 
            g.fillRect(phone.x, phone.y, Phone.phoneWidth, Phone.phoneHeight);
            g.setColor(Color.LIGHT_GRAY);
            g.fillRect(phone.x + screenMargin, phone.y + screenMargin,
                    Phone.phoneWidth - 2 * screenMargin, Phone.phoneHeight - 2 * screenMargin);
            g.setColor(Color.BLACK);
            g.fillOval(phone.x + Phone.phoneWidth / 2 - 5, phone.y + Phone.phoneHeight - 20, 10, 10);
        }

        //shop location
        repairShop.x = (getWidth() - RepairShop.SHOP_WIDTH) / 2;

        int shopX = (getWidth() - RepairShop.SHOP_WIDTH) / 2;
        g.setColor(Color.GRAY);
        g.fillRect(shopX, 0, RepairShop.SHOP_WIDTH, RepairShop.SHOP_HEIGHT);

        //windows
        int windowWidth = 10;
        int windowHeight = 10;
        int windowMarginTop = 15;
        int windowMarginSide = 20;
        g.setColor(Color.BLUE);
        g.fillRect(shopX + windowMarginSide, windowMarginTop, windowWidth, windowHeight);
        g.fillRect(shopX + RepairShop.SHOP_WIDTH - windowMarginSide - windowWidth,
                windowMarginTop, windowWidth, windowHeight);

        //door
        int doorWidth = 20;
        int doorHeight = 30;
        int doorX = shopX + (RepairShop.SHOP_WIDTH - doorWidth) / 2;
        int doorY = RepairShop.SHOP_HEIGHT - doorHeight;
        g.setColor(Color.DARK_GRAY);
        g.fillRect(doorX, doorY, doorWidth, doorHeight);
    }

    public void spreadInfection() {
        //infected phones should
        for (Phone infectedPhone : phones) {
            //check for infected or moving to repair to avoid
            if (infectedPhone.getPhoneState() == Phone.PhoneState.INFECTED || infectedPhone.getPhoneState() == Phone.PhoneState.MOVING_TO_REPAIR) {
                //target phones should
                for (Phone targetPhone : phones) {
                    //check to see if health
                    if (targetPhone.getPhoneState() == Phone.PhoneState.HEALTHY) {
                        //create distance virable relative to infected / target phones
                        double distance = Math.sqrt(Math.pow(infectedPhone.x - targetPhone.x, 2) + Math.pow(infectedPhone.y - targetPhone.y, 2));
                        //if close enough
                        if (distance <= 40) {
                            //infect
                            targetPhone.setPhoneState(Phone.PhoneState.INFECTED);
                        }
                    }
                }
            }
        }
    }

    ////////////////////key / Component events and listeners
    @Override
    public void keyTyped(KeyEvent ke) {

    }

    @Override
    public void keyPressed(KeyEvent ke) {
        //start new thread and create phone
        switch (ke.getKeyCode()) {
            case KeyEvent.VK_UP:
                Phone newPhone = new Phone(this, frame.getWidth(), frame.getHeight() - RepairShop.SHOP_HEIGHT);
                phones.add(newPhone);
                newPhone.start();
                break;
            //if no phones in arraylist make random phone infected
            case KeyEvent.VK_V:
                //wasn't sure you were going to just do a single infected or spam multiple infected phones so I added this just incase
                List<Phone> infectablePhones = new ArrayList<>();
                for (Phone phone : phones) {
                    if (phone.getPhoneState() != Phone.PhoneState.MOVING_TO_REPAIR && phone.getPhoneState() != Phone.PhoneState.INFECTED) {
                        infectablePhones.add(phone);
                    }
                }
                //if arraylist of infectable phones not empty, infect a random phone in the arraylist
                if (!infectablePhones.isEmpty()) {
                    Random random = new Random();
                    int index = random.nextInt(infectablePhones.size());
                    Phone selectedPhone = infectablePhones.get(index);
                    selectedPhone.setPhoneState(Phone.PhoneState.INFECTED);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void componentResized(ComponentEvent ce) {
        for (Phone phone : phones) {
            phone.setRange(frame.getWidth(), frame.getHeight() - RepairShop.SHOP_HEIGHT);
        }
    }

}
