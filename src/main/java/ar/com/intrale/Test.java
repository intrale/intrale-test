package ar.com.intrale;

import java.io.IOException;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.inject.Inject;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.fasterxml.jackson.databind.ObjectMapper;

import ar.com.intrale.BaseFunction;
import ar.com.intrale.FunctionBuilder;
import ar.com.intrale.Lambda;
import ar.com.intrale.RequestRoot;
import ar.com.intrale.config.ApplicationConfig;
import ar.com.intrale.exceptions.FunctionException;
import io.micronaut.context.ApplicationContext;
import io.micronaut.core.beans.BeanIntrospection;
import io.micronaut.core.util.StringUtils;
import io.micronaut.runtime.EmbeddedApplication;

public abstract class Test {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(Test.class);

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
		try {
			beforeEach();
			lambdaInstantiation();
		} catch (Exception e) {
			LOGGER.error(FunctionException.toString(e));
		}
	}

	public abstract void beforeEach();
	
	/**
	 * Instancia Lambda y setea el proveedor en caso de que corresponda
	 */
	public void lambdaInstantiation() {
 		if (lambda == null) {
			BeanIntrospection<Lambda> beanIntrospection = BeanIntrospection.getIntrospection(Lambda.class);
			lambda = beanIntrospection.instantiate();

			Collection<BaseFunction> functions = lambda.getApplicationContext().getBeansOfType(BaseFunction.class);
			Iterator<BaseFunction> it = functions.iterator();
			while (it.hasNext()) {
				BaseFunction function = (BaseFunction) it.next();

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
	
	public APIGatewayProxyRequestEvent makeRequestEvent(RequestRoot request, String function) throws Exception{
        APIGatewayProxyRequestEvent requestEvent = new APIGatewayProxyRequestEvent();
        Map<String, String> headers = new HashMap<String, String>();
        headers.put(FunctionBuilder.HEADER_BUSINESS_NAME, DUMMY_VALUE);
        headers.put(FunctionBuilder.HEADER_FUNCTION, function);
        requestEvent.setHeaders(headers);
        requestEvent.setBody(Base64.getEncoder().encodeToString(mapper.writeValueAsString(request).getBytes()));
        return requestEvent;
	}
	
	public <T> T readValue(byte[] content, Class<T> valueType) throws IOException {
		return mapper.readValue(content, valueType);
	}
	
	public <T> T readEncodedValue(String content, Class<T> valueType) throws IOException {
		if (StringUtils.isNotEmpty(content)) {
			return readValue(Base64.getDecoder().decode(content), valueType);
		}
		return null;
	}
}