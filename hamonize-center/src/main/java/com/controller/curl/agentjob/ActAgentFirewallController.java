package com.controller.curl.agentjob;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mapper.IActAgentDeviceMapper;
import com.mapper.IActAgentFirewallMapper;
import com.mapper.IActAgentLogInOutMapper;
import com.mapper.IActAgentProgrmMapper;
import com.mapper.IGetAgentJobMapper;
import com.mapper.IGetAgentRecoveryMapper;
import com.model.ActAgentBackupRecoveryVo;
import com.model.ActAgentDeviceVo;
import com.model.ActAgentFirewallVo;
import com.model.ActAgentProgrmVo;
import com.model.GetAgentJobVo;
import com.model.LogInOutVo;

@RestController
@RequestMapping("/act")
public class ActAgentFirewallController {

	@Autowired
	private IGetAgentJobMapper agentJobMapper;

	@Autowired
	private IActAgentFirewallMapper actAgentFirewallMapper;

	@Autowired
	private IActAgentDeviceMapper actAgentDeviceMapper;

	@Autowired
	private IActAgentProgrmMapper actAgentProgrmMapper;

	@Autowired
	private IGetAgentRecoveryMapper getAgentRecoveryMapper;

	@Autowired
	private IActAgentLogInOutMapper actAgentLogInOutMapper;

	@RequestMapping("/loginout")
	public void login(HttpServletRequest request) throws Exception {
		StringBuffer json = new StringBuffer();
		String line = null;

		try {
			BufferedReader reader = request.getReader();
			while ((line = reader.readLine()) != null) {
				json.append(line);
			}

		} catch (Exception e) {
			System.out.println("Error reading JSON string: " + e.toString());
		}

		System.out.println("json===> " + json);

		JSONParser jsonParser = new JSONParser();
		JSONObject jsonObj = (JSONObject) jsonParser.parse(json.toString());
		JSONArray logInArray = (JSONArray) jsonObj.get("events");

		LogInOutVo inputVo = new LogInOutVo();
		for (int i = 0; i < logInArray.size(); i++) {
			JSONObject tempObj = (JSONObject) logInArray.get(i);

			inputVo.setDatetime(tempObj.get("datetime").toString());
			inputVo.setUuid(tempObj.get("uuid").toString());
			inputVo.setGubun(tempObj.get("gubun").toString());

		}

		int retVal = 0;

		if (inputVo.getGubun().equals("LOGIN")) { // login insert
			inputVo.setLogin_dt(inputVo.getDatetime());
			retVal = actAgentLogInOutMapper.insertLoginLog(inputVo);
			System.out.println("uuid" + inputVo.getUuid());

		} else if (inputVo.getGubun().equals("LOGOUT")) { // logout update
			inputVo.setSeq(actAgentLogInOutMapper.selectLoginLogSeq(inputVo));
			inputVo.setLogout_dt(inputVo.getDatetime());
			retVal = actAgentLogInOutMapper.updateLoginLog(inputVo);
		}

	
	}

	@RequestMapping("/firewallAct")
	public void firewallAct(HttpServletRequest request) throws Exception {
		StringBuffer json = new StringBuffer();
		String line = null;

		try {
			BufferedReader reader = request.getReader();
			while ((line = reader.readLine()) != null) {
				json.append(line);
			}

		} catch (Exception e) {
			System.out.println("Error reading JSON string: " + e.toString());
		}

		System.out.println("json===> " + json);

		JSONParser jsonParser = new JSONParser();
		JSONObject jsonObj = (JSONObject) jsonParser.parse(json.toString());
		JSONArray hmdArray = (JSONArray) jsonObj.get("events");

		ActAgentFirewallVo inputVo = new ActAgentFirewallVo();
		for (int i = 0; i < hmdArray.size(); i++) {
			JSONObject tempObj = (JSONObject) hmdArray.get(i);

			inputVo.setDatetime(tempObj.get("datetime").toString());
			inputVo.setUuid(tempObj.get("uuid").toString().trim());
			inputVo.setHostname(tempObj.get("hostname").toString());
			inputVo.setStatus(tempObj.get("status").toString());
			inputVo.setStatus_yn(tempObj.get("status_yn").toString());
			inputVo.setRetport(tempObj.get("retport").toString());

		}

		System.out.println("\n에이전트에서 받은 값 inputVo : " + inputVo.toString());

		int uuid = pcUUID(inputVo.getUuid());
		inputVo.setOrgseq(uuid);

		int retVal = actAgentFirewallMapper.insertActAgentFirewall(inputVo);
		System.out.println("retVal ==== " + retVal);

	}


