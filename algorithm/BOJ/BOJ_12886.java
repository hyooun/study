import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.Queue;
import java.util.StringTokenizer;

public class BOJ_12886 {
    static int a, b, c;
    static boolean[][] visited;
    static Queue<Stone> q;

    private static class Stone {
        int a;
        int b;
        int c;

        public Stone(int a, int b, int c) {
            this.a = a;
            this.b = b;
            this.c = c;
        }
    }

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st = new StringTokenizer(br.readLine());

        a = Integer.parseInt(st.nextToken());
        b = Integer.parseInt(st.nextToken());
        c = Integer.parseInt(st.nextToken());

        if ((a + b + c) % 3 != 0) {
            System.out.println(0);
            return;
        }
        if (bfs()) {
            System.out.println(1);
        } else {
            System.out.println(0);
        }
    }

    private static boolean bfs() {
        q = new LinkedList<>();
        visited = new boolean[1501][1501];
        q.offer(new Stone(a, b, c));
        visited[a][b] = true;

        while(!q.isEmpty()) {
            Stone now = q.poll();
            if (now.a == now.b && now.b == now.c) {
                return true;
            }
            makeCases(now.a, now.b, now.c);
        }
        return false;
    }

    private static void makeCases(int a, int b, int c) {
        if (a != b) {
            int na = a > b ? a - b : a << 1;
            int nb = a > b ? b << 1 : b - a;
            if (!visited[na][nb]) {
                q.offer(new Stone(na, nb, c));
                visited[na][nb] = true;
            }
        }

        if (b != c) {
            int nb = b > c ? b - c : b << 1;
            int nc = b > c ? c << 1 : c - b;
            if (!visited[nb][nc]) {
                q.offer(new Stone(a, nb, nc));
                visited[a][nb] = true;
            }
        }

        if (a != c) {
            int na = a > c ? a - c : a << 1;
            int nc = a > c ? c << 1 : c - a;
            if (!visited[na][b]) {
                q.offer(new Stone(na, b, nc));
                visited[na][b] = true;
            }
        }
    }
}
