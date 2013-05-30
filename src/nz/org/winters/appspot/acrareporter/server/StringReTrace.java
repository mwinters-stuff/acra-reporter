/*
 * ProGuard -- shrinking, optimization, obfuscation, and preverification
 *             of Java bytecode.
 *
 * Copyright (c) 2002-2012 Eric Lafortune (eric@graphics.cornell.edu)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package nz.org.winters.appspot.acrareporter.server;
/*
 * Copyright 2013 Mathew Winters

 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import proguard.classfile.util.ClassUtil;
import proguard.obfuscate.MappingProcessor;

/**
 * Tool for de-obfuscating stack traces of applications that were obfuscated
 * with ProGuard.
 * 
 * @author Eric Lafortune
 */
public class StringReTrace implements MappingProcessor
{
  //private static final String REGEX_OPTION           = "-regex";
 // private static final String VERBOSE_OPTION         = "-verbose";

  public static final String  STACK_TRACE_EXPRESSION = "(?:.*?\\bat\\s+%c.%m\\s*\\(.*?(?::%l)?\\)\\s*)|(?:(?:.*?[:\"]\\s+)?%c(?::.*)?)";

  private static final String REGEX_CLASS            = "\\b(?:[A-Za-z0-9_$]+\\.)*[A-Za-z0-9_$]+\\b";
  private static final String REGEX_CLASS_SLASH      = "\\b(?:[A-Za-z0-9_$]+/)*[A-Za-z0-9_$]+\\b";
  private static final String REGEX_LINE_NUMBER      = "\\b[0-9]+\\b";
  private static final String REGEX_TYPE             = REGEX_CLASS + "(?:\\[\\])*";
  private static final String REGEX_MEMBER           = "<?\\b[A-Za-z0-9_$]+\\b>?";
  private static final String REGEX_ARGUMENTS        = "(?:" + REGEX_TYPE + "(?:\\s*,\\s*" + REGEX_TYPE + ")*)?";

  // The class settings.
  private final String        regularExpression;
  private final boolean       verbose;
  private final String        mappingFile;
  private final String        stackTraceFile;
  private String        result;

  private Map<String,String>                 classMap               = new HashMap<String,String>();
  private Map<String,Map<String,Set<FieldInfo>>>                 classFieldMap          = new HashMap<String,Map<String,Set<FieldInfo>>>();
  private Map<String,Map<String,Set<MethodInfo>>>                 classMethodMap         = new HashMap<String,Map<String,Set<MethodInfo>>>();

  /**
   * Creates a new ReTrace object to process stack traces on the standard input,
   * based on the given mapping file name.
   * 
   * @param regularExpression
   *          the regular expression for parsing the lines in the stack trace.
   * @param verbose
   *          specifies whether the de-obfuscated stack trace should be verbose.
   * @param mappingFile
   *          the mapping file that was written out by ProGuard.
   */
  public StringReTrace(String regularExpression, boolean verbose, String mappingFile)
  {
    this(regularExpression, verbose, mappingFile, null);
  }

  /**
   * Creates a new ReTrace object to process a stack trace from the given file,
   * based on the given mapping file name.
   * 
   * @param regularExpression
   *          the regular expression for parsing the lines in the stack trace.
   * @param verbose
   *          specifies whether the de-obfuscated stack trace should be verbose.
   * @param mappingFile
   *          the mapping file that was written out by ProGuard.
   * @param stackTraceFile
   *          the optional name of the file that contains the stack trace.
   */
  public StringReTrace(String regularExpression, boolean verbose, String mappingFile, String stackTraceFile)
  {
    this.regularExpression = regularExpression;
    this.verbose = verbose;
    this.mappingFile = mappingFile;
    this.stackTraceFile = stackTraceFile;
    this.result = "";
  }

