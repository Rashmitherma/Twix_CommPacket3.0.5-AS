package com.twix;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class FormPackage implements Serializable
	{
	private static final long serialVersionUID = -5093695656468825870L;
	
	public List<Object> Form;
	public List<Object> FormSection;
	public List<Object> FormSecXRef;
	public List<Object> FormMatrix;
	public List<Object> FormMatrixXRef;
	public List<Object> FormOptions;
	public List<Object> FormSecHeights;
	public List<Object> FormSecWidths;
	public List<Object> FormType;
	
	public FormPackage()
		{
		Form = new ArrayList<Object>();
		FormSection = new ArrayList<Object>();
		FormSecXRef = new ArrayList<Object>();
		FormMatrix = new ArrayList<Object>();
		FormMatrixXRef = new ArrayList<Object>();
		FormOptions = new ArrayList<Object>();
		FormSecHeights = new ArrayList<Object>();
		FormSecWidths = new ArrayList<Object>();
		FormType = new ArrayList<Object>();
		}
	
	public static class Form implements Serializable
		{
		private static final long serialVersionUID = -3110023686717440036L;
		
		public int FormId;
		public String Type;
		public int EquipmentCategoryId;
		public long AttrId;
		public String Description;
		public String DateChanged;
		public long VersionId;
		public int VersionNum;
		}
	
	public static class FormSection implements Serializable
		{
		private static final long serialVersionUID = 3465452993710588396L;
		
		public long FormSecId;
		public String Title;
		public int RowCnt;
		public int ColCnt;
		public String DateChanged;
		public long VersionId;
		public int VersionNum;
		}
	
	public static class FormSecXRef implements Serializable
		{
		private static final long serialVersionUID = 3443657186204413002L;
		
		public long XRefId;
		public int FormId;
		public long FormSecId;
		
		public int SortOrder;
		public String SectionType;
		}
	
	public static class FormMatrix implements Serializable
		{
		private static final long serialVersionUID = 205251097993890563L;
		
		public long MatrixId;
		public String InputType;
		public byte[] Image;
		public String ImageType;
		}
	
	public static class FormMatrixXRef implements Serializable
		{
		private static final long serialVersionUID = 6595389847631555456L;
		
		public long MatrixId;
		public int Row;
		public int Col;
		public long FormSecId;
		public int RowSpan;
		public int ColSpan;
		
		public long AttrId;
		public long PickId;
		public String Text;
		public String Modifiable;
		public long ChildFormId;
		
		public int FontSize;
		public String FontColor;
		public String Bold;
		public String Italic;
		public String Underline;
		public String Align;
		public String VAlign;
		public String BGColor;
		
		public String BorderLeft;
		public String BorderBottom;
		public String BorderRight;
		public String BorderTop;
		
		public String Required;
		}
	
	public static class FormOptions implements Serializable
		{
		private static final long serialVersionUID = 297832989481999361L;
		
		public long FormSecId;
		public long MatrixId;
		public String Value;
		}
	
	public static class FormSecHeights implements Serializable
		{
		private static final long serialVersionUID = -2436586503499106529L;
		
		public long FormSecId;
		public int Row;
		public String Height;
		}
	
	public static class FormSecWidths implements Serializable
		{
		private static final long serialVersionUID = 5743698780386107179L;
		
		public long FormSecId;
		public int Col;
		public String Width;
		}
	
	public static class FormType implements Serializable
		{
		private static final long serialVersionUID = -213242300843390112L;
		
		public String FormType;
		public String Description;
		}
	
	}
