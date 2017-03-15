/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.ws.internal;


import static org.apache.commons.lang.StringUtils.isBlank;
import static org.mule.runtime.module.xml.util.XMLUtils.toW3cDocument;
import org.mule.extension.ws.api.SoapMessageBuilder;
import org.mule.extension.ws.api.WscAttributes;
import org.mule.extension.ws.api.exception.WscException;
import org.mule.extension.ws.internal.metadata.ConsumeOutputResolver;
import org.mule.extension.ws.internal.metadata.MessageBuilderResolver;
import org.mule.extension.ws.internal.metadata.OperationKeysResolver;
import org.mule.extension.ws.internal.metadata.WscAttributesResolver;
import org.mule.runtime.extension.api.annotation.OnException;
import org.mule.runtime.extension.api.annotation.error.Throws;
import org.mule.runtime.extension.api.annotation.metadata.MetadataKeyId;
import org.mule.runtime.extension.api.annotation.metadata.OutputResolver;
import org.mule.runtime.extension.api.annotation.metadata.TypeResolver;
import org.mule.runtime.extension.api.annotation.param.Connection;
import org.mule.runtime.extension.api.annotation.param.NullSafe;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.runtime.operation.Result;
import org.mule.services.soap.api.client.SoapClient;
import org.mule.services.soap.api.exception.BadRequestException;
import org.mule.services.soap.api.exception.SoapFaultException;
import org.mule.services.soap.api.message.SoapRequest;
import org.mule.services.soap.api.message.SoapRequestBuilder;
import org.mule.services.soap.api.message.SoapResponse;

import com.google.common.collect.ImmutableMap;

import java.io.StringWriter;
import java.util.Collections;
import java.util.Map;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import net.sf.saxon.jaxp.SaxonTransformerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * The only {@link WebServiceConsumer} operation. the {@link ConsumeOperation} consumes an operation of the connected web service
 * and returns it's response.
 * <p>
 * The consume operation expects an XML body and a set of headers and attachments if required.
 * <p>
 *
 * @since 4.0
 */
public class ConsumeOperation {

  /**
   * Consumes an operation from a SOAP Web Service.
   *
   * @param connection the connection resolved to execute the operation.
   * @param operation  the name of the web service operation that aims to invoke.
   * @param message    the constructed SOAP message to perform the request.
   */
  @OnException(WscExceptionEnricher.class)
  @Throws(ConsumeErrorTypeProvider.class)
  @OutputResolver(output = ConsumeOutputResolver.class, attributes = WscAttributesResolver.class)
  public Result<Object, WscAttributes> consume(@Connection SoapClient connection,
                                               @MetadataKeyId(OperationKeysResolver.class) String operation,
                                               // TODO MULE-11235
                                               @NullSafe @Optional @TypeResolver(MessageBuilderResolver.class) SoapMessageBuilder message)
      throws SoapFaultException {
    SoapRequestBuilder requestBuilder = SoapRequest.builder();
    message.getAttachments().values().forEach(requestBuilder::withAttachment);
    requestBuilder.withOperation(operation);
    requestBuilder.withSoapHeaders(transformToCxfHeaders(message.getHeaders()));

    if (!isBlank(message.getBody())) {
      requestBuilder.withContent(message.getBody());
    }

    SoapResponse response = connection.consume(requestBuilder.build());
    return Result.<Object, WscAttributes>builder().output(response.getContent())
        .attributes(new WscAttributes(response.getSoapHeaders(), response.getTransportHeaders()))
        .build();
  }

  private Map<String, String> transformToCxfHeaders(String headers) {
    if (isBlank(headers)) {
      return Collections.emptyMap();
    }
    ImmutableMap.Builder<String, String> soapHeaders = ImmutableMap.builder();
    try {
      Element document = toW3cDocument(headers).getDocumentElement();
      NodeList nodes = document.getChildNodes();
      for (int i = 0; i < nodes.getLength(); i++) {
        Node currentNode = nodes.item(i);
        if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
          soapHeaders.put(currentNode.getNodeName(), nodeToString(currentNode));
        }
      }
    } catch (Exception e) {
      throw new BadRequestException("Error while parsing the provided soap headers", e);
    }
    return soapHeaders.build();
  }

  static String nodeToString(Node node) {
    try {
      StringWriter writer = new StringWriter();
      DOMSource source = new DOMSource(node);
      StreamResult result = new StreamResult(writer);
      TransformerFactory idTransformer = new SaxonTransformerFactory();
      Transformer transformer = idTransformer.newTransformer();
      transformer.transform(source, result);
      return writer.toString();
    } catch (Exception e) {
      throw new WscException("Could not transform Node to String", e);
    }
  }
}
