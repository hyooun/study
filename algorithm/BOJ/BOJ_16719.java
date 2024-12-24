import java.io.BufferedReader;
import java.io.InputStreamReader;

public class BOJ_16719 {
    static StringBuilder result;

    public static void main(String[] args) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        result = new StringBuilder();
        String s = br.readLine();

        dfs(new StringBuilder(s));
        result.append(s);
        System.out.println(result);
    }

    public static void dfs(StringBuilder sb) {
        if (sb.length() == 1) {
            return;
        }
        for (int i = 0; i < sb.length(); i++) {
            StringBuilder temp = new StringBuilder(sb);
            temp.deleteCharAt(i);
            if (temp.toString().compareTo(sb.toString()) < 0) {
                dfs(temp);
                result.append(temp).append('\n');
                break;
            }
        }
    }
}
