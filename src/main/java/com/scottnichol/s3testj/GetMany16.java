package com.scottnichol.s3testj;

import java.io.BufferedInputStream;
import java.io.IOException;

import java.lang.Runnable;
import java.lang.Thread;

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
 *	Get many files, one on each of 16 threads.  This was an early experiment, but
 *	it does not provide any particularly useful measurement.
 */
public class GetMany16 {
	public static void main(String[] args) {
		final String bucket_name = (args.length > 0) ? args[0] : "com.scottnichol.s3test";

		final Thread[] threads = new Thread[16];
		
		for (int i = 0; i < threads.length; i++) {
			final int j = i;
			threads[i] = new Thread(new Runnable() {
				@Override
				public void run() {
					AmazonS3 s3 = S3Utils.createClient();
					String key = "test/30mb-" + j;
					S3Utils.getObject(s3, bucket_name, key);
				}
			}, "thread-" + j);
		}
		
		for (Thread thread : threads) {
			thread.start();
		}
		
		for (Thread thread: threads) {
			try {
				thread.join();
				System.out.println("Joined " + thread.getName());
			} catch (InterruptedException e) {
				System.err.println("Joining " + thread.getName() + ": " + e);
			}
		}
	}
}
