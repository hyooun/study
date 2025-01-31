import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class BOJ_10422 {
    private static final int MOD_NUM = 1000000007;

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        long[] dp = new long[5001];

        dp[0] = 1;
        dp[2] = 1;
        for (int i = 4; i <= 5000; i+=2) {
            for (int j = 0; j <= i - 1; j+=2) {
                dp[i] += dp[i-j-2] * dp[j];
                if (dp[i] >= MOD_NUM) {
                    dp[i] = dp[i] % MOD_NUM;
                }
            }
        }

        int t = Integer.parseInt(br.readLine());
        for (int tc = 1; tc <= t; tc++) {
            int n = Integer.parseInt(br.readLine());
            System.out.println(dp[n]);
        }
    }
}
