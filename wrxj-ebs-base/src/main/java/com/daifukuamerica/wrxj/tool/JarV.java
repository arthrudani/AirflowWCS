package com.daifukuamerica.wrxj.tool;
 
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public class JarV
{
	public static void main(String[] args)
	{
		if (args.length < 1)
		{
			System.out.println(
				"Syntax for Jar Version Checking\n"+
				"java JarV name.jar"
				);
			System.exit(1);
		}
		try
		{
			JarFile jar = new JarFile(args[0]);
			Manifest manifest = jar.getManifest();
			Attributes attr = manifest.getMainAttributes();
			if (attr == null)
			{
				System.out.println("Jar " + args[0] + " does not contain Manifest Attributes");
				System.exit(0);
			}
			
System.out.println("MANIFEST_VERSION:         " + (attr.getValue(Attributes.Name.MANIFEST_VERSION)         == null ? " " :	attr.getValue(Attributes.Name.MANIFEST_VERSION)));
System.out.println("SIGNATURE_VERSION:        " + (attr.getValue(Attributes.Name.SIGNATURE_VERSION)        == null ? " " :	attr.getValue(Attributes.Name.SIGNATURE_VERSION)));
System.out.println("SPECIFICATION_TITLE:      " + (attr.getValue(Attributes.Name.SPECIFICATION_TITLE)      == null ? " " :	attr.getValue(Attributes.Name.SPECIFICATION_TITLE)));
System.out.println("SPECIFICATION_VERSION:    " + (attr.getValue(Attributes.Name.SPECIFICATION_VERSION)    == null ? " " :	attr.getValue(Attributes.Name.SPECIFICATION_VERSION)));
System.out.println("SPECIFICATION_VENDOR:     " + (attr.getValue(Attributes.Name.SPECIFICATION_VENDOR)     == null ? " " :	attr.getValue(Attributes.Name.SPECIFICATION_VENDOR)));
System.out.println("IMPLEMENTATION_TITLE:     " + (attr.getValue(Attributes.Name.IMPLEMENTATION_TITLE)     == null ? " " :	attr.getValue(Attributes.Name.IMPLEMENTATION_TITLE)));
System.out.println("IMPLEMENTATION_VERSION:   " + (attr.getValue(Attributes.Name.IMPLEMENTATION_VERSION)   == null ? " " :	attr.getValue(Attributes.Name.IMPLEMENTATION_VERSION)));
System.out.println("IMPLEMENTATION_VENDOR:    " + (attr.getValue(Attributes.Name.IMPLEMENTATION_VENDOR)    == null ? " " :	attr.getValue(Attributes.Name.IMPLEMENTATION_VENDOR)));
System.out.println("IMPLEMENTATION_VENDOR_ID: " + (attr.getValue(Attributes.Name.IMPLEMENTATION_VENDOR_ID) == null ? " " :	attr.getValue(Attributes.Name.IMPLEMENTATION_VENDOR_ID)));
System.out.println("IMPLEMENTATION_URL:       " + (attr.getValue(Attributes.Name.IMPLEMENTATION_URL)       == null ? " " :	attr.getValue(Attributes.Name.IMPLEMENTATION_URL)));
System.out.println("CONTENT_TYPE:             " + (attr.getValue(Attributes.Name.CONTENT_TYPE)             == null ? " " :	attr.getValue(Attributes.Name.CONTENT_TYPE)));
System.out.println("CLASS_PATH:               " + (attr.getValue(Attributes.Name.CLASS_PATH)               == null ? " " :	attr.getValue(Attributes.Name.CLASS_PATH)));
System.out.println("MAIN_CLASS:               " + (attr.getValue(Attributes.Name.MAIN_CLASS)               == null ? " " :	attr.getValue(Attributes.Name.MAIN_CLASS)));
System.out.println("SEALED:                   " + (attr.getValue(Attributes.Name.SEALED)                   == null ? " " :	attr.getValue(Attributes.Name.SEALED)));
System.out.println("EXTENSION_LIST:           " + (attr.getValue(Attributes.Name.EXTENSION_LIST)           == null ? " " :	attr.getValue(Attributes.Name.EXTENSION_LIST)));
System.out.println("EXTENSION_NAME:           " + (attr.getValue(Attributes.Name.EXTENSION_NAME)           == null ? " " :	attr.getValue(Attributes.Name.EXTENSION_NAME)));
System.out.println("EXTENSION_INSTALLATION:   " + (attr.getValue(Attributes.Name.EXTENSION_INSTALLATION)   == null ? " " :	attr.getValue(Attributes.Name.EXTENSION_INSTALLATION)));

/*			
This works, but not standalone - only if you are running Manifest from the same jar
			String mainClass = attr.getValue(Attributes.Name.MAIN_CLASS);
			if (mainClass == null)
			{
				System.out.println("Jar does not specify a main application - can not give version information");
				System.exit(0);
			}
			
			System.out.println("Main-Class: " + mainClass);
			int index = mainClass.indexOf("Manifest");
			if (index == -1)
			{
				System.out.println("Jars application is not Manifest - can not give version information on this jar");
				System.exit(0);
			}
			String validate = mainClass.substring(index);
			if (!validate.equals("Manifest"))
			{
				System.out.println("Jars application is not Manifest - can not give version information on this jar");
				System.exit(0);
			}
			String[] cargs = new String[0];
			Class c = Class.forName(mainClass);
			Method m = c.getMethod("main", new Class[] { cargs.getClass() });
			m.setAccessible(true);
			int mods = m.getModifiers();
			if (m.getReturnType() != void.class || !Modifier.isStatic(mods) || !Modifier.isPublic(mods)) 
				throw new NoSuchMethodException("main");
			m.invoke(null, new Object[] { cargs });
*/			
		} catch (Throwable t)
		{
			System.out.println("Problem with processing jar file " + args[0] + " : " + t.getClass().getName() + " " + t.getMessage());
			t.printStackTrace();
		}
	}
}