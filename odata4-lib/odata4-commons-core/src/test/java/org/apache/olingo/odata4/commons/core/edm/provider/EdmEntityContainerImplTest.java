/* 
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.olingo.odata4.commons.core.edm.provider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.olingo.odata4.commons.api.edm.EdmActionImport;
import org.apache.olingo.odata4.commons.api.edm.EdmEntityContainer;
import org.apache.olingo.odata4.commons.api.edm.EdmEntitySet;
import org.apache.olingo.odata4.commons.api.edm.EdmException;
import org.apache.olingo.odata4.commons.api.edm.EdmFunctionImport;
import org.apache.olingo.odata4.commons.api.edm.EdmSingleton;
import org.apache.olingo.odata4.commons.api.edm.provider.ActionImport;
import org.apache.olingo.odata4.commons.api.edm.provider.EdmProvider;
import org.apache.olingo.odata4.commons.api.edm.provider.EntityContainerInfo;
import org.apache.olingo.odata4.commons.api.edm.provider.EntitySet;
import org.apache.olingo.odata4.commons.api.edm.provider.FullQualifiedName;
import org.apache.olingo.odata4.commons.api.edm.provider.FunctionImport;
import org.apache.olingo.odata4.commons.api.edm.provider.Singleton;
import org.apache.olingo.odata4.commons.api.exception.ODataException;
import org.junit.Before;
import org.junit.Test;

public class EdmEntityContainerImplTest {

  EdmEntityContainer container;

  @Before
  public void setup() {
    EdmProvider provider = new CustomProvider();
    EdmProviderImpl edm = new EdmProviderImpl(provider);
    EntityContainerInfo entityContainerInfo =
        new EntityContainerInfo().setContainerName(new FullQualifiedName("space", "name"));
    container = new EdmEntityContainerImpl(edm, provider, entityContainerInfo);
  }

  @Test
  public void checkEdmExceptionConversion() throws Exception {
    EdmProvider provider = mock(EdmProvider.class);
    FullQualifiedName containerName = new FullQualifiedName("space", "name");
    when(provider.getEntitySet(containerName, null)).thenThrow(new ODataException("msg"));
    when(provider.getSingleton(containerName, null)).thenThrow(new ODataException("msg"));
    when(provider.getFunctionImport(containerName, null)).thenThrow(new ODataException("msg"));
    when(provider.getActionImport(containerName, null)).thenThrow(new ODataException("msg"));
    EdmProviderImpl edm = new EdmProviderImpl(provider);
    EntityContainerInfo entityContainerInfo =
        new EntityContainerInfo().setContainerName(containerName);
    EdmEntityContainer container = new EdmEntityContainerImpl(edm, provider, entityContainerInfo);
    boolean thrown = false;
    try {
      container.getEntitySet(null);
    } catch (EdmException e) {
      thrown = true;
    }
    if (!thrown) {
      fail("Expected EdmException not thrown");
    }
    try {
      container.getSingleton(null);
    } catch (EdmException e) {
      thrown = true;
    }
    if (!thrown) {
      fail("Expected EdmException not thrown");
    }
    try {
      container.getActionImport(null);
    } catch (EdmException e) {
      thrown = true;
    }
    if (!thrown) {
      fail("Expected EdmException not thrown");
    }
    try {
      container.getFunctionImport(null);
    } catch (EdmException e) {
      thrown = true;
    }
    if (!thrown) {
      fail("Expected EdmException not thrown");
    }
  }

  @Test
  public void simpleContainerGetter() {
    assertEquals("name", container.getName());
    assertEquals("space", container.getNamespace());
  }

  @Test
  public void getExistingFunctionImport() {
    EdmFunctionImport functionImport = container.getFunctionImport("functionImportName");
    assertNotNull(functionImport);
    assertEquals("functionImportName", functionImport.getName());
    // Caching
    assertTrue(functionImport == container.getFunctionImport("functionImportName"));
  }

  @Test
  public void getNonExistingFunctionImport() {
    assertNull(container.getFunctionImport(null));
  }

  @Test
  public void getExistingActionImport() {
    EdmActionImport actionImport = container.getActionImport("actionImportName");
    assertNotNull(actionImport);
    assertEquals("actionImportName", actionImport.getName());
    // Caching
    assertTrue(actionImport == container.getActionImport("actionImportName"));
  }

  @Test
  public void getNonExistingActionImport() {
    assertNull(container.getActionImport(null));
  }

  @Test
  public void getExistingSingleton() {
    EdmSingleton singleton = container.getSingleton("singletonName");
    assertNotNull(singleton);
    assertEquals("singletonName", singleton.getName());
    // Caching
    assertTrue(singleton == container.getSingleton("singletonName"));
  }

  @Test
  public void getNonExistingSingleton() {
    assertNull(container.getSingleton(null));
  }

  @Test
  public void getExistingEntitySet() {
    EdmEntitySet entitySet = container.getEntitySet("entitySetName");
    assertNotNull(entitySet);
    assertEquals("entitySetName", entitySet.getName());
    // Caching
    assertTrue(entitySet == container.getEntitySet("entitySetName"));
  }

  @Test
  public void getNonExistingEntitySet() {
    assertNull(container.getEntitySet(null));
  }

  private class CustomProvider extends EdmProvider {
    @Override
    public EntitySet getEntitySet(final FullQualifiedName entityContainer, final String entitySetName)
        throws ODataException {
      if (entitySetName != null) {
        return new EntitySet().setName("entitySetName");
      }
      return null;
    }

    @Override
    public Singleton getSingleton(final FullQualifiedName entityContainer, final String singletonName)
        throws ODataException {
      if (singletonName != null) {
        return new Singleton().setName("singletonName");
      }
      return null;
    }

    @Override
    public ActionImport getActionImport(final FullQualifiedName entityContainer, final String actionImportName)
        throws ODataException {
      if (actionImportName != null) {
        return new ActionImport().setName("singletonName");
      }
      return null;
    }

    @Override
    public FunctionImport getFunctionImport(final FullQualifiedName entityContainer, final String functionImportName)
        throws ODataException {
      if (functionImportName != null) {
        return new FunctionImport().setName("singletonName");
      }
      return null;
    }
  }
}
