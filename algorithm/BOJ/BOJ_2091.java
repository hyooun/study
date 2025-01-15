import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

public class BOJ_2091 {

    public static void main(String[] args) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st = new StringTokenizer(br.readLine());
        StringBuilder sb = new StringBuilder();

        int x = Integer.parseInt(st.nextToken());
        int[] coinCnt = new int[4];
        final int[] value = { 1, 5, 10, 25 };
        for (int i = 0; i < 4; i++) {
            coinCnt[i] = Integer.parseInt(st.nextToken());
        }

        int[][] dp = new int[x + 1][5];
        for (int i = 1; i <= x; i++) {
            dp[i][4] = -1;
        }

        for (int i = 1; i <= x; i++) {
            for (int j = 0; j < 4; j++) {
                if (i - value[j] < 0 || dp[i - value[j]][4] == -1) {
                    continue;
                }
                if (dp[i - value[j]][j] + 1 <= coinCnt[j]) {
                    if (dp[i - value[j]][4] + 1 > dp[i][4]) {
                        dp[i][4] = dp[i - value[j]][4] + 1;
                        for (int k = 0; k < 4; k++) {
                            dp[i][k] = dp[i - value[j]][k];
                        }
                        dp[i][j]++;
                    }
                }
            }
        }
        for (int i = 0; i < 4; i++) {
            sb.append(dp[x][i]).append(' ');
        }
        System.out.println(sb);
    }
}
