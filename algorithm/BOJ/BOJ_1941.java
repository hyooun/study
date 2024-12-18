import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.Queue;

public class BOJ_1941 {
    static char[][] board;
    static int result;
    static int[][] dirs = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
    static int[] selected;

    public static void main(String[] args) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        board = new char[5][5];
        selected = new int[7];

        for (int i = 0; i < 5; i++) {
            String line = br.readLine();
            for (int j = 0; j < 5; j++) {
                board[i][j] = line.charAt(j);
            }
        }

        comb(0, 0, 0);
        System.out.println(result);
    }

    public static void comb(int depth, int yCnt, int start) {
        if (yCnt >= 4) {
            return;
        }

        if (depth == 7) {
            check();
            return;
        }

        for (int i = start; i < 25; i++) {
            selected[depth] = i;
            if (board[i/5][i%5] == 'Y') {
                comb(depth + 1, yCnt + 1, i + 1);
            } else {
                comb(depth + 1, yCnt, i + 1);
            }
        }
    }

    public static void check() {
        Queue<int[]> q = new LinkedList<>();
        boolean[] visited = new boolean[7];
        q.offer(new int[] { selected[0] / 5, selected[0] % 5 });
        visited[0] = true;

        int cnt = 1;
        while (!q.isEmpty()) {
            int[] now = q.poll();
            for (int[] dir : dirs) {
                int nx = now[0] + dir[0];
                int ny = now[1] + dir[1];
                if (nx < 0 || ny < 0 || nx >= 5 || ny >= 5) {
                    continue;
                }
                int idx = nx * 5 + ny;
                for (int i = 1; i < 7; i++) {
                    if (!visited[i] && selected[i] == idx) {
                        q.offer(new int[] { nx, ny });
                        visited[i] = true;
                        cnt++;
                    }
                }
            }
        }
        if (cnt == 7) {
            result++;
        }
    }
}
