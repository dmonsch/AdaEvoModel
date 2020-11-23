package adaevomodel.tools.experimental.pcm.test;

import smile.data.DataFrame;
import smile.data.formula.Formula;
import smile.regression.LASSO;
import smile.regression.LinearModel;

public class SmileRegressionTest {

	public static void main(String[] args) {
		double[][] data = new double[1000][5];
		for (int i = 0; i < data.length; i++) {
			data[i][0] = i;
			data[i][1] = data[i][0] * data[i][0];
			data[i][2] = data[i][0] * data[i][0] * data[i][0];
			data[i][3] = data[i][0] * data[i][0] * data[i][0] * data[i][0];
			data[i][4] = data[i][0] * data[i][0] * data[i][0] + data[i][0] * data[i][0];
		}

		DataFrame df = DataFrame.of(data, "x", "x^2", "x^3", "x^4", "y");
		Formula formula = Formula.lhs("y");
		LinearModel model = LASSO.fit(formula, df);
		System.out.println(model);

		System.out.println(model.predict(new double[] { 6, 36, 36 * 6, 36 * 6 * 6 }));
	}

}
