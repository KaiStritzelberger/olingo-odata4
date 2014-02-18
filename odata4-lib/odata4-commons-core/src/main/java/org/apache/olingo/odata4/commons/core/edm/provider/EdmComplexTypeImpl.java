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

import org.apache.olingo.odata4.commons.api.edm.EdmComplexType;
import org.apache.olingo.odata4.commons.api.edm.EdmException;
import org.apache.olingo.odata4.commons.api.edm.EdmStructuralType;
import org.apache.olingo.odata4.commons.api.edm.constants.EdmTypeKind;
import org.apache.olingo.odata4.commons.api.edm.provider.ComplexType;
import org.apache.olingo.odata4.commons.api.edm.provider.FullQualifiedName;

public class EdmComplexTypeImpl extends EdmStructuralTypeImpl implements EdmComplexType {

  public EdmComplexTypeImpl(final EdmProviderImpl edm, final FullQualifiedName name, final ComplexType complexType) {
    super(edm, name, complexType, EdmTypeKind.COMPLEX);
  }

  @Override
  public EdmComplexType getBaseType() {
    return (EdmComplexType) baseType;
  }

  @Override
  protected EdmStructuralType buildBaseType(final FullQualifiedName baseTypeName) {
    EdmComplexType baseType = null;
    if (baseTypeName != null) {
      baseType = edm.getComplexType(baseTypeName);
      if (baseType == null) {
        throw new EdmException("Can't find base type with name: " + baseTypeName + " for complex type: " + getName());
      }
    }
    return baseType;
  }

}
