import java.io.BufferedReader;
import java.io.InputStreamReader;

public class BOJ_2179 {

    public static void main(String[] args) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        int n = Integer.parseInt(br.readLine());
        String[] words = new String[n];

        for (int i = 0; i < n; i++) {
            words[i] = br.readLine();
        }

        int maxPrefixLen = 0;
        int hashVal = 0;

        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                if (words[i].compareTo(words[j]) == 0) {
                    continue;
                }
                int len = Math.min(words[i].length(), words[j].length());
                int idx = 0;
                while (idx < len) {
                    if (words[i].charAt(idx) == words[j].charAt(idx)) {
                        idx++;
                    } else {
                        break;
                    }
                }
                if (idx > maxPrefixLen) {
                    maxPrefixLen = idx;
                    hashVal = i * 100000 + j;
                }
            }
        }
        System.out.println(words[hashVal / 100000]);
        System.out.println(words[hashVal % 100000]);
    }
}
