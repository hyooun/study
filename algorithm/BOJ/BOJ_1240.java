import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.StringTokenizer;

public class BOJ_1240 {
    static int n, m;
    static ArrayList<Info>[] graph;

    static class Info {
        int node;
        int w;

        public Info(int node, int w) {
            this.node = node;
            this.w = w;
        }
    }

    public static void main(String[] args) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st = new StringTokenizer(br.readLine());
        StringBuilder sb = new StringBuilder();

        n = Integer.parseInt(st.nextToken());
        m = Integer.parseInt(st.nextToken());
        graph = new ArrayList[n+1];
        for (int i = 1; i <= n; i++) {
            graph[i] = new ArrayList<>();
        }

        for (int i = 1; i < n; i++) {
            st = new StringTokenizer(br.readLine());
            int a = Integer.parseInt(st.nextToken());
            int b = Integer.parseInt(st.nextToken());
            int w = Integer.parseInt(st.nextToken());
            graph[a].add(new Info(b, w));
            graph[b].add(new Info(a, w));
        }

        for (int i = 0; i < m; i++) {
            st = new StringTokenizer(br.readLine());
            int from = Integer.parseInt(st.nextToken());
            int to = Integer.parseInt(st.nextToken());
            sb.append(bfs(from, to)).append('\n');
        }
        System.out.print(sb);
    }

    public static int bfs(int from, int to) {
        Queue<Info> q = new LinkedList<>();
        boolean[] visited = new boolean[n+1];
        q.offer(new Info(from, 0));
        visited[from] = true;

        while (!q.isEmpty()) {
            Info now = q.poll();
            if (now.node == to) {
                return now.w;
            }
            for (Info next : graph[now.node]) {
                if (!visited[next.node]) {
                    visited[next.node] = true;
                    q.offer(new Info(next.node, now.w + next.w));
                }
            }
        }
        return 0;
    }
}
