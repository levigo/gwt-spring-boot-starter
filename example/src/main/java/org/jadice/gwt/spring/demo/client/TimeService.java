package org.jadice.gwt.spring.demo.client;

import java.io.Serializable;
import java.util.Date;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("time")
public interface TimeService extends RemoteService {
  public static class TimeResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    public TimeResponse() {
    }
    
    public TimeResponse(final Date date) {
      time = date;
    }

    public Date time;
  }
  
  TimeResponse getTime();
}