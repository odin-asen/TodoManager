package i18n;

import java.text.MessageFormat;
import java.util.ResourceBundle;

/**
 * User: Timm Herrmann
 * Date: 20.11.12
 * Time: 20:35
 */
public class I18nSupport {
  private static final String I18N_POINT = "i18n."; //NON-NLS

  public static String getValue(String bundleName, String key, Object... params) {
    try {
      final String value = getBundle(bundleName).getString(key);
      if(params.length > 0) return MessageFormat.format(value,  params);
      return value;
    } catch (Exception ex) {
      return "!" +bundleName+"/"+key+"!";
    }
  }

  private static ResourceBundle getBundle(String bundleName) {
    return ResourceBundle.getBundle(I18N_POINT+bundleName);
  }
}
