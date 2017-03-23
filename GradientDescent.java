import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.List;
import java.util.ArrayList;
import java.io.FileWriter;

public class GradientDescent {
	private double[] coefficients = new double[385];
	private double[][] numsList = null;
	private double[] referenceList = null;
	private double[] cache = null;
	private int colCount = 0;
	private int multiThreadLevel = 3;
	private CalThread[] calThreads;

	public GradientDescent(Scanner sc, int count) {
		colCount = count;
		numsList = new double[count][385];
		referenceList = new double[count];
		cache = new double[count];
		for (int i = 0; i < count && sc.hasNextLine(); i++) {
			String s = sc.nextLine();
			referenceList[i] = getNums(s, i);
		}

		for (int i = 0; i < coefficients.length; i++)
			coefficients[i] = 1;

		calThreads = new CalThread[multiThreadLevel];
		for (int i = 0; i < multiThreadLevel; i++) {
			int start = i * coefficients.length / multiThreadLevel;
			int end = (i + 1) * coefficients.length / multiThreadLevel;
			calThreads[i] = new CalThread(start, end);
			calThreads[i].setDaemon(true);
			calThreads[i].start();
		}
	}

	private double getNums(String s, int col) {
		String[] nums = s.split(",");
		numsList[col][0] = 1;
		for (int i = 1; i < 385; i++) {
			numsList[col][i] = Double.valueOf(nums[i]);
		}
		return Double.valueOf(nums[nums.length - 1]);
	}

	private double getNums(String s, List<Double> list) {
		String[] nums = s.split(",");
		list.add((double)1);
		for (int i = 1; i < nums.length - 1; i++)
			list.add(Double.valueOf(nums[i]));
		return Double.valueOf(nums[nums.length - 1]);
	}

	public void run(double factor, int count) {
		double res = oneRun(factor);
		for (int i = 1; i < count; i++) {
			double temp =  oneRun(factor);
			if (temp > res)
				factor /= 2;
			res = temp; 
		}
	}

	public void test(Scanner sc, int count) {
		double res = 0;
		int t = count;
		while (sc.hasNextLine() && t > 0) {
			String s = sc.nextLine();
			List<Double> tempList = new ArrayList<Double>();
			Double reference = getNums(s, tempList);
			double sum = 0;
			for (int i = 0; i < tempList.size(); i++)
				sum += coefficients[i] * tempList.get(i);
			// System.out.println(reference + " " + sum);
			res += Math.pow(reference - sum, 2)/count;
			t--;
		}
		// System.out.println(Math.pow(res, 0.5));
		try(FileWriter out = new FileWriter("./result.txt")) {
			out.write(String.valueOf(Math.pow(res, 0.5)));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void writeRes() {
		try(FileWriter fw = new FileWriter("./coefficients.txt")) {
			for (int i = 0; i < coefficients.length; i++)
				fw.write(String.valueOf(coefficients[i]) + " ");
		} catch (Exception e) {

		}
	}

	class CalThread extends Thread {
		int start, end;
		double factor;
		boolean startOneRun = false;
		public CalThread(int start, int end) {
			super();
			this.start = start;
			this.end = end;
		}

		public synchronized boolean getStartOneRun() {
			return startOneRun;
		}

		public synchronized void startOneRun(double factor) {
			startOneRun = true;
			this.factor = factor;
			notify();
		}

		public void run() {
			try {
				while (true) {
					synchronized(this) {
						while (!startOneRun)
							wait();
						double sum = 0;
						for (int i = start; i < end; i++) {
							for (int j = 0; j < cache.length; j++)
								sum += cache[j] * numsList[j][i];
							coefficients[i] -= factor*sum/colCount;
						}
						startOneRun = false;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private double oneRun(double factor) {
		double res = 0;
		for (int i = 0; i < colCount; i++) {
			double sum = 0;
			for (int j = 0; j < 385; j++) {
				sum += numsList[i][j] * coefficients[j];
			}
			sum -= referenceList[i];
			cache[i] = sum;
			res += Math.pow(cache[i], 2)/colCount;
		}

		for (int i = 0; i < multiThreadLevel; i++) {
			calThreads[i].startOneRun(factor);
		}

		for (int i = 0; i < multiThreadLevel; i++) {
			while (calThreads[i].getStartOneRun())
				;
		}

		return Math.pow(res, 0.5);
	}

	public static void main(String[] args) {
		Scanner sc = null;
		try {
			sc = new Scanner(new BufferedInputStream(new FileInputStream("./save_train.csv")));
			if (sc.hasNextLine())
				sc.nextLine();
			GradientDescent gd = new GradientDescent(sc, Integer.valueOf(args[0]));
			Scanner terminal = new Scanner(System.in);
			if (terminal.hasNextLine()) {
				int times = terminal.nextInt();
				double factor = terminal.nextDouble();
				gd.run(factor, times);
				System.out.println("This run done!");
				if (args.length > 1)
					gd.test(sc, Integer.valueOf(args[1]));
			}
			terminal.close();
			gd.writeRes();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (sc != null)
				sc.close();
		}
	}
}