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
package org.apache.olingo.ext.proxy.commons;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.olingo.commons.api.domain.CommonODataProperty;
import org.apache.olingo.commons.api.domain.ODataComplexValue;
import org.apache.olingo.commons.api.domain.ODataLinked;
import org.apache.olingo.commons.api.edm.EdmElement;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.core.edm.EdmTypeInfo;
import org.apache.olingo.ext.proxy.AbstractService;
import org.apache.olingo.ext.proxy.api.annotations.ComplexType;
import org.apache.olingo.ext.proxy.api.annotations.NavigationProperty;
import org.apache.olingo.ext.proxy.api.annotations.Property;
import org.apache.olingo.ext.proxy.context.AttachedEntityStatus;
import org.apache.olingo.ext.proxy.utils.ClassUtils;
import org.apache.olingo.ext.proxy.utils.CoreUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.olingo.client.api.communication.request.retrieve.ODataPropertyRequest;
import org.apache.olingo.client.api.communication.response.ODataRetrieveResponse;
import org.apache.olingo.client.api.uri.CommonURIBuilder;
import org.apache.olingo.commons.api.domain.ODataValue;
import org.apache.olingo.ext.proxy.api.ComplexCollection;
import org.apache.olingo.ext.proxy.api.EdmStreamType;
import org.apache.olingo.ext.proxy.api.PrimitiveCollection;

public class ComplexInvocationHandler extends AbstractStructuredInvocationHandler {

  private static Pair<ODataComplexValue<? extends CommonODataProperty>, Class<?>> init(
          final AbstractService<?> service,
          final Class<?> reference) {

    final Class<?> complexTypeRef;
    if (Collection.class.isAssignableFrom(reference)) {
      complexTypeRef = ClassUtils.extractTypeArg(reference);
    } else {
      complexTypeRef = reference;
    }

    final ComplexType annotation = complexTypeRef.getAnnotation(ComplexType.class);
    if (annotation == null) {
      throw new IllegalArgumentException("Invalid complex type " + complexTypeRef);
    }

    final FullQualifiedName typeName =
            new FullQualifiedName(ClassUtils.getNamespace(complexTypeRef), annotation.name());

    final ODataComplexValue<? extends CommonODataProperty> complex =
            service.getClient().getObjectFactory().newComplexValue(typeName.toString());

    return new ImmutablePair<ODataComplexValue<? extends CommonODataProperty>, Class<?>>(complex, complexTypeRef);
  }

  public static ComplexInvocationHandler getInstance(
          final String propertyName,
          final Class<?> reference,
          final EntityInvocationHandler handler) {

    final Pair<ODataComplexValue<? extends CommonODataProperty>, Class<?>> init = init(handler.service, reference);
    return new ComplexInvocationHandler(init.getLeft(), init.getRight(), handler);
  }

  public static ComplexInvocationHandler getInstance(
          final ODataComplexValue<?> complex,
          final Class<?> reference,
          final AbstractService<?> service) {

    return new ComplexInvocationHandler(complex, reference, service);
  }

  public static ComplexInvocationHandler getInstance(
          final Class<?> typeRef,
          final AbstractService<?> service) {
    final Pair<ODataComplexValue<? extends CommonODataProperty>, Class<?>> init = init(service, typeRef);
    return new ComplexInvocationHandler(init.getLeft(), init.getRight(), service);
  }

  public static ComplexInvocationHandler getInstance(
          final Class<?> reference,
          final AbstractService<?> service,
          final CommonURIBuilder<?> uri) {
    final Pair<ODataComplexValue<? extends CommonODataProperty>, Class<?>> init = init(service, reference);
    return new ComplexInvocationHandler(init.getLeft(), init.getRight(), service, uri);
  }

  public static ComplexInvocationHandler getInstance(
          final ODataComplexValue<? extends CommonODataProperty> complex,
          final Class<?> reference,
          final AbstractService<?> service,
          final CommonURIBuilder<?> uri) {
    return new ComplexInvocationHandler(complex, reference, service, uri);
  }

