/*
 *  soapUI, copyright (C) 2004-2009 eviware.com
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the
 *  terms of version 2.1 of the GNU Lesser General Public License as published by
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.impl.wadl.inference.schema.content;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;

import com.eviware.soapui.impl.wadl.inference.ConflictHandler;
import com.eviware.soapui.impl.wadl.inference.schema.Content;
import com.eviware.soapui.impl.wadl.inference.schema.Context;
import com.eviware.soapui.impl.wadl.inference.schema.Particle;
import com.eviware.soapui.impl.wadl.inference.schema.Schema;
import com.eviware.soapui.impl.wadl.inference.schema.Settings;
import com.eviware.soapui.inferredSchema.ParticleConfig;
import com.eviware.soapui.inferredSchema.SequenceContentConfig;
import com.eviware.soapui.inferredSchema.SequenceContentConfig.ComesBefore;

/**
 * SequenceContent represents an xs:sequence. It infers ordering and occurrences
 * of its children. It can also represent an xs:choice, if each element has only
 * occured by itself.
 * 
 * @author Dain Nilsson
 */
public class SequenceContent implements Content
{
	private Schema schema;
	private Map<QName, Particle> particles;
	private HashMap<QName, List<QName>> comesBefore;
	private boolean completed;

	public SequenceContent( Schema schema, boolean completed )
	{
		this.schema = schema;
		this.completed = completed;
		particles = new LinkedHashMap<QName, Particle>(); // LinkedHashMap
																			// preserves order.
		comesBefore = new HashMap<QName, List<QName>>();
	}

	public SequenceContent( SequenceContentConfig xml, Schema schema )
	{
		this.schema = schema;
		completed = xml.getCompleted();
		particles = new LinkedHashMap<QName, Particle>(); // LinkedHashMap
																			// preserves order.
		for( ParticleConfig particleXml : xml.getParticleList() )
		{
			Particle p = Particle.Factory.parse( particleXml, schema );
			// TODO: Fix namespace!
			particles.put( p.getName(), p );
		}
		comesBefore = new HashMap<QName, List<QName>>();
		for( ComesBefore item : xml.getComesBeforeList() )
		{
			List<QName> others = new ArrayList<QName>();
			for( QName item2 : item.getOtherList() )
				others.add( item2 );
			comesBefore.put( item.getQname(), others );
		}
	}

	public SequenceContentConfig save()
	{
		SequenceContentConfig xml = SequenceContentConfig.Factory.newInstance();
		xml.setCompleted( completed );
		List<ParticleConfig> particleList = new ArrayList<ParticleConfig>();
		for( Particle item : particles.values() )
			particleList.add( item.save() );
		xml.setParticleArray( particleList.toArray( new ParticleConfig[0] ) );
		for( Map.Entry<QName, List<QName>> entry : comesBefore.entrySet() )
		{
			ComesBefore comesBeforeEntry = xml.addNewComesBefore();
			comesBeforeEntry.setQname( entry.getKey() );
			for( QName item : entry.getValue() )
				comesBeforeEntry.addOther( item );
		}
		return xml;
	}

	public Content validate( Context context ) throws XmlException
	{
		XmlCursor cursor = context.getCursor();

		// Find element order
		List<QName> orderSet = new ArrayList<QName>();
		List<QName> orderList = new ArrayList<QName>();
		if( !cursor.isEnd() )
		{
			cursor.push();
			do
			{
				QName qname = cursor.getName();
				if( qname == null )
					break;
				if( orderSet.contains( qname ) )
				{
					if( !orderSet.get( orderSet.size() - 1 ).equals( qname ) )
					{
						// Same element occurs more an once but not in a sequence!
						cursor.pop();
						throw new XmlException( "Same element occurs multiple times in sequence!" );
					}
				}
				else
					orderSet.add( qname );
				orderList.add( qname );
			}
			while( cursor.toNextSibling() );
			cursor.pop();
		}
		// Check element order against schema
		if( validateOrder( context, orderSet ) && validateOccurances( context, orderList ) )
		{
			// Validate elements
			for( QName item : orderList )
			{
				cursor.push();
				particles.get( item ).validate( context );
				cursor.pop();
				cursor.toNextSibling();
			}
		}
		else
			throw new XmlException( "Sequence validation" );
		completed = true;
		return this;
	}

	public String toString( String attrs )
	{
		if( particles.size() == 0 )
			return attrs;
		fixOrder();
		String type = isChoice() ? ":choice" : ":sequence";
		StringBuilder s = new StringBuilder( "<" + schema.getPrefixForNamespace( Settings.xsdns ) + type + ">" );
		for( Particle item : particles.values() )
			s.append( item );
		s.append( "</" + schema.getPrefixForNamespace( Settings.xsdns ) + type + ">" + attrs );
		return s.toString();
	}

