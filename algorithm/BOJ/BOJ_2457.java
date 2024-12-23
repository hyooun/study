import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.StringTokenizer;

public class BOJ_2457 {
    static class Flower implements Comparable<Flower> {
        int s;
        int e;
        public Flower(int s, int e) {
            this.s = s;
            this.e = e;
        }
        @Override
        public int compareTo(Flower o) {
            return this.s == o.s ? o.e - this.e : this.s - o.s;
        }
    }

    public static void main(String[] args) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st;
        int n = Integer.parseInt(br.readLine());
        Flower[] flowers = new Flower[n];
        int sd = calDate(3, 1);
        int ed = calDate(12, 1);

        for (int i = 0; i < n; i++) {
            st = new StringTokenizer(br.readLine());
            int start = calDate(Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken()));
            int end = calDate(Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken()));
            flowers[i] = new Flower(start, end);
        }
        Arrays.sort(flowers);

        int result = 0;
        int end = 0;
        int idx = 0;
        while (sd < ed) {
            boolean flag = false;
            for (int i = idx; i < n; i++) {
                if (flowers[i].s > sd) {
                    break;
                }
                if (flowers[i].e > end) {
                    end = flowers[i].e;
                    idx++;
                    flag = true;
                }
            }
            if (flag) {
                sd = end;
                result++;
            } else {
                break;
            }
        }
        System.out.println(end < ed ? "0" : result);
    }

    public static int calDate(int month, int date) {
        int cnt = date;
        for (int i = 1; i < month; i++) {
            if (i == 2) {
                cnt += 28;
            }
            else if (i == 4 || i == 6 || i == 9 || i == 11) {
                cnt += 30;
            }
            else {
                cnt += 31;
            }
        }
        return cnt;
    }
}
