package com.twix;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class AttributePackage implements Serializable
	{
	private static final long serialVersionUID = 6823267450841873232L;
	
	public List<Object> AttrDef;
	public List<Object> AttrXRef;
	public List<Object> CategoryAttrLink;
	
	public AttributePackage()
		{
		AttrDef = new ArrayList<Object>();
		AttrXRef = new ArrayList<Object>();
		CategoryAttrLink = new ArrayList<Object>();
		}
	
	public static class AttrDef implements Serializable
		{
		private static final long serialVersionUID = -4628023968635797354L;
		
		public long AttrId;
		public String Type;
		public int Len;
		public String DisplayName;
		public String ShortName;
		public int SortOrder;
		public String InputMask;
		public String HostTable;
		public String HostColumn;
		
		public String DateChanged;
		public String Deprecated;
		}
	
	public static class AttrXRef implements Serializable
		{
		private static final long serialVersionUID = 4252293559106236069L;
		
		public long Parent;
		public long Child;
		public String DateChanged;
		}
	
	public static class CategoryAttrLink implements Serializable
		{
		private static final long serialVersionUID = 1417887326649038816L;
		
		public int equipmentCategoryId;
		public long AttrId;
		}
	}
