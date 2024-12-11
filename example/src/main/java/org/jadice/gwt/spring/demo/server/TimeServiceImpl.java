package org.jadice.gwt.spring.demo.server;

import java.util.Date;

import jakarta.servlet.annotation.WebServlet;

import org.jadice.gwt.spring.demo.client.TimeService;

import com.google.gwt.user.server.rpc.jakarta.RemoteServiceServlet;


// Expose the RPC service at the desired URL
@WebServlet(urlPatterns = "/springdemo/time")
public class TimeServiceImpl extends RemoteServiceServlet implements TimeService {
  private static final long serialVersionUID = 1L;

  /* (non-Javadoc)
   * @see org.jadice.gwt.spring.demo.server.TimeService#getTime()
   */
  @Override
  public TimeResponse getTime() {
    return new TimeResponse(new Date());
  }
}