	/**
	 * 디바이스 정책 배포 결과
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/deviceAct")
	public void deviceAct(HttpServletRequest request) throws Exception {
		StringBuffer json = new StringBuffer();
		String line = null;

		try {
			BufferedReader reader = request.getReader();
			while ((line = reader.readLine()) != null) {
				json.append(line);
			}

		} catch (Exception e) {
			System.out.println("Error reading JSON string: " + e.toString());
		}

		System.out.println("json===> " + json);

		JSONParser jsonParser = new JSONParser();
		JSONObject jsonObj = (JSONObject) jsonParser.parse(json.toString());
		JSONArray hmdArray = (JSONArray) jsonObj.get("events");

		List<ActAgentDeviceVo> inputVoList = new ArrayList<ActAgentDeviceVo>();
		for (int i = 0; i < hmdArray.size(); i++) {
			JSONObject tempObj = (JSONObject) hmdArray.get(i);
			ActAgentDeviceVo tmpVo = new ActAgentDeviceVo();

			tmpVo.setUuid(tempObj.get("uuidVal").toString().trim());
			tmpVo.setHostname(tempObj.get("hostname").toString());
			tmpVo.setStatus_yn(tempObj.get("statusyn").toString());
			tmpVo.setProduct(tempObj.get("product").toString());
			tmpVo.setVendorCode(tempObj.get("vendorCode").toString());
			tmpVo.setProductCode(tempObj.get("productCode").toString());
			tmpVo.setOrg_seq(pcUUID(tempObj.get("uuidVal").toString().trim()));
			inputVoList.add(tmpVo);
		}

		int retVal = actAgentDeviceMapper.insertActAgentDevice(inputVoList);
		System.out.println("retVal ==== " + retVal);
		
	}

	/**
	 * insresert : 프로그램 정책 적용 (ins:적용, del:해제)
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/progrmAct")
	public void progrmAct(HttpServletRequest request) throws Exception {
		StringBuffer json = new StringBuffer();
		String line = null;

		try {
			BufferedReader reader = request.getReader();
			while ((line = reader.readLine()) != null) {
				json.append(line);
			}

		} catch (Exception e) {
			System.out.println("Error reading JSON string: " + e.toString());
		}

		JSONParser Parser = new JSONParser();
		JSONObject jsonObj = (JSONObject) Parser.parse(json.toString());
	
		JSONArray insArray = (JSONArray) jsonObj.get("insresert");
		ActAgentProgrmVo[] inputVo = new ActAgentProgrmVo[insArray.size()];

		if (insArray.size() != 0) {
			for (int i = 0; i < insArray.size(); i++) {
				JSONObject tempObj = (JSONObject) insArray.get(i);
				inputVo[i] = new ActAgentProgrmVo();
				inputVo[i].setUuid(tempObj.get("uuid").toString().trim());
				inputVo[i].setHostname(tempObj.get("hostname").toString());
				inputVo[i].setStatus(tempObj.get("status").toString());
				inputVo[i].setStatus_yn(tempObj.get("status_yn").toString());
				inputVo[i].setProgrmname(tempObj.get("progrmname").toString());
				inputVo[i].setDatetime(tempObj.get("datetime").toString());
				inputVo[i].setOrgseq(pcUUID(tempObj.get("uuid").toString().trim()));
			}
		}

		Map<String, Object> insertDataMap = new HashMap<String, Object>();
		insertDataMap.put("list", inputVo);

		if (inputVo.length != 0) {
			actAgentProgrmMapper.insertActAgentProgrm(insertDataMap);
		}

	}

	 /**
	  * 에이전트에서 복구 실행결과 리턴
	  * @param request
	  * @return
	  * @throws Exception
	  */
	@RequestMapping("/stBackupRecoveryJob")
	public void stBackupRecoveryAct(HttpServletRequest request) throws Exception {		
		StringBuffer json = new StringBuffer();
		String line = null;

		try {
			BufferedReader reader = request.getReader();
			while ((line = reader.readLine()) != null) {
				json.append(line);
			}

		} catch (Exception e) {
			System.out.println("Error reading JSON string: " + e.toString());
		}

		System.out.println("json===> " + json);

		JSONParser jsonParser = new JSONParser();
		JSONObject jsonObj = (JSONObject) jsonParser.parse(json.toString());
		JSONArray hmdArray = (JSONArray) jsonObj.get("events");

		ActAgentBackupRecoveryVo inputVo = new ActAgentBackupRecoveryVo();
		for (int i = 0; i < hmdArray.size(); i++) {
			JSONObject tempObj = (JSONObject) hmdArray.get(i);

			inputVo.setDatetime(tempObj.get("datetime").toString());
			inputVo.setUuid(tempObj.get("uuid").toString().trim());
			inputVo.setHostname(tempObj.get("hostname").toString());
			inputVo.setAction_status(tempObj.get("action_status").toString());
			inputVo.setResult(tempObj.get("result").toString());

		}

		int uuid = pcUUID(inputVo.getUuid());
		inputVo.setOrg_seq(uuid);

		int retVal = getAgentRecoveryMapper.insertActAgentBackupRecovery(inputVo);
		if (retVal >= 1) {
			/**
			 * 복구 실행 후 기존 작업 내역 삭제
			 */
			ActAgentBackupRecoveryVo retVo = getAgentRecoveryMapper.getDataActAgentBackupRecovery(inputVo);

			if ("N".equals(retVo.getResult())) {
				try {
					int delPolicyVal = getAgentRecoveryMapper.deleteActPolicy(inputVo);
					getAgentRecoveryMapper.updateDataActAgentBackupRecovery(inputVo);
				} catch (Exception e) {
					System.out.println("e===============" + e.getMessage());
				}

			}

		} else {

		}

	}

	/*
	 * 부서 UUID로 부서 seq 가져오기
	 * 
	 * @param pcuuid
	 * 
	 * @return 부서seq
	 */
	public int pcUUID(String pcuuid) {
		GetAgentJobVo agentVo = new GetAgentJobVo();
		agentVo.setPc_uuid(pcuuid);
		agentVo = agentJobMapper.getAgentJobPcUUID(agentVo);
		int segSeq = 0;
		if (agentVo != null) {
			segSeq = agentVo.getSeq();
		}
		return segSeq;
	}

}
