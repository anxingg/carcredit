package com.pujjr;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.pujjr.business.domain.SysAccount;
import com.pujjr.business.service.KeyService;
import com.pujjr.business.service.LeasingAppService;
import com.pujjr.business.service.SequenceService;
import com.pujjr.business.service.SysAccountService;
import com.pujjr.business.service.WSSqlserverService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:conf/spring*.xml"})
public class SeqTest 
{
	@Autowired
	private SequenceService seqSerivce;
	@Autowired
	private SysAccountService sysAccountService;
	@Autowired
	private KeyService keyService;
	@Autowired
	private LeasingAppService leasingAppServ;
	@Autowired
	private WSSqlserverService wsServ;
	@Test
	public void testMultiDataSource()
	{
		List<HashMap<String,Object>> list = wsServ.sqlserverTest();
		
		for(int i=0;i<list.size();i++)
		{
			System.out.println(list.get(i).toString());
		}
		List<Map> list1=leasingAppServ.getInCaseList(null, null, null, null);
		for(int i=0;i<list1.size();i++)
		{
			Map item=list1.get(i);
			System.out.println(i+"-"+item.get("ygk"));
		}
	}
	@Test
	public void getInCaseList()
	{
		List<Map> list=leasingAppServ.getInCaseList(null, null, null, null);
		for(int i=0;i<list.size();i++)
		{
			Map item=list.get(i);
			System.out.println(item.toString());
		}
	}
	@Test
	public void conGenSeq()
	{
		System.out.println("start genseq");
		Thread1 t1=new Thread1();
		Thread ta=new Thread(t1,"A");
		//Thread tb=new Thread(t1,"B");
		//Thread tc=new Thread(t1,"C");
		ta.start();
		//tb.start();
		//tc.start();
		/*int seq=seqSerivce.getNextVal("appid");
		System.out.println(seq);*/
		System.out.println("start genseq");
	}
	
	public class Thread1 implements Runnable {  
		
		
		public void run() {  
	    	 for (int i = 0; i < 5; i++) {  
	    		 int seq=seqSerivce.getNextVal("appid");
	    		 System.out.println(Thread.currentThread().getName()+" seq="+i);
            }  
	     }  
	}
	@Test
	public void genkey() throws UnsupportedEncodingException, NoSuchAlgorithmException
	{
		/*
		List<SysAccount> list=sysAccountService.getList();
		for(int i=0;i<list.size();i++)
		{
			SysAccount item=list.get(i);
			String passwd=keyService.generateEncryptPasswd(item.getId(), item.getPasswd());
			item.setPasswd(passwd);
			sysAccountService.onlyUpdateAccount(item);
			System.out.println(item.getId()+" "+passwd);
		}*/
		
		String passwd=keyService.generateEncryptPasswd("�½����", "111111");
		System.out.println(passwd);
		
	}
}
