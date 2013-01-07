package nz.org.winters.appspot.acrareporter.server;

import proguard.obfuscate.MappingProcessor;

public class MappingStringReader
{
  String mapping;

  public MappingStringReader(String mapping)
  {
    this.mapping = mapping;
  }

  public void pump(MappingProcessor mappingProcessor)
  {
    String[] lines = mapping.split("\n");
    String className = null;

    // Read the subsequent class mappings and class member mappings.
    int linecount = 0;
    while (linecount < lines.length)
    {
      String line = lines[linecount++];

      if (line == null)
      {
        break;
      }

      line = line.trim();

      // The distinction between a class mapping and a class
      // member mapping is the initial whitespace.
      if (line.endsWith(":"))
      {
        // Process the class mapping and remember the class's
        // old name.
        className = processClassMapping(line, mappingProcessor);
      } else if (className != null)
      {
        // Process the class member mapping, in the context of the
        // current old class name.
        processClassMemberMapping(className, line, mappingProcessor);
      }
    }
  }

  /**
   * Parses the given line with a class mapping and processes the results with
   * the given mapping processor. Returns the old class name, or null if any
   * subsequent class member lines can be ignored.
   */
  private String processClassMapping(String line, MappingProcessor mappingProcessor)
  {
    // See if we can parse "___ -> ___:", containing the original
    // class name and the new class name.

    int arrowIndex = line.indexOf("->");
    if (arrowIndex < 0)
    {
      return null;
    }

    int colonIndex = line.indexOf(':', arrowIndex + 2);
    if (colonIndex < 0)
    {
      return null;
    }

    // Extract the elements.
    String className = line.substring(0, arrowIndex).trim();
    String newClassName = line.substring(arrowIndex + 2, colonIndex).trim();

    // Process this class name mapping.
    boolean interested = mappingProcessor.processClassMapping(className, newClassName);

    return interested ? className : null;
  }

  /**
   * Parses the given line with a class member mapping and processes the results
   * with the given mapping processor.
   */
  private void processClassMemberMapping(String className, String line, MappingProcessor mappingProcessor)
  {
    // See if we can parse "___:___:___ ___(___) -> ___",
    // containing the optional line numbers, the return type, the original
    // field/method name, optional arguments, and the new field/method name.

    int colonIndex1 = line.indexOf(':');
    int colonIndex2 = colonIndex1 < 0 ? -1 : line.indexOf(':', colonIndex1 + 1);
    int spaceIndex = line.indexOf(' ', colonIndex2 + 2);
    int argumentIndex1 = line.indexOf('(', spaceIndex + 1);
    int argumentIndex2 = argumentIndex1 < 0 ? -1 : line.indexOf(')', argumentIndex1 + 1);
    int arrowIndex = line.indexOf("->", Math.max(spaceIndex, argumentIndex2) + 1);

    if (spaceIndex < 0 || arrowIndex < 0)
    {
      return;
    }

    // Extract the elements.
    String type = line.substring(colonIndex2 + 1, spaceIndex).trim();
    String name = line.substring(spaceIndex + 1, argumentIndex1 >= 0 ? argumentIndex1 : arrowIndex).trim();
    String newName = line.substring(arrowIndex + 2).trim();

    // Process this class member mapping.
    if (type.length() > 0 && name.length() > 0 && newName.length() > 0)
    {
      // Is it a field or a method?
      if (argumentIndex2 < 0)
      {
        mappingProcessor.processFieldMapping(className, type, name, newName);
      } else
      {
        int firstLineNumber = 0;
        int lastLineNumber = 0;

        if (colonIndex2 > 0)
        {
          firstLineNumber = Integer.parseInt(line.substring(0, colonIndex1).trim());
          lastLineNumber = Integer.parseInt(line.substring(colonIndex1 + 1, colonIndex2).trim());
        }

        String arguments = line.substring(argumentIndex1 + 1, argumentIndex2).trim();

        mappingProcessor.processMethodMapping(className, firstLineNumber, lastLineNumber, type, name, arguments, newName);
      }
    }
  }

}
