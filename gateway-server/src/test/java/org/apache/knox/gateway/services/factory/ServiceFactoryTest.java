/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.knox.gateway.services.factory;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.lang.reflect.Field;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.knox.gateway.services.GatewayServices;
import org.apache.knox.gateway.services.Service;
import org.apache.knox.gateway.services.ServiceType;
import org.apache.knox.gateway.services.security.AliasService;
import org.apache.knox.gateway.services.security.KeystoreService;
import org.apache.knox.gateway.services.security.MasterService;
import org.easymock.EasyMock;
import org.junit.Rule;
import org.junit.rules.ExpectedException;

class ServiceFactoryTest {

  @SuppressWarnings("deprecation")
  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  protected final GatewayServices gatewayServices = EasyMock.createNiceMock(GatewayServices.class);

  protected void initConfig() {
    final MasterService masterService = EasyMock.createNiceMock(MasterService.class);
    expect(gatewayServices.getService(ServiceType.MASTER_SERVICE)).andReturn(masterService).anyTimes();
    final KeystoreService keystoreservice = EasyMock.createNiceMock(KeystoreService.class);
    expect(gatewayServices.getService(ServiceType.KEYSTORE_SERVICE)).andReturn(keystoreservice).anyTimes();
    final AliasService aliasService = EasyMock.createNiceMock(AliasService.class);
    expect(gatewayServices.getService(ServiceType.ALIAS_SERVICE)).andReturn(aliasService).anyTimes();
    replay(gatewayServices);
  }

  protected void testBasics(AbstractServiceFactory serviceFactory, ServiceType nonMatchingServiceType, ServiceType matchingServiceType) throws Exception {
    testBasics(serviceFactory, nonMatchingServiceType, matchingServiceType, false);
  }

  protected void testBasics(AbstractServiceFactory serviceFactory, ServiceType nonMatchingServiceType, ServiceType matchingServiceType, boolean checkUnknownImplementation)
      throws Exception {
    shouldReturnCorrectServiceType(serviceFactory, matchingServiceType);
    shouldReturnNullForNonMatchingServiceType(serviceFactory, nonMatchingServiceType);
    if (checkUnknownImplementation) {
      shouldThrowIllegalArgumentExceptionIfNoMatchingImplementationFound(serviceFactory, matchingServiceType);
    }
  }

  private void shouldReturnCorrectServiceType(AbstractServiceFactory serviceFactory, ServiceType serviceType) {
    assertEquals(serviceType, serviceFactory.getServiceType());
  }

  private void shouldReturnNullForNonMatchingServiceType(AbstractServiceFactory serviceFactory, ServiceType serviceType) throws Exception {
    assertNull(serviceFactory.create(gatewayServices, serviceType, null, null, null));
  }

  private void shouldThrowIllegalArgumentExceptionIfNoMatchingImplementationFound(AbstractServiceFactory serviceFactory, ServiceType serviceType) throws Exception {
    expectedException.expect(IllegalArgumentException.class);
    final String serviceName = ServiceType.TOKEN_STATE_SERVICE == serviceType ? "Token State" : StringUtils.capitalize(serviceType.getShortName());
    expectedException.expectMessage(String.format(Locale.ROOT, "Invalid %s Service implementation provided: unknown", serviceName));
    serviceFactory.create(gatewayServices, serviceType, null, null, "unknown");
  }

  protected boolean isMasterServiceSet(Service serviceToCheck) throws Exception {
    return isServiceSet(serviceToCheck, "masterService");
  }

  protected boolean isKeystoreServiceSet(Service serviceToCheck) throws Exception {
    return isServiceSet(serviceToCheck, "keystoreService");
  }

  protected boolean isAliasServiceSet(Service serviceToCheck) throws Exception {
    return isServiceSet(serviceToCheck, "aliasService");
  }

  private boolean isServiceSet(Service serviceToCheck, String expectedServiceName) throws Exception {
    final Field aliasServiceField = FieldUtils.getDeclaredField(serviceToCheck.getClass(), expectedServiceName, true);
    final Object aliasServiceValue = aliasServiceField.get(serviceToCheck);
    return aliasServiceValue != null;
  }
}
