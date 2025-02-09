/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.runtime.core.internal.message;

import java.util.Set;

import javax.activation.DataHandler;

/**
 * Use to obtain message attachments defined in two scopes, inbound and outbound.
 *
 * @deprecated Transport infrastructure is deprecated.
 */
@Deprecated
public interface MessageAttachments {

  /**
   * Retrieve an attachment with the given name. If the attachment does not exist, null will be returned
   * 
   * @param name the name of the attachment to retrieve
   * @return the attachment with the given name or null if the attachment does not exist
   * @see DataHandler
   * @since 3.0
   */
  DataHandler getInboundAttachment(String name);

  /**
   * Retrieve an attachment with the given name. If the attachment does not exist, null will be returned
   * 
   * @param name the name of the attachment to retrieve
   * @return the attachment with the given name or null if the attachment does not exist
   * @see DataHandler
   * @since 3.0
   */
  DataHandler getOutboundAttachment(String name);

  /**
   * @return a set of the names of the attachments on this message. If there are no attachments an empty set will be returned.
   * @since 3.0
   */
  Set<String> getInboundAttachmentNames();

  /**
   * @return a set of the names of the attachments on this message. If there are no attachments an empty set will be returned.
   * @since 3.0
   */
  Set<String> getOutboundAttachmentNames();

}
