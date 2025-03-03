package Question_2;

import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Phone extends Thread {

    Random random = new Random();
    private final Panel panel;

    int x = 0;
    int y = 0;
    int vx;
    int vy;
    int width;
    int height;

    public Phone(Panel panel, int width, int height) {
        this.panel = panel;
        this.width = width;
        this.height = height;
        //starting pos
        x = random.nextInt(width - Phone.phoneWidth);
        y = random.nextInt(height - Phone.phoneHeight);
        //360 direction 
        double angle = Math.toRadians(random.nextDouble() * 360);
        int speed = 3;
        vx = (int) (Math.cos(angle) * speed);
        vy = (int) (Math.sin(angle) * speed);
    }

    public void setRange(int width, int height) {
        this.width = width;
        this.height = height;
    }

    //////////////////////////////////phone states, getters, setters
    public enum PhoneState {
        HEALTHY, MOVING_TO_REPAIR, INFECTED
    }

    private PhoneState phoneState = PhoneState.HEALTHY;

    public void setPhoneState(PhoneState phoneState) {
        this.phoneState = phoneState;
    }

    public PhoneState getPhoneState() {
        return this.phoneState;
    }

    //////////////////////////////////lifespan
    private int lifeSpan = 500;

    public void decrementLifeSpan() {
        if (this.phoneState == PhoneState.INFECTED) {
            lifeSpan--;
        }
    }

    public boolean alivePhone() {
        return lifeSpan > 0;
    }

    ///////////////////////////////thread run()
    @Override
    public void run() {
        while (true) {
            if (!alivePhone()) {
                break;
            }
            
            if (getPhoneState() == PhoneState.MOVING_TO_REPAIR) {
                moveToRepairShop();
            } else {
                move();
            }
            try {
                //15ms delay
                Thread.sleep(15);
                decrementLifeSpan();
            } catch (InterruptedException ex) {
                Logger.getLogger(Phone.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    //////////////////////// phone regular movement Logic
    public static int phoneWidth = 50;
    public static int phoneHeight = 100;

    public void move() {
        //update pos
        x += vx;
        y += vy;

        //bounce of edges
        if (x < 0 || x > width - Phone.phoneWidth) {
            vx = -vx;
        }
        if (y < 0 || y > height - Phone.phoneHeight) {
            vy = -vy;
        }
        panel.repaint();
    }

    /////////synchronized Move to repairshop movement / repair state
    public void moveToRepairShop() {
        if (getPhoneState() == Phone.PhoneState.MOVING_TO_REPAIR) {
            int targetX = panel.repairShop.x + RepairShop.SHOP_WIDTH / 2 - phoneWidth / 2;
            int targetY = panel.repairShop.y;

            double dx = targetX - x;
            double dy = targetY - y;
            double distance = Math.sqrt(dx * dx + dy * dy);
            double speed = 3;
            double vX = (dx / distance) * speed;
            double vY = (dy / distance) * speed;

            x += vX;
            y += vY;

            /////check if the phone has arrived repairshop w/ room for error
            if (Math.abs(x - targetX) <= 5 && Math.abs(y - targetY) <= 5) {
                //access RepairShop init. in the panel, to access class and sync. method to repair phone.
                panel.repairShop.repairPhone(this);
                //reopen shop
                panel.makeRepairShopAvailable();
            }
            panel.repaint();
        }
    }
}
