import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class BOJ_2412 {

    static int n, t;
    static ArrayList<Integer>[] rock;
    static int result;

    private static class Pos {
        int x;
        int y;
        int cnt;

        public Pos(int x, int y, int cnt) {
            this.x = x;
            this.y = y;
            this.cnt = cnt;
        }
    }

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st = new StringTokenizer(br.readLine());

        n = Integer.parseInt(st.nextToken());
        t = Integer.parseInt(st.nextToken());
        rock = new ArrayList[t+1];
        for (int i = 0; i <= t; i++) {
            rock[i] = new ArrayList<>();
        }

        rock[0].add(0);
        for (int i = 1; i <= n; i++) {
            st = new StringTokenizer(br.readLine());
            int x = Integer.parseInt(st.nextToken());
            int y = Integer.parseInt(st.nextToken());
            rock[y].add(x);
        }

        for (int i = 1; i <= t; i++) {
            Collections.sort(rock[i]);
        }

        result = Integer.MAX_VALUE;
        bfs();
        System.out.println(result == Integer.MAX_VALUE ? -1 : result);
    }

    private static void bfs() {
        Queue<Pos> q = new LinkedList<>();
        q.offer(new Pos(0, 0, 0));

        while (!q.isEmpty()) {
            Pos now = q.poll();
            if (now.y == t) {
                result = Math.min(result, now.cnt);
                return;
            }
            for (int y = now.y - 2; y <= now.y + 2; y++) {
                if (y < 0 || y > t) {
                    continue;
                }
                for (int i = 0; i < rock[y].size(); i++) {
                    if (Math.abs(now.x - rock[y].get(i)) <= 2) {
                        q.offer(new Pos(rock[y].get(i), y, now.cnt + 1));
                        rock[y].remove(i);
                        i--;
                    }
                }
            }
        }
    }
}
