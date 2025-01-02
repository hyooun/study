import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;

public class BOJ_3687 {

    public static void main(String[] args) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        int t = Integer.parseInt(br.readLine());
        StringBuilder sb = new StringBuilder();

        // 2 5 5 4 5 6 3 7 6 6
        long[] dp = new long[101];
        Arrays.fill(dp, Long.MAX_VALUE);

        dp[2] = 1;
        dp[3] = 7;
        dp[4] = 4;
        dp[5] = 2;
        dp[6] = 6;
        dp[7] = 8;
        dp[8] = 10;

        // 2, 3, 4, 5, 6, 7개일 때 최소값
        int[] minForEach = { 1, 7, 4, 2, 0, 8 };
        for (int i = 9; i <= 100; i++) {
            for (int j = 2; j <= 7; j++) {
                String temp = String.valueOf(dp[i - j]) + minForEach[j - 2];
                dp[i] = Math.min(dp[i], Long.parseLong(temp));
            }
        }

        for (int tc = 0; tc < t; tc++) {
            int n = Integer.parseInt(br.readLine());
            sb.append(dp[n]).append(' ').append(getMaxNumber(n)).append('\n');
        }
        System.out.print(sb);
    }

    private static String getMaxNumber(int n) {
        StringBuilder sb = new StringBuilder();
        // 홀수면 7부터 시작, 나머지 1
        if (n % 2 == 1) {
            sb.append(7);
            for (int i = 0; i < n / 2 - 1; i++) {
                sb.append(1);
            }
        }
        // 짝수면 전부 1
        else {
            for (int i = 0; i < n / 2; i++) {
                sb.append(1);
            }
        }
        return sb.toString();
    }
}
