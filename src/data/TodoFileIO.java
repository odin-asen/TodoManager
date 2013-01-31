package data;

import com.thoughtworks.xstream.XStream;
import dto.DTOTask;
import dto.DTOTaskConverter;

import java.io.*;
import java.util.List;

/**
 * User: Timm Herrmann
 * Date: 23.09.12
 * Time: 00:18
 */
@SuppressWarnings("unchecked")
public class TodoFileIO {

  private static XStream getXStream() {
    final XStream xStream = new XStream();
    xStream.alias("todo-list", List.class);
    xStream.alias("task", DTOTask.class);
    xStream.registerConverter(new DTOTaskConverter());
    return xStream;
  }

  /**
   * Reads a list of tasks from a file.
   * @param file Source file to read from.
   * @return Returns a list of DTOTask objects.
   * @throws IOException {@link java.io.FileReader#FileReader(java.lang.String)} and
   * {@link java.io.BufferedReader#close()}
   */
  public static List<DTOTask> readTodoFile(File file)
      throws IOException {
    final XStream xStream = getXStream();

    final BufferedReader reader = new BufferedReader(new FileReader(file));
    final List<DTOTask> list = (List<DTOTask>) xStream.fromXML(reader);
    reader.close();

    return list;
  }

  /**
   * Writes a list of tasks to a file. A file will be overwritten if the path already exists.
   *
   * @param tasks Tasks to write to a file.
   * @param file Destination file.
   * @throws IOException {@link java.io.FileWriter#FileWriter(java.lang.String)} and
   * {@link java.io.BufferedWriter#close()}
   */
  public static void writeTodoFile(List<DTOTask> tasks, File file) throws IOException {
    final XStream xStream = getXStream();

    final BufferedWriter writer = new BufferedWriter(new FileWriter(file));
    xStream.toXML(tasks, writer);
    writer.close();
  }
}
