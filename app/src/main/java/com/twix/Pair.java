package com.twix;

import java.io.Serializable;

public class Pair implements Serializable
	{
	private static final long serialVersionUID = -2420377895845317646L;
	public int id;
	public int newid;
	
	@Override
	public boolean equals(Object info)
		{
		if( info.getClass() == Pair.class )
			return ( ((Pair)info).id == this.id );
		else if( info.getClass() == Integer.class )
			return ( ((Integer)info).intValue() == this.id );
		else
			// FUUUUUU
			return false;
		}
	}