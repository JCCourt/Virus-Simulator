package Question_2;

public class RepairShop {

    public static final int SHOP_WIDTH = 100;
    public static final int SHOP_HEIGHT = 50;

    public int x;
    public int y;

    public RepairShop() {
        x = 0;
    }

    public synchronized void repairPhone(Phone phone) {
        phone.setPhoneState(Phone.PhoneState.HEALTHY);
    }
}
