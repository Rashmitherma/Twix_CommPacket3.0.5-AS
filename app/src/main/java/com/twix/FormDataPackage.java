package com.twix;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FormDataPackage implements Serializable
	{
	private static final long serialVersionUID = 511436250641130306L;
	
	public List<Object> FormData;
	public List<Object> FormDataValues;
	public List<Object> FormDataSignatures;
	public List<Object> FormPhotos;
	public Map<Long, Long> ResolvedFormDataIds;
	
	public FormDataPackage()
		{
		FormData = new ArrayList<Object>();
		FormDataValues = new ArrayList<Object>();
		FormDataSignatures = new ArrayList<Object>();
		FormPhotos = new ArrayList<Object>();
		}
	
	public static class FormData implements Serializable
		{
		private static final long serialVersionUID = -4686780538161026403L;
		
		public long FormDataId;
		public int FormId;
		public String ParentTable;
		public int ParentId;
		public String LinkTable;
		public int LinkId;
		public String InputByEmpno;
		public String Completed;
		public String DateEntered;
		public String tabletMEID;
		}
	
	public static class FormDataValues implements Serializable
		{
		private static final long serialVersionUID = 2381699291275048395L;
		
		public long FormDataId;
		public long XRefId;
		public String MatrixTrail;
		public String Value;
		}
	
	public static class FormDataSignatures implements Serializable
		{
		private static final long serialVersionUID = -2150810263720111117L;
		
		public long FormDataId;
		public long XRefId;
		public String MatrixTrail;
		public byte[] Value;
		}
	
	public static class FormPhotos implements Serializable
		{
		private static final long serialVersionUID = 2101970094513398432L;
		
		public long FormPhotoId;
		public long FormDataId;
		public byte[] Photo;
		public String DateCreated;
		public String Comments;
		}
	}
