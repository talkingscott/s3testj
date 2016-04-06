package com.scottnichol.s3testj;

import java.util.ArrayList;
import java.util.List;

import com.amazonaws.services.s3.AmazonS3;

/**
 *	Get many objects of various sizes and do simple linear regression on the results to estimate
 *	latency and transfer rate.
 */
public class GetMany {
	public static void main(String[] args) {
		final String bucket_name = (args.length > 0) ? args[0] : "com.scottnichol.s3test";

		final List<SLR.Sample> samples = new ArrayList<SLR.Sample>();
		final AmazonS3 s3 = S3Utils.createClient();

		final String[] key_bases = new String[]{"30mb", "10mb", "6mb", "3mb", "1mb", "600kb", "300kb"};
		for (String key_base : key_bases) {
			for (int i = 0; i < 16; i++) {
				final String key = "test/" + key_base + "-" + i;
				long start_time = System.currentTimeMillis();
				long bytes = S3Utils.getObject(s3, bucket_name, key);
				long end_time = System.currentTimeMillis();
				samples.add(new SLR.Sample(bytes, (end_time - start_time) / 1000.0));
			}
		}

		SLR.Line fit = SLR.slr(samples);
		System.out.println("latency (secs): " + fit.getIntercept() + " transfer rate (bytes/sec): " + (1 / fit.getSlope()));
	}
}
