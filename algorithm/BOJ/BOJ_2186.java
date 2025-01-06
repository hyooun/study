import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.StringTokenizer;

public class BOJ_2186 {
    static int n, m, k;
    static String keyword;
    static char[][] board;
    static int[][] dirs = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
    static int[][][] dp;
    static int result;

    public static void main(String[] args) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st = new StringTokenizer(br.readLine());

        n = Integer.parseInt(st.nextToken());
        m = Integer.parseInt(st.nextToken());
        k = Integer.parseInt(st.nextToken());
        board = new char[n][m];

        for (int i = 0; i < n; i++) {
            String line = br.readLine();
            for (int j = 0; j < m; j++) {
                board[i][j] = line.charAt(j);
            }
        }
        keyword = br.readLine();
        dp = new int[n][m][keyword.length()+1];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                Arrays.fill(dp[i][j], -1);
            }
        }

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                if (board[i][j] == keyword.charAt(0)) {
                    result += dfs(1, i, j);
                }
            }
        }
        System.out.println(result);
    }

    private static int dfs(int depth, int x, int y) {
        if (dp[x][y][depth] != -1) {
            return dp[x][y][depth];
        }
        if (depth == keyword.length()) {
            return dp[x][y][depth] = 1;
        }
        dp[x][y][depth] = 0;
        for (int i = 1; i <= k; i++) {
            for (int[] dir : dirs) {
                int nx = x + dir[0] * i;
                int ny = y + dir[1] * i;
                if (nx < 0 || ny < 0 || nx >= n || ny >= m) {
                    continue;
                }
                if (board[nx][ny] == keyword.charAt(depth)) {
                    dp[x][y][depth] += dfs(depth + 1, nx, ny);
                }
            }
        }
        return dp[x][y][depth];
    }
}
