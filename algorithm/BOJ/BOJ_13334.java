import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.PriorityQueue;
import java.util.StringTokenizer;

public class BOJ_13334 {
    static int n, d;
    private static class Pos implements Comparable<Pos> {
        int start;
        int end;

        public Pos(int start, int end) {
            this.start = start;
            this.end = end;
        }

        @Override
        public int compareTo(Pos o) {
            return Integer.compare(this.end, o.end);
        }
    }

    public static void main(String[] args) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st;

        n = Integer.parseInt(br.readLine());
        PriorityQueue<Pos> pq = new PriorityQueue<>();
        PriorityQueue<Integer> temp = new PriorityQueue<>();
        for (int i = 0; i < n; i++) {
            st = new StringTokenizer(br.readLine());
            int h = Integer.parseInt(st.nextToken());
            int o = Integer.parseInt(st.nextToken());
            if (h < o) {
                pq.offer(new Pos(h, o));
            }
            else {
                pq.offer(new Pos(o, h));
            }
        }
        d = Integer.parseInt(br.readLine());

        int result = 0;
        while(!pq.isEmpty()) {
            Pos now = pq.poll();
            temp.offer(now.start);
            while (!temp.isEmpty() && temp.peek() < now.end - d) {
                temp.poll();
            }
            result = Math.max(result, temp.size());
        }
        System.out.println(result);
    }
}