  /**
   * Performs the subsequent ReTrace operations.
   */
  public void execute()
  {
    // Read the mapping file.
    MappingStringReader mappingReader = new MappingStringReader(mappingFile);
    mappingReader.pump(this);

    StringBuffer expressionBuffer = new StringBuffer(regularExpression.length() + 32);
    char[] expressionTypes = new char[32];
    int expressionTypeCount = 0;
    int index = 0;
    while (true)
    {
      int nextIndex = regularExpression.indexOf('%', index);
      if (nextIndex < 0 || nextIndex == regularExpression.length() - 1 || expressionTypeCount == expressionTypes.length)
      {
        break;
      }

      expressionBuffer.append(regularExpression.substring(index, nextIndex));
      expressionBuffer.append('(');

      char expressionType = regularExpression.charAt(nextIndex + 1);
      switch (expressionType)
      {
        case 'c':
          expressionBuffer.append(REGEX_CLASS);
          break;

        case 'C':
          expressionBuffer.append(REGEX_CLASS_SLASH);
          break;

        case 'l':
          expressionBuffer.append(REGEX_LINE_NUMBER);
          break;

        case 't':
          expressionBuffer.append(REGEX_TYPE);
          break;

        case 'f':
          expressionBuffer.append(REGEX_MEMBER);
          break;

        case 'm':
          expressionBuffer.append(REGEX_MEMBER);
          break;

        case 'a':
          expressionBuffer.append(REGEX_ARGUMENTS);
          break;
      }

      expressionBuffer.append(')');

      expressionTypes[expressionTypeCount++] = expressionType;

      index = nextIndex + 2;
    }

    expressionBuffer.append(regularExpression.substring(index));

    Pattern pattern = Pattern.compile(expressionBuffer.toString());

    // Read the stack trace file.
    String reader[] = stackTraceFile.split("\n");
    // LineNumberReader reader =
    // new LineNumberReader(stackTraceFile == null ?
    // (Reader)new InputStreamReader(System.in) :
    // (Reader)new BufferedReader(new FileReader(stackTraceFile)));

    StringBuffer outLine = new StringBuffer(256);
    List<StringBuffer> extraOutLines = new ArrayList<StringBuffer>();

    String className = null;

    // Read the line in the stack trace.
    int lineCount = 0;
    while (lineCount < reader.length)
    {
      String line = reader[lineCount++];
      if (line == null)
      {
        break;
      }

      Matcher matcher = pattern.matcher(line);

      if (matcher.matches())
      {
        int lineNumber = 0;
        String type = null;
        String arguments = null;

        // Figure out a class name, line number, type, and
        // arguments beforehand.
        for (int expressionTypeIndex = 0; expressionTypeIndex < expressionTypeCount; expressionTypeIndex++)
        {
          int startIndex = matcher.start(expressionTypeIndex + 1);
          if (startIndex >= 0)
          {
            String match = matcher.group(expressionTypeIndex + 1);

            char expressionType = expressionTypes[expressionTypeIndex];
            switch (expressionType)
            {
              case 'c':
                className = originalClassName(match);
                break;

              case 'C':
                className = originalClassName(ClassUtil.externalClassName(match));
                break;

              case 'l':
                lineNumber = Integer.parseInt(match);
                break;

              case 't':
                type = originalType(match);
                break;

              case 'a':
                arguments = originalArguments(match);
                break;
            }
          }
        }

        // Actually construct the output line.
        int lineIndex = 0;

        outLine.setLength(0);
        extraOutLines.clear();

        for (int expressionTypeIndex = 0; expressionTypeIndex < expressionTypeCount; expressionTypeIndex++)
        {
          int startIndex = matcher.start(expressionTypeIndex + 1);
          if (startIndex >= 0)
          {
            int endIndex = matcher.end(expressionTypeIndex + 1);
            String match = matcher.group(expressionTypeIndex + 1);

            // Copy a literal piece of input line.
            outLine.append(line.substring(lineIndex, startIndex));

            char expressionType = expressionTypes[expressionTypeIndex];
            switch (expressionType)
            {
              case 'c':
                className = originalClassName(match);
                outLine.append(className);
                break;

              case 'C':
                className = originalClassName(ClassUtil.externalClassName(match));
                outLine.append(ClassUtil.internalClassName(className));
                break;

              case 'l':
                lineNumber = Integer.parseInt(match);
                outLine.append(match);
                break;

              case 't':
                type = originalType(match);
                outLine.append(type);
                break;

              case 'f':
                originalFieldName(className, match, type, outLine, extraOutLines);
                break;

              case 'm':
                originalMethodName(className, match, lineNumber, type, arguments, outLine, extraOutLines);
                break;

              case 'a':
                arguments = originalArguments(match);
                outLine.append(arguments);
                break;
            }

            // Skip the original element whose processed version
            // has just been appended.
            lineIndex = endIndex;
          }
        }

        // Copy the last literal piece of input line.
        outLine.append(line.substring(lineIndex));

        // Print out the main line.
        result = result + outLine.toString() + "\n";
     //   System.out.println(outLine);

        // Print out any additional lines.
        for (int extraLineIndex = 0; extraLineIndex < extraOutLines.size(); extraLineIndex++)
        {
       //   System.out.println(extraOutLines.get(extraLineIndex));
          result = result + extraOutLines.get(extraLineIndex) + "\n";
        }
      } else
      {
        // Print out the original line.
      //  System.out.println(line);
        result = result + line + "\n";
      }
    }
  }

