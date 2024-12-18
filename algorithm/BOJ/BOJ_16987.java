import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

public class BOJ_16987 {
    static int n;
    static int result;
    static int[][] egg;

    public static void main(String[] args) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st;
        n = Integer.parseInt(br.readLine());
        egg = new int[n][2];

        for (int i = 0; i < n; i++) {
            st = new StringTokenizer(br.readLine());
            egg[i][0] = Integer.parseInt(st.nextToken());
            egg[i][1] = Integer.parseInt(st.nextToken());
        }

        // egg는 s, w, check는 visited + broken
        dfs(0, 0);
        System.out.println(result);
    }

    public static void dfs(int holding, int broken) {
        if (holding == n) {
            calResult(egg);
            return;
        }
        if (egg[holding][0] <= 0 || broken == n-1) {
            dfs(holding + 1, broken);
            return;
        }
        int temp = broken;
        for (int i = 0; i < n; i++) {
            if (holding == i || egg[i][0] <= 0) {
                continue;
            }
            conflict(holding, i);
            if (egg[holding][0] <= 0) {
                broken++;
            }
            if (egg[i][0] <= 0) {
                broken++;
            }
            dfs(holding + 1, broken);
            rollback(holding, i);
            broken = temp;
        }
    }

    public static void conflict(int left, int right) {
        egg[left][0] -= egg[right][1];
        egg[right][0] -= egg[left][1];
    }

    public static void rollback(int left, int right) {
        egg[left][0] += egg[right][1];
        egg[right][0] += egg[left][1];
    }

    public static void calResult(int[][] egg) {
        int cnt = 0;
        for (int i = 0; i < n; i++) {
            if (egg[i][0] <= 0) {
                cnt++;
            }
        }
        if (cnt > result) {
            result = cnt;
        }
    }
}
