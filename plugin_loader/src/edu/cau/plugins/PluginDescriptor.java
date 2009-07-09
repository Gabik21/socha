package edu.cau.plugins;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface PluginDescriptor
{
	String name();
	String author() default "Anonymous";
	String version() default "1.0";
	String uuid();
}