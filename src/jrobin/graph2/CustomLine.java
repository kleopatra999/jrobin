/* ============================================================
 * JRobin : Pure java implementation of RRDTool's functionality
 * ============================================================
 *
 * Project Info:  http://www.jrobin.org
 * Project Lead:  Sasa Markovic (saxon@jrobin.org)
 * 
 * Developers:    Sasa Markovic (saxon@jrobin.org)
 *                Arne Vandamme (cobralord@jrobin.org)
 *
 * (C) Copyright 2003, by Sasa Markovic.
 *
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation;
 * either version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * library; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307, USA.
 */
package jrobin.graph2;

import java.awt.Color;
import java.awt.BasicStroke;
import java.util.HashMap;

import jrobin.core.RrdException;

/**
 * <p>description</p>
 * 
 * @author Arne Vandamme (arne.vandamme@jrobin.org)
 */
class CustomLine extends PlotDef
{
	private long xVal1;
	private long xVal2;
	
	private double yVal1;
	private double yVal2;
	
	private double dc;
	
	CustomLine( long startTime, double startValue, long endTime, double endValue, Color color )
	{
		this.color = color;
		if ( color == null )
			visible = false;
		
		this.xVal1 = startTime;
		this.xVal2 = endTime;
		this.yVal1 = startValue;
		this.yVal2 = endValue;
		
		try
		{
			long xc	   = xVal2 - xVal1;
			if ( xc != 0 )
				this.dc		= ( yVal2 - yVal1 ) / xc;
			else
				this.dc		= 0;
		}
		catch (Exception e) {
			this.dc = 0;
		}  
	}
	
	CustomLine( long startTime, double startValue, long endTime, double endValue, Color color, int lineWidth )
	{
		this( startTime, startValue, endTime, endValue, color );
		this.lineWidth = lineWidth;
	}
	
	void draw( ChartGraphics g, int[] xValues, int[] stackValues, int lastPlotType ) throws RrdException
	{
		g.setColor( color );
		g.setStroke( new BasicStroke(lineWidth) );
		
		int ax, ay, nx, ny;
		
		// Get X positions
		if ( xVal1 == Long.MIN_VALUE )
			ax = g.getMinX();
		else if ( xVal1 == Long.MAX_VALUE )
			ax = g.getMaxX();
		else
			ax = g.getX( xVal1 );
		
		if ( xVal2 == Long.MIN_VALUE )
			nx = g.getMinX();
		else if ( xVal2 == Long.MAX_VALUE )
			nx = g.getMaxX();
		else
			nx = g.getX( xVal2 );
		
		// Get Y positions
		if ( yVal1 == Double.MIN_VALUE )
			ay = g.getMinY();
		else if ( yVal1 == Double.MAX_VALUE )
			ay = g.getMaxY();
		else
			ay = g.getY( yVal1 );
		
		if ( yVal2 == Double.MIN_VALUE )
			ny = g.getMinY();
		else if ( yVal2 == Double.MAX_VALUE )
			ny = g.getMaxY();
		else
			ny = g.getY( yVal2 );

		// Draw the line
		if ( visible )		
			g.drawLine( ax, ay, nx, ny );
		 
		// Set the stackvalues
		int rx	= nx - ax;
		if ( rx != 0 )
		{
			double rc = ((ny - ay) * 1.0d) / rx;
			for (int i = 0; i < xValues.length; i++) {
				if ( xValues[i] < ax || xValues[i] > nx ) 
					stackValues[i] = 0;
				else if ( ay == ny )
					stackValues[i] = ay;
				else
					stackValues[i] = new Double(rc * (xValues[i] - ax) + ay).intValue();
			}
		}
		
				 
		g.setStroke( new BasicStroke() );
	}
	
	double getValue( int tblPos, long[] timestamps )
	{
		long time = timestamps[tblPos];
		
		// Out of range
		if ( time > xVal2 || time < xVal1 )
			return Double.NaN;
		
		// Hrule
		if ( yVal1 == yVal2 )
			return yVal1;
		
		// Vrule
		if ( yVal1 == Double.MIN_VALUE && yVal2 == Double.MAX_VALUE )
			return Double.NaN;
		
		// No line, very rare, will usually be 'out of range' first
		if ( xVal1 == xVal2 )
			return Double.NaN;
				
		// Custom line
		return ( dc * ( time - xVal1 ) + yVal1 );
	}
	
	void setSource( Source[] sources, HashMap sourceIndex ) throws RrdException
	{
		// Stub
	}
}
