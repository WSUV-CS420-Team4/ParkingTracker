package OcrEngine;

import java.io.File;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.util.Log;

import com.googlecode.tesseract.android.TessBaseAPI;
import com.southwaterfront.parkingtracker.AssetManager.AssetManager;

/**
 * This is a secondary abstraction of the Tesseract OCR engine. This is done
 * because {@link TessBaseAPI} is the interface to the native library and we want to
 * configure it properly before releasing it for use. A singleton approach is chosen,
 * so only one instance of this class will exist so fine grained control over the engine
 * configuration will be enabled. This class is not thread safe.
 * 
 * @author Vitaliy Gavrilov
 *
 */
public class OcrEngine {

	private final String LOG_TAG = "OcrEngine";

	private final String englishLangID = "eng";

	private final String whiteListChars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz-";

	private static final OcrEngine instance = new OcrEngine();

	private final TessBaseAPI tess;

	private final AssetManager assetManager;

	private final BlockingQueue<ImageWrapper> imagesTasks;

	private final Thread worker;

	private static class ImageWrapper {
		public final Bitmap bitmap;
		public final Rect rect;
		public final OcrCallBack callBack;

		public ImageWrapper(Bitmap b, Rect r, OcrCallBack c) {
			this.bitmap = b;
			this.rect = r;
			this.callBack = c;
		}
	}

	private static class OcrWorker implements Runnable {
		private static final String LOG_TAG = "OcrWorker";
		private final TessBaseAPI tess;
		private final BlockingQueue<ImageWrapper> images;

		public OcrWorker(TessBaseAPI t, BlockingQueue<ImageWrapper> i) {
			if (i == null || t == null)
				throw new IllegalArgumentException("Arguments cannot be null");
			this.tess = t;
			this.images = i;
		}

		@Override
		public void run() {
			while(true) {
				ImageWrapper w = null;
				try {
					w = images.take();
				} catch (InterruptedException e) {
					break;
				} finally {
					if (w != null) {
						Bitmap bmp = w.bitmap;
						Rect rect = w.rect;
						OcrCallBack callBack = w.callBack;

						this.tess.setImage(bmp);
						if (rect != null)
							this.tess.setRectangle(rect);
						String result = this.tess.getUTF8Text();
						this.tess.clear();
						
						callBack.call(result);
					}
				}
			}

		}

	}

	private OcrEngine() {
		this.tess = new TessBaseAPI();
		this.assetManager = AssetManager.getInstance();
		String path = this.assetManager.getEnglishLanguageDataDir();
		try {
			tess.init(path, this.englishLangID);
		} catch (Exception e) {
			Log.e(LOG_TAG, e.getMessage());
			throw new RuntimeException(e);
		}
		configTess();

		this.imagesTasks = new LinkedBlockingQueue<ImageWrapper>();
		OcrWorker w = new OcrWorker(this.tess, this.imagesTasks);
		this.worker = new Thread(w);
		this.worker.setDaemon(true);
		this.worker.setName("OcrWorker");
		this.worker.start();
	}

	/**
	 * Getter for the instance of this class
	 * 
	 * @return Singleton instance of {@link OcrEngine}
	 */
	public static OcrEngine getInstance() {
		return OcrEngine.instance;
	}

	private void configTess() {
		this.tess.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST, whiteListChars);
	}

	/**
	 * Runs the {@link TessBaseAPI} engine to produce a String
	 * result from the image
	 * 
	 * @param image File containing the image to be processed
	 * @param rect Rectangle defining processing area in image
	 * @return Recognized text
	 */
	@Deprecated
	public String runOcr(File image, Rect rect) {
		if (image == null || !image.exists() || !image.canRead())
			throw new IllegalArgumentException("Image file must exist and be readable");

		this.tess.setImage(image);

		if (rect != null)
			this.tess.setRectangle(rect);

		String result = this.tess.getUTF8Text();

		this.tess.clear();

		return result;
	}

	/**
	 * ASYNC
	 * <br>
	 * Runs the {@link TessBaseAPI} engine to produce a String
	 * result from the image given in {@link Bitmap} form. The user is
	 * responsible for passing a valid Bitmap. The only supported encoding
	 * is {@link Bitmap.Config#ARGB_8888}. Attempting to use a different encoding
	 * will result in a {@link RuntimeException}.
	 * <br>
	 * To get the result, provide an {@link OcrCallBack}. This will be called
	 * when the engine is done working and provide the result.
	 * 
	 * @param bmp Bitmap to run the OCR on
	 * @param rect Area to constrain to, may be null
	 * @param resultCallBack An OcrCallBack to call when done
	 */
	public void runOcr(Bitmap bmp, Rect rect, OcrCallBack resultCallBack) {
		if (bmp == null)
			throw new IllegalArgumentException("Bitmap cannot be null");
		if (resultCallBack == null)
			return;

		ImageWrapper i = new ImageWrapper(bmp, rect, resultCallBack);
		this.imagesTasks.add(i);
	}

}
