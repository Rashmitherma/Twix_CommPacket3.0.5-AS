package com.twix_agent;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import android.content.Context;

/*******************************************************************************************************************
 * Class: Twix_AgentTheme
 * 
 * Purpose: Provides all basic colorings for Twix_Agent. Themes can be created and set. Themes only effect dynamically
 * 			build objects so far.
 * 
 * Relevant XML: none
 * 
 * 
 * @author Michael Nowak, Comp Three Inc.
 *Modified by Rashmi Kulkarni, Therma Corp.
 ********************************************************************************************************************/
public class Twix_AgentTheme
	{
	public int	tableBG2		= 0xffa0a0d0;
	public int	tableBG			= 0xff9999cc;
	public int	tableBGAlt		= 0xff9999ff;
	public int	headerText		= 0xFFFFFFFF;
	public int	headerValue		= 0xff000055;
	public int	headerSize		= 20;
	public int	headerSizeLarge	= 25;
	public int	headerBG		= 0xFFAAAADD;
	public int	headerBGAlt		= 0xFF8888FF;
	public int	sub1Header		= 0xFFFFFFFF;
	public int	sub1Value		= 0xff000055;
	public int	sub1BG			= 0x00000000;
	public int	sub2BG			= 0xFFCCCCFF;
	public int	subSize			= 20;
	public int	editBG			= 0xffddddff;
	public int	lineColor		= 0xff555588;	// #ff9999cc
	public int	sortDesc		= 0xFF555588;
	public int	sortAsc			= 0xFF9999CC;
	public int	sortNone		= 0xFF7777AA;
	public int	warnColor		= 0xffff0000;
	public int	warnColorLight	= 0xffffaaaa;
	public int	disabledColor	= 0xff666666;
	public int	disabledColorBG	= 0xffbbbbbb;
	
	private String currentTheme = "therma";
	private Document doc;
	
	public Twix_AgentTheme(Context c)
		{
		try
			{
			InputStream in = c.getAssets().open("themes.xml");
			
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder docb = dbf.newDocumentBuilder();
			doc = docb.parse(in);
			doc.normalize();
			parseTheme();
			}
		catch ( Exception e )
			{
			// TODO Auto-generated catch block
			e.printStackTrace();
			}
		}

	public void setTheme(String s)
		{
		currentTheme = s;
		parseTheme();
		}
	
	private void parseTheme()
		{
		List<String> colHeader = new ArrayList<String>();

		colHeader.add("tableBG");
		colHeader.add("headerText");
		colHeader.add("headerValue");
		colHeader.add("headerSize");
		colHeader.add("headerSizeLarge");
		colHeader.add("headerBG");
		colHeader.add("sub1Header");
		colHeader.add("sub1Value");
		colHeader.add("sub1BG");
		colHeader.add("sub2BG");
		colHeader.add("subSize");
		colHeader.add("editBG");
		colHeader.add("lineColor");
		colHeader.add("sortDesc");
		colHeader.add("sortAsc");
		colHeader.add("sortNone");
		colHeader.add("warnColor");
		colHeader.add("warnColorLight");

		int curSize = colHeader.size();

		NodeList nodeList = doc.getElementsByTagName(currentTheme);
		int size = nodeList.getLength();

		Element valueElement;
		NodeList eleList;
		Element fstElmnt;
		int value = 0;
		
		for (int i = 0; i < size; i++) // Row Loop
			{
			Node node = nodeList.item(i);
			fstElmnt = (Element) node;

			for (int j = 0; j < curSize; j++)
				{
				eleList = fstElmnt.getElementsByTagName( (colHeader.get(j)) );

				valueElement = (Element) eleList.item(0);
				eleList = valueElement.getChildNodes();
				
				switch(j)
					{
					case 3:
					case 4:
						value = Integer.parseInt( ((Node) eleList.item(0)).getNodeValue() );
						break;
					case 10:
						value = Integer.parseInt( ((Node) eleList.item(0)).getNodeValue() );
						break;
					default:
						value = Long.decode(((Node) eleList.item(0)).getNodeValue()).intValue();
						break;
					}
				
				
				switch (j)
					{
					case 0:
						tableBG			= value;
						break;
					case 1:
						headerText		= value;
						break;
					case 2:
						headerValue		= value;
						break;
					case 3:
						headerSize 		= value;
						break;
					case 4:
						headerSizeLarge = value;
						break;
					case 5:
						headerBG		= value;
						break;
					case 6:
						sub1Header		= value;
						break;
					case 7:
						sub1Value		= value;
						break;
					case 8:
						sub1BG			= value;
						break;
					case 9:
						sub2BG			= value;
						break;
					case 10:
						subSize			= value;
						break;
					case 11:
						editBG			= value;
						break;
					case 12:
						lineColor		= value;
						break;
					case 13:
						sortDesc		= value;
						break;
					case 14:
						sortAsc			= value;
						break;
					case 15:
						sortNone		= value;
						break;
					case 16:
						warnColor		= value;
						break;
					case 17:
						warnColorLight	= value;
						break;
					}
				}
			}
		}
	}
