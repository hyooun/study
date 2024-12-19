import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayDeque;
import java.util.Arrays;

public class BOJ_5430 {
    static String p;
    static int n;
    static ArrayDeque<String> deque;

    public static void main(String[] args) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringBuilder sb = new StringBuilder();

        int t = Integer.parseInt(br.readLine());
        for (int tc = 0; tc < t; tc++) {
            // false이면 앞부터, true이면 뒤부터
            boolean flag = false;
            boolean isError = false;
            p = br.readLine();
            n = Integer.parseInt(br.readLine());
            deque = new ArrayDeque<>();
            String line = br.readLine();
            String[] arr = line.substring(1, line.length()-1).split(",");
            if (n != 0) {
                deque.addAll(Arrays.asList(arr));
            }

            for (int i = 0; i < p.length(); i++) {
                if (p.charAt(i) == 'R') {
                    flag = !flag;
                }
                else {
                    // 덱이 비어있는데 D인 경우 error
                    if (deque.isEmpty()) {
                        sb.append("error").append('\n');
                        isError = true;
                        break;
                    }
                    // 덱이 비어있지 않은 경우 하나 제거
                    else {
                        // flag가 true인 경우 뒤에서 하나 제거
                        if (flag) {
                            deque.pollLast();
                        }
                        // flag가 false인 경우 앞에서 하나 제거
                        else {
                            deque.pollFirst();
                        }
                    }
                }
            }
            if (!isError) {
                sb.append('[');
                while (!deque.isEmpty()) {
                    if (flag) {
                        sb.append(deque.pollLast());
                    } else {
                        sb.append(deque.pollFirst());
                    }
                    if (!deque.isEmpty()) {
                        sb.append(',');
                    }
                }
                sb.append(']').append('\n');
            }
        }
        System.out.println(sb);
    }
}
