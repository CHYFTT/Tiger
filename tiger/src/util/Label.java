package util;

import java.io.Serializable;

public class Label implements Serializable
{
  /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public int i;
	private static int count = 0;

  public Label()
  {
    i = count++;
  }

  @Override
  public String toString()
  {
    return "L_" + (Integer.toString(this.i));
  }
}
