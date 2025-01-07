import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;

public class BOJ_17472 {
    static int n, m;
    static int[][] board;
    static boolean[][] visited;
    static ArrayList<Integer>[] islands;
    static int islandNum;
    static int[][] dirs = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
    static PriorityQueue<Edge> pq;
    static int[] parents;

    private static class Edge implements Comparable<Edge> {
        int from;
        int to;
        int dist;

        public Edge(int from, int to, int dist) {
            this.from = from;
            this.to = to;
            this.dist = dist;
        }

        @Override
        public int compareTo(Edge o) {
            return Integer.compare(this.dist, o.dist);
        }
    }

    public static void main(String[] args) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st = new StringTokenizer(br.readLine());

        n = Integer.parseInt(st.nextToken());
        m = Integer.parseInt(st.nextToken());
        board = new int[n][m];
        islands = new ArrayList[7];
        for (int i = 1; i <= 6; i++) {
            islands[i] = new ArrayList<>();
        }
        for (int i = 0; i < n; i++) {
            st = new StringTokenizer(br.readLine());
            for (int j = 0; j < m; j++) {
                board[i][j] = Integer.parseInt(st.nextToken());
            }
        }

        visited = new boolean[n][m];
        islandNum = 1;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                if (board[i][j] == 1 && !visited[i][j]) {
                    bfs(i, j);
                    islandNum++;
                }
            }
        }
        pq = new PriorityQueue<>();
        getEdges();
        System.out.println(kruskal());
    }

    private static void bfs(int sx, int sy) {
        Queue<Integer> q = new LinkedList<>();
        q.offer(sx * 100 + sy);
        visited[sx][sy] = true;
        while (!q.isEmpty()) {
            int now = q.poll();
            int x = now / 100;
            int y = now % 100;
            board[x][y] = islandNum;
            islands[islandNum].add(now);
            for (int[] dir : dirs) {
                int nx = x + dir[0];
                int ny = y + dir[1];
                if (nx < 0 || ny < 0 || nx >= n || ny >= m || visited[nx][ny] || board[nx][ny] == 0) {
                    continue;
                }
                visited[nx][ny] = true;
                q.offer(nx * 100 + ny);
            }
        }
    }

    private static void getEdges() {
        for (int i = 1; i < islandNum; i++) {
            for (int j = 0; j < islands[i].size(); j++) {
                int point = islands[i].get(j);
                int x = point / 100;
                int y = point % 100;
                for (int[] dir : dirs) {
                    for (int d = 1; d < 10; d++) {
                        int nx = x + dir[0] * d;
                        int ny = y + dir[1] * d;
                        if (nx < 0 || ny < 0 || nx >= n || ny >= m) {
                            break;
                        }
                        if (board[nx][ny] == i) {
                            break;
                        }
                        if (board[nx][ny] != 0 && board[nx][ny] != i) {
                            if (d > 2) {
                                pq.offer(new Edge(i, board[nx][ny], d - 1));
                            }
                            break;
                        }
                    }
                }
            }
        }
    }

    private static int kruskal() {
        parents = new int[7];
        for (int i = 1; i <= 6; i++) {
            parents[i] = i;
        }
        int result = 0;
        int cnt = 0;
        while (!pq.isEmpty()) {
            Edge now = pq.poll();
            if (union(now.from, now.to)) {
                result += now.dist;
                cnt++;
            }
        }
        if (cnt != islandNum - 2) {
            return -1;
        }
        if (result == 0) {
            return -1;
        }
        return result;
    }

    private static int find(int x) {
        if (parents[x] == x) {
            return x;
        }
        return parents[x] = find(parents[x]);
    }

    private static boolean union(int x, int y) {
        x = find(x);
        y = find(y);

        if (x != y) {
            parents[y] = x;
            return true;
        }
        return false;
    }
}
