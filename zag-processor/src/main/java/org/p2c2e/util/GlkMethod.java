package org.p2c2e.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
public @interface GlkMethod {
	int value();
}
