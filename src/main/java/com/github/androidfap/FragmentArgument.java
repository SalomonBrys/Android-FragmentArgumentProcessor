package com.github.androidfap;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * An argument of a fragment
 *
 * Annotates a fragment or inside a {@link FragmentArguments} if many.
 *
 * @author Salomon BRYS (salomon.brys@gmail.com)
 */
@Documented
@Target(ElementType.TYPE)
public @interface FragmentArgument {

	/**
	 * @return The type of the argument
	 */
	Class<?> type();

	/**
	 * @return The name of the argument. Must be a valid identifier (no space, punctuation, etc.)
	 */
	String name();

}
