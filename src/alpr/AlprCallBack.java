package alpr;

/**
 * Callback interface for the {@link AlprEngine}
 * 
 * @author Vitaliy Gavrilov
 *
 */
public interface AlprCallBack {

	/**
	 * Calls this method when done. The input will be the string
	 * containing the OCR engine result of the image processing
	 * @param result Result
	 */
	public void call(final String[] result);
	
}
