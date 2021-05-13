import java.lang.Math;

public class OverloadingTest {
    public OverloadingTest() {}

    public static int retInt() {
        return 1;
    }

    public static boolean retBool() {
        return true;
    }

    public static int sqrt(int i) {
        return (int) Math.floor(Math.sqrt((double) i));
    }
}
