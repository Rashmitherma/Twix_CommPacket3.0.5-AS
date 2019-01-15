package com.twix;

import java.io.Serializable;

public class Compressor implements Serializable
	{
	private static final long serialVersionUID = -6302515604002130850L;
	
	public int circuitId;
	
	public String compressorNo;
	public String manufacturer;
	public String model;
	public String serialNo;
	public String dateInService;
	public String dateOutService;
	}