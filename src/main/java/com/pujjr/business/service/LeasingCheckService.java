package com.pujjr.business.service;

import java.sql.Timestamp;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pujjr.business.dao.LeasingAppMapper;
import com.pujjr.business.dao.LeasingApproveMapper;
import com.pujjr.business.dao.LeasingCheckMapper;
import com.pujjr.business.domain.LeasingApp;
import com.pujjr.business.domain.LeasingApprove;
import com.pujjr.business.domain.LeasingCheck;
import com.pujjr.business.domain.SysAccount;
import com.pujjr.business.vo.AppData;
import com.pujjr.common.Utils;

@Service
@Transactional(rollbackFor=Exception.class) 
public class LeasingCheckService {
	
	@Autowired
	private LeasingCheckMapper leasingCheckDao;
	@Autowired
	private LeasingAppMapper leasingAppDao;
	@Autowired
	private LeasingApproveMapper leasingApproveDao;
	
	public LeasingCheck get(String id)
	{
		return leasingCheckDao.selectByPrimaryKey(id);
	}
	
	public void update(LeasingCheck record)
	{
		leasingCheckDao.updateByPrimaryKey(record);
	}
	
	public void delete(String id)
	{
		leasingCheckDao.deleteByPrimaryKey(id);
	}
	
	public void add(LeasingCheck record)
	{
		leasingCheckDao.insert(record);
	}
	/***
	 * ͨ����˱���ӿ�
	 * ����ύ���״̬Ϊ��������
	 * 		�����ǰ״̬Ϊ����ˣ��������App,����Check��Approve��״̬���Ϊ�����
	 * 		�����ǰ״̬Ϊ����У�����Ҫ����App,Check,Approve��״̬ά�������
	 * ����ύ���״̬��Ϊ��������
	 * 		�����ǰ״̬Ϊ����ˣ��������App,����Check��Approve��״̬���Ϊ�����
	 * 		�����ǰ״̬Ϊ����У�����Ҫ����App,Check,Approve��״̬ά�������
	 * **/
	public void saveCheckData(AppData data,SysAccount checkAccount)
	{
		LeasingApp app=data.getApp();
		LeasingCheck check=data.getCheck();
		LeasingApprove approve=data.getApprove();
		
		String newAppStatus=app.getSqdzt();
		LeasingApp curApp=leasingAppDao.selectByPrimaryKey(app.getId());
		String curAppStatus=curApp.getSqdzt();
		
		if(check!=null)
		{
			check.setId(app.getId());
			check.setShr(checkAccount.getId());
			check.setShsj(new Timestamp(new Date().getTime()));
		}
		
		if(approve!=null)
		{
			approve.setId(app.getId());
			approve.setSpr(checkAccount.getId());
			approve.setSpsj(new Timestamp(new Date().getTime()));
		}
			
		if((curAppStatus.equals("�����")&&newAppStatus.equals("������"))||(curAppStatus.equals("�����")&&newAppStatus.equals("�����")))
		{
			
			leasingAppDao.updateByPrimaryKey(app);
			
			if(leasingCheckDao.selectByPrimaryKey(app.getId())!=null)
			{
				leasingCheckDao.updateByPrimaryKey(check);
			}
			else
			{
				leasingCheckDao.insert(check);
			}
			
			if(leasingApproveDao.selectByPrimaryKey(app.getId())!=null)
			{
				leasingApproveDao.updateByPrimaryKey(approve);
			}
			else
			{
				leasingApproveDao.insert(approve);
			}
		}
		else if(curAppStatus.equals("�����")&&newAppStatus.equals("����˻� "))
		{
			leasingAppDao.updateByPrimaryKey(app);
		}
		else
		{
			leasingAppDao.updateByPrimaryKey(app);
			leasingCheckDao.updateByPrimaryKey(check);
			leasingApproveDao.updateByPrimaryKey(approve);
		}
		
		
		
	}
}
