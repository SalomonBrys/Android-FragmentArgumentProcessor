package com.github.salomonbrys.androidfap;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Holds multiple {@link FragmentArgument}
 * @author Salomon BRYS (salomon.brys@gmail.com)
 */
@Documented
@Target(ElementType.TYPE)
public @interface FragmentArguments {

	/**
	 * @return All {@link FragmentArgument} for this fragment
	 */
	public FragmentArgument[] value();

}
