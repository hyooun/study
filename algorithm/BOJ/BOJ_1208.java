import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.StringTokenizer;

public class BOJ_1208 {
    static int[] arr;

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st = new StringTokenizer(br.readLine());

        int n = Integer.parseInt(st.nextToken());
        int s = Integer.parseInt(st.nextToken());
        arr = new int[n];
        ArrayList<Integer> leftSum = new ArrayList<>();
        ArrayList<Integer> rightSum = new ArrayList<>();

        st = new StringTokenizer(br.readLine());
        for (int i = 0; i < n; i++) {
            arr[i] = Integer.parseInt(st.nextToken());
        }
        addSum(0, n/2, 0, leftSum);
        addSum(n/2, n, 0, rightSum);
        Collections.sort(leftSum);
        Collections.sort(rightSum);

        long result = getResult(rightSum, leftSum, s);
        System.out.println(s == 0 ? result - 1 : result);
    }

    private static long getResult(ArrayList<Integer> rightSum, ArrayList<Integer> leftSum, int s) {
        int pl = 0;
        int pr = rightSum.size() - 1;
        long result = 0;

        while (pl < leftSum.size() && pr >= 0) {
            int sum = leftSum.get(pl) + rightSum.get(pr);
            if (sum == s) {
                int leftVal = leftSum.get(pl);
                int rightVal = rightSum.get(pr);
                long leftCnt = 0;
                long rightCnt = 0;
                while (pl < leftSum.size() && leftSum.get(pl) == leftVal) {
                    leftCnt++;
                    pl++;
                }
                while (pr >= 0 && rightSum.get(pr) == rightVal) {
                    rightCnt++;
                    pr--;
                }
                result += leftCnt * rightCnt;
            } else if (sum < s) {
                pl++;
            } else {
                pr--;
            }
        }
        return result;
    }

    private static void addSum(int start, int end, int sum, ArrayList<Integer> list) {
        if (start == end) {
            list.add(sum);
            return;
        }
        addSum(start + 1, end, sum, list);
        addSum(start + 1, end, sum + arr[start], list);
    }
}
