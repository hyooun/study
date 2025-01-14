import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;

public class BOJ_1897 {

    static int d;
    static String[] words;
    static boolean[] visited;
    static Queue<Integer> q;
    static String result;

    public static void main(String[] args) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st = new StringTokenizer(br.readLine());

        d = Integer.parseInt(st.nextToken());
        String start = st.nextToken();;
        words = new String[d];
        visited = new boolean[d];
        q = new LinkedList<>();

        for (int i = 0; i < d; i++) {
            words[i] = br.readLine();
            if (words[i].equals(start)) {
                visited[i] = true;
                q.offer(i);
            }
        }
        result = "";
        bfs();
        System.out.println(result);
    }

    private static void bfs() {
        while (!q.isEmpty()) {
            int now = q.poll();
            if (words[now].length() > result.length()) {
                result = words[now];
            }
            for (int i = 0; i < words.length; i++) {
                if (visited[i]) {
                    continue;
                }
                if (compare(words[now], words[i])) {
                    q.offer(i);
                    visited[i] = true;
                }
            }
        }
    }

    private static boolean compare(String from, String to) {
        if (from.length() + 1 != to.length()) {
            return false;
        }
        boolean flag = true;
        for (int i = 0, j = 0; i < from.length() && j < to.length();) {
            if (from.charAt(i) != to.charAt(j)) {
                j++;
                if (flag) {
                    flag = false;
                }
                else {
                    return false;
                }
            } else {
                i++;
                j++;
            }
        }
        return true;
    }
}
