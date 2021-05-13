import java.util.Scanner;

public class BlackJackTest {
    public static int getInput() {
        System.out.println("1) For hit.\n2) For Stand");
        Scanner sc = new Scanner(System.in);
        int i = sc.nextInt();
        return i;
    }

    public static void printHand(int[] hand, int p) {
        if(p==1)
            System.out.println("Your Hand:\n");
        else
            System.out.println("Computer Hand:\n");

        for (int v : hand)
        {
            if(v != 0)
                System.out.println("\t" + v);
        }
    }

    public static void printWinner(int myVal, int compVal) {
        if (myVal > 21)
            System.out.println("You lost!");
        else if (compVal > 21)
            System.out.println("You win!");
        else if (myVal < compVal)
            System.out.println("You lost!");
        else
            System.out.println("You win!");
    }

}
