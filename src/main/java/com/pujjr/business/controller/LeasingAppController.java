package com.pujjr.business.controller;

import io.jsonwebtoken.Claims;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pujjr.business.domain.LeasingApp;
import com.pujjr.business.domain.LeasingApprove;
import com.pujjr.business.domain.LeasingCheck;
import com.pujjr.business.domain.SysAccount;
import com.pujjr.business.service.LeasingAppService;
import com.pujjr.business.service.LeasingApproveService;
import com.pujjr.business.service.LeasingCheckService;
import com.pujjr.business.service.SequenceService;
import com.pujjr.business.service.SysAccountService;
import com.pujjr.business.vo.AppData;
import com.pujjr.business.vo.AppManageDetail;
import com.pujjr.business.vo.PageVo;
import com.pujjr.common.CellStyleCache;
import com.pujjr.common.Utils;

@Controller
@RequestMapping("/api/leasingapps")
public class LeasingAppController extends BaseController 
{
	@Autowired
	private LeasingAppService leasingAppServ;
	@Autowired
	private SysAccountService sysAccountService;
	@Autowired
	private LeasingCheckService leasingCheckServ;
	@Autowired
	private LeasingApproveService leasingApproveServ;
	@Autowired
	private SequenceService seqService;
	
	private CellStyleCache cellStyleCache;
	
	@RequestMapping(value="/{id}", method=RequestMethod.GET)
	@ResponseBody
	public LeasingApp get(@PathVariable String id)
	{
		return leasingAppServ.get(id);
	}
	@RequestMapping(value="/{id}",method=RequestMethod.PUT)
	@ResponseBody
	public void update(@RequestBody LeasingApp record)
	{
		//���Ϊ�������������뷢��ʱ��
		if(record.getSqdzt().equals("�����"))
		{
			record.setSqfqsj(new Timestamp(new Date().getTime()));
		}
		if(record.getSqdzt().equals("������"))
		{
			record.setReserver7(Utils.getCurrentTime("yyyy-MM-dd HH:mm:ss"));
		}
		leasingAppServ.update(record);
	}
	@RequestMapping(method=RequestMethod.POST)
	@ResponseBody
	public void add(@RequestBody LeasingApp record,HttpServletRequest request )
	{
		/*��ȡ�û���Ϣ*/
		Claims claims=(Claims)request.getAttribute("claims");
		String userid=claims.getSubject();
		SysAccount sysAccount=sysAccountService.get(userid);
		record.setReserver5(leasingAppServ.queryCustType(record));
		record.setSqdzt("δȷ��");
		record.setJxsid(sysAccount.getSysBranch().getId());
		record.setSqfqr(sysAccount.getId());
		record.setSqfqsj(new Timestamp(new Date().getTime()));
		leasingAppServ.add(record);
	}
	
