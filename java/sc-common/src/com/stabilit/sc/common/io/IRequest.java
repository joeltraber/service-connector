package com.stabilit.sc.common.io;

import com.stabilit.sc.common.ctx.IRequestContext;
import com.stabilit.sc.common.util.MapBean;

public interface IRequest {

	public SCMPMsgType getKey();

	public IRequestContext getContext();
		
	public SCMP getSCMP();
	
	public String getSessionId();
	
	public void setAttribute(String key, Object value);
	
	public Object getAttribute(String key);

	public MapBean<Object> getAttributeMapBean();
		
}
