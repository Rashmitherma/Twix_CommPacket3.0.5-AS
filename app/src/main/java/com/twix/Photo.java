package com.twix;

import java.io.Serializable;

public class Photo implements Serializable
	{
	private static final long serialVersionUID = 8303708768609262087L;
	
	public String photoDate;
	public byte[] photo;
	public String comments;
	}