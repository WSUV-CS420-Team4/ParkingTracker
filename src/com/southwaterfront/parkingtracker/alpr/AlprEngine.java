package com.southwaterfront.parkingtracker.alpr;

import java.io.Closeable;
import java.io.File;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.openalpr.Alpr;

import android.util.Log;

import com.southwaterfront.parkingtracker.AssetManager.AssetManager;
import com.southwaterfront.parkingtracker.jsonify.AlprParser;

/**
 * This is a secondary abstraction of the Alpr engine. Done as a sort of adapter
 * class to fit the way we generate data with the way OpenAlpr processes it.
 * 
 * @author Vitaliy Gavrilov
 *
 */
public class AlprEngine implements Closeable {

	private final String LOG_TAG = "AlprEngine";

	private static final AlprEngine instance = new AlprEngine();

	private static final String ERROR_CLOSED = "The OcrEngine is closed and cannot be used";

	private static final int DEFAULT_RESULT_COUNT = 5;

	private boolean closed;

	private final Alpr alpr;

	private final AssetManager assetManager;

	private final BlockingQueue<TaskWrapper> imagesTasks;

	private final Thread worker;
	
	private final String configFilePath;
	
	private volatile int numResults;

	private static class TaskWrapper {
		public final String imagePath;
		public final AlprCallBack callBack;

		public TaskWrapper(String i, AlprCallBack c) {
			this.imagePath = i;
			this.callBack = c;
		}
	}

	private class AlprWorker implements Runnable {
		private static final String LOG_TAG = "AlprWorker";
		private final Alpr alpr;
		private final BlockingQueue<TaskWrapper> images;

		public AlprWorker(Alpr alpr, BlockingQueue<TaskWrapper> i) {
			if (i == null || alpr == null)
				throw new IllegalArgumentException("Arguments cannot be null");
			this.alpr = alpr;
			this.images = i;
		}

		@Override
		public void run() {
			while(true) {
				TaskWrapper w = null;
				try {
					w = images.take();
				} catch (InterruptedException e) {
					break;
				} finally {
					if (w != null) {
						String path = w.imagePath;
						AlprCallBack callBack = w.callBack;
						String alprResult = this.alpr.recognizeWithCountryRegionNConfig("us", "", path, configFilePath, numResults);
						String[] result = AlprParser.parseAlprResult(alprResult);
						if (callBack != null)
							callBack.call(result);
					}
				}
			}
		}
	}

	private AlprEngine() {
		this.closed = false;
		this.alpr = Alpr.Factory.create();
		this.assetManager = AssetManager.getInstance();


		this.imagesTasks = new LinkedBlockingQueue<TaskWrapper>();
		AlprWorker w = new AlprWorker(this.alpr, this.imagesTasks);
		this.worker = new Thread(w);
		//this.worker.setDaemon(true);
		this.worker.setName("OcrWorker");
		this.worker.start();
		
		this.configFilePath = this.assetManager.getAlprConfigFile().getAbsolutePath();
		
		this.numResults = DEFAULT_RESULT_COUNT;
	}

	/**
	 * Getter for the instance of this class
	 * 
	 * @return Singleton instance of {@link AlprEngine}
	 */
	public static AlprEngine getInstance() {
		return AlprEngine.instance;
	}

	/**
	 * Getter to check how many results the ALPR engine
	 * is generating per picture
	 * @return Num of results
	 */
	public int getNumberOfResults() {
		return this.numResults;
	}
	
	/**
	 * Set the number of results for the ALPR engine to produce per picture
	 * 
	 * @param results Number of results to change to 1 - MAX
	 */
	public void setNumberOfResults(int results) {
		if (results > 0)
			this.numResults = results;
	}
	
	/*
	private void configTess() {
		this.tess.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST, whiteListChars);
	}*/

	/**
	 * Runs the {@link TessBaseAPI} engine to produce a String
	 * result from the image
	 * 
	 * @param image File containing the image to be processed
	 * @param rect Rectangle defining processing area in image
	 */
	public void runOcr(File image, AlprCallBack resultCallBack) {
		checkNotClosed();
		if (image == null || !image.exists() || !image.canRead())
			throw new IllegalArgumentException("Image file must exist and be readable");
		if (resultCallBack == null)
			return;

		TaskWrapper w = new TaskWrapper(image.getAbsolutePath(), resultCallBack);
		this.imagesTasks.add(w);
	}

	private void checkNotClosed() {
		if (this.closed)
			throw new IllegalStateException(ERROR_CLOSED);
	}

	/**
	 * Only call in onDestroy
	 */
	@Override
	public void close() {
		if (!this.closed) {
			Log.i(LOG_TAG, "Shutting down Alpr");
			this.worker.interrupt();
			this.closed = true;
		}
	}

}