	@RequestMapping(value="/{id}",method=RequestMethod.DELETE)
	@ResponseBody
	public void delete(@PathVariable String id)
	{
		leasingAppServ.delete(id);
	}
	@RequestMapping(method=RequestMethod.GET)
	@ResponseBody
	public PageVo getList(String sqdzt,
						  String id,
				     	  String name,
						  String sqfqrq,
						  String curPage ,
						  String pageSize) throws UnsupportedEncodingException, ParseException
	{
		sqdzt=Utils.convertStr2Utf8(sqdzt);
		name=Utils.convertStr2Utf8(name);
		if(sqfqrq!=null && sqfqrq!="")
		{
			sqfqrq = sqfqrq.replace("Z", " UTC");//ע���ǿո�+UTC
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS Z");//ע���ʽ���ı��ʽ
			sqfqrq=Utils.getFormatDate(format.parse(sqfqrq), "yyyy-MM-dd");
		}
		PageHelper.startPage(Integer.parseInt(curPage), Integer.parseInt(pageSize),true);
		List<LeasingApp> list=leasingAppServ.getList(id,name,sqfqrq,sqdzt);
		PageVo page=new PageVo();
		page.setTotalItem(((Page)list).getTotal());
		page.setData(list);
		return page;
	}
	@RequestMapping(value="/jxs",method=RequestMethod.GET)
	@ResponseBody
	public PageVo getJxsAppList(String sqdzt,
								String id,
								String name,
								String sqfqrq,
								String curPage ,
								String pageSize,
								HttpServletRequest request) throws UnsupportedEncodingException, ParseException
	{
		sqdzt=Utils.convertStr2Utf8(sqdzt);
		name=Utils.convertStr2Utf8(name);
		if(sqfqrq!=null && sqfqrq!="")
		{
			sqfqrq = sqfqrq.replace("Z", " UTC");//ע���ǿո�+UTC
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS Z");//ע���ʽ���ı��ʽ
			sqfqrq=Utils.getFormatDate(format.parse(sqfqrq), "yyyy-MM-dd");
		}
		
		Claims claims=(Claims)request.getAttribute("claims");
		String userid=claims.getSubject();
		SysAccount sysAccount=sysAccountService.get(userid);
		PageHelper.startPage(Integer.parseInt(curPage), Integer.parseInt(pageSize),true);
		List<LeasingApp> list=leasingAppServ.getJxsList(id,name,sqfqrq,sqdzt,sysAccount.getId());
		PageVo page=new PageVo();
		page.setTotalItem(((Page)list).getTotal());
		page.setData(list);
		return page;
	}
	@RequestMapping(value="/check",method=RequestMethod.GET)
	@ResponseBody
	public PageVo getCheckAppList(String sqdzt,
								  String id,
								  String name,
								  String sqfqrq,
								  String curPage ,
								  String pageSize,
								  HttpServletRequest request) throws UnsupportedEncodingException, ParseException
	{
		sqdzt=Utils.convertStr2Utf8(sqdzt);
		name=Utils.convertStr2Utf8(name);
		if(sqfqrq!=null && sqfqrq!="")
		{
			sqfqrq = sqfqrq.replace("Z", " UTC");//ע���ǿո�+UTC
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS Z");//ע���ʽ���ı��ʽ
			sqfqrq=Utils.getFormatDate(format.parse(sqfqrq), "yyyy-MM-dd");
		}
		Claims claims=(Claims)request.getAttribute("claims");
		String userid=claims.getSubject();
		SysAccount sysAccount=sysAccountService.get(userid);
		PageHelper.startPage(Integer.parseInt(curPage), Integer.parseInt(pageSize),true);
		List<LeasingApp> list=leasingAppServ.getCheckList(id,name,sqfqrq,sqdzt,sysAccount.getId());
		PageVo page=new PageVo();
		page.setTotalItem(((Page)list).getTotal());
		page.setData(list);
		return page;
	}
	
	@RequestMapping(value="/getId",method=RequestMethod.GET)
	@ResponseBody
	public HashMap<String,String> getAppId(HttpServletRequest request) throws Exception
	{
		//throw new Exception("���뵥��ŷ���������");
		
		Claims claims=(Claims)request.getAttribute("claims");
		String userid=claims.getSubject();
		SysAccount sysAccount=sysAccountService.get(userid);
		String seq=String.format("%04d", seqService.getNextVal("appid"));
		String appid=sysAccount.getBranchid()+"-"+Utils.getFormatDate(new Date(), "yyyyMMdd")+"-"+seq+"-";
		HashMap<String,String> map=new HashMap<String,String>();
		map.put("id", appid);
		return map;
	}
	
	@RequestMapping(value="/updateAllData",method=RequestMethod.POST)
	@ResponseBody
	public void updateAllData(@RequestBody AppData data,HttpServletRequest request )
	{
		leasingAppServ.updateAllAppData(data);
	}
	@RequestMapping(value="/managerAppDetail",method=RequestMethod.GET)
	@ResponseBody
	public PageVo queryManagerAppDetail(String sqdzt,
										String id,
										String name,
										String sqfqrq,
										String curPage,
										String pageSize) throws UnsupportedEncodingException, ParseException
	{
		sqdzt=Utils.convertStr2Utf8(sqdzt);
		name=Utils.convertStr2Utf8(name);
		if(sqfqrq!=null && sqfqrq!="")
		{
			sqfqrq = sqfqrq.replace("Z", " UTC");//ע���ǿո�+UTC
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS Z");//ע���ʽ���ı��ʽ
			sqfqrq=Utils.getFormatDate(format.parse(sqfqrq), "yyyy-MM-dd");
		}
		PageHelper.startPage(Integer.parseInt(curPage), Integer.parseInt(pageSize),true);
		List<AppManageDetail> list=leasingAppServ.selectManagerAppDetail(id,name,sqfqrq,sqdzt);
		PageVo page=new PageVo();
		page.setTotalItem(((Page)list).getTotal());
		page.setData(list);
		return page;
	}
	
