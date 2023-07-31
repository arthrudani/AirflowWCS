package com.daifukuamerica.wrxj.factory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Map;


/**
 * General-purpose factory.
 * 
 * <p><b>Details:</b> This singleton class is a general-<wbr>purpose factory 
 * that facilitates the replacement of standard types with specialized types,
 * according to application-<wbr>specific requirements.</p>
 * 
 * <p>Standard types may be concrete classes, abstract classes, or interfaces.
 * Client code designed to accept alternate implementations for certain types
 * should not create instances of those types using the types' constructors.  
 * Instead, the client code should use this factory to instantiate the object.  
 * The factory method is {@link #create(Class, Object[])}.  Replacement 
 * implementations are registered using 
 * {@link #setImplementation(Class, Class)}.</p>
 * 
 * <blockquote>
 * 
 *   <p><b>Example:</b> Suppose few of the methods in our application use the 
 *   {@code Widget} type.  In our code, however, we are reluctant to use the 
 *   expression {@code new Widget()}, because we anticipate that at least one 
 *   customer in the future will have special requirements for {@code Widget}'s 
 *   behavior.  To stay flexible, we use the factory for instantiation:</p>
 * 
 *   <blockquote><pre><code>
      *Widget vpWidget = Factory.create(Widget.class);
      *vpWidget.doSomething();
      *doSomethingElse(vpWidget);
      *...
 *   </code></pre></blockquote>
 * 
 *   <p>If {@code Widget} is concrete, then nothing more needs to be done, and 
 *   the factory will automatically instantiate {@code Widget} using its
 *   zero-<wbr>parameter constructor.  And each time, the new instance is 
 *   returned without the need for any casting.</p>
 *   
 *   <p>Suppose that later on, our new customer Acme has special requirements 
 *   for {@code Widget}'s behavior.  Fortunately, a substitution is easy to 
 *   make: We simply register a replacement type with the factory:</p>
 *   
 *   <blockquote><pre><code>
      *Factory.setImplementation(Widget.class, AcmeWidget.class);
 *   </code></pre></blockquote>
 * 
 *   <p>After this registration is executed, the factory will instantiate and 
 *   return the specialized implementation wherever {@code Widget} is called 
 *   for.</p>
 *   
 * </blockquote>
 * 
 * <p>Note that in the method and field descriptions below, the standard type 
 * is referred to as the "interface" type, because factory clients typically use 
 * only the base type's exposed methods.  By default, the implementing type is 
 * the same as the base type, and {@link #create(Class, Object[])} will attempt 
 * to instantiate the exact type that was named, unless an alternate 
 * implementation is registered.  Thus, if this factory is used to instantiate 
 * pure interfaces or abstract classes, {@link #setImplementation(Class, Class)} 
 * <em>must</em> be called first to associate the interface with the 
 * implementation.</p>
 * 
 * @author Sharky
 */
public final class Factory
{

  /**
   * Hides default constructor.
   */
  private Factory()
  {
  }

  /**
   * Maps interface types to implementation types.
   * 
   * <p><b>Details:</b> This field associates interface types with implementing
   * types. These associations are registered with 
   * {@link #setImplementation(Class, Class)}.</p>
   */
  private static ImplementationsMap gpImplementations = new ImplementationsMap();

  /**
   * Returns registered implementation.
   * 
   * <p><b>Details:</b> This method looks up the implementing type registered
   * for the interface type, if any, and returns its class. If no implementing 
   * type has been registered, this method returns <code>null</code>.</p>
   * 
   * @param <Type> the interface's type
   * @param itInterface the interface class
   * @return the registered type
   */
  @SuppressWarnings("unchecked")
  private static <Type> Class<Type> getImplementingClass(Class<Type> itInterface)
  {
    if (gpImplementations.isEmpty())
    {
      System.err.println("ERROR: Factory not initialized!" + itInterface.getName());
    }
    return gpImplementations.get(itInterface);
  }

  /**
   * Returns implementation for interface.
   * 
   * <p><b>Details:</b> This method determines the implementation to use for the 
   * given interface.  If a specific implementation has been registered, this 
   * method returns it.  Otherwise, this method returns the original 
   * interface, which may or may not be instantiatable.</p>
   * 
   * @param <Type> the interface's type
   * @param itInterface the interface class
   * @return the implementation class
   */
  public static <Type> Class<Type> getImplementation(Class<Type> itInterface)
  {
    Class<Type> vtImplementation = getImplementingClass(itInterface);
    if (vtImplementation == null)
      vtImplementation = itInterface;
    return vtImplementation;
  }

