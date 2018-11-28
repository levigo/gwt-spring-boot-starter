/**
 * <pre>
 * Copyright (c), levigo holding gmbh.
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 * </pre>
 */
package org.jadice.gwt.spring.demo.client;

import org.jadice.gwt.spring.demo.client.TimeService.TimeResponse;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootLayoutPanel;

public class ApplicationEntryPoint implements EntryPoint {

  @Override
  public void onModuleLoad() {
    Label serverInfoLabel = new Label("The server's time is: ...");
    
    RootLayoutPanel.get().add(serverInfoLabel);
    
    TimeServiceAsync timeService = GWT.create(TimeService.class);
    
    timeService.getTime(new AsyncCallback<TimeResponse>() {
      
      @Override
      public void onSuccess(final TimeResponse result) {
        serverInfoLabel.setText("The server's time is: " + result.time.toString());
      }
      
      @Override
      public void onFailure(final Throwable caught) {
        serverInfoLabel.setText("Failed to get server's time: " + caught.getLocalizedMessage());
      }
    });
  }
}