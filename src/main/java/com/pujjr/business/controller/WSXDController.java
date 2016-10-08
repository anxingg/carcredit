package com.pujjr.business.controller;

import org.apache.commons.io.FileUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.pujjr.business.domain.LeasingApp;
import com.pujjr.business.service.WSSqlserverService;
import com.pujjr.common.CellStyleCache;
import com.pujjr.common.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

@RestController
public class WSXDController extends BaseController
{
	@Autowired
	private WSSqlserverService wssqlServ;

	private CellStyleCache cellStyleCache;
	@RequestMapping("/api/queryWsxdIncaseList")
	public List<HashMap<String,Object>> queryWsxdIncaseList(String sqfqrq) throws Exception
	{
		if(sqfqrq!=null && sqfqrq!="")
		{
			sqfqrq = sqfqrq.replace("Z", " UTC");//ע���ǿո�+UTC
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS Z");//ע���ʽ���ı��ʽ
			sqfqrq=Utils.getFormatDate(format.parse(sqfqrq), "yyyy-MM-dd");
		}
		return wssqlServ.getInCaseList(sqfqrq);
	}
	
	@RequestMapping("/api/queryChargeBackList")
	public List<HashMap<String,Object>> queryChargeBackList() throws Exception
	{
		return wssqlServ.getChargeBackList();
	}
	@RequestMapping("/api/queryCustRepay")
	public List<HashMap<String,Object>> queryCustRepay(String qyrq) throws Exception 
	{
		if(qyrq!=null && qyrq!="")
		{
			qyrq = qyrq.replace("Z", " UTC");//ע���ǿո�+UTC
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS Z");//ע���ʽ���ı��ʽ
			qyrq=Utils.getFormatDate(format.parse(qyrq), "yyyy-MM-dd");
		}
		return wssqlServ.getCustRepayList(qyrq);
	}
	private void  writeCellValue(Workbook wb,Sheet sheet,Row row,int colNum,String value,Font font,short color,short align)
	{
		Cell cell=row.createCell(colNum);
		cell.setCellStyle(cellStyleCache.getCellStyle(align, color, false,false));
		cell.setCellValue(value);
	}
	private void  writeCellAmount(Workbook wb,Sheet sheet,Row row,int colNum,String value,Font font,short color,short align)
	{
		Cell cell=row.createCell(colNum);
		cell.setCellValue(Double.valueOf(value.replaceAll(",","")));
		cell.setCellStyle(cellStyleCache.getCellStyle(align, color, false,true));		
	}
	@RequestMapping("/api/chargeback/export")
	public ResponseEntity<byte[]> exportKhhzhzb(HttpSession session) throws ParseException, IOException
	{
		List<HashMap<String,Object>> list=wssqlServ.getChargeBackList();
		
		Workbook wb=new HSSFWorkbook();
		Sheet sheet=wb.createSheet();
		cellStyleCache=new CellStyleCache(wb);
		Font font=wb.createFont();
		font.setFontHeightInPoints((short)10);
		font.setBoldweight(XSSFFont.BOLDWEIGHT_BOLD);
		
		sheet.setColumnWidth(0, 2000);
		sheet.setColumnWidth(1, 3000);
		sheet.setColumnWidth(2, 5000);
		sheet.setColumnWidth(3, 12000);
		sheet.setColumnWidth(4, 2000);
		sheet.setColumnWidth(5, 5000);
		sheet.setColumnWidth(6, 6000);
		sheet.setColumnWidth(7, 8000);
		
		Row row=sheet.createRow(0);
		writeCellValue(wb,sheet,row,0,"���",font,IndexedColors.GREY_25_PERCENT.getIndex(),CellStyle.ALIGN_CENTER);
		writeCellValue(wb,sheet,row,1,"������",font,IndexedColors.GREY_25_PERCENT.getIndex(),CellStyle.ALIGN_CENTER);
		writeCellValue(wb,sheet,row,2,"�ͻ�����",font,IndexedColors.GREY_25_PERCENT.getIndex(),CellStyle.ALIGN_CENTER);
		writeCellValue(wb,sheet,row,3,"����",font,IndexedColors.GREY_25_PERCENT.getIndex(),CellStyle.ALIGN_CENTER);
		writeCellValue(wb,sheet,row,4,"��Ϣ",font,IndexedColors.GREY_25_PERCENT.getIndex(),CellStyle.ALIGN_CENTER);
		writeCellValue(wb,sheet,row,5,"��Ϣ",font,IndexedColors.GREY_25_PERCENT.getIndex(),CellStyle.ALIGN_CENTER);
		writeCellValue(wb,sheet,row,6,"�ۿ��ܶ�",font,IndexedColors.GREY_25_PERCENT.getIndex(),CellStyle.ALIGN_CENTER);
		writeCellValue(wb,sheet,row,7,"ʣ�౾��",font,IndexedColors.GREY_25_PERCENT.getIndex(),CellStyle.ALIGN_CENTER);
		Font fontNormal=wb.createFont();		
		fontNormal.setFontHeightInPoints((short)10);
		
		for(int i=0;i<list.size();i++)
		{
			row=sheet.createRow(i+1);
			Map item=list.get(i);
			String seq=String.valueOf(i+1);
			String id=item.containsKey("appid")?item.get("appid").toString():"";
			String name=item.containsKey("name")?item.get("name").toString():"";
			BigDecimal bj=(BigDecimal) (item.containsKey("bj")?(BigDecimal)item.get("bj"):0);
			BigDecimal lx=(BigDecimal) (item.containsKey("lx")?(BigDecimal)item.get("lx"):0);
			BigDecimal fx=(BigDecimal) (item.containsKey("fx")?(BigDecimal)item.get("fx"):0);
			BigDecimal total=(BigDecimal) (item.containsKey("total")?(BigDecimal)item.get("total"):0);
			BigDecimal sybj=(BigDecimal) (item.containsKey("sybj")?(BigDecimal)item.get("sybj"):0);
			
			
			DecimalFormat df=new DecimalFormat("#,###,###,###,##0.00");
			DecimalFormat df1=new DecimalFormat("#.000000");
			writeCellValue(wb,sheet,row,0,seq,fontNormal,IndexedColors.WHITE.getIndex(),CellStyle.ALIGN_CENTER);
			writeCellValue(wb,sheet,row,1,id,fontNormal,IndexedColors.WHITE.getIndex(),CellStyle.ALIGN_CENTER);
			writeCellValue(wb,sheet,row,2,name,fontNormal,IndexedColors.WHITE.getIndex(),CellStyle.ALIGN_CENTER);
			writeCellAmount(wb,sheet,row,3,df.format(bj.doubleValue()),fontNormal,IndexedColors.WHITE.getIndex(),CellStyle.ALIGN_RIGHT);
			writeCellAmount(wb,sheet,row,4,df.format(lx.doubleValue()),fontNormal,IndexedColors.WHITE.getIndex(),CellStyle.ALIGN_RIGHT);
			writeCellAmount(wb,sheet,row,5,df.format(fx.doubleValue()),fontNormal,IndexedColors.WHITE.getIndex(),CellStyle.ALIGN_RIGHT);
			writeCellAmount(wb,sheet,row,6,df.format(total.doubleValue()),fontNormal,IndexedColors.WHITE.getIndex(),CellStyle.ALIGN_RIGHT);
			writeCellAmount(wb,sheet,row,7,df.format(sybj.doubleValue()),fontNormal,IndexedColors.WHITE.getIndex(),CellStyle.ALIGN_RIGHT);	
		}
		
		String fileName=Utils.get16UUID();
		String filePath=session.getServletContext().getRealPath("/")+"download"+File.separator+fileName+".xls";
		FileOutputStream fileOut = new FileOutputStream(filePath);   
		wb.write(fileOut);
		HttpHeaders headers = new HttpHeaders();  
	    headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);  
	    headers.setContentDispositionFormData("attachment", new String("���뵥��ϸ".getBytes("GB2312"), "ISO_8859_1")+".xls");  
	    return new ResponseEntity<byte[]>(FileUtils.readFileToByteArray(new File(filePath)),  
	                                      headers, HttpStatus.OK);  
		
	}
	
	@RequestMapping("/api/incase/export")
	public ResponseEntity<byte[]> exportIncase(HttpSession session) throws ParseException, IOException
	{
		List<HashMap<String,Object>> list=wssqlServ.getChargeBackList();
		
		Workbook wb=new HSSFWorkbook();
		Sheet sheet=wb.createSheet();
		cellStyleCache=new CellStyleCache(wb);
		Font font=wb.createFont();
		font.setFontHeightInPoints((short)10);
		font.setBoldweight(XSSFFont.BOLDWEIGHT_BOLD);
		
		sheet.setColumnWidth(0, 2000);
		sheet.setColumnWidth(1, 10000);
		sheet.setColumnWidth(2, 5000);
		sheet.setColumnWidth(3, 8000);
		sheet.setColumnWidth(4, 5000);
		sheet.setColumnWidth(5, 5000);
		sheet.setColumnWidth(6, 6000);
		sheet.setColumnWidth(7, 6000);
		
		Row row=sheet.createRow(0);
		writeCellValue(wb,sheet,row,0,"���",font,IndexedColors.GREY_25_PERCENT.getIndex(),CellStyle.ALIGN_CENTER);
		writeCellValue(wb,sheet,row,1,"������",font,IndexedColors.GREY_25_PERCENT.getIndex(),CellStyle.ALIGN_CENTER);
		writeCellValue(wb,sheet,row,2,"����",font,IndexedColors.GREY_25_PERCENT.getIndex(),CellStyle.ALIGN_CENTER);
		writeCellValue(wb,sheet,row,3,"����",font,IndexedColors.GREY_25_PERCENT.getIndex(),CellStyle.ALIGN_CENTER);
		writeCellValue(wb,sheet,row,4,"��Ϣ",font,IndexedColors.GREY_25_PERCENT.getIndex(),CellStyle.ALIGN_CENTER);
		writeCellValue(wb,sheet,row,5,"��Ϣ",font,IndexedColors.GREY_25_PERCENT.getIndex(),CellStyle.ALIGN_CENTER);
		writeCellValue(wb,sheet,row,6,"�ۿ�ϼ�",font,IndexedColors.GREY_25_PERCENT.getIndex(),CellStyle.ALIGN_CENTER);
		writeCellValue(wb,sheet,row,7,"ʣ�౾��",font,IndexedColors.GREY_25_PERCENT.getIndex(),CellStyle.ALIGN_CENTER);
		Font fontNormal=wb.createFont();		
		fontNormal.setFontHeightInPoints((short)10);
		
		for(int i=0;i<list.size();i++)
		{
			row=sheet.createRow(i+1);
			Map item=list.get(i);
			String seq=String.valueOf(i+1);
			String id=item.containsKey("appid")?item.get("appid").toString():"";
			String name=item.containsKey("name")?item.get("name").toString():"";
			BigDecimal bj=(BigDecimal) (item.containsKey("bj")?(BigDecimal)item.get("bj"):0);
			BigDecimal lx=(BigDecimal) (item.containsKey("lx")?(BigDecimal)item.get("lx"):0);
			BigDecimal fx=(BigDecimal) (item.containsKey("fx")?(BigDecimal)item.get("fx"):0);
			BigDecimal total=(BigDecimal) (item.containsKey("total")?(BigDecimal)item.get("total"):0);
			BigDecimal sybj=(BigDecimal) (item.containsKey("sybj")?(BigDecimal)item.get("sybj"):0);
			
			
			DecimalFormat df=new DecimalFormat("#,###,###,###,##0.00");
			DecimalFormat df1=new DecimalFormat("#.000000");
			writeCellValue(wb,sheet,row,0,seq,fontNormal,IndexedColors.WHITE.getIndex(),CellStyle.ALIGN_CENTER);
			writeCellValue(wb,sheet,row,1,id,fontNormal,IndexedColors.WHITE.getIndex(),CellStyle.ALIGN_CENTER);
			writeCellValue(wb,sheet,row,2,name,fontNormal,IndexedColors.WHITE.getIndex(),CellStyle.ALIGN_CENTER);
			writeCellAmount(wb,sheet,row,3,df.format(bj.doubleValue()),fontNormal,IndexedColors.WHITE.getIndex(),CellStyle.ALIGN_RIGHT);
			writeCellAmount(wb,sheet,row,4,df.format(lx.doubleValue()),fontNormal,IndexedColors.WHITE.getIndex(),CellStyle.ALIGN_RIGHT);
			writeCellAmount(wb,sheet,row,5,df.format(fx.doubleValue()),fontNormal,IndexedColors.WHITE.getIndex(),CellStyle.ALIGN_RIGHT);
			writeCellAmount(wb,sheet,row,6,df.format(total.doubleValue()),fontNormal,IndexedColors.WHITE.getIndex(),CellStyle.ALIGN_RIGHT);
			writeCellAmount(wb,sheet,row,7,df.format(sybj.doubleValue()),fontNormal,IndexedColors.WHITE.getIndex(),CellStyle.ALIGN_RIGHT);	
		}
		
		String fileName=Utils.get16UUID();
		String filePath=session.getServletContext().getRealPath("/")+"download"+File.separator+fileName+".xls";
		FileOutputStream fileOut = new FileOutputStream(filePath);   
		wb.write(fileOut);
		HttpHeaders headers = new HttpHeaders();  
	    headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);  
	    headers.setContentDispositionFormData("attachment", new String("���뵥��ϸ".getBytes("GB2312"), "ISO_8859_1")+".xls");  
	    return new ResponseEntity<byte[]>(FileUtils.readFileToByteArray(new File(filePath)),  
	                                      headers, HttpStatus.OK);  
		
	}
	
	
	@RequestMapping("/api/creditdata/export")
	public ResponseEntity<byte[]> exportCreditData( String sqfqrq, HttpSession session) throws Exception
	{
		if(sqfqrq!=null && sqfqrq!="")
		{
			sqfqrq = sqfqrq.replace("Z", " UTC");//ע���ǿո�+UTC
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS Z");//ע���ʽ���ı��ʽ
			sqfqrq=Utils.getFormatDate(format.parse(sqfqrq), "yyyy-MM-dd");
		}
		
		List<HashMap<String,Object>> list=wssqlServ.getCreditDataList(sqfqrq);
		
		Workbook wb=new HSSFWorkbook();
		Sheet sheet=wb.createSheet();
		cellStyleCache=new CellStyleCache(wb);
		Font font=wb.createFont();
		font.setFontHeightInPoints((short)10);
		font.setBoldweight(XSSFFont.BOLDWEIGHT_BOLD);
		
		sheet.setColumnWidth(0, 5000);
		sheet.setColumnWidth(1, 3000);
		sheet.setColumnWidth(2, 3000);
		sheet.setColumnWidth(3, 5500);
		sheet.setColumnWidth(4, 5000);
		sheet.setColumnWidth(5, 5000);
		sheet.setColumnWidth(6, 15000);
		sheet.setColumnWidth(7, 8000);
		sheet.setColumnWidth(8, 5000);
		sheet.setColumnWidth(9, 15000);
		sheet.setColumnWidth(10, 2500);
		sheet.setColumnWidth(11, 4000);
		sheet.setColumnWidth(12, 4000);
		
		Row row=sheet.createRow(0);
		writeCellValue(wb,sheet,row,0,"����ʱ��",font,IndexedColors.GREY_25_PERCENT.getIndex(),CellStyle.ALIGN_CENTER);
		writeCellValue(wb,sheet,row,1,"����",font,IndexedColors.GREY_25_PERCENT.getIndex(),CellStyle.ALIGN_CENTER);
		writeCellValue(wb,sheet,row,2,"�ͻ��ȼ�",font,IndexedColors.GREY_25_PERCENT.getIndex(),CellStyle.ALIGN_CENTER);
		writeCellValue(wb,sheet,row,3,"���֤����",font,IndexedColors.GREY_25_PERCENT.getIndex(),CellStyle.ALIGN_CENTER);
		writeCellValue(wb,sheet,row,4,"�ֻ�����",font,IndexedColors.GREY_25_PERCENT.getIndex(),CellStyle.ALIGN_CENTER);
		writeCellValue(wb,sheet,row,5,"QQ����",font,IndexedColors.GREY_25_PERCENT.getIndex(),CellStyle.ALIGN_CENTER);
		writeCellValue(wb,sheet,row,6,"��סַ",font,IndexedColors.GREY_25_PERCENT.getIndex(),CellStyle.ALIGN_CENTER);
		writeCellValue(wb,sheet,row,7,"��˾����",font,IndexedColors.GREY_25_PERCENT.getIndex(),CellStyle.ALIGN_CENTER);
		writeCellValue(wb,sheet,row,8,"��˾�绰",font,IndexedColors.GREY_25_PERCENT.getIndex(),CellStyle.ALIGN_CENTER);
		writeCellValue(wb,sheet,row,9,"��˾��ַ",font,IndexedColors.GREY_25_PERCENT.getIndex(),CellStyle.ALIGN_CENTER);
		writeCellValue(wb,sheet,row,10,"�׸�����",font,IndexedColors.GREY_25_PERCENT.getIndex(),CellStyle.ALIGN_CENTER);
		writeCellValue(wb,sheet,row,11,"���ʽ��",font,IndexedColors.GREY_25_PERCENT.getIndex(),CellStyle.ALIGN_CENTER);
		writeCellValue(wb,sheet,row,12,"����˰",font,IndexedColors.GREY_25_PERCENT.getIndex(),CellStyle.ALIGN_CENTER);
		
		Font fontNormal=wb.createFont();		
		fontNormal.setFontHeightInPoints((short)10);
		
		for(int i=0;i<list.size();i++)
		{
			row=sheet.createRow(i+1);
			HashMap<String,Object> item=list.get(i);
			String sqfqsj = item.containsKey("sqfqsj")?item.get("sqfqsj").toString():"";
			String appid = item.containsKey("appid")?item.get("appid").toString():"";
			String name = item.containsKey("name")?item.get("name").toString():"";
			String idno = item.containsKey("idno")?item.get("idno").toString():"";
			String mobile = item.containsKey("mobile")?item.get("mobile").toString():"";
			String qq = item.containsKey("qq")?item.get("qq").toString():"";
			String address = item.containsKey("address")?item.get("address").toString():"";
			String dwmc = item.containsKey("dwmc")?item.get("dwmc").toString():"";
			String dwdh = item.containsKey("dwdh")?item.get("dwdh").toString():"";
			String dwdz = item.containsKey("dwdz")?item.get("dwdz").toString():"";
			BigDecimal sfbl=(BigDecimal) (item.containsKey("sfbl")?(BigDecimal)item.get("sfbl"):0);
			BigDecimal rzje=(BigDecimal) (item.containsKey("rzje")?(BigDecimal)item.get("rzje"):0);
			BigDecimal gzs=(BigDecimal) (item.containsKey("gzs")?(BigDecimal)item.get("gzs"):0);
			writeCellValue(wb,sheet,row,0,sqfqsj,fontNormal,IndexedColors.WHITE.getIndex(),CellStyle.ALIGN_LEFT);
			writeCellValue(wb,sheet,row,1,name,fontNormal,IndexedColors.WHITE.getIndex(),CellStyle.ALIGN_CENTER);
			//writeCellValue(wb,sheet,row,2,name,fontNormal,IndexedColors.WHITE.getIndex(),CellStyle.ALIGN_CENTER);
			writeCellValue(wb,sheet,row,3,idno,fontNormal,IndexedColors.WHITE.getIndex(),CellStyle.ALIGN_CENTER);
			writeCellValue(wb,sheet,row,4,mobile,fontNormal,IndexedColors.WHITE.getIndex(),CellStyle.ALIGN_CENTER);
			writeCellValue(wb,sheet,row,5,qq,fontNormal,IndexedColors.WHITE.getIndex(),CellStyle.ALIGN_CENTER);
			writeCellValue(wb,sheet,row,6,address,fontNormal,IndexedColors.WHITE.getIndex(),CellStyle.ALIGN_LEFT);
			writeCellValue(wb,sheet,row,7,dwmc,fontNormal,IndexedColors.WHITE.getIndex(),CellStyle.ALIGN_LEFT);
			writeCellValue(wb,sheet,row,8,dwdh,fontNormal,IndexedColors.WHITE.getIndex(),CellStyle.ALIGN_CENTER);
			writeCellValue(wb,sheet,row,9,dwdz,fontNormal,IndexedColors.WHITE.getIndex(),CellStyle.ALIGN_LEFT);
			DecimalFormat df=new DecimalFormat("#,###,###,###,##0.00");
			writeCellAmount(wb,sheet,row,10,df.format(sfbl.doubleValue()),fontNormal,IndexedColors.WHITE.getIndex(),CellStyle.ALIGN_LEFT);
			writeCellValue(wb,sheet,row,11,df.format(rzje.doubleValue()),fontNormal,IndexedColors.WHITE.getIndex(),CellStyle.ALIGN_RIGHT);
			writeCellValue(wb,sheet,row,12,df.format(gzs.doubleValue()),fontNormal,IndexedColors.WHITE.getIndex(),CellStyle.ALIGN_RIGHT);	
		}
		
		String fileName=Utils.get16UUID();
		String filePath=session.getServletContext().getRealPath("/")+"download"+File.separator+fileName+".xls";
		FileOutputStream fileOut = new FileOutputStream(filePath);   
		wb.write(fileOut);
		HttpHeaders headers = new HttpHeaders();  
	    headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);  
	    headers.setContentDispositionFormData("attachment", new String("���뵥��ϸ".getBytes("GB2312"), "ISO_8859_1")+".xls");  
	    return new ResponseEntity<byte[]>(FileUtils.readFileToByteArray(new File(filePath)),  
	                                      headers, HttpStatus.OK);  
		
	}
	
	@RequestMapping("/api/fkmxdata/export")
	public ResponseEntity<byte[]> exportFkmxData( String fkrq, HttpSession session) throws Exception
	{
		if(fkrq!=null && fkrq!="")
		{
			fkrq = fkrq.replace("Z", " UTC");//ע���ǿո�+UTC
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS Z");//ע���ʽ���ı��ʽ
			fkrq=Utils.getFormatDate(format.parse(fkrq), "yyyy-MM-dd");
		}
		
		List<HashMap<String,Object>> list=wssqlServ.getFkmxList(fkrq);
		
		Workbook wb=new HSSFWorkbook();
		Sheet sheet=wb.createSheet();
		cellStyleCache=new CellStyleCache(wb);
		Font font=wb.createFont();
		font.setFontHeightInPoints((short)10);
		font.setBoldweight(XSSFFont.BOLDWEIGHT_BOLD);
		
		sheet.setColumnWidth(0, 5000);
		sheet.setColumnWidth(1, 3000);
		sheet.setColumnWidth(2, 3000);
		sheet.setColumnWidth(3, 5500);
		sheet.setColumnWidth(4, 5000);
		sheet.setColumnWidth(5, 5000);
		sheet.setColumnWidth(6, 15000);
		sheet.setColumnWidth(7, 8000);
		sheet.setColumnWidth(8, 5000);
		sheet.setColumnWidth(9, 15000);
		sheet.setColumnWidth(10, 2500);
		sheet.setColumnWidth(11, 4000);
		sheet.setColumnWidth(12, 4000);
		sheet.setColumnWidth(13, 4000);
		sheet.setColumnWidth(14, 4000);
		sheet.setColumnWidth(15, 4000);
		Row row=sheet.createRow(0);
		writeCellValue(wb,sheet,row,0,"����ID",font,IndexedColors.GREY_25_PERCENT.getIndex(),CellStyle.ALIGN_CENTER);
		writeCellValue(wb,sheet,row,1,"����",font,IndexedColors.GREY_25_PERCENT.getIndex(),CellStyle.ALIGN_CENTER);
		writeCellValue(wb,sheet,row,2,"���֤��",font,IndexedColors.GREY_25_PERCENT.getIndex(),CellStyle.ALIGN_CENTER);
		writeCellValue(wb,sheet,row,3,"�㳵��",font,IndexedColors.GREY_25_PERCENT.getIndex(),CellStyle.ALIGN_CENTER);
		writeCellValue(wb,sheet,row,4,"���շ�",font,IndexedColors.GREY_25_PERCENT.getIndex(),CellStyle.ALIGN_CENTER);
		writeCellValue(wb,sheet,row,5,"����������",font,IndexedColors.GREY_25_PERCENT.getIndex(),CellStyle.ALIGN_CENTER);
		writeCellValue(wb,sheet,row,6,"������",font,IndexedColors.GREY_25_PERCENT.getIndex(),CellStyle.ALIGN_CENTER);
		writeCellValue(wb,sheet,row,7,"GPS��",font,IndexedColors.GREY_25_PERCENT.getIndex(),CellStyle.ALIGN_CENTER);
		writeCellValue(wb,sheet,row,8,"�����",font,IndexedColors.GREY_25_PERCENT.getIndex(),CellStyle.ALIGN_CENTER);
		writeCellValue(wb,sheet,row,9,"��ͬ���",font,IndexedColors.GREY_25_PERCENT.getIndex(),CellStyle.ALIGN_CENTER);
		writeCellValue(wb,sheet,row,10,"����",font,IndexedColors.GREY_25_PERCENT.getIndex(),CellStyle.ALIGN_CENTER);
		writeCellValue(wb,sheet,row,11,"����",font,IndexedColors.GREY_25_PERCENT.getIndex(),CellStyle.ALIGN_CENTER);
		writeCellValue(wb,sheet,row,12,"�¹�",font,IndexedColors.GREY_25_PERCENT.getIndex(),CellStyle.ALIGN_CENTER);
		writeCellValue(wb,sheet,row,13,"��Ʒ����",font,IndexedColors.GREY_25_PERCENT.getIndex(),CellStyle.ALIGN_CENTER);
		writeCellValue(wb,sheet,row,14,"�׸�����",font,IndexedColors.GREY_25_PERCENT.getIndex(),CellStyle.ALIGN_CENTER);
		writeCellValue(wb,sheet,row,15,"�ֻ�����",font,IndexedColors.GREY_25_PERCENT.getIndex(),CellStyle.ALIGN_CENTER);
		
		Font fontNormal=wb.createFont();		
		fontNormal.setFontHeightInPoints((short)10);
		
		for(int i=0;i<list.size();i++)
		{
			row=sheet.createRow(i+1);
			HashMap<String,Object> item=list.get(i);
			String appid = item.containsKey("appid")?item.get("appid").toString():"";
			String name = item.containsKey("name")?item.get("name").toString():"";
			String idno = item.containsKey("idno")?item.get("idno").toString():"";
			String productName = item.containsKey("productName")?item.get("productName").toString():"";
			
			BigDecimal lcj=(BigDecimal) (item.containsKey("lcj")?(BigDecimal)item.get("lcj"):BigDecimal.valueOf(0.00));
			BigDecimal bxf=(BigDecimal) (item.containsKey("bxf")?(BigDecimal)item.get("bxf"):BigDecimal.valueOf(0.00));
			BigDecimal gzs=(BigDecimal) (item.containsKey("gzs")?(BigDecimal)item.get("gzs"):BigDecimal.valueOf(0.00));
			BigDecimal pgj=(BigDecimal) (item.containsKey("pgj")?(BigDecimal)item.get("pgj"):BigDecimal.valueOf(0.00));
			BigDecimal gpsfee=(BigDecimal) (item.containsKey("gpsfee")?(BigDecimal)item.get("gpsfee"):BigDecimal.valueOf(0.00));
			BigDecimal fwf=(BigDecimal) (item.containsKey("fwf")?(BigDecimal)item.get("fwf"):BigDecimal.valueOf(0.00));
			BigDecimal htje=(BigDecimal) (item.containsKey("htje")?(BigDecimal)item.get("htje"):BigDecimal.valueOf(0.00));
			int qs = (Integer)(item.containsKey("qs")?item.get("qs"):0);
			BigDecimal lv=(BigDecimal) (item.containsKey("lv")?(BigDecimal)item.get("lv"):BigDecimal.valueOf(0.00));
			BigDecimal sfbl=(BigDecimal) (item.containsKey("sfbl")?(BigDecimal)item.get("sfbl"):BigDecimal.valueOf(0.00));
			BigDecimal ygk=(BigDecimal) (item.containsKey("ygk")?(BigDecimal)item.get("ygk"):BigDecimal.valueOf(0.00));
			String mobile = item.containsKey("mobile")?item.get("mobile").toString():"";
			writeCellValue(wb,sheet,row,0,appid,fontNormal,IndexedColors.WHITE.getIndex(),CellStyle.ALIGN_LEFT);
			writeCellValue(wb,sheet,row,1,name,fontNormal,IndexedColors.WHITE.getIndex(),CellStyle.ALIGN_CENTER);
			writeCellValue(wb,sheet,row,2,idno,fontNormal,IndexedColors.WHITE.getIndex(),CellStyle.ALIGN_CENTER);
			
			DecimalFormat df=new DecimalFormat("#,###,###,###,##0.0000");
			
			writeCellAmount(wb,sheet,row,3,df.format(lcj.doubleValue()),fontNormal,IndexedColors.WHITE.getIndex(),CellStyle.ALIGN_LEFT);
			writeCellAmount(wb,sheet,row,4,df.format(bxf.doubleValue()),fontNormal,IndexedColors.WHITE.getIndex(),CellStyle.ALIGN_RIGHT);
			writeCellAmount(wb,sheet,row,5,df.format(gzs.doubleValue()),fontNormal,IndexedColors.WHITE.getIndex(),CellStyle.ALIGN_RIGHT);	
			writeCellAmount(wb,sheet,row,6,df.format(pgj.doubleValue()),fontNormal,IndexedColors.WHITE.getIndex(),CellStyle.ALIGN_RIGHT);	
			writeCellAmount(wb,sheet,row,7,df.format(gpsfee.doubleValue()),fontNormal,IndexedColors.WHITE.getIndex(),CellStyle.ALIGN_LEFT);
			writeCellAmount(wb,sheet,row,8,df.format(fwf.doubleValue()),fontNormal,IndexedColors.WHITE.getIndex(),CellStyle.ALIGN_RIGHT);
			writeCellAmount(wb,sheet,row,9,df.format(htje.doubleValue()),fontNormal,IndexedColors.WHITE.getIndex(),CellStyle.ALIGN_RIGHT);	
			writeCellValue(wb,sheet,row,10,String.valueOf(qs),fontNormal,IndexedColors.WHITE.getIndex(),CellStyle.ALIGN_CENTER);
			writeCellAmount(wb,sheet,row,11,df.format(lv.doubleValue()),fontNormal,IndexedColors.WHITE.getIndex(),CellStyle.ALIGN_RIGHT);
			writeCellAmount(wb,sheet,row,12,df.format(ygk.doubleValue()),fontNormal,IndexedColors.WHITE.getIndex(),CellStyle.ALIGN_RIGHT);
			writeCellValue(wb,sheet,row,13,productName,fontNormal,IndexedColors.WHITE.getIndex(),CellStyle.ALIGN_CENTER);
			writeCellAmount(wb,sheet,row,14,df.format(sfbl.doubleValue()),fontNormal,IndexedColors.WHITE.getIndex(),CellStyle.ALIGN_RIGHT);
			writeCellValue(wb,sheet,row,15,mobile,fontNormal,IndexedColors.WHITE.getIndex(),CellStyle.ALIGN_CENTER);
		}
		
		String fileName=Utils.get16UUID();
		String filePath=session.getServletContext().getRealPath("/")+"download"+File.separator+fileName+".xls";
		FileOutputStream fileOut = new FileOutputStream(filePath);   
		wb.write(fileOut);
		HttpHeaders headers = new HttpHeaders();  
	    headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);  
	    headers.setContentDispositionFormData("attachment", new String("���뵥��ϸ".getBytes("GB2312"), "ISO_8859_1")+".xls");  
	    return new ResponseEntity<byte[]>(FileUtils.readFileToByteArray(new File(filePath)),  
	                                      headers, HttpStatus.OK);  
		
	}
	
}
