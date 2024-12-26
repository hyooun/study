import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.StringTokenizer;

public class BOJ_2461 {

    public static void main(String[] args) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st = new StringTokenizer(br.readLine());

        int n = Integer.parseInt(st.nextToken());
        int m = Integer.parseInt(st.nextToken());
        int[][] stat = new int[n][m];

        for (int i = 0; i < n; i++) {
            st = new StringTokenizer(br.readLine());
            for (int j = 0; j < m; j++) {
                stat[i][j] = Integer.parseInt(st.nextToken());
            }
            Arrays.sort(stat[i]);
        }
        int[] idx = new int[n];
        Arrays.fill(idx, 0);

        int diff = Integer.MAX_VALUE;
        while (true) {
            int min = Integer.MAX_VALUE;
            int max = 0;
            int minRow = 0;
            for (int i = 0; i < n; i++) {
                if (stat[i][idx[i]] > max) {
                    max = stat[i][idx[i]];
                }
                if (stat[i][idx[i]] < min) {
                    min = stat[i][idx[i]];
                    minRow = i;
                }
            }
            diff = Math.min(diff, max - min);
            idx[minRow]++;
            if (idx[minRow] >= m) {
                break;
            }
        }

        System.out.println(diff);
    }
}
