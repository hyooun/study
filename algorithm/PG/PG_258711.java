import java.util.*;

class PG_258711 {

    private static final int MAX_EDGE_CNT = 1000000;

    public int[] solution(int[][] edges) {
        int[] answer = new int[4];
        Map<Integer, List<Integer>> map = new HashMap<>();
        int[] inCnt = new int[MAX_EDGE_CNT + 1];
        int[] outCnt = new int[MAX_EDGE_CNT + 1];

        for (int[] edge : edges) {
            int from = edge[0];
            int to = edge[1];
            inCnt[to]++;
            outCnt[from]++;
            if (!map.containsKey(from)) {
                map.put(from, new ArrayList<>());
            }
            map.get(from).add(to);
        }

        for (int i = 1; i <= MAX_EDGE_CNT; i++) {
            if (inCnt[i] == 0 && outCnt[i] >= 2) {
                answer[0] = i;
                break;
            }
        }

        for (int start : map.get(answer[0])) {
            // 막대인 경우
            if (outCnt[start] == 0) {
                answer[2]++;
                continue;
            }
            int next = map.get(start).get(0);
            while (true) {
                // 막대인 경우
                if (outCnt[next] == 0) {
                    answer[2]++;
                    break;
                }
                // 8자인 경우
                if (outCnt[next] == 2) {
                    answer[3]++;
                    break;
                }
                // 도넛인 경우
                if (start == next) {
                    answer[1]++;
                    break;
                }
                next = map.get(next).get(0);
            }
        }

        return answer;
    }
}