package com.orctom.mojo.was;

import com.orctom.was.model.Command;
import com.orctom.was.model.WebSphereModel;
import com.orctom.was.service.impl.AbstractWebSphereServiceImpl;

/**
 * Service to execute WAS tasks
 * Created by hao on 11/11/15.
 */
public class WebSphereServiceImpl extends AbstractWebSphereServiceImpl {

	public WebSphereServiceImpl(WebSphereModel model, String targetDir) {
		super(model, targetDir);
	}

	@Override
	protected void executeCommand(Command command) {

	}
}
