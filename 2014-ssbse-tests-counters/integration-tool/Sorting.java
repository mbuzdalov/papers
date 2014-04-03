import java.util.Random;

public class Sorting {
  private static Random random = new Random();

  private static int number() {
    return random.nextInt(1000);
  }
  
  public static void main(String[] args) {
    int N = 500;
    int[] arr = new int[N];
    for (int i = 0; i < N; ++i) {
      arr[i] = number();
    }
    for (int i = 0; i < N; ++i) {
      for (int j = 1; j < N; ++j) {
        if (arr[j - 1] > arr[j]) {
          int tmp = arr[j - 1];
          arr[j - 1] = arr[j];
          arr[j] = tmp;
        }
      }
    }
  }
}
