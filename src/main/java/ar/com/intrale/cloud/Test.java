package ar.com.intrale.cloud;

import java.lang.reflect.ParameterizedType;

import javax.inject.Inject;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.micronaut.context.ApplicationContext;
import io.micronaut.core.beans.BeanIntrospection;
import io.micronaut.runtime.EmbeddedApplication;

public abstract class Test <PROV>{

	   public static final String DUMMY_VALUE = "DUMMY";
	   public static final String DUMMY_EMAIL = "DUMMY@DUMMY.COM";
	   
	   private final Class<Request> providerType = (Class<Request>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
	
	   @Inject
	   protected ApplicationContext applicationContext;
	   
	   @Inject
	   protected EmbeddedApplication app;
	   
	   protected ObjectMapper mapper = new ObjectMapper();
	   
	   protected static Lambda lambda;
	   
	   protected PROV provider;
	   
	   
	   /**
	    * Instancia Lambda y setea el proveedor en caso de que corresponda
	    */
	   public void lambdaInstantiation() {
		   if (lambda==null) {
			   BeanIntrospection<Lambda> beanIntrospection = BeanIntrospection.getIntrospection(Lambda.class);
			   lambda = beanIntrospection.instantiate();
		   }
		   if (provider!=null) {
			   lambda.getFunction().setProvider(provider);
		   }
	   }
	   
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
	   
	   @AfterEach
	   public void finalizeTest() {
		   afterEach();
	   }
	   
	   public abstract void afterEach();
	   
	   @AfterAll
	   public static void end() {
			if (lambda != null) {
	            lambda.getApplicationContext().close();
	        }
	   }
	   
	   
}