  private ComplexInvocationHandler(
          final ODataComplexValue<? extends CommonODataProperty> complex,
          final Class<?> typeRef,
          final AbstractService<?> service,
          final CommonURIBuilder<?> uri) {

    super(typeRef, complex, service);
    this.uri = uri;
    this.baseURI = this.uri == null ? null : this.uri.build();
  }

  private ComplexInvocationHandler(
          final ODataComplexValue<? extends CommonODataProperty> complex,
          final Class<?> typeRef,
          final EntityInvocationHandler handler) {

    super(typeRef, complex, handler);
    this.uri = null;
  }

  private ComplexInvocationHandler(
          final ODataComplexValue<? extends CommonODataProperty> complex,
          final Class<?> typeRef,
          final AbstractService<?> service) {

    super(typeRef, complex, service);
    this.uri = null;
  }

  @SuppressWarnings("unchecked")
  public ODataComplexValue<CommonODataProperty> getComplex() {
    return (ODataComplexValue<CommonODataProperty>) this.internal;
  }

  @Override
  @SuppressWarnings({"unchecked", "rawtypes"})
  protected Object getPropertyValue(final String name, final Type type) {
    try {
      Object res;
      Class<?> ref = ClassUtils.getTypeClass(type);

      if (ref == EdmStreamType.class) {
        res = Proxy.newProxyInstance(
                Thread.currentThread().getContextClassLoader(),
                new Class<?>[] {EdmStreamType.class}, new EdmStreamTypeHandler(
                getClient().newURIBuilder(baseURI.toASCIIString()).appendPropertySegment(name),
                service));

        return res;
      } else {

        final CommonODataProperty property = getComplex().get(name);

        if (ref != null && ClassUtils.getTypeClass(type).isAnnotationPresent(ComplexType.class)) {
          res = getComplex(
                  name,
                  property == null || property.hasNullValue() ? null : property.getValue(),
                  ref,
                  entityHandler,
                  baseURI,
                  false);
        } else if (ref != null && ComplexCollection.class.isAssignableFrom(ref)) {

          final ComplexCollectionInvocationHandler<?> collectionHandler;
          final Class<?> itemRef = ClassUtils.extractTypeArg(ref, ComplexCollection.class);

          if (property == null || property.hasNullValue()) {
            collectionHandler = new ComplexCollectionInvocationHandler(
                    service,
                    itemRef,
                    baseURI == null
                    ? null : getClient().newURIBuilder(baseURI.toASCIIString()).appendPropertySegment(name));
          } else {
            List items = new ArrayList();

            for (ODataValue item : property.getValue().asCollection()) {
              items.add(getComplex(
                      name,
                      item,
                      itemRef,
                      entityHandler,
                      baseURI,
                      true));
            }

            collectionHandler = new ComplexCollectionInvocationHandler(
                    service,
                    items,
                    itemRef,
                    baseURI == null
                    ? null : getClient().newURIBuilder(baseURI.toASCIIString()).appendPropertySegment(name));
          }

          res = Proxy.newProxyInstance(
                  Thread.currentThread().getContextClassLoader(),
                  new Class<?>[] {ref}, collectionHandler);

        } else if (ref != null && PrimitiveCollection.class.isAssignableFrom(ref)) {
          final PrimitiveCollectionInvocationHandler collectionHandler;
//          Class<?> itemRef = ref.getMethod("reference").getReturnType();

          if (property == null || property.hasNullValue()) {
            collectionHandler = new PrimitiveCollectionInvocationHandler(
                    service,
                    null,
                    baseURI == null
                    ? null : getClient().newURIBuilder(baseURI.toASCIIString()).appendPropertySegment(name));
          } else {
            List items = new ArrayList();
            for (ODataValue item : property.getValue().asCollection()) {
              items.add(item.asPrimitive().toValue());
            }
            collectionHandler = new PrimitiveCollectionInvocationHandler(
                    service,
                    items,
                    null,
                    baseURI == null
                    ? null : getClient().newURIBuilder(baseURI.toASCIIString()).appendPropertySegment(name));
          }

          res = Proxy.newProxyInstance(
                  Thread.currentThread().getContextClassLoader(),
                  new Class<?>[] {PrimitiveCollection.class}, collectionHandler);
        } else {
          res = property == null || property.hasNullValue()
                  ? null
                  : CoreUtils.getObjectFromODataValue(property.getValue(), type, service);
        }

        return res;
      }
    } catch (Exception e) {
      throw new IllegalArgumentException("Error getting value for property '" + name + "'", e);
    }
  }

