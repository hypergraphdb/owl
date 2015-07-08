package org.hypergraphdb.app.owl.test.versioning;

import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

public class MyTestName implements MethodRule
{
	private String name;

	@Override
	public Statement apply(Statement base, FrameworkMethod method, Object target)
	{
		name = method.getMethod().getDeclaringClass().getName() + '#' + method.getName();
		return base;
	}

	public String getMethodName()
	{
		return name;
	}
}