import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

public class BOJ_1669 {

    public static void main(String[] args) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        StringTokenizer st = new StringTokenizer(br.readLine());
        int x = Integer.parseInt(st.nextToken());
        int y = Integer.parseInt(st.nextToken());

        int diff = y - x;
        if (diff < 4) {
            System.out.println(diff);
            return;
        }

        long n = 0;
        while (n * n < diff) {
            n++;
        }
        if (n * n != diff) {
            n--;
        } else {
            System.out.println(2 * n - 1);
            return;
        }

        long result = 2 * n - 1;
        diff -= (int) (n * n);
        while (diff > 0) {
            diff -= (int) Math.min(n, diff);
            result++;
        }

        System.out.println(result);
    }
}
