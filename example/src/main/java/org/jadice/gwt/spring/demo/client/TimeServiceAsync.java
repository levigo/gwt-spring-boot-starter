package org.jadice.gwt.spring.demo.client;

import org.jadice.gwt.spring.demo.client.TimeService.TimeResponse;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface TimeServiceAsync {

  void getTime(AsyncCallback<TimeResponse> callback);

}