	private void fixOrder()
	{
		List<QName> order = new ArrayList<QName>();
		for( QName item : particles.keySet() )
		{
			int i;
			for( i = order.size(); !canAppend( order.subList( 0, i ), item ); i-- )
				;
			order.add( i, item );
		}
		LinkedHashMap<QName, Particle> fixedParticles = new LinkedHashMap<QName, Particle>();
		for( QName item : order )
			fixedParticles.put( item, particles.get( item ) );
		particles = fixedParticles;
	}

	private boolean canAppend( List<QName> before, QName item )
	{
		for( QName item2 : before )
		{
			if( comesBefore.get( item ).contains( item2 ) )
				return false;
		}
		return true;
	}

	private boolean validateOccurances( Context context, List<QName> sequence ) throws XmlException
	{
		Map<QName, Integer> seen = new HashMap<QName, Integer>();
		for( QName item : particles.keySet() )
			seen.put( item, 0 );
		for( QName item : sequence )
			seen.put( item, seen.get( item ) + 1 );
		for( Map.Entry<QName, Integer> entry : seen.entrySet() )
		{
			Particle particle = particles.get( entry.getKey() );
			if( Integer.parseInt( particle.getAttribute( "minOccurs" ) ) > entry.getValue() )
			{
				if( context.getHandler().callback( ConflictHandler.Event.MODIFICATION, ConflictHandler.Type.ELEMENT,
						entry.getKey(), context.getPath(), "Element occurs less times than required." ) )
				{
					particle.setAttribute( "minOccurs", entry.getValue().toString() );
				}
				else
					throw new XmlException( "Element '" + entry.getKey().getLocalPart()
							+ "' required at least minOccurs times!" );
			}
			if( !particle.getAttribute( "maxOccurs" ).equals( "unbounded" )
					&& Integer.parseInt( particle.getAttribute( "maxOccurs" ) ) < entry.getValue() )
			{
				if( context.getHandler().callback( ConflictHandler.Event.MODIFICATION, ConflictHandler.Type.TYPE,
						new QName( schema.getNamespace(), context.getAttribute( "typeName" ) ), context.getPath(),
						"Element occurs more times than allowed." ) )
				{
					particle.setAttribute( "maxOccurs", entry.getValue().toString() );
				}
				else
					throw new XmlException( "Element '" + entry.getKey().getLocalPart()
							+ "' must not occur more than maxOccurs times!" );
			}
		}
		return true;
	}

	@SuppressWarnings( "unchecked" )
	private boolean validateOrder( Context context, List<QName> sequence )
	{
		List<QName> seen = new ArrayList<QName>();
		HashMap<QName, List<QName>> comesBefore = ( HashMap<QName, List<QName>> )this.comesBefore.clone();
		for( QName item : sequence )
		{
			if( !particles.containsKey( item ) )
			{
				if( context.getHandler().callback( ConflictHandler.Event.CREATION, ConflictHandler.Type.ELEMENT, item,
						context.getPath() + "/" + item.getLocalPart(), "Element has undeclared child element." ) )
				{
					if( item.getNamespaceURI().equals( schema.getNamespace() ) )
					{
						Particle element = Particle.Factory.newElementInstance( schema, item.getLocalPart() );
						if( completed )
							element.setAttribute( "minOccurs", "0" );
						particles.put( item, element );
					}
					else
					{
						Schema otherSchema = context.getSchemaSystem().getSchemaForNamespace( item.getNamespaceURI() );
						schema.putPrefixForNamespace( item.getPrefix(), item.getNamespaceURI() );
						if( otherSchema == null )
						{
							otherSchema = context.getSchemaSystem().newSchema( item.getNamespaceURI() );
						}
						Particle ref = otherSchema.getParticle( item.getLocalPart() );
						if( ref == null )
						{
							ref = otherSchema.newElement( item.getLocalPart() );
						}
						if( completed )
							ref.setAttribute( "minOccurs", "0" );
						particles.put( item, Particle.Factory.newReferenceInstance( schema, ref ) );
					}
				}
				else
					return false;
			}
			if( comesBefore.containsKey( item ) )
			{
				for( QName item2 : comesBefore.get( item ) )
				{
					if( seen.contains( item2 ) )
						return false;
				}
			}
			else
			{
				comesBefore.put( item, new ArrayList<QName>() );
			}
			for( QName item2 : seen )
			{
				if( !comesBefore.get( item2 ).contains( item ) )
					comesBefore.get( item2 ).add( item );
			}
			seen.add( item );
		}
		this.comesBefore = comesBefore;
		return true;
	}

	private boolean isChoice()
	{
		for( Particle e : particles.values() )
		{
			if( !( "0".equals( e.getAttribute( "minOccurs" ) ) && "1".equals( e.getAttribute( "maxOccurs" ) ) && comesBefore
					.get( e.getName() ).size() == 0 ) )
				return false;
		}
		return true;
	}
}
