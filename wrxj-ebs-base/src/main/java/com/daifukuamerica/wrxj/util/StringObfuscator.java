package com.daifukuamerica.wrxj.util;

import java.math.BigInteger;

/**
 * Obfuscates strings.
 *
 * <p><b>Details:</b> <code>StringObfuscator</code> obfuscates and unobfuscates
 * strings using a simple, unkeyed string-<wbr>to-<wbr>number mapping.  This
 * allows strings to be stored in a semi-<wbr>secure format in publicly readable
 * files.  Since this obfuscation map is unkeyed, security is achieved only
 * through obscurity in the mapping algorithm.  Do not use this class to secure
 * highly confidential data.</p>
 *
 * <p>To obfuscate a string, use <code>encode</code>.  To restore the original
 * string, use <code>decode</code>.</p>
 *
 * <p>Strings obfuscated in one locale may not be decodable in another
 * locale.</p>
 *
 * @author Sharky
 */
public final class StringObfuscator
{

  /**
   * Prepended to unencoded string prior to obfuscation.
   *
   * <p><b>Details:</b> <code>LEFT_PAD</code> is prepended to the unencoded
   * string prior to mapping it to a BigInteger.  This is done to ensure that
   * the length of the string is not zero, and also to increase mapping
   * obscurity.</p>
   *
   * <p>Note that either <code>LEFT_PAD</code> or <code>RIGHT_PAD</code> may be
   * the empty string, but not both.</p>
   */
  private static final String LEFT_PAD = "[";

  /**
   * Appended to unencoded string prior to obfuscation.
   *
   * <p><b>Details:</b> <code>RIGHT_PAD</code> is appended to the unencoded
   * string prior to mapping it to a BigInteger.  This is done to ensure that
   * the length of the string is not zero, and also to increase mapping
   * obscurity.</p>
   *
   * <p>Note that either <code>LEFT_PAD</code> or <code>RIGHT_PAD</code> may be
   * the empty string, but not both.</p>
   */
  private static final String RIGHT_PAD = "]";

  /**
   * Radix of encoded string.
   *
   * <p><b>Details:</b> <code>RADIX</code> is the radix of the integer
   * representing the encoded string.  It is currently set to 10 + 36 so that
   * all digits and letters are used.  The offers better compression and
   * obscurity in the obfuscated string.</p>
   */
  private static final int RADIX = 10 + 26;

  /**
   * Prepended to string's byte array.
   *
   * <p><b>Details:</b> <code>POSITIVE_PREFIX</code> is a positive
   * <code>byte</code> prepended to the pre-<wbr>obfuscated string's byte array
   * in order to make sure the represented <code>BigInteger</code> is a positive
   * value.  This constant may be any positive value.</p>
   */
  private static final byte POSITIVE_PREFIX = 0x7f;

  /**
   * Obfuscates string.
   *
   * <p><b>Details:</b> <code>encode</code> maps the supplied string
   * (<code>isUnencoded</code>) to an obfuscated string.  The value returned
   * can be passed to <code>decode</code> to obtain the original string.</p>
   *
   * <p>If <code>null</code> is given, <code>null</code> is returned.</p>
   *
   * @param isUnencoded the string to encode
   * @return the encoded string
   * @see #decode(String)
   */
  public static String encode(String isUnencoded)
  {
    if (isUnencoded == null)
      return null;
    isUnencoded = LEFT_PAD + isUnencoded + RIGHT_PAD;
    final byte[] vpbRaw = isUnencoded.getBytes();
    final int vnLen = vpbRaw.length;
    final byte[] vpbPrefixed = new byte[vnLen + 1];
    vpbPrefixed[0] = POSITIVE_PREFIX;
    System.arraycopy(vpbRaw, 0, vpbPrefixed, 1, vnLen);
    return new BigInteger(vpbPrefixed).toString(RADIX);
  }

  /**
   * Unobfuscates string.
   *
   * <p><b>Details:</b> <code>decode</code> maps the obfuscated string back to
   * the original string.  Only strings produced by <code>encode</code> should
   * be decoded with this method.  All other strings will cause an
   * <code>IllegalArgumentException</code> to be thrown.</p>
   *
   * <p>If <code>null</code> is given, <code>null</code> is returned.</p>
   *
   * @param isEncoded the string to decode
   * @return the decoded string
   * @throws IllegalArgumentException if isEncoded was not encoded using encode
   * @see #encode(String)
   */
  public static String decode(final String isEncoded)
  {
    if (isEncoded == null)
      return null;
    decode:
    try
    {
      final byte[] vpbPrefixed = new BigInteger(isEncoded, RADIX).toByteArray();
      if (vpbPrefixed[0] != POSITIVE_PREFIX)
        break decode;
      vpbPrefixed[0] = 0;
      final String vsUnencoded = new String(vpbPrefixed, 1, vpbPrefixed.length - 1);
      if (! vsUnencoded.startsWith(LEFT_PAD) || ! vsUnencoded.endsWith(RIGHT_PAD))
        break decode;
      return vsUnencoded.substring(1, vsUnencoded.length() - 1);
    }
    catch (final NumberFormatException ve)
    {
    }
    throw new IllegalArgumentException("isEncoded=\"" + isEncoded + '\"');
  }

}

