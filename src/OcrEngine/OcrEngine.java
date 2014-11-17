package OcrEngine;

import java.io.File;

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
	 * Runs the {@link TessBaseAPI} engine to produce a String
	 * result from the image given in {@link Bitmap} form. The user is
	 * responsible for passing a valid Bitmap. The only supported encoding
	 * is {@link Bitmap.Config#ARGB_8888}. Attempting to use a different encoding
	 * will result in a {@link RuntimeException}.
	 * 
	 * @param bmp Bitmap of image
	 * @param rect Rectangle defining processing area in image
	 * @return Recognized text
	 */
	public String runOcr(Bitmap bmp, Rect rect) {
		if (bmp == null)
			throw new IllegalArgumentException("Bitmap cannot be null");
		
		this.tess.setImage(bmp);
		
		if (rect != null)
			this.tess.setRectangle(rect);
		
		String result = this.tess.getUTF8Text();
		
		this.tess.clear();
		
		return result;
	}
	
}
