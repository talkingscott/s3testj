package com.scottnichol.s3testj;

import java.io.BufferedInputStream;
import java.io.IOException;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.TimeZone;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;

/**
 *	Utilities for S3 tests, not general purpose S3 access.
 */
public final class S3Utils {

	private S3Utils() { }

	public static AmazonS3 createClient() {
		ClientConfiguration config = new ClientConfiguration()
			.withMaxErrorRetry(1)
			.withConnectionTimeout(5000)
			.withThrottledRetries(true);
		AmazonS3 s3 = new AmazonS3Client(config);
		Region region = Region.getRegion(Regions.US_EAST_1);
		s3.setRegion(region);

		return s3;
	}

	public static long getObject(AmazonS3 s3, String bucket_name, String key) {
		info("Get " + key + " from " + bucket_name);
		long start_time = System.currentTimeMillis();
		S3Object object;
		try {
			object = s3.getObject(new GetObjectRequest(bucket_name, key));
			long metadata_time = System.currentTimeMillis();
			info("Metadata " + key + " Content-Type: "  + object.getObjectMetadata().getContentType() + " (" + (metadata_time - start_time) + "ms)");
		} catch (Exception e) {
			long metadata_time = System.currentTimeMillis();
			error("Error " + key + " (" + (metadata_time - start_time) + "ms): " + e);
			return 0;
		}

		try {
			try (BufferedInputStream bis = new BufferedInputStream(object.getObjectContent(), 262144)) {
				byte[] buf = new byte[16384];
				long bytes = 0;
				int nbytes = bis.read(buf, 0, buf.length);
				while (nbytes >= 0) {
					bytes += nbytes;
					nbytes = bis.read(buf, 0, buf.length);
				}
				long end_time = System.currentTimeMillis();
				info("Got " + key + " " + bytes + " bytes (" + (end_time - start_time) + "ms)");
				return bytes;
			}
		} catch (IOException e) {
			long end_time = System.currentTimeMillis();
			error("Error " + key + " (" + (end_time - start_time) + "ms): " + e);
			return 0;
		}
	}

	private static final ThreadLocal<DateFormat> dfs = new ThreadLocal<DateFormat>();

	private static String getTimestamp() {
		DateFormat df = dfs.get();
		if (df == null) {
			TimeZone tz = TimeZone.getTimeZone("UTC");
			df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
			df.setTimeZone(tz);
			dfs.set(df);
		}
		return df.format(new Date());
	}
	
	private static void error(String msg) {
		System.err.println(getTimestamp() + " " + Thread.currentThread().getName() + " " + msg);
	}
	
	private static void info(String msg) {
		System.out.println(getTimestamp() + " " + Thread.currentThread().getName() + " " + msg);
	}
}
