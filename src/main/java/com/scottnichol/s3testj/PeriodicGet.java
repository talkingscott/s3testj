package com.scottnichol.s3testj;

import com.amazonaws.services.s3.AmazonS3;

/**
 *	Gets a particular object periodically, specifically every 30 seconds.  This provides a gross
 *	indication of the variation in time to get a single object.
 */
public class PeriodicGet {
	public static void main(String[] args) {
		final String bucket_name = (args.length > 0) ? args[0] : "com.scottnichol.s3test";
		final String key = (args.length > 1) ? args[1] : "test/30mb-5";
		final int period_ms = 30000;

		AmazonS3 s3 = S3Utils.createClient();

		while (true) {
			long start_time = System.currentTimeMillis();
			S3Utils.getObject(s3, bucket_name, key);
			long end_time = System.currentTimeMillis();
			try {
				Thread.sleep((end_time - start_time) < period_ms ? period_ms - (end_time - start_time) : 0);
			} catch (InterruptedException e) {
				// do nothing
			}
		}
	}
}
