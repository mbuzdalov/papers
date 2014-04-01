package jobshop;

import ru.ifmo.ctd.ngp.util.*;

import java.io.*;
import java.util.*;

public class DataFileReader {
	public static class InputDataSet {
		private InputDataSet(int[][] times, int[][] machines) {
			this.times = times;
			this.machines = machines;
		}
		
		private final int[][] times;
		private final int[][] machines;
		
		public int[][] getTimes() {
			return ArraysEx.copy(times);
		}
		
		public int[][] getMachines() {
			return ArraysEx.copy(machines);
		}
	}
	
	private final Map<String, InputDataSet> dataSets = new HashMap<>();

    private void loadFrom(BufferedReader in) throws IOException {
        String line;
        while ((line = in.readLine()) != null) {
            line = line.trim();
            if (line.startsWith("instance")) {
                String id = line.substring("instance".length()).trim();
                for (int i = 0; i < 3; ++i) {
                    in.readLine();
                }
                line = in.readLine();
                if (line != null) {
                    line = line.trim();
                    int ws = line.indexOf(' ');
                    int rows = Integer.parseInt(line.substring(0, ws));
                    int cols = Integer.parseInt(line.substring(ws + 1));
                    int[][] machines = new int[rows][cols];
                    int[][] times = new int[rows][cols];
                    for (int i = 0; i < rows; ++i) {
                        StringTokenizer st = new StringTokenizer(in.readLine());
                        for (int j = 0; j < cols; ++j) {
                            machines[i][j] = Integer.parseInt(st.nextToken());
                            times[i][j] = Integer.parseInt(st.nextToken());
                        }
                    }
                    dataSets.put(id, new InputDataSet(times, machines));
                }
            }
        }
    }

	public InputDataSet get(String name) {
		return dataSets.get(name);
	}

    public DataFileReader(File file) throws IOException {
		try (FileReader in = new FileReader(file);
             BufferedReader inr = new BufferedReader(in)) {
            loadFrom(inr);
        }
	}
}
