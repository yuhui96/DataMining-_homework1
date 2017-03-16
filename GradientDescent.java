import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.List;
import java.util.ArrayList;

public class GradientDescent {
	private double[] coefficients = new double[385];
	private List< List<Double> > numsList = new ArrayList<>();
	private List<Double> referenceList = new ArrayList<Double>();
	List<Double> cache = new ArrayList<Double>();

	public GradientDescent(Scanner sc, int count) {
		while (sc.hasNextLine() && count > 0) {
			String s = sc.nextLine();
			List<Double> tempList = new ArrayList<Double>();
			Double reference = getNums(s, tempList);
			numsList.add(tempList);
			referenceList.add(reference);
			count--;
		}

		for (int i = 0; i < coefficients.length; i++)
			coefficients[i] = 1;
	}

	private double getNums(String s, List<Double> list) {
		String[] nums = s.split(",");
		list.add((double)1);
		for (int i = 1; i < nums.length - 1; i++)
			list.add(Double.valueOf(nums[i]));
		return Double.valueOf(nums[nums.length - 1]);
	}

	public void run(double factor, int count) {
		for (int i = 0; i < count; i++)
			oneRun(factor);
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
			res += Math.pow(reference - sum, 2);
			t--;
		}
		res /= count;
		System.out.println(Math.pow(res, 0.5));
	}

	private void oneRun(double factor) {
		for (int i = 0; i < numsList.size(); i++) {
			double sum = 0;
			List<Double> tempList = numsList.get(i);
			for (int j = 0; j < tempList.size(); j++) {
				sum += tempList.get(j) * coefficients[j];
			}
			sum -= referenceList.get(i);
			cache.add(factor*sum/numsList.size());
		}

		for (int i = 0; i < coefficients.length; i++) {
			double sum = 0;
			for (int j = 0; j < cache.size(); j++)
				sum += cache.get(j) * numsList.get(j).get(i);
			coefficients[i] -= sum;
		}
		cache.clear();
	}

	public static void main(String[] args) {
		Scanner sc = null;
		try {
			sc = new Scanner(new BufferedInputStream(new FileInputStream("./save_train.csv")));
			if (sc.hasNextLine())
				sc.nextLine();
			GradientDescent gd = new GradientDescent(sc, Integer.valueOf(args[0]));
			Scanner terminal = new Scanner(System.in);
			while (terminal.hasNextLine()) {
				int times = terminal.nextInt();
				if (times == -1)
					break;
				double factor = terminal.nextDouble();
				gd.run(factor, times);
			}
			terminal.close();
			gd.test(sc, Integer.valueOf(args[1]));
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (sc != null)
				sc.close();
		}
	}
}