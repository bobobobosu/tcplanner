/*
 * Copyright 2010 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package bo.tc.tcplanner.persistable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.firebase.database.Exclude;
import org.optaplanner.core.api.domain.lookup.PlanningId;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public abstract class AbstractPersistable implements Serializable {
    @JsonIgnore
    @Exclude
    protected UUID id;
    @JsonIgnore
    @Exclude
    protected boolean volatileFlag;

    protected AbstractPersistable() {
        this.id = UUID.randomUUID();
        this.volatileFlag = false;
    }

    protected AbstractPersistable(AbstractPersistable abstractPersistable) {
        this.id = UUID.randomUUID();
        this.volatileFlag = abstractPersistable.volatileFlag;
    }

    @PlanningId
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return getClass().getName().replaceAll(".*\\.", "") + "-" + id;
    }

    public boolean isVolatileFlag() {
        return volatileFlag;
    }

    public AbstractPersistable setVolatileFlag(boolean volatileFlag) {
        this.volatileFlag = volatileFlag;
        return this;
    }

    abstract public AbstractPersistable removeVolatile();

    abstract public AbstractPersistable removeEmpty();

    abstract public boolean checkValid();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractPersistable that = (AbstractPersistable) o;
        return volatileFlag == that.volatileFlag &&
                Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, volatileFlag);
    }
}
