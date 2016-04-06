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
 */
public class App 
{
	public static void main( String[] args )
	{
		Thread[] threads = new Thread[16];
		
		for (int i = 0; i < threads.length; i++) {
			final int j = i;
			threads[i] = new Thread(new Runnable() {
				@Override
				public void run() {
					AmazonS3 s3 = new AmazonS3Client();
					Region region = Region.getRegion(Regions.US_EAST_1);
					s3.setRegion(region);
			
					String bucketName = "com.scottnichol.s3test";
					String key = "test/30mb-" + j;

					System.out.println("Start " + key);
					long start_time = System.currentTimeMillis();
					S3Object object = s3.getObject(new GetObjectRequest(bucketName, key));
					long metadata_time = System.currentTimeMillis();
					System.out.println("Headers " + key + " Content-Type: "  + object.getObjectMetadata().getContentType() + " (" + (metadata_time - start_time) + "ms)");
					try {
						try (BufferedInputStream bis = new BufferedInputStream(object.getObjectContent())) {
							byte[] buf = new byte[16384];
							long bytes = 0;
							int nbytes = bis.read(buf, 0, buf.length);
							while (nbytes >= 0) {
								bytes += nbytes;
								nbytes = bis.read(buf, 0, buf.length);
							}
							long end_time = System.currentTimeMillis();
							System.out.println("Done " + key + " (" + (end_time - start_time) + "ms)");
						}
					} catch (IOException e) {
						long end_time = System.currentTimeMillis();
						System.err.println("Error " + key + " (" + (end_time - start_time) + "ms): " + e);
					}
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