  /**
   * Finds the original field name(s), appending the first one to the out line,
   * and any additional alternatives to the extra lines.
   */
  private void originalFieldName(String className, String obfuscatedFieldName, String type, StringBuffer outLine, List<StringBuffer> extraOutLines)
  {
    int extraIndent = -1;

    // Class name -> obfuscated field names.
    Map<String,Set<FieldInfo>> fieldMap = classFieldMap.get(className);
    if (fieldMap != null)
    {
      // Obfuscated field names -> fields.
      Set<FieldInfo> fieldSet = fieldMap.get(obfuscatedFieldName);
      if (fieldSet != null)
      {
        // Find all matching fields.
        Iterator<FieldInfo> fieldInfoIterator = fieldSet.iterator();
        while (fieldInfoIterator.hasNext())
        {
          FieldInfo fieldInfo = (FieldInfo) fieldInfoIterator.next();
          if (fieldInfo.matches(type))
          {
            // Is this the first matching field?
            if (extraIndent < 0)
            {
              extraIndent = outLine.length();

              // Append the first original name.
              if (verbose)
              {
                outLine.append(fieldInfo.type).append(' ');
              }
              outLine.append(fieldInfo.originalName);
            } else
            {
              // Create an additional line with the proper
              // indentation.
              StringBuffer extraBuffer = new StringBuffer();
              for (int counter = 0; counter < extraIndent; counter++)
              {
                extraBuffer.append(' ');
              }

              // Append the alternative name.
              if (verbose)
              {
                extraBuffer.append(fieldInfo.type).append(' ');
              }
              extraBuffer.append(fieldInfo.originalName);

              // Store the additional line.
              extraOutLines.add(extraBuffer);
            }
          }
        }
      }
    }

    // Just append the obfuscated name if we haven't found any matching
    // fields.
    if (extraIndent < 0)
    {
      outLine.append(obfuscatedFieldName);
    }
  }

  /**
   * Finds the original method name(s), appending the first one to the out line,
   * and any additional alternatives to the extra lines.
   */
  private void originalMethodName(String className, String obfuscatedMethodName, int lineNumber, String type, String arguments, StringBuffer outLine, List<StringBuffer> extraOutLines)
  {
    int extraIndent = -1;

    // Class name -> obfuscated method names.
    Map<String,Set<MethodInfo>> methodMap =  classMethodMap.get(className);
    if (methodMap != null)
    {
      // Obfuscated method names -> methods.
      Set<MethodInfo> methodSet = methodMap.get(obfuscatedMethodName);
      if (methodSet != null)
      {
        // Find all matching methods.
        Iterator<MethodInfo> methodInfoIterator = methodSet.iterator();
        while (methodInfoIterator.hasNext())
        {
          MethodInfo methodInfo = methodInfoIterator.next();
          if (methodInfo.matches(lineNumber, type, arguments))
          {
            // Is this the first matching method?
            if (extraIndent < 0)
            {
              extraIndent = outLine.length();

              // Append the first original name.
              if (verbose)
              {
                outLine.append(methodInfo.type).append(' ');
              }
              outLine.append(methodInfo.originalName);
              if (verbose)
              {
                outLine.append('(').append(methodInfo.arguments).append(')');
              }
            } else
            {
              // Create an additional line with the proper
              // indentation.
              StringBuffer extraBuffer = new StringBuffer();
              for (int counter = 0; counter < extraIndent; counter++)
              {
                extraBuffer.append(' ');
              }

              // Append the alternative name.
              if (verbose)
              {
                extraBuffer.append(methodInfo.type).append(' ');
              }
              extraBuffer.append(methodInfo.originalName);
              if (verbose)
              {
                extraBuffer.append('(').append(methodInfo.arguments).append(')');
              }

              // Store the additional line.
              extraOutLines.add(extraBuffer);
            }
          }
        }
      }
    }

    // Just append the obfuscated name if we haven't found any matching
    // methods.
    if (extraIndent < 0)
    {
      outLine.append(obfuscatedMethodName);
    }
  }

