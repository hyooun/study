import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;

public class BOJ_4179 {
    static int r, c, result;
    static char[][] board;
    static int[][] fire;
    static int sx, sy;
    static int[][] dirs = { {1, 0}, {-1, 0}, {0, 1}, {0, -1} };

    static class Info {
        int x;
        int y;
        int t;

        public Info(int x, int y, int t) {
            this.x = x;
            this.y = y;
            this.t = t;
        }
    }

    public static void main(String[] args) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st = new StringTokenizer(br.readLine());

        r = Integer.parseInt(st.nextToken());
        c = Integer.parseInt(st.nextToken());
        board = new char[r][c];
        fire = new int[r][c];
        for (int i = 0; i < r; i++) {
            Arrays.fill(fire[i], -1);
        }
        Queue<Info> q = new LinkedList<>();

        for (int i = 0; i < r; i++) {
            String line = br.readLine();
            for (int j = 0; j < c; j++) {
                board[i][j] = line.charAt(j);
                if (board[i][j] == 'F') {
                    q.add(new Info(i, j, 0));
                    fire[i][j] = 0;
                } else if (board[i][j] == 'J') {
                    sx = i; sy = j;
                }
            }
        }
        while (!q.isEmpty()) {
            Info now = q.poll();
            for (int[] dir : dirs) {
                int nx = now.x + dir[0];
                int ny = now.y + dir[1];
                if (nx < 0 || ny < 0 || nx >= r || ny >= c) {
                    continue;
                }
                if (fire[nx][ny] >= 0 || board[nx][ny] == '#') {
                    continue;
                }
                fire[nx][ny] = now.t + 1;
                q.offer(new Info(nx, ny, now.t + 1));
            }
        }

        result = Integer.MAX_VALUE;
        boolean[][] visited = new boolean[r][c];
        visited[sx][sy] = true;
        q.offer(new Info(sx, sy, 0));
        while (!q.isEmpty()) {
            Info now = q.poll();
            for (int[] dir : dirs) {
                int nx = now.x + dir[0];
                int ny = now.y + dir[1];
                if (nx < 0 || ny < 0 || nx >= r || ny >= c) {
                    result = Math.min(result, now.t + 1);
                    continue;
                }
                if (!visited[nx][ny] && board[nx][ny] == '.') {
                    visited[nx][ny] = true;
                    if (fire[nx][ny] < 0 || now.t + 1 < fire[nx][ny]) {
                        q.offer(new Info(nx, ny, now.t + 1));
                    }
                }
            }
        }

        if (result == Integer.MAX_VALUE) {
            System.out.println("IMPOSSIBLE");
        }
        else {
            System.out.println(result);
        }
    }
}
