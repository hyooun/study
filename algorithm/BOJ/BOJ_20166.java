import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public class BOJ_20166 {
    static int n, m, k;
    static char[][] board;
    static String[] words;
    static int[][] dirs = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}, {1, 1}, {1, -1}, {-1, 1}, {-1, -1}};
    static Map<String, Integer> map;

    public static void main(String[] args) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st = new StringTokenizer(br.readLine());

        n = Integer.parseInt(st.nextToken());
        m = Integer.parseInt(st.nextToken());
        k = Integer.parseInt(st.nextToken());
        board = new char[n][m];
        words = new String[k];
        map = new HashMap<>();

        for (int i = 0; i < n; i++) {
            String line = br.readLine();
            for (int j = 0; j < m; j++) {
                board[i][j] = line.charAt(j);
            }
        }

        for (int i = 0; i < k; i++) {
            words[i] = br.readLine();
        }

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                dfs(1, i, j, new StringBuilder().append(board[i][j]));
            }
        }

        for (String s : words) {
            if (map.containsKey(s)) {
                System.out.println(map.get(s));
            }
            else {
                System.out.println(0);
            }
        }
    }

    public static void dfs(int depth, int x, int y, StringBuilder sb) {
        if (!map.containsKey(sb.toString())) {
            map.put(sb.toString(), 1);
        }
        else {
            map.put(sb.toString(), map.get(sb.toString()) + 1);
        }
        if (depth == 5) {
            return;
        }
        for (int[] dir : dirs) {
            int nx = x + dir[0];
            int ny = y + dir[1];
            if (nx < 0) {
                nx = n-1;
            }
            if (nx >= n) {
                nx = 0;
            }
            if (ny < 0) {
                ny = m-1;
            }
            if (ny >= m) {
                ny = 0;
            }
            dfs(depth + 1, nx, ny, sb.append(board[nx][ny]));
            sb.deleteCharAt(sb.length()-1);
        }
    }
}