  @Override
  public Collection<String> getAdditionalPropertyNames() {
    final Set<String> res = new HashSet<String>();
    final Set<String> propertyNames = new HashSet<String>();
    for (Method method : typeRef.getMethods()) {
      final Annotation ann = method.getAnnotation(Property.class);
      if (ann != null) {
        final String property = ((Property) ann).name();
        propertyNames.add(property);
      }
    }

    for (final CommonODataProperty property : getComplex()) {
      if (!propertyNames.contains(property.getName())) {
        res.add(property.getName());
      }
    }

    return res;
  }

  @Override
  protected void setPropertyValue(final Property property, final Object value) {
    final FullQualifiedName fqn =
            new FullQualifiedName(ClassUtils.getNamespace(typeRef), typeRef.getAnnotation(ComplexType.class).name());

    final EdmElement edmProperty = getClient().getCachedEdm().getComplexType(fqn).getProperty(property.name());

    final EdmTypeInfo type = new EdmTypeInfo.Builder().setEdm(getClient().getCachedEdm()).setTypeExpression(
            edmProperty.isCollection() ? "Collection(" + property.type() + ")" : property.type()).build();

    setPropertyValue(property.name(), type, value);
  }

  private void setPropertyValue(final String name, final EdmTypeInfo type, final Object value) {
    final Object toBeAdded;

    if (value == null) {
      toBeAdded = null;
    } else if (Collection.class.isAssignableFrom(value.getClass())) {
      toBeAdded = new ArrayList<Object>((Collection<? extends Object>) value);
    } else {
      toBeAdded = value;
    }

    getClient().getBinder().add(getComplex(), CoreUtils.getODataProperty(getClient(), name, type, toBeAdded));

    if (getEntityHandler() != null && !getContext().entityContext().isAttached(getEntityHandler())) {
      getContext().entityContext().attach(getEntityHandler(), AttachedEntityStatus.CHANGED);
    }
  }

  @Override
  protected Object getNavigationPropertyValue(final NavigationProperty property, final Method getter) {
    if (!(internal instanceof ODataLinked)) {
      throw new UnsupportedOperationException("Internal object is not navigable");
    }

    return retrieveNavigationProperty(property, getter);
  }

  @Override
  public void addAdditionalProperty(final String name, final Object value) {
    setPropertyValue(name, null, value);
    attach(AttachedEntityStatus.CHANGED);
  }

  @Override
  public void removeAdditionalProperty(final String name) {
    final CommonODataProperty property = getComplex().get(name);
    if (property != null && !property.hasNullValue()) {
      setPropertyValue(name, null, null);
      attach(AttachedEntityStatus.CHANGED);
    }
  }

  @Override
  protected void addLinkChanges(final NavigationProperty navProp, final Object value) {
    // do nothing ....
  }

  @Override
  public boolean isChanged() {
    return getEntityHandler() == null ? false : getEntityHandler().isChanged();
  }

  @Override
  protected void load() {
    try {
      if (this.uri != null) {
        final ODataPropertyRequest<CommonODataProperty> req =
                getClient().getRetrieveRequestFactory().getPropertyRequest(uri.build());

        final ODataRetrieveResponse<CommonODataProperty> res = req.execute();
        this.internal = res.getBody().getValue();
      }
    } catch (IllegalArgumentException e) {
      LOG.warn("Complex at '" + uri + "' not found", e);
      throw e;
    } catch (Exception e) {
      LOG.warn("Error retrieving complex '" + uri + "'", e);
      throw new IllegalArgumentException("Error retrieving " + typeRef.getSimpleName(), e);
    }
  }
}
