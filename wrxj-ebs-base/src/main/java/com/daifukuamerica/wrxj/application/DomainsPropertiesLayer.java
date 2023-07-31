package com.daifukuamerica.wrxj.application;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Delegates to property layers based on name prefix.
 * 
 * <p><b>Details:</b> This class aggregates several properties layers into a 
 * single layer, allowing the properties in each layer to be distinguished by a 
 * unique prefix.  Client code sees this aggregate layer as if the prefix for 
 * each layer is prepended to the layer's property names.  In this aggregation, 
 * no two properties layers can be associated with prefixes that are also 
 * prefixes of each other.  Hence, there can be no naming conflicts in this 
 * aggregation, and all property names supplied to or received from this 
 * aggregate can correspond to at most one layer in this aggregation.  
 * Throughout the remainder of this documentation, the uniquely prefixed 
 * properties layers in this aggregation are called "properties domains" or,
 * simply, "domains."</p>
 * 
 * <p>When properties queries are made, the queries are intelligently forwarded 
 * only to domains that can service the query, based on prefix matching.  This 
 * switching mechanism allows multiple properties sources to be merged, without 
 * requiring that they be layered.  It also ensures that computing resources 
 * will not be wasted in layers that cannot possibly service the queries.</p>
 *  
 * @author Sharky
 */
public final class DomainsPropertiesLayer implements PropertiesLayer
{

  /**
   * Maps names to domains.
   * 
   * <p><b>Details:</b> This map relates names to domains.  Client code 
   * populates this map by calling 
   * {@link #addDomain(String, PropertiesLayer)}.</p>
   */
  private final Map<String, PropertiesLayer> mpDomains = new HashMap<String, PropertiesLayer>();
  
  /**
   * Associates prefix with properties source.
   * 
   * <p><b>Details:</b> This method associates the given name with the given 
   * domain.  Queries for properties beginning with this name will be converted 
   * into queries for properties in this domain.</p>
   * 
   * @param isName the prefix
   * @param ipDomain the domain
   */
  public void addDomain(String isName, PropertiesLayer ipDomain)
  {
    Set<String> vpExistingPrefixes = mpDomains.keySet();
    for (String vsExistingPrefix: vpExistingPrefixes)
    {
      if (vsExistingPrefix.startsWith(isName) || isName.startsWith(vsExistingPrefix))
      {
        String vsMessage = 
          "The name of the domain being added (\"" + isName + "\") conflicts " +
          "with the name of a previously added domain (\"" + vsExistingPrefix + 
          "\").  One may not be a prefix of the other.";
        throw new IllegalArgumentException(vsMessage);
      }
    }
    mpDomains.put(isName, ipDomain);
  }
  
  /**
   * Queries property from matching domain.
   * 
   * <p><b>Details:</b> This method returns the value of the named property from 
   * the matching domain.  It does this by selecting a domain based on the 
   * name's leading characters.  Specifically, the domain whose name is a prefix 
   * of the supplied property name is used.  The query is converted by stripping 
   * the domain's name from the supplied property name and forwarding the 
   * remaining suffix.  The domain's response is then returned.</p>
   * 
   * <p>If the supplied property name does not match a domain, or if the 
   * selected domain does not define the requested property, this method returns 
   * <code>null</code>.</p>
   * 
   * @param isName the property name
   */
  @Override
  public String getProperty(String isName)
  {
    Set<Map.Entry<String, PropertiesLayer>> vpEntries = mpDomains.entrySet();
    for (Map.Entry<String, PropertiesLayer> vpEntry: vpEntries)
    {
      String vsPrefix = vpEntry.getKey();
      if (isName.startsWith(vsPrefix))
      {
        int vnPrefixLength = vsPrefix.length();
        String vsSuffix = isName.substring(vnPrefixLength);
        PropertiesLayer vpLayer = vpEntry.getValue();
        String vsValue = vpLayer.getProperty(vsSuffix);
        return vsValue;
      }
    }
    return null;
  }

  /**
   * Returns supported property names.
   * 
   * <p><b>Details:</b> This method returns the set of all property names 
   * that are recognized by this aggregation, where the names begin with the
   * supplied prefix.  Depending on the prefix, this query may invoke operations 
   * on multiple domains.</p>
   *
   * @param isAbsSearchPrefix the search prefix
   * @return the names
   */
  @Override
  public Set<String> getPropertyNames(String isAbsSearchPrefix)
  {
    int vnSearchPrefixLength = isAbsSearchPrefix.length();
    Set<String> vpAbsoluteNames = new HashSet<String>();
    for (Map.Entry<String, PropertiesLayer> vpEntry: mpDomains.entrySet())
    {
      String vsDomainPrefix = vpEntry.getKey();
      PropertiesLayer vpDomain = vpEntry.getValue();
      int vnDomainPrefixLength = vsDomainPrefix.length();
      Set<String> vpSuffixes;
      if (vnSearchPrefixLength < vnDomainPrefixLength)
      {
        if (! vsDomainPrefix.startsWith(isAbsSearchPrefix))
          continue;
        vpSuffixes = vpDomain.getPropertyNames("");
      }
      else
      {
        if (! isAbsSearchPrefix.startsWith(vsDomainPrefix))
          continue;
        String vsRelSearchPrefix = isAbsSearchPrefix.substring(vnDomainPrefixLength);
        vpSuffixes = vpDomain.getPropertyNames(vsRelSearchPrefix);
      }
      for (String vsSuffix: vpSuffixes)
      {
        String vsName = vsDomainPrefix + vsSuffix;
        vpAbsoluteNames.add(vsName);
      }
    }
    return vpAbsoluteNames;
  }

  /**
   * {@inheritDoc}
   * 
   * <p><i>Currently unsupported by this implementation.</i></p>
   */ 
  @Override
  public void refresh()
  {
    for (String s : mpDomains.keySet()) {
      mpDomains.get(s).refresh();
    }
  }
}

