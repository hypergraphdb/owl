package org.hypergraphdb.app.owl;

import org.semanticweb.owlapi.model.OWLAnnotationValueVisitor;
import org.semanticweb.owlapi.model.OWLAnnotationValueVisitorEx;
import org.semanticweb.owlapi.model.OWLDataVisitor;
import org.semanticweb.owlapi.model.OWLDataVisitorEx;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectVisitor;
import org.semanticweb.owlapi.model.OWLObjectVisitorEx;

public class OWLLiteralHGDB extends OWLObjectHGDB implements OWLLiteral
{
	private String literal;
	private OWLDatatype datatype;
	private String lang;

	public OWLLiteralHGDB(String literal, OWLDatatype datatype, String lang)
	{
		this.literal = literal;
		this.datatype = datatype;
		this.lang = lang;
	}
	
	public void setLiteral(String literal)
	{
		this.literal = literal;
	}
	
	public String getLiteral()
	{
		return literal;
	}

	public boolean isRDFPlainLiteral()
	{		
		return datatype.equals(getOWLDataFactory().getRDFPlainLiteral());
	}

	public boolean hasLang()
	{
		return !lang.equals("");
	}

	public boolean isInteger()
	{
		return datatype.equals(getOWLDataFactory().getIntegerOWLDatatype());
	}

	public int parseInteger() throws NumberFormatException
	{
		return Integer.parseInt(literal);
	}

	public boolean isBoolean()
	{
		return datatype.equals(getOWLDataFactory().getBooleanOWLDatatype());
	}

	public boolean parseBoolean() throws NumberFormatException
	{
		if (literal.equals("0"))
		{
			return false;
		}
		if (literal.equals("1"))
		{
			return true;
		}
		if (literal.equals("true"))
		{
			return true;
		}
		if (literal.equals("false"))
		{
			return false;
		}
		return false;
	}

	public boolean isDouble()
	{
		return datatype.equals(getOWLDataFactory().getDoubleOWLDatatype());
	}

	public double parseDouble() throws NumberFormatException
	{
		return Double.parseDouble(literal);
	}

	public boolean isFloat()
	{
		return datatype.equals(getOWLDataFactory().getFloatOWLDatatype());
	}

	public float parseFloat() throws NumberFormatException
	{
		return Float.parseFloat(literal);
	}

	public void setLang(String lang)
	{
		this.lang = lang;
	}
	
	public String getLang()
	{
		return lang;
	}

	public boolean hasLang(String lang)
	{
		return this.lang != null && this.lang.equalsIgnoreCase(lang.trim());
	}

	public void setDatatype(OWLDatatype datatype)
	{
		this.datatype = datatype;
	}
	
	public OWLDatatype getDatatype()
	{
		return datatype;
	}

	public boolean equals(Object obj)
	{
		if (super.equals(obj))
		{
			if (!(obj instanceof OWLLiteral))
			{
				return false;
			}
			OWLLiteral other = (OWLLiteral) obj;
			return literal.equals(other.getLiteral())
					&& datatype.equals(other.getDatatype())
					&& lang.equals(other.getLang());
		}
		return false;
	}

	public void accept(OWLDataVisitor visitor)
	{
		visitor.visit(this);
	}

	public <O> O accept(OWLDataVisitorEx<O> visitor)
	{
		return visitor.visit(this);
	}

	public void accept(OWLAnnotationValueVisitor visitor)
	{
		visitor.visit(this);
	}

	public <O> O accept(OWLAnnotationValueVisitorEx<O> visitor)
	{
		return visitor.visit(this);
	}

	@Override
	protected int compareObjectOfSameType(OWLObject object)
	{
		OWLLiteral other = (OWLLiteral) object;
		int diff = literal.compareTo(other.getLiteral());
		if (diff != 0)
		{
			return diff;
		}
		diff = datatype.compareTo(other.getDatatype());
		if (diff != 0)
		{
			return diff;
		}
		return lang.compareTo(other.getLang());

	}

	public void accept(OWLObjectVisitor visitor)
	{
		visitor.visit(this);
	}

	public <O> O accept(OWLObjectVisitorEx<O> visitor)
	{
		return visitor.visit(this);
	}
}