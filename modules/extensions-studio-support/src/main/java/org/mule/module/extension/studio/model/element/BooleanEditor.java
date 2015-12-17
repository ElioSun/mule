/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.extension.studio.model.element;

import org.mule.module.extension.studio.model.IEditorElementVisitor;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "boolean")
public class BooleanEditor extends BaseFieldEditorElement
{

    private Boolean value;// TODO para que? si el defaultValue sobra
    private Boolean defaultValue;
    private Boolean negative;

    @XmlAttribute
    public Boolean getDefaultValue()
    {
        return defaultValue;
    }

    public void setDefaultValue(Boolean defaultValue)
    {
        this.defaultValue = defaultValue;
    }

    @Override
    public void accept(IEditorElementVisitor visitor)
    {
        visitor.visit(this);
    }

    @XmlAttribute
    public Boolean getNegative()
    {
        return negative;
    }

    public void setNegative(Boolean negative)
    {
        this.negative = negative;
    }

    @XmlAttribute
    public Boolean getValue()
    {
        return value;
    }

    public void setValue(Boolean value)
    {
        this.value = value;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((defaultValue == null) ? 0 : defaultValue.hashCode());
        result = prime * result + ((negative == null) ? 0 : negative.hashCode());
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (!super.equals(obj))
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        BooleanEditor other = (BooleanEditor) obj;
        if (defaultValue == null)
        {
            if (other.defaultValue != null)
            {
                return false;
            }
        }
        else if (!defaultValue.equals(other.defaultValue))
        {
            return false;
        }
        if (negative == null)
        {
            if (other.negative != null)
            {
                return false;
            }
        }
        else if (!negative.equals(other.negative))
        {
            return false;
        }
        if (value == null)
        {
            if (other.value != null)
            {
                return false;
            }
        }
        else if (!value.equals(other.value))
        {
            return false;
        }
        return true;
    }
}