  /**
   * Returns typesafe constructor list.
   * 
   * <p><b>Details:</b> This method obtains and returns a typesafe array of all 
   * the constructors for the given class.  This method is necessary to 
   * accomodate incompatible interface changes introduced in J2SE 1.6.0.</p>
   *  
   * @param <Type> the class type
   * @param ipClass the class
   * @return the constructors
   */
  @SuppressWarnings("unchecked")
  private static <Type> Constructor<Type>[] getConstructors(Class<Type> ipClass)
  {
    return (Constructor<Type>[]) ipClass.getConstructors();
  }
  
  /**
   * Finds most compatible constructor.
   *  
   * <p><b>Details:</b> This method examines all of the public constructors for
   * the given class, searching for the constructor that best fits the given
   * argument types.  If a constructor is found whose signature exactly matches
   * the argument types, it is returned.  Otherwise, if only inexact matches are 
   * found, the constructor whose signature most closely matches the argument 
   * types is returned.  If no compatible constructor is identified, 
   * <code>null</code> is returned.</p>
   * 
   * <p>The degree to which a constructor "fits" the provided argument is 
   * measured by counting the number of constructor parameter types that are 
   * assignment-<wbr>compatible with their corresponding argument types, but not 
   * exactly the same type.  The constructor with the least number of 
   * differences is the winner.  In the event of a tie, the first constructor 
   * examined (by chance) is chosen.</p>
   * 
   * @param <Type> the class' type
   * @param itClass the class
   * @param iatArgTypes the argument types
   * @return the best match
   */
  private static <Type> Constructor<Type> findBestConstructor(Class<Type> itClass, Class[] iatArgTypes)
  {
    final int vnArgTypesLength = iatArgTypes.length;
    Constructor<Type> vpBestConstructor = null;
    int vnBestDiffs = Integer.MAX_VALUE;
    // Examine all public constructors to determine best fit:
    searching:
    for (Constructor<Type> vpConstructor: getConstructors(itClass))
    {
      // Determine if the constructor is a potential match, and determine how
      // well it matches.
      Class<?>[] vatParamTypes = vpConstructor.getParameterTypes();
      // At the very least, the candidate constructor must require the same 
      // number of parameters as provided by the caller. 
      if (vatParamTypes.length != vnArgTypesLength)
        continue;
      // Let's see how closely the candidate constructor's signature matches the 
      // provided arguments.
      int vnDiffs = 0;
      for (int vnI = 0; vnI < vnArgTypesLength; ++ vnI)
      {
        Class<?> vpSigClass = vatParamTypes[vnI];
        Class vpArgClass = iatArgTypes[vnI];
        if (vpSigClass.equals(vpArgClass))
          // Perfect match!
          continue;
        if (vpSigClass.isPrimitive())
        {
          if (vpArgClass == null)
            // Can't assign null to a primitive.  Reject candidate. 
            continue searching;
        }
        else
        {
          if (vpArgClass != null)
            if (! vpSigClass.isAssignableFrom(vpArgClass))
              // Provide object not assignable to constructor.  Reject candidate.
              continue searching;
        }
        // Slightly different parameter type, but compatible.
        ++ vnDiffs;
      }
      // Stop searching if we found an exact match!
      if (vnDiffs == 0)
        return vpConstructor;
      // This constructor would work.  Is it our best match so far?
      if (vnDiffs < vnBestDiffs)
      {
        vpBestConstructor = vpConstructor;
        vnBestDiffs = vnDiffs;
      }
    }
    // Return the best we found, or null if we found none.
    return vpBestConstructor;
  }
  
