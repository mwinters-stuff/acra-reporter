package nz.org.winters.appspot.acrareporter.shared;

public enum ErrorListFilter
{
  elfNew("New"),
  elfAll("All"),
  elfNotFixed("NotFixed"),
  elfLookedAt("LookedAt"),
  elfFixed("Fixed");

  private String filterStr;
  
  private ErrorListFilter(String filterStr)
  {
    this.filterStr = filterStr;
  }
  
  public static ErrorListFilter fromFilterString(String filter)
  {
    for(ErrorListFilter elf: ErrorListFilter.values())
    {
      if(elf.filterStr.equalsIgnoreCase(filter))
        return elf;
    }
    return elfAll;
  }
}