  /**
   * Returns the original argument types.
   */
  private String originalArguments(String obfuscatedArguments)
  {
    StringBuffer originalArguments = new StringBuffer();

    int startIndex = 0;
    while (true)
    {
      int endIndex = obfuscatedArguments.indexOf(',', startIndex);
      if (endIndex < 0)
      {
        break;
      }

      originalArguments.append(originalType(obfuscatedArguments.substring(startIndex, endIndex).trim())).append(',');

      startIndex = endIndex + 1;
    }

    originalArguments.append(originalType(obfuscatedArguments.substring(startIndex).trim()));

    return originalArguments.toString();
  }

  /**
   * Returns the original type.
   */
  private String originalType(String obfuscatedType)
  {
    int index = obfuscatedType.indexOf('[');

    return index >= 0 ? originalClassName(obfuscatedType.substring(0, index)) + obfuscatedType.substring(index) : originalClassName(obfuscatedType);
  }

  /**
   * Returns the original class name.
   */
  private String originalClassName(String obfuscatedClassName)
  {
    String originalClassName = (String) classMap.get(obfuscatedClassName);

    return originalClassName != null ? originalClassName : obfuscatedClassName;
  }

  // Implementations for MappingProcessor.

  public boolean processClassMapping(String className, String newClassName)
  {
    // Obfuscated class name -> original class name.
    classMap.put(newClassName, className);

    return true;
  }

  public void processFieldMapping(String className, String fieldType, String fieldName, String newFieldName)
  {
    // Original class name -> obfuscated field names.
    Map<String,Set<FieldInfo>> fieldMap = classFieldMap.get(className);
    if (fieldMap == null)
    {
      fieldMap = new HashMap<String,Set<FieldInfo>>();
      classFieldMap.put(className, fieldMap);
    }

    // Obfuscated field name -> fields.
    Set<FieldInfo> fieldSet = fieldMap.get(newFieldName);
    if (fieldSet == null)
    {
      fieldSet = new LinkedHashSet<FieldInfo>();
      fieldMap.put(newFieldName, fieldSet);
    }

    // Add the field information.
    fieldSet.add(new FieldInfo(fieldType, fieldName));
  }

  public void processMethodMapping(String className, int firstLineNumber, int lastLineNumber, String methodReturnType, String methodName, String methodArguments, String newMethodName)
  {
    // Original class name -> obfuscated method names.
    Map<String,Set<MethodInfo>> methodMap = (Map<String,Set<MethodInfo>>) classMethodMap.get(className);
    if (methodMap == null)
    {
      methodMap = new HashMap<String,Set<MethodInfo>>();
      classMethodMap.put(className, methodMap);
    }

    // Obfuscated method name -> methods.
    Set<MethodInfo> methodSet = (Set<MethodInfo>) methodMap.get(newMethodName);
    if (methodSet == null)
    {
      methodSet = new LinkedHashSet<MethodInfo>();
      methodMap.put(newMethodName, methodSet);
    }

    // Add the method information.
    methodSet.add(new MethodInfo(firstLineNumber, lastLineNumber, methodReturnType, methodArguments, methodName));
  }

  /**
   * A field record.
   */
  private static class FieldInfo
  {
    private String type;
    private String originalName;

    private FieldInfo(String type, String originalName)
    {
      this.type = type;
      this.originalName = originalName;
    }

    private boolean matches(String type)
    {
      return type == null || type.equals(this.type);
    }
  }

  /**
   * A method record.
   */
  private static class MethodInfo
  {
    private int    firstLineNumber;
    private int    lastLineNumber;
    private String type;
    private String arguments;
    private String originalName;

    private MethodInfo(int firstLineNumber, int lastLineNumber, String type, String arguments, String originalName)
    {
      this.firstLineNumber = firstLineNumber;
      this.lastLineNumber = lastLineNumber;
      this.type = type;
      this.arguments = arguments;
      this.originalName = originalName;
    }

    private boolean matches(int lineNumber, String type, String arguments)
    {
      return (lineNumber == 0 || (firstLineNumber <= lineNumber && lineNumber <= lastLineNumber) || lastLineNumber == 0) && (type == null || type.equals(this.type)) && (arguments == null || arguments.equals(this.arguments));
    }
  }

  /**
   * The main program for ReTrace.
   */
  public static String doReTrace(String mappingFile, String stackTraceFile)
  {
    String regularExpresssion = STACK_TRACE_EXPRESSION;
    boolean verbose = false;

    StringReTrace reTrace = new StringReTrace(regularExpresssion, verbose, mappingFile, stackTraceFile);

    reTrace.execute();

    return reTrace.result;
  }
}
