package com.helicaltech.pcni.rules;

import com.helicaltech.pcni.rules.interfaces.IRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * The class dynamically produces a Business Rule implementation that extends
 * IRule, which is a marker interface. This practically means any interface can
 * simply extend IRule and we can get an instance of any interface. Acts like a
 * factory of objects which instantiates the objects based on the name.
 * <p/>
 * Note: The singleton implementation is also supported. If
 * <code>getInstance()</code> method is provided, that method is used for
 * getting an instance.
 *
 * @author Rajasekhar
 * @since 1.1
 */
public class BusinessRulesFactory {

	private static final Logger logger = LoggerFactory.getLogger(BusinessRulesFactory.class);

	@SuppressWarnings("unchecked")
	public <T extends IRule> T getBusinessRuleImplementation(String clazz) {
		try {
			Class<?> ruleObject = Class.forName(clazz);
			Method factoryMethod = ruleObject.getMethod("getInstance");
			return (T) factoryMethod.invoke(null);
		} catch (ClassNotFoundException e) {
			logger.error("ClassNotFoundException", e);
		} catch (IllegalAccessException e) {
			logger.error("IllegalAccessException", e);
		} catch (SecurityException e) {
			logger.error("SecurityException", e);
		} catch (NoSuchMethodException e) {
			/*
			 * In case of getInstance singleton pattern is not followed, return
			 * an object of that class
			 */
			logger.error("NoSuchMethodException, getInstance() singleton pattern is not used. " + "Using default constructor.");
			try {
				return (T) Class.forName(clazz).newInstance();
			} catch (InstantiationException ex) {
				logger.error("InstantiationException", ex);
			} catch (IllegalAccessException ex) {
				logger.error("IllegalAccessException", ex);
			} catch (ClassNotFoundException ex) {
				logger.error("ClassNotFoundException", ex);
			}
		} catch (IllegalArgumentException e) {
			logger.error("IllegalArgumentException", e);
		} catch (InvocationTargetException e) {
			logger.error("InvocationTargetException", e);
		}
		/*
		 * We will never reach here
		 */
		return null;
	}
}
