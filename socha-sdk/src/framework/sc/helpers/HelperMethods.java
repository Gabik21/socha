package sc.helpers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sc.shared.SlotDescriptor;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class HelperMethods {

  private static final Logger logger = LoggerFactory.getLogger(HelperMethods.class);

  private static final DateFormat dateTimeFormat = new SimpleDateFormat(
          "yyyy.MM.dd HH_mm_ss");

  /**
   * Returns the current date and time as string formatted as yyyy.MM.dd
   * HH_mm_ss.
   *
   * @return current date and time
   */
  public static String getCurrentDateTime() {
    return dateTimeFormat.format(new Date());
  }

  /**
   * Returns a new generated filename for a replay file.
   *
   * @param pluginUuid UUID of the plugin
   * @param descriptors descriptor of player slot
   * @return name of replay
   */
  public static String generateReplayFilename(String pluginUuid, List<SlotDescriptor> descriptors) {
    StringBuilder replayFileName = new StringBuilder("./replays/replay");
    replayFileName.append("_");
    replayFileName.append(pluginUuid); // something like hui_2018
    for (SlotDescriptor descriptor : descriptors) {
      replayFileName.append("_");
      replayFileName.append(descriptor.getDisplayName().replace(' ', '_'));
    }
    replayFileName.append("_");
    replayFileName.append(HelperMethods.getCurrentDateTime().replace(' ', '_'));
    replayFileName.append(".xml");
    logger.debug("Generated file name {}", replayFileName.toString());
    return replayFileName.toString();
  }

}
