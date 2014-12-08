package OcrEngine;

/**
 * Callback interface for the {@link OcrEngine}
 * 
 * @author vitogavrilov
 *
 */
public interface OcrCallBack {

	/**
	 * Calls this method when done. The input will be the string
	 * containing the OCR engine result of the image processing
	 * @param result Result
	 */
	public void call(final String result);
	
}
