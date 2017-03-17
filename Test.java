import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.util.Scanner;
import java.util.List;
import java.util.LinkedList;
import java.util.ArrayList;
import java.io.FileWriter;

public class Test {
	private static void getNums(String s, List<Double> list) {
		String[] nums = s.split(",");
		list.add((double)1);
		for (int i = 1; i < nums.length; i++)
			list.add(Double.valueOf(nums[i]));
	}

	public static void test(Scanner sc, List<Double> list) {
		try(FileWriter out = new FileWriter("./test_res.txt")) {
			out.write("id,reference\n");
			int id = 0;
			while (sc.hasNextLine()) {
				String s = sc.nextLine();
				List<Double> tempList = new ArrayList<Double>();
				getNums(s, tempList);

				double sum = 0;
				int i = 0;
				for (Double d : list) {
					sum += d * tempList.get(i);
					i++;
				}
				out.write((id++) + "," + sum + "\n");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	public static void main(String[] args) {
		List<Double> list = new LinkedList<>();
		try(Scanner sc = new Scanner(new BufferedInputStream(new FileInputStream("./coefficients.txt")))) {
			while (sc.hasNextDouble())
				list.add(sc.nextDouble());
		} catch (Exception e) {
			
		}

		try(Scanner sc = new Scanner(new BufferedInputStream(new FileInputStream("./save_test.csv")))) {
			if (sc.hasNextLine())
				sc.nextLine();
			test(sc, list);
		} catch (Exception e) {
			
		}
		
	}
}