  /**
   * Instantiates type.
   * 
   * <p><b>Details:</b> This method creates and returns an instance of the 
   * specified interface type using the provided construction parameters. If an 
   * implementing type was been registered using 
   * {@link #setImplementation(Class, Class)}, then the new instance will be 
   * of the type that was registered; otherwise, the new instance's type will
   * default to that of the given interface.</p>
   * 
   * @param <Type> the type
   * @param itInterface the type's class
   * @param iapParams optional construction parameters
   * @return the new instance
   */
  public static <Type> Type create(Class<Type> itInterface, Object... iapParams)
  {
    Class<Type> vtImplementingClass = getImplementation(itInterface);
    int vnParamsLength = iapParams.length;
    Class[] vatParamsClasses = new Class[vnParamsLength];
    for (int vnI = 0; vnI < vnParamsLength; ++ vnI)
    {
      Object vpParam = iapParams[vnI];
      Class vtParamClass;
      if (vpParam != null)
        vtParamClass = vpParam.getClass();
      else
        vtParamClass = null;
      vatParamsClasses[vnI] = vtParamClass;
    }
    try
    {
      Constructor<Type> vpConstructor = findBestConstructor(vtImplementingClass, vatParamsClasses);
      if (vpConstructor == null)
        throw new FactoryException("Matching constructor not found for " 
            + vtImplementingClass.getCanonicalName(), null);
      Type vpNewInstance = vpConstructor.newInstance(iapParams);
      return vpNewInstance;
    }
    catch (InstantiationException ee)
    {
      throw new FactoryException("Implementing type is abstract for "
          + vtImplementingClass.getCanonicalName(), ee);
    }
    catch (IllegalAccessException ee)
    {
      throw new FactoryException("Constructor is inaccessible for "
          + vtImplementingClass.getCanonicalName(), ee);
    }
    catch (InvocationTargetException ee)
    {
      throw new FactoryException("Constructor failed for "
          + vtImplementingClass.getCanonicalName(), ee);
    }
  }

  /**
   * Associates interface with implementation.
   * 
   * <p><b>Details:</b> This method associates the given interface type with the
   * given implementation type.  If the interface type is not a supertype of the 
   * implementing type, or if the implementing type is not concrete, this method 
   * will throw an {@link IllegalArgumentException}.  If either the interface 
   * type or the implementing type are <code>null</code>, this method will throw 
   * a {@link NullPointerException}.  Otherwise, it will record the association 
   * and return silently.</p>
   * 
   * <p>This method is typically called during application warm-<wbr>up.</p>
   *   
   * @param <Type> the interface type
   * @param itInterface the interface class
   * @param itImplementation the implementation class
   */
  public static <Type> void setImplementation
  ( Class<Type> itInterface, 
    Class<? extends Type> itImplementation
  )
  {
    if (! itInterface.isAssignableFrom(itImplementation))
      throw new IllegalArgumentException
      ( "itInterface (" + 
        itInterface + 
        ") is not a supertype of itImplementation (" + 
        itImplementation + 
        ")"
       );
    int vnModifiers = itImplementation.getModifiers();
    if (Modifier.isAbstract(vnModifiers) || Modifier.isInterface(vnModifiers))
      throw new IllegalArgumentException
      ( "itImplementation (" + 
        itImplementation + 
        ") is not concrete."
      );
    // TODO Check that the implementing type is not an Enum.
    gpImplementations.put(itInterface, itImplementation);
  }

  /**
   * Associates interfaces with implementations.
   * 
   * <p><b>Details:</b> This method calls 
   * {@link #setImplementation(Class, Class)} for each of the entries in the 
   * given map.</p>
   * 
   * <p>If the provided map contains an association that is invalid (see 
   * {@link #setImplementation(Class, Class)} for possible reasons), this method 
   * will throw an exception after attempting to process the other (valid) 
   * associations.</p>
   * 
   * <p>Clients are advised to use {@link ImplementationsMap} in conjunction 
   * with this method.</p> 
   * 
   * @param ipMap the map
   */
  public static void setImplementations(Map<Class, Class> ipMap)
  {
    RuntimeException veDeferredException = null;
    for (Map.Entry<Class, Class> vpEntry: ipMap.entrySet())
    {
      Class<Object> vtInterface = toClassOfObject(vpEntry.getKey());
      Class<Object> vtImplementation = toClassOfObject(vpEntry.getValue());
      try
      {
        setImplementation(vtInterface, vtImplementation);
      }
      catch (IllegalArgumentException ee)
      {
        if (veDeferredException == null)
          veDeferredException = ee;
      }
      catch (NullPointerException ee)
      {
        if (veDeferredException == null)
          veDeferredException = ee;
      }
    }
    if (veDeferredException != null)
      throw veDeferredException;
  }

  /**
   * Genericizes Class instance.
   *  
   * <p><b>Details:</b> This method casts the given {@code Class} to a 
   * {@code Class<Object>}, without "unchecked" warnings.</p>
   * 
   * @param ipClass the class
   * @return the genericized class
   */
  @SuppressWarnings("unchecked")
  private static Class<Object> toClassOfObject(Class ipClass)
  {
    return ipClass;
  }
  
}

