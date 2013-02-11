package business;

/**
 * User: Timm Herrmann
 * Date: 11.02.13
 * Time: 19:35
 * <p/>
 * These type of exceptions should be thrown if somethings went wrong while accessing methods from
 * {@link Converter}.
 */
public class ConverterException extends Throwable {
  ConverterException(String message) {
    super(message);
  }
}
