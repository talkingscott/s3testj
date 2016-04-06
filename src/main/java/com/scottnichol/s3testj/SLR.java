package com.scottnichol.s3testj;

import java.util.List;

/**
 *  Simple linear regression.
 */
public final class SLR {
	public static class Sample {
		private double x;
		private double y;
		public Sample(double x, double y) {
			this.x = x;
			this.y = y;
		}
		public double getX() {
			return x;
		}
		public double getY() {
			return y;
		}
	}

	public static class Line {
		private double slope;
		private double intercept;
		public Line(double slope, double intercept) {
			this.slope = slope;
			this.intercept = intercept;
		}
		public double getIntercept() {
			return intercept;
		}
		public double getSlope() {
			return slope;
		}
	}

	private SLR() { }

	public static Line slr(List<Sample> samples) {
		double sum_xy = 0;
		double sum_x = 0;
		double sum_y = 0;
		double sum_x2 = 0;
	
		for (Sample s : samples) {
			sum_xy += s.getX() * s.getY();
			sum_x += s.getX();
			sum_y += s.getY();
			sum_x2 += s.getX() * s.getX();
		}
	  
		double slope = (sum_xy - ((sum_x * sum_y) / samples.size())) / (sum_x2 - ((sum_x * sum_x) / samples.size()));
		double intercept = (sum_y / samples.size()) - (slope * (sum_x / samples.size()));

		return new Line(slope, intercept);
	}

}