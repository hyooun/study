import java.io.*;
import java.util.*;

public class BOJ_13975 {

    public static void main(String[] args) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st;
        StringBuilder sb = new StringBuilder();

        PriorityQueue<Long> pq;
        int t = Integer.parseInt(br.readLine());
        for (int tc = 0; tc < t; tc++) {
            int k = Integer.parseInt(br.readLine());
            pq = new PriorityQueue<>();
            st = new StringTokenizer(br.readLine());
            for (int i = 0; i < k; i++) {
                pq.offer(Long.parseLong(st.nextToken()));
            }
            long result = 0;
            while (true) {
                long p1 = pq.poll();
                if (pq.isEmpty()) {
                    break;
                }
                long p2 = pq.poll();
                result += p1 + p2;
                pq.offer(p1 + p2);
            }
            sb.append(result).append('\n');
        }
        System.out.println(sb);
    }
}
