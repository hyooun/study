import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

public class BOJ_1765 {
    static int n, m;
    static ArrayList<Integer>[] friend;
    static ArrayList<Integer>[] enemy;
    static int[] parent;

    public static void main(String[] args) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st;

        n = Integer.parseInt(br.readLine());
        m = Integer.parseInt(br.readLine());
        friend = new ArrayList[n + 1];
        enemy = new ArrayList[n + 1];
        parent = new int[n + 1];
        for (int i = 1; i <= n; i++) {
            friend[i] = new ArrayList<>();
            enemy[i] = new ArrayList<>();
            parent[i] = i;
        }

        for (int i = 0; i < m; i++) {
            st = new StringTokenizer(br.readLine());
            String relation = st.nextToken();
            int p = Integer.parseInt(st.nextToken());
            int q = Integer.parseInt(st.nextToken());
            if (relation.equals("E")) {
                enemy[p].add(q);
                enemy[q].add(p);
            } else {
                friend[p].add(q);
                friend[q].add(p);
            }
        }

        for (int i = 1; i <= n; i++) {
            for (int j = 0; j < enemy[i].size(); j++) {
                int temp = enemy[i].get(j);
                for (int k = 0; k < enemy[temp].size(); k++) {
                    if (enemy[temp].get(k) == i) {
                        continue;
                    }
                    friend[i].add(enemy[temp].get(k));
                    friend[enemy[temp].get(k)].add(i);
                }
            }
        }

        for (int i = 1; i <= n; i++) {
            for (int j = 0; j < friend[i].size(); j++) {
                union(i, friend[i].get(j));
            }
        }

        Set<Integer> set = new HashSet<>();
        for (int i = 1; i <= n; i++) {
            set.add(parent[i]);
        }
        System.out.println(set.size());
    }

    private static void union(int x, int y) {
        x = find(x);
        y = find(y);
        if (x != y) {
            parent[y] = x;
        }
    }

    private static int find(int x) {
        if (parent[x] == x) {
            return x;
        }
        return parent[x] = find(parent[x]);
    }
}
