package com.twix;

import java.io.Serializable;
import java.util.List;

public class PackageData implements Serializable
	{
	private static final long	serialVersionUID	= 1606346908855976110L;
	
	public List<Object> PMChecklistHeader;
	public List<Object> PMChecklistItem;
	public List<Object> PMChecklistEquipmentXRef;
	
	public static class PMChecklistHeader implements Serializable
		{
		private static final long	serialVersionUID	= -1899677097456611782L;
		public int pmChecklistId;
		public int DispatchId;
		public int EquipmentCategoryId;
		public String empno;
		public String tabletMEID;
		public String NotServiced;
		}
	
	public static class PMChecklistItem implements Serializable
		{
		private static final long	serialVersionUID	= 6954396311053331896L;
		public int pmChecklistId;
		public int pmChecklistItemId;
		public String itemText;
		public String itemType;
		public String itemValue;
		public String itemNA;
		public String itemComment;
		public String identifier;
		}
	
	public static class PMChecklistEquipmentXRef implements Serializable
		{
		private static final long	serialVersionUID	= -8899898417808469750L;
		public int pmChecklistId;
		public int EquipmentId;
		}
	}
