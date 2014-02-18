/**
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
package com.msopentech.odatajclient.engine.metadata.edm.v4.annotation;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.ArrayList;
import java.util.List;

@JsonDeserialize(using = RecordDeserializer.class)
public class Record extends AnnotatedDynExprConstruct {

    private static final long serialVersionUID = -2886526224721870304L;

    private String type;

    private final List<PropertyValue> propertyValues = new ArrayList<PropertyValue>();

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public List<PropertyValue> getPropertyValues() {
        return propertyValues;
    }

}