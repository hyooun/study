import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

public class BOJ_2662 {

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st = new StringTokenizer(br.readLine());

        int n = Integer.parseInt(st.nextToken());
        int m = Integer.parseInt(st.nextToken());
        int[][] data = new int[n+1][m+1];
        int[][] temp = new int[n+1][m+1];
        int[][] dp = new int[n+1][m+1];
        int[] result = new int[m+1];

        for (int i = 1; i <= n; i++) {
            st = new StringTokenizer(br.readLine());
            st.nextToken();
            for (int j = 1; j <= m; j++) {
                data[i][j] = Integer.parseInt(st.nextToken());
            }
        }

        for (int j = 1; j <= m; ++j) {
            for (int i = 0; i <= n; ++i) {
                for (int k = n - i; k >= 0; --k) {
                    if (dp[i + k][j] < dp[k][j - 1] + data[i][j]) {
                        dp[i + k][j] = dp[k][j - 1] + data[i][j];
                        temp[i + k][j] = i;
                    }
                }
            }
        }

        getResult(n, m, result, temp);
        System.out.println(dp[n][m]);
        for (int i = 1; i <= m; i++) {
            System.out.print(result[i] + " ");
        }
    }

    private static void getResult(int n, int m, int[] result, int[][] temp) {
        if (m == 0) {
            return;
        }
        result[m] = temp[n][m];
        getResult(n - result[m], m - 1, result, temp);
    }
}
