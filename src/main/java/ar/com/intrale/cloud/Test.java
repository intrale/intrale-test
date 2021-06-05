package ar.com.intrale.cloud;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.inject.Inject;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.fasterxml.jackson.databind.ObjectMapper;

import ar.com.intrale.cloud.config.ApplicationConfig;
import io.micronaut.context.ApplicationContext;
import io.micronaut.core.beans.BeanIntrospection;
import io.micronaut.runtime.EmbeddedApplication;

public abstract class Test {

	public static final Long   DUMMY_ID = Long.valueOf(777);
	public static final String DUMMY_VALUE = "DUMMY";
	public static final String CHANGED_VALUE = "CHANGED";
	public static final String DUMMY_EMAIL = "prueba@intrale.com.ar";
	public static final String DUMMY_PASS = "123#abCD";
	public static final String DUMMY_PASS_2 = "456#efGH";

	@Inject
	protected ApplicationContext applicationContext;

	@Inject
	protected EmbeddedApplication app;
	
	@Inject
	protected ApplicationConfig config;

	protected ObjectMapper mapper = new ObjectMapper();

	protected Lambda lambda;

	/**
	 * Se ejecuta por unica vez previo a todos los test
	 */
	@BeforeAll
	public static void setup() {
	}

	@BeforeEach
	public void initializeTest() {
		beforeEach();
		lambdaInstantiation();
	}

	public abstract void beforeEach();
	
	/**
	 * Instancia Lambda y setea el proveedor en caso de que corresponda
	 */
	public void lambdaInstantiation() {
 		if (lambda == null) {
			BeanIntrospection<Lambda> beanIntrospection = BeanIntrospection.getIntrospection(Lambda.class);
			lambda = beanIntrospection.instantiate();

			Collection<Function> functions = lambda.getApplicationContext().getBeansOfType(Function.class);
			Iterator<Function> it = functions.iterator();
			while (it.hasNext()) {
				Function function = (Function) it.next();
				function.setProvider(applicationContext.getBean(function.getProviderType()));
				lambda.getApplicationContext().registerSingleton(function.getProviderType(), function.getProvider());
			}
			
			
		}
	}

	@AfterEach
	public void finalizeTest() {
		afterEach();
	}

	public abstract void afterEach();

	@AfterAll
	public static void end() {
	}
	
	public APIGatewayProxyRequestEvent makeRequestEvent(Request request, String function) throws Exception{
        APIGatewayProxyRequestEvent requestEvent = new APIGatewayProxyRequestEvent();
        Map<String, String> headers = new HashMap<String, String>();
        headers.put(Lambda.HEADER_FUNCTION, function);
        requestEvent.setHeaders(headers);
        requestEvent.setBody(mapper.writeValueAsString(request));
        return requestEvent;
	}

}
