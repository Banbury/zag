package de.banbury.zag.aspects;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Aspect
public class TraceGlkAspect {
	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Before("execution(* org.p2c2e.zing.IGlk.*(..)) && !execution(void org.p2c2e.zing.IGlk.progress(String, int, int, int))")
	public void logGlkCalls(JoinPoint jp) {
		StringBuilder sb = new StringBuilder();
		sb.append(jp.getSignature());
		sb.append(": ");
		for (Object o : jp.getArgs()) {
			if (o != null)
				sb.append(o.toString());
			else
				sb.append("null");
			sb.append(" ");
		}

		log.trace(sb.toString());
	}
}
