import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;

public class BOJ_9328 {
    static int h, w;
    static char[][] board;
    static Set<Character> keys;
    static Map<Character, Set<Integer>> unlocked;
    static Queue<Integer> q;
    static int[][] dirs = { {1, 0}, {-1, 0}, {0, 1}, {0, -1} };
    static int result;

    public static void main(String[] args) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringBuilder sb = new StringBuilder();

        int t = Integer.parseInt(br.readLine());
        for (int tc = 1; tc <= t; tc++) {
            StringTokenizer st = new StringTokenizer(br.readLine());
            h = Integer.parseInt(st.nextToken());
            w = Integer.parseInt(st.nextToken());

            board = new char[h][w];
            keys = new HashSet<>();

            for (int i = 0; i < h; i++) {
                String line = br.readLine();
                for (int j = 0; j < w; j++) {
                    board[i][j] = line.charAt(j);
                }
            }
            String key = br.readLine();
            if (!key.equals("0")) {
                for (int i = 0; i < key.length(); i++) {
                    keys.add(key.charAt(i));
                }
            }
            result = 0;
            bfs();
            sb.append(result).append('\n');
        }
        System.out.print(sb);
    }

    static void bfs() {
        unlocked = new HashMap<>();
        q = new LinkedList<>();
        boolean[][] visited = new boolean[h][w];

        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                if (i == 0 || j == 0 || i == h-1 || j == w-1) {
                    if (board[i][j] == '.') {
                        visited[i][j] = true;
                        q.offer(i * 100 + j);
                    } else if (board[i][j] >= 'a' && board[i][j] <= 'z') {
                        findKey(i, j, visited);
                    } else if (board[i][j] >= 'A' && board[i][j] <= 'Z') {
                        findRoom(i, j, visited);
                    } else if (board[i][j] == '$') {
                        visited[i][j] = true;
                        q.offer(i * 100 + j);
                        result++;
                    }
                }
            }
        }

        while (!q.isEmpty()) {
            int now = q.poll();
            int x = now / 100;
            int y = now % 100;
            for (int[] dir : dirs) {
                int nx = x + dir[0];
                int ny = y + dir[1];
                if (nx < 0 || ny < 0 || nx >= h || ny >= w || visited[nx][ny] || board[nx][ny] == '*') {
                    continue;
                }
                if (board[nx][ny] == '.') {
                    visited[nx][ny] = true;
                    q.offer(nx * 100 + ny);
                } else if (board[nx][ny] >= 'a' && board[nx][ny] <= 'z') {
                    findKey(nx, ny, visited);
                } else if (board[nx][ny] >= 'A' && board[nx][ny] <= 'Z') {
                    findRoom(nx, ny, visited);
                } else if (board[nx][ny] == '$') {
                    visited[nx][ny] = true;
                    q.offer(nx * 100 + ny);
                    result++;
                }
            }
        }
    }

    static void findKey(int x, int y, boolean[][] visited) {
        keys.add(board[x][y]);
        visited[x][y] = true;
        q.offer(x * 100 + y);
        if (unlocked.containsKey(board[x][y])) {
            Set<Integer> set = unlocked.get(board[x][y]);
            for (int pos : set) {
                visited[x][y] = true;
                board[x][y] = '.';
                q.offer(pos);
            }
            unlocked.remove(board[x][y]);
        }
        board[x][y] = '.';
    }

    static void findRoom(int x, int y, boolean[][] visited) {
        visited[x][y] = true;
        if (keys.contains((char) (board[x][y] + 32))) {
            board[x][y] = '.';
            q.offer(x * 100 + y);
        } else {
            char key = (char) (board[x][y] + 32);
            if (unlocked.containsKey(key)) {
                unlocked.get(key).add(x * 100 + y);
            } else {
                Set<Integer> set = new HashSet<>();
                set.add(x * 100 + y);
                unlocked.put(key, set);
            }
        }
    }
}
