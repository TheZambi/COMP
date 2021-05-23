import java.util.Scanner;

public class BubbleSortTest {
    public static int getInput() {
        System.out.println("Insert integer");
        Scanner sc = new Scanner(System.in);
        int i = sc.nextInt();
        return i;
    }

    public static int getArraySize() {
        System.out.println("Insert array Size");
        Scanner sc = new Scanner(System.in);
        int i = sc.nextInt();
        return i;
    }


    public static void printResult(int[] result) {
        System.out.print("[");
        int len = result.length;
        int counter = 0;
        for (int v : result)
        {
            System.out.print(v);
            counter++;
            if(counter != len){
                System.out.print(",");
            }
        }
        System.out.print("]");
    }

}
