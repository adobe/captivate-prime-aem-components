/*
  Copyright 2021 Adobe. All rights reserved. This file is licensed to you under the Apache License,
  Version 2.0 (the "License"); you may not use this file except in compliance with the License. You
  may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software distributed under the License
  is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS OF ANY KIND, either
  express or implied. See the License for the specific language governing permissions and
  limitations under the License.*/

package com.adobe.prime.core.services;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.Session;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.jackrabbit.api.JackrabbitSession;
import org.apache.jackrabbit.api.security.user.User;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.adobe.prime.core.Constants;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith({AemContextExtension.class, MockitoExtension.class}) 
public class EmbeddableWidgetUserServiceImplTest { 

	private final AemContext ctx = new AemContext();

	private EmbeddableWidgetUserServiceImpl userServiceImpl;

	private static final String SUBSERVICE_NAME = "writeService"; 
	private static final Map<String, Object> SERVICE_PARAMS = Collections.<String, Object>singletonMap(ResourceResolverFactory.SUBSERVICE, SUBSERVICE_NAME);

	@Mock 
	private ResourceResolverFactory resolverFactory;

	@Mock 
	private EmbeddableWidgetConfigurationService widgetConfigService;

	@Mock 
	UserManager userManager;

	@Mock 
	JackrabbitSession jackSession;

	@Mock 
	User user;
	
	@Mock
	Node node;

	@BeforeEach 
	public void setUp() throws Exception {
		Map<String, String> pageProperties = new HashMap<>();
	    pageProperties.put("cq:conf", "/conf/global/captivate-prime/testConfig");
	    ctx.create().page("/content/mypage", null, pageProperties);
	    ctx.currentResource("/content/mypage");
	    ctx.load().json("/files/AdminConfigRsrc.json", "/conf/global/captivate-prime/testConfig/settings/cloudconfigs/cpwidget");

	    lenient().when(resolverFactory.getServiceResourceResolver(SERVICE_PARAMS)).thenReturn(ctx.resourceResolver());
	    ctx.registerService(ResourceResolverFactory.class, resolverFactory, org.osgi.framework.Constants.SERVICE_RANKING, Integer.MAX_VALUE);
	    
	    Map<String, Object> adminConfigs = new HashMap<>();
	    adminConfigs.put(Constants.CP_NODE_PROPERTY_PREFIX + "commonConfig.captivateHostName", "https://captivateprimeqe.adobe.com");
	    adminConfigs.put(Constants.CP_NODE_PROPERTY_PREFIX + "refreshToken", "1234");
	    adminConfigs.put(Constants.CP_NODE_PROPERTY_PREFIX + "clientId", "1234");
	    adminConfigs.put(Constants.CP_NODE_PROPERTY_PREFIX + "clientSecret", "1234");
	    adminConfigs.put(Constants.CP_NODE_PROPERTY_PREFIX + "theme.background", "transparent");
	    lenient().when(widgetConfigService.getAvailaleAdminConfiguration(any(Resource.class))).thenReturn(adminConfigs);
	    lenient().when(widgetConfigService.getGeneralConfigs(any(Resource.class))).thenReturn(adminConfigs);
	    ctx.registerService(EmbeddableWidgetConfigurationService.class, widgetConfigService, org.osgi.framework.Constants.SERVICE_RANKING,
	        Integer.MAX_VALUE);

	    userServiceImpl = new EmbeddableWidgetUserServiceImpl();
	    
	    Field resourceResolverFactory = EmbeddableWidgetUserServiceImpl.class.getDeclaredField("resourceResolverFactory");
	    resourceResolverFactory.set(userServiceImpl, resolverFactory);

	    Field replicatorField = EmbeddableWidgetUserServiceImpl.class.getDeclaredField("widgetConfigService");
	    replicatorField.setAccessible(true);
	    replicatorField.set(userServiceImpl, widgetConfigService);

	    ctx.load().json("/files/UserRsrc.json", "/home/user/vaishnav");

	    ValueMock[] emailValues = new ValueMock[] {new ValueMock("vaishnav@adobe.com")};

	    lenient().when(user.getProperty(Constants.LearnerConfigurations.USER_EMAIL_PATH)).thenReturn(emailValues);
	    lenient().when(user.getPath()).thenReturn("/home/user/vaishnav");

	    lenient().when(userManager.getAuthorizable(eq("vaishnav"))).thenReturn(user);
	    
	    lenient().when(jackSession.getUserManager()).thenReturn(userManager);
	    lenient().when(jackSession.getUserID()).thenReturn("vaishnav");
	    ctx.registerAdapter(ResourceResolver.class, Session.class, jackSession);
	}

	@Test 
	public void testGetUserEmail() 
	{
		String email = userServiceImpl.getUserEmail(ctx.request());
		assertTrue("vaishnav@adobe.com".equals(email));
	}
	
	@Test 
	public void testGetAccessTokenWithExpiry() throws Exception {
		ValueMock[] values = new ValueMock[] {new ValueMock("testAccess")};
		String tokenSpecificPath = "_" + DigestUtils.sha512Hex("1234");
		lenient().when(user.getProperty(Constants.LearnerConfigurations.USER_ACCESS_TOKEN_PATH + tokenSpecificPath)).thenReturn(values);
		
		values = new ValueMock[] {new ValueMock(String.valueOf(Long.MAX_VALUE))};
		lenient().when(user.getProperty(Constants.LearnerConfigurations.USER_ACCESS_TOKEN_EXPIRY_PATH + tokenSpecificPath)).thenReturn(values);
		Pair<String, Long> pair = userServiceImpl.getAccessTokenWithExpiry(ctx.request(), ctx.currentPage(), "test@test.com");
		assertTrue("testAccess".equals(pair.getLeft()));
		assertTrue(Long.valueOf(Long.MAX_VALUE).equals(pair.getRight()));
	}

	@Test 
	public void testGetAccessTokenWithExpiryNullValue() {
		Pair<String, Long> pair = userServiceImpl.getAccessTokenWithExpiry(ctx.request(), ctx.currentPage(), "test@test.com");
		assertTrue(pair == null);
	}
	
	@Test
	public void testSetAccessTokenWithExpiry()
	{
		ctx.registerAdapter(Resource.class, Node.class, node);
		boolean isSuccess = userServiceImpl.setAccessTokenWithExpiry(ctx.request(), ctx.currentPage(), "testAccessToken", 100L, "test@test.com");
		assertTrue(isSuccess);
	}
	
	@Test
	public void testSetAccessTokenWhenNoUserNode()
	{
		boolean isSuccess = userServiceImpl.setAccessTokenWithExpiry(ctx.request(), ctx.currentPage(), "testAccessToken", 100L, "test@test.com");
		assertTrue(!isSuccess);
	}

}

