import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.PriorityQueue;
import java.util.StringTokenizer;

public class BOJ_28092 {
    static int n, q;
    static int[] parent, size;
    static boolean[] deleted;
    static PriorityQueue<Info> pq;
    private static class Info {
        int nodeNum;
        int size;

        public Info(int nodeNum, int size) {
            this.nodeNum = nodeNum;
            this.size = size;
        }
    };

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st = new StringTokenizer(br.readLine());
        StringBuilder sb = new StringBuilder();

        pq = new PriorityQueue<>((o1, o2) -> {
            int sizeCompare = Integer.compare(o2.size, o1.size);
            if (sizeCompare != 0) {
                return sizeCompare;
            }
            return Integer.compare(o1.nodeNum, o2.nodeNum);
        });

        n = Integer.parseInt(st.nextToken());
        q = Integer.parseInt(st.nextToken());
        parent = new int[n + 1];
        size = new int[n + 1];
        deleted = new boolean[n + 1];

        for (int i = 1; i <= n; i++) {
            parent[i] = i;
            size[i] = 1;
            pq.offer(new Info(i, 1));
        }

        for (int i = 0; i < q; i++) {
            st = new StringTokenizer(br.readLine());
            int type = Integer.parseInt(st.nextToken());
            if (type == 1) {
                int a = Integer.parseInt(st.nextToken());
                int b = Integer.parseInt(st.nextToken());
                union(a, b);
            } else {
                while(!pq.isEmpty()) {
                    Info now = pq.poll();
                    int x = find(now.nodeNum);
                    if (deleted[x]) {
                        continue;
                    }
                    sb.append(x).append('\n');
                    deleted[x] = true;
                    break;
                }
            }
        }
        System.out.print(sb);
    }

    private static void union(int x, int y) {
        x = find(x);
        y = find(y);

        if (x != y) {
            if (deleted[x] || deleted[y]) {
                deleted[x] = true;
                deleted[y] = true;
                return;
            }
            if (x > y) {
                int temp = x;
                x = y;
                y = temp;
            }
            parent[y] = x;
            size[x] += size[y];
            pq.offer(new Info(x, size[x]));
        } else {
            deleted[find(x)] = true;
        }
    }

    private static int find(int x) {
        if (x == parent[x]) {
            return x;
        }
        return parent[x] = find(parent[x]);
    }
}