	@RequestMapping("/downloadAppDetail")
	@ResponseBody
	public ResponseEntity<byte[]> downloadAppDetail(String sqdzt,
			  										String id,
			  										String name,
			  										String sqfqrq, HttpSession session) throws IOException, ParseException
	{
		sqdzt=Utils.convertStr2Utf8(sqdzt);
		name=Utils.convertStr2Utf8(name);
		if(sqfqrq!=null && sqfqrq!="")
		{
			sqfqrq = sqfqrq.replace("Z", " UTC");//ע���ǿո�+UTC
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS Z");//ע���ʽ���ı��ʽ
			sqfqrq=Utils.getFormatDate(format.parse(sqfqrq), "yyyy-MM-dd");
		}
		List<LeasingApp> list=leasingAppServ.getList(id,name,sqfqrq,sqdzt);
		
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
			LeasingApp item=list.get(i);
			writeCellValue(wb,sheet,row,0,item.getSqfqsj().toString(),fontNormal,IndexedColors.WHITE.getIndex(),CellStyle.ALIGN_LEFT);
			writeCellValue(wb,sheet,row,1,item.getName(),fontNormal,IndexedColors.WHITE.getIndex(),CellStyle.ALIGN_CENTER);
			writeCellValue(wb,sheet,row,2,item.getReserver5(),fontNormal,IndexedColors.WHITE.getIndex(),CellStyle.ALIGN_CENTER);
			writeCellValue(wb,sheet,row,3,item.getIdno(),fontNormal,IndexedColors.WHITE.getIndex(),CellStyle.ALIGN_CENTER);
			writeCellValue(wb,sheet,row,4,item.getMobile(),fontNormal,IndexedColors.WHITE.getIndex(),CellStyle.ALIGN_CENTER);
			writeCellValue(wb,sheet,row,5,item.getQq(),fontNormal,IndexedColors.WHITE.getIndex(),CellStyle.ALIGN_CENTER);
			writeCellValue(wb,sheet,row,6,item.getXxxdz(),fontNormal,IndexedColors.WHITE.getIndex(),CellStyle.ALIGN_LEFT);
			writeCellValue(wb,sheet,row,7,item.getCzr1dwmc(),fontNormal,IndexedColors.WHITE.getIndex(),CellStyle.ALIGN_LEFT);
			writeCellValue(wb,sheet,row,8,item.getCzr1dwdh(),fontNormal,IndexedColors.WHITE.getIndex(),CellStyle.ALIGN_CENTER);
			writeCellValue(wb,sheet,row,9,item.getCzr1dwdz(),fontNormal,IndexedColors.WHITE.getIndex(),CellStyle.ALIGN_LEFT);
			DecimalFormat df=new DecimalFormat("#,###,###,###,##0.00");
			double sfbl=Double.valueOf((item.getSfbl()==null)?"0.00":String.valueOf(item.getSfbl()));
			writeCellAmount(wb,sheet,row,10,df.format(sfbl),fontNormal,IndexedColors.WHITE.getIndex(),CellStyle.ALIGN_LEFT);
			double rzje=Double.valueOf((item.getRzje()==null)?"0.00":String.valueOf(item.getRzje()));
			writeCellValue(wb,sheet,row,11,df.format(rzje),fontNormal,IndexedColors.WHITE.getIndex(),CellStyle.ALIGN_RIGHT);
			double gzs=Double.valueOf((item.getGzs()==null)?"0.00":String.valueOf(item.getGzs()));
			writeCellValue(wb,sheet,row,12,df.format(gzs),fontNormal,IndexedColors.WHITE.getIndex(),CellStyle.ALIGN_RIGHT);	
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
	@RequestMapping("/exportInCase")
	@ResponseBody
	public ResponseEntity<byte[]> exportInCase(String sqdzt,
							 				   String id,
							 				   String name,
							 				   String sqfqrq, 
							 				   HttpSession session) throws ParseException, IOException
	{
		sqdzt=Utils.convertStr2Utf8(sqdzt);
		name=Utils.convertStr2Utf8(name);
		if(sqfqrq!=null && sqfqrq!="")
		{
			sqfqrq = sqfqrq.replace("Z", " UTC");//ע���ǿո�+UTC
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS Z");//ע���ʽ���ı��ʽ
			sqfqrq=Utils.getFormatDate(format.parse(sqfqrq), "yyyy-MM-dd");
		}
		List<Map> list=leasingAppServ.getInCaseList(id, name, sqfqrq, sqdzt);
		
		
		
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
		sheet.setColumnWidth(8, 5000);
		sheet.setColumnWidth(9, 5000);
		sheet.setColumnWidth(10, 5000);
		sheet.setColumnWidth(11, 5000);
		sheet.setColumnWidth(12, 4000);
		sheet.setColumnWidth(12, 4000);
		sheet.setColumnWidth(13, 4000);
		sheet.setColumnWidth(14, 4000);
		sheet.setColumnWidth(15, 4000);
		sheet.setColumnWidth(16, 4000);
		sheet.setColumnWidth(17, 4000);
		sheet.setColumnWidth(18, 4000);
		sheet.setColumnWidth(19, 4000);
		sheet.setColumnWidth(20, 4000);
		sheet.setColumnWidth(21, 4000);
		sheet.setColumnWidth(22, 4000);
		sheet.setColumnWidth(23, 4000);
		sheet.setColumnWidth(24, 4000);
		sheet.setColumnWidth(25, 4000);
		sheet.setColumnWidth(26, 4000);
		
		Row row=sheet.createRow(0);
		writeCellValue(wb,sheet,row,0,"���",font,IndexedColors.GREY_25_PERCENT.getIndex(),CellStyle.ALIGN_CENTER);
		writeCellValue(wb,sheet,row,1,"��Ʒ����",font,IndexedColors.GREY_25_PERCENT.getIndex(),CellStyle.ALIGN_CENTER);
		writeCellValue(wb,sheet,row,2,"��Ʒ���",font,IndexedColors.GREY_25_PERCENT.getIndex(),CellStyle.ALIGN_CENTER);
		writeCellValue(wb,sheet,row,3,"����",font,IndexedColors.GREY_25_PERCENT.getIndex(),CellStyle.ALIGN_CENTER);
		writeCellValue(wb,sheet,row,4,"�ͻ�����",font,IndexedColors.GREY_25_PERCENT.getIndex(),CellStyle.ALIGN_CENTER);
		writeCellValue(wb,sheet,row,5,"�ͻ�����",font,IndexedColors.GREY_25_PERCENT.getIndex(),CellStyle.ALIGN_CENTER);
		writeCellValue(wb,sheet,row,6,"���֤��",font,IndexedColors.GREY_25_PERCENT.getIndex(),CellStyle.ALIGN_CENTER);
		writeCellValue(wb,sheet,row,7,"������",font,IndexedColors.GREY_25_PERCENT.getIndex(),CellStyle.ALIGN_CENTER);
		writeCellValue(wb,sheet,row,8,"���Ա",font,IndexedColors.GREY_25_PERCENT.getIndex(),CellStyle.ALIGN_CENTER);
		writeCellValue(wb,sheet,row,9,"����Ա",font,IndexedColors.GREY_25_PERCENT.getIndex(),CellStyle.ALIGN_CENTER);
		writeCellValue(wb,sheet,row,10,"��������",font,IndexedColors.GREY_25_PERCENT.getIndex(),CellStyle.ALIGN_CENTER);
		writeCellValue(wb,sheet,row,11,"��������",font,IndexedColors.GREY_25_PERCENT.getIndex(),CellStyle.ALIGN_CENTER);
		writeCellValue(wb,sheet,row,12,"���ʱ��",font,IndexedColors.GREY_25_PERCENT.getIndex(),CellStyle.ALIGN_CENTER);
		writeCellValue(wb,sheet,row,13,"�ſ�/ȡ������",font,IndexedColors.GREY_25_PERCENT.getIndex(),CellStyle.ALIGN_CENTER);
		writeCellValue(wb,sheet,row,14,"���˽��",font,IndexedColors.GREY_25_PERCENT.getIndex(),CellStyle.ALIGN_CENTER);
		writeCellValue(wb,sheet,row,15,"GPS��",font,IndexedColors.GREY_25_PERCENT.getIndex(),CellStyle.ALIGN_CENTER);
		writeCellValue(wb,sheet,row,16,"�����",font,IndexedColors.GREY_25_PERCENT.getIndex(),CellStyle.ALIGN_CENTER);
		writeCellValue(wb,sheet,row,17,"����˰",font,IndexedColors.GREY_25_PERCENT.getIndex(),CellStyle.ALIGN_CENTER);
		writeCellValue(wb,sheet,row,18,"��������",font,IndexedColors.GREY_25_PERCENT.getIndex(),CellStyle.ALIGN_CENTER);
		writeCellValue(wb,sheet,row,19,"�׸�����",font,IndexedColors.GREY_25_PERCENT.getIndex(),CellStyle.ALIGN_CENTER);
		writeCellValue(wb,sheet,row,20,"ִ������",font,IndexedColors.GREY_25_PERCENT.getIndex(),CellStyle.ALIGN_CENTER);
		writeCellValue(wb,sheet,row,21,"�¹���",font,IndexedColors.GREY_25_PERCENT.getIndex(),CellStyle.ALIGN_CENTER);
		writeCellValue(wb,sheet,row,22,"����״̬",font,IndexedColors.GREY_25_PERCENT.getIndex(),CellStyle.ALIGN_CENTER);
		writeCellValue(wb,sheet,row,23,"����״��",font,IndexedColors.GREY_25_PERCENT.getIndex(),CellStyle.ALIGN_CENTER);
		writeCellValue(wb,sheet,row,24,"��������",font,IndexedColors.GREY_25_PERCENT.getIndex(),CellStyle.ALIGN_CENTER);
		writeCellValue(wb,sheet,row,25,"���Ա���",font,IndexedColors.GREY_25_PERCENT.getIndex(),CellStyle.ALIGN_CENTER);
		writeCellValue(wb,sheet,row,26,"������һ���",font,IndexedColors.GREY_25_PERCENT.getIndex(),CellStyle.ALIGN_CENTER);
		
		Font fontNormal=wb.createFont();		
		fontNormal.setFontHeightInPoints((short)10);
		
		for(int i=0;i<list.size();i++)
		{
			row=sheet.createRow(i+1);
			Map item=list.get(i);
			System.out.println(String.valueOf(i)+item.get("producttype").toString());
			String seq=String.valueOf(i+1);
			String productType=item.get("producttype").toString();
			String productSerial=item.get("productname").toString().split("-")[1];
			String ppcx=item.get("ppcx").toString();
			String custType=item.get("reserver5").toString();
			String custName=item.containsKey("name")?item.get("name").toString():"";
			String idNo=item.get("idno").toString();
			String jxs=item.get("sqfqr").toString();
			Object tmpShr=item.get("shr");
			String shr=tmpShr==null?"":tmpShr.toString();
			Object tmpSpr=item.get("spr");
			String spr=tmpSpr==null?"":tmpSpr.toString();
			String sqfqsj=item.get("sqfqsj").toString();
			Object tmpSpsj=item.get("spsj");
			String spsj=tmpSpsj==null?"":tmpSpsj.toString();
			double phje=(Double)item.get("rzje");
			double gpsfee=(Double)item.get("gpsfee");
			double gzs=(Double)item.get("gzs");
			double fwf=(Double)item.get("fwf");
			String rzqx=String.valueOf((Integer)item.get("rzqx"));
			double sfbl=(Double)item.get("sfbl");
			double rate=(Double)item.get("rate");
			String sqdzt1=item.get("sqdzt").toString();
			double ygk=(Double)item.get("ygk");
			String hyzk=item.containsKey("hyzk")?item.get("hyzk").toString():"";
			String fkfjtj=item.containsKey("fkfjtj")?item.get("fkfjtj").toString():"";
			String shyyj=item.containsKey("shyyj")?item.get("shyyj").toString():"";
			String qxr1yj=item.containsKey("qxr1yj")?item.get("qxr1yj").toString():"";
			writeCellValue(wb,sheet,row,0,seq,fontNormal,IndexedColors.WHITE.getIndex(),CellStyle.ALIGN_CENTER);
			writeCellValue(wb,sheet,row,1,productType,fontNormal,IndexedColors.WHITE.getIndex(),CellStyle.ALIGN_CENTER);
			writeCellValue(wb,sheet,row,2,productSerial,fontNormal,IndexedColors.WHITE.getIndex(),CellStyle.ALIGN_CENTER);
			writeCellValue(wb,sheet,row,3,ppcx,fontNormal,IndexedColors.WHITE.getIndex(),CellStyle.ALIGN_LEFT);
			writeCellValue(wb,sheet,row,4,custType,fontNormal,IndexedColors.WHITE.getIndex(),CellStyle.ALIGN_CENTER);
			writeCellValue(wb,sheet,row,5,custName,fontNormal,IndexedColors.WHITE.getIndex(),CellStyle.ALIGN_CENTER);
			writeCellValue(wb,sheet,row,6,idNo,fontNormal,IndexedColors.WHITE.getIndex(),CellStyle.ALIGN_LEFT);
			writeCellValue(wb,sheet,row,7,jxs,fontNormal,IndexedColors.WHITE.getIndex(),CellStyle.ALIGN_LEFT);
			writeCellValue(wb,sheet,row,8,shr,fontNormal,IndexedColors.WHITE.getIndex(),CellStyle.ALIGN_CENTER);
			writeCellValue(wb,sheet,row,9,spr,fontNormal,IndexedColors.WHITE.getIndex(),CellStyle.ALIGN_CENTER);
			writeCellValue(wb,sheet,row,10,sqfqsj,fontNormal,IndexedColors.WHITE.getIndex(),CellStyle.ALIGN_LEFT);
			writeCellValue(wb,sheet,row,11,spsj,fontNormal,IndexedColors.WHITE.getIndex(),CellStyle.ALIGN_LEFT);
			writeCellValue(wb,sheet,row,12,"",fontNormal,IndexedColors.WHITE.getIndex(),CellStyle.ALIGN_LEFT);
			writeCellValue(wb,sheet,row,13,"",fontNormal,IndexedColors.WHITE.getIndex(),CellStyle.ALIGN_LEFT);
			DecimalFormat df=new DecimalFormat("#,###,###,###,##0.00");
			DecimalFormat df1=new DecimalFormat("#.000000");
			writeCellAmount(wb,sheet,row,14,df.format(phje),fontNormal,IndexedColors.WHITE.getIndex(),CellStyle.ALIGN_RIGHT);
			writeCellAmount(wb,sheet,row,15,df.format(gpsfee),fontNormal,IndexedColors.WHITE.getIndex(),CellStyle.ALIGN_RIGHT);
			writeCellAmount(wb,sheet,row,16,df.format(gzs),fontNormal,IndexedColors.WHITE.getIndex(),CellStyle.ALIGN_RIGHT);	
			writeCellAmount(wb,sheet,row,17,df.format(fwf),fontNormal,IndexedColors.WHITE.getIndex(),CellStyle.ALIGN_RIGHT);
			writeCellValue(wb,sheet,row,18,rzqx,fontNormal,IndexedColors.WHITE.getIndex(),CellStyle.ALIGN_CENTER);
			writeCellValue(wb,sheet,row,19,df1.format(sfbl*100)+"%",fontNormal,IndexedColors.WHITE.getIndex(),CellStyle.ALIGN_CENTER);
			writeCellValue(wb,sheet,row,20,df1.format(rate*100)+"%",fontNormal,IndexedColors.WHITE.getIndex(),CellStyle.ALIGN_CENTER);
			writeCellValue(wb,sheet,row,21,df.format(ygk),fontNormal,IndexedColors.WHITE.getIndex(),CellStyle.ALIGN_RIGHT);
			writeCellValue(wb,sheet,row,22,sqdzt1,fontNormal,IndexedColors.WHITE.getIndex(),CellStyle.ALIGN_CENTER);
			writeCellValue(wb,sheet,row,23,hyzk,fontNormal,IndexedColors.WHITE.getIndex(),CellStyle.ALIGN_CENTER);
			writeCellValue(wb,sheet,row,24,fkfjtj,fontNormal,IndexedColors.WHITE.getIndex(),CellStyle.ALIGN_CENTER);
			writeCellValue(wb,sheet,row,25,shyyj,fontNormal,IndexedColors.WHITE.getIndex(),CellStyle.ALIGN_CENTER);
			writeCellValue(wb,sheet,row,26,qxr1yj,fontNormal,IndexedColors.WHITE.getIndex(),CellStyle.ALIGN_CENTER);
			
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
	
	@RequestMapping(value="/getBranchAvailablyGpsLvl")
	@ResponseBody
	public List<Map> getBranchAvailablyGpsLvl(String rzje,HttpServletRequest request)
	{
		Claims claims=(Claims)request.getAttribute("claims");
		String userid=claims.getSubject();
		SysAccount sysAccount=sysAccountService.get(userid);
		return leasingAppServ.getBranchAvailablyGpsLvl(sysAccount.getBranchid(), Double.valueOf(rzje));
	}
	@RequestMapping(value="/getOnApproveRecord/{id}")
	@ResponseBody
	public Map getOnApproveRecord(@PathVariable String id)
	{
		return leasingAppServ.getOnApproveRecord(id);
	}
	
	@RequestMapping("/exportKhhzhzb")
	@ResponseBody
	public ResponseEntity<byte[]> exportKhhzhzb(String ids,HttpSession session) throws ParseException, IOException
	{
		
		List<Map> list=leasingAppServ.getkhhzhzb();
		
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
		sheet.setColumnWidth(7, 5000);
		sheet.setColumnWidth(8, 5000);
		sheet.setColumnWidth(9, 5000);
		sheet.setColumnWidth(10, 5000);
		sheet.setColumnWidth(11, 5000);
		sheet.setColumnWidth(12, 5000);
		sheet.setColumnWidth(13, 10000);
		sheet.setColumnWidth(14, 5000);
		sheet.setColumnWidth(15, 5000);
		
		Row row=sheet.createRow(0);
		writeCellValue(wb,sheet,row,0,"���",font,IndexedColors.GREY_25_PERCENT.getIndex(),CellStyle.ALIGN_CENTER);
		writeCellValue(wb,sheet,row,1,"������",font,IndexedColors.GREY_25_PERCENT.getIndex(),CellStyle.ALIGN_CENTER);
		writeCellValue(wb,sheet,row,2,"����",font,IndexedColors.GREY_25_PERCENT.getIndex(),CellStyle.ALIGN_CENTER);
		writeCellValue(wb,sheet,row,3,"������",font,IndexedColors.GREY_25_PERCENT.getIndex(),CellStyle.ALIGN_CENTER);
		writeCellValue(wb,sheet,row,4,"���ʽ��",font,IndexedColors.GREY_25_PERCENT.getIndex(),CellStyle.ALIGN_CENTER);
		writeCellValue(wb,sheet,row,5,"�ſ���",font,IndexedColors.GREY_25_PERCENT.getIndex(),CellStyle.ALIGN_CENTER);
		writeCellValue(wb,sheet,row,6,"��������",font,IndexedColors.GREY_25_PERCENT.getIndex(),CellStyle.ALIGN_CENTER);
		writeCellValue(wb,sheet,row,7,"��������",font,IndexedColors.GREY_25_PERCENT.getIndex(),CellStyle.ALIGN_CENTER);
		writeCellValue(wb,sheet,row,8,"�����",font,IndexedColors.GREY_25_PERCENT.getIndex(),CellStyle.ALIGN_CENTER);
		writeCellValue(wb,sheet,row,9,"������ܶ�",font,IndexedColors.GREY_25_PERCENT.getIndex(),CellStyle.ALIGN_CENTER);
		writeCellValue(wb,sheet,row,10,"��Ϣ�ܶ�",font,IndexedColors.GREY_25_PERCENT.getIndex(),CellStyle.ALIGN_CENTER);
		writeCellValue(wb,sheet,row,11,"GPS��λ",font,IndexedColors.GREY_25_PERCENT.getIndex(),CellStyle.ALIGN_CENTER);
		writeCellValue(wb,sheet,row,12,"GPS��λ",font,IndexedColors.GREY_25_PERCENT.getIndex(),CellStyle.ALIGN_CENTER);
		writeCellValue(wb,sheet,row,13,"��GPS�����",font,IndexedColors.GREY_25_PERCENT.getIndex(),CellStyle.ALIGN_CENTER);
		writeCellValue(wb,sheet,row,14,"������",font,IndexedColors.GREY_25_PERCENT.getIndex(),CellStyle.ALIGN_CENTER);
		writeCellValue(wb,sheet,row,15,"�����",font,IndexedColors.GREY_25_PERCENT.getIndex(),CellStyle.ALIGN_CENTER);
		
		Font fontNormal=wb.createFont();		
		fontNormal.setFontHeightInPoints((short)10);
		
		for(int i=0;i<list.size();i++)
		{
			row=sheet.createRow(i+1);
			Map item=list.get(i);
			String seq=String.valueOf(i+1);
			String id=item.containsKey("id")?item.get("id").toString():"";
			String name=item.containsKey("name")?item.get("name").toString():"";
			String sqfqr=item.containsKey("sqfqr")?item.get("sqfqr").toString():"";
			double yhk=item.containsKey("yhk")?(Double)item.get("yhk"):0;
			double rzje=item.containsKey("rzje")?(Double)item.get("rzje"):0;
			int rzqx=item.containsKey("rzqx")?(Integer)item.get("rzqx"):0;
			double rate=item.containsKey("rate")?(Double)item.get("rate"):0;
			double gpsfee=item.containsKey("gpsfee")?(Double)item.get("gpsfee"):0;
			double gpsjw=item.containsKey("gpsjw")?(Double)item.get("gpsjw"):0;
			double jxsfy=item.containsKey("jxsfy")?(Double)item.get("jxsfy"):0;
			double pgf=item.containsKey("pgf")?(Double)item.get("pgf"):0;
			double fwf=item.containsKey("fwf")?(Double)item.get("fwf"):0;
			double rzsxf=item.containsKey("rzsxf")?(Double)item.get("rzsxf"):0;
			double fkje=0.00;
			double ygk=0.00;
			
			int appdate=Integer.valueOf(id.split("-")[1]);
			if(appdate<20160618)
			{
				if(Double.compare(gpsfee, 0.00)>0)
				{
					fkje=Math.round(rzje-1998-fwf)-rzsxf;
				}
				else
				{
					fkje=Math.round(rzje-fwf)-rzsxf;
				}
				ygk=Math.round((rzje-fwf)/10000*yhk)+Math.ceil(fwf/rzqx);
				
			}
			else if(appdate>=20160618&&appdate<20160622)
			{
				fkje=Math.round(rzje-gpsfee+jxsfy-pgf);
				ygk=Math.round(rzje/10000*yhk);
			}
			else
			{
				fkje=Math.round(rzje-gpsfee-pgf);
				ygk=Math.round(rzje/10000*yhk);
			}
			double ygkze=ygk*rzqx;
			double lxze=ygkze-rzje;
			DecimalFormat df=new DecimalFormat("#,###,###,###,##0.00");
			DecimalFormat df1=new DecimalFormat("#.000000");
			writeCellValue(wb,sheet,row,0,seq,fontNormal,IndexedColors.WHITE.getIndex(),CellStyle.ALIGN_CENTER);
			writeCellValue(wb,sheet,row,1,id,fontNormal,IndexedColors.WHITE.getIndex(),CellStyle.ALIGN_CENTER);
			writeCellValue(wb,sheet,row,2,name,fontNormal,IndexedColors.WHITE.getIndex(),CellStyle.ALIGN_CENTER);
			writeCellValue(wb,sheet,row,3,sqfqr,fontNormal,IndexedColors.WHITE.getIndex(),CellStyle.ALIGN_LEFT);
			writeCellAmount(wb,sheet,row,4,df.format(rzje),fontNormal,IndexedColors.WHITE.getIndex(),CellStyle.ALIGN_RIGHT);
			writeCellAmount(wb,sheet,row,5,df.format(fkje),fontNormal,IndexedColors.WHITE.getIndex(),CellStyle.ALIGN_RIGHT);
			writeCellAmount(wb,sheet,row,6,df.format(rzqx),fontNormal,IndexedColors.WHITE.getIndex(),CellStyle.ALIGN_RIGHT);
			writeCellAmount(wb,sheet,row,7,df.format(rate),fontNormal,IndexedColors.WHITE.getIndex(),CellStyle.ALIGN_RIGHT);
			writeCellAmount(wb,sheet,row,8,df.format(ygk),fontNormal,IndexedColors.WHITE.getIndex(),CellStyle.ALIGN_RIGHT);
			writeCellAmount(wb,sheet,row,9,df.format(ygkze),fontNormal,IndexedColors.WHITE.getIndex(),CellStyle.ALIGN_RIGHT);
			writeCellAmount(wb,sheet,row,10,df.format(lxze),fontNormal,IndexedColors.WHITE.getIndex(),CellStyle.ALIGN_RIGHT);
			writeCellAmount(wb,sheet,row,11,df.format(gpsfee),fontNormal,IndexedColors.WHITE.getIndex(),CellStyle.ALIGN_RIGHT);
			writeCellAmount(wb,sheet,row,12,df.format(gpsjw),fontNormal,IndexedColors.WHITE.getIndex(),CellStyle.ALIGN_RIGHT);
			writeCellAmount(wb,sheet,row,13,df.format(jxsfy),fontNormal,IndexedColors.WHITE.getIndex(),CellStyle.ALIGN_RIGHT);
			writeCellAmount(wb,sheet,row,14,df.format(pgf),fontNormal,IndexedColors.WHITE.getIndex(),CellStyle.ALIGN_RIGHT);
			writeCellAmount(wb,sheet,row,15,df.format(fwf),fontNormal,IndexedColors.WHITE.getIndex(),CellStyle.ALIGN_RIGHT);		
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
