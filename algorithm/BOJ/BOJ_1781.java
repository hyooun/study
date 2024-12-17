import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.PriorityQueue;
import java.util.StringTokenizer;

public class BOJ_1781 {

    static class Problem implements Comparable<Problem> {
        int deadline;
        int ramenCnt;

        public Problem(int deadline, int ramenCnt) {
            this.deadline = deadline;
            this.ramenCnt = ramenCnt;
        }

        @Override
        public int compareTo(Problem o) {
            return this.deadline == o.deadline ? o.ramenCnt - this.ramenCnt : this.deadline - o.deadline;
        }
    }
    public static void main(String[] args) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st;

        int n = Integer.parseInt(br.readLine());
        PriorityQueue<Problem> pq = new PriorityQueue<>();
        for (int i = 0; i < n; i++) {
            st = new StringTokenizer(br.readLine());
            pq.offer(new Problem(Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken())));
        }

        int result = 0;
        PriorityQueue<Integer> temp = new PriorityQueue<>();
        while (!pq.isEmpty()) {
            Problem p = pq.poll();
            temp.offer(p.ramenCnt);
            if (temp.size() > p.deadline) {
                temp.poll();
            }
        }
        while (!temp.isEmpty()) {
            result += temp.poll();
        }

        System.out.println(result);
    }
}
