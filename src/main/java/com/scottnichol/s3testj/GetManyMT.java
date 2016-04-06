package com.scottnichol.s3testj;

import java.io.BufferedInputStream;
import java.io.IOException;

import java.lang.Runnable;

import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;

/**
 *	Get many objects of various sizes using multiple threads to estimate total throughput.
 *	Note that this does not attempt to measure maximum throughput.  Using objects of various sizes
 *	more closely simulates a "natural" workload.
 */
public class GetManyMT {
	public static void main(String[] args) {
		final int nthreads = (args.length > 0) ? Integer.parseInt(args[0]) : 16;
		final String bucket_name = (args.length > 1) ? args[1] : "com.scottnichol.s3test";

		final ThreadLocal<AmazonS3> s3s = new ThreadLocal<AmazonS3>();
		final AtomicLong total_bytes = new AtomicLong();
		final ExecutorService pool = Executors.newFixedThreadPool(nthreads);

		for (int i = 0; i < nthreads; i++) {
			pool.submit(new Runnable() {
				@Override
				public void run() {
					AmazonS3 s3 = s3s.get();
					if (s3 == null) {
						s3 = S3Utils.createClient();
						s3s.set(s3);
						System.out.println("Pre-created client for thread " + Thread.currentThread().getName());
					}
				}
			});
		}

		final long started_at = System.currentTimeMillis();
		final String[] key_bases = new String[]{"30mb", "10mb", "6mb", "3mb", "1mb", "600kb", "300kb"};
		for (String key_base : key_bases) {
			for (int i = 0; i < 16; i++) {
				final String key = "test/" + key_base + "-" + i;
				pool.submit(new Runnable() {
					@Override
					public void run() {
						AmazonS3 s3 = s3s.get();
						if (s3 == null) {
							s3 = S3Utils.createClient();
							s3s.set(s3);
							System.out.println("Created client for thread " + Thread.currentThread().getName());
						}

						long bytes = S3Utils.getObject(s3, bucket_name, key);	
						total_bytes.addAndGet(bytes);
					}
				});
			}
		}
		
		pool.shutdown(); // Disable new tasks from being submitted
		try {
			// Wait a while for existing tasks to terminate
			if (!pool.awaitTermination(180, TimeUnit.SECONDS)) {
				pool.shutdownNow(); // Cancel currently executing tasks
				// Wait a while for tasks to respond to being cancelled
				if (!pool.awaitTermination(60, TimeUnit.SECONDS))
					System.err.println("Pool did not terminate");
			}
		} catch (InterruptedException ie) {
			// (Re-)Cancel if current thread also interrupted
			pool.shutdownNow();
			// Preserve interrupt status
			Thread.currentThread().interrupt();
		}

		final long ended_at = System.currentTimeMillis();
		System.out.println("Bytes: " + total_bytes.get() + " elapsed: " + (ended_at - started_at) + " rate (kB/s): " + (total_bytes.get() / (ended_at - started_at)));
	}
}
