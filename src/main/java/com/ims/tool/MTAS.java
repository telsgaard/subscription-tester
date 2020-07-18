package com.ims.tool;

import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

public class MTAS {

  private static final Logger logger = Logger.getLogger(SOAPClient.class.getName());

  public static void main(String[] args) {

    try {

      Map<String, String> replacements = new HashMap<>();
      replacements.put("${PublicId}", "sip_uri");

      SOAPClient client = new SOAPClient();
      Response response = client.executeRequest(ResourcePaths.REQUEST_ENVELOP_XML, replacements);
      String responseBody = response.getResponseBody();
      String chargingProfile = findChargingProfile(responseBody);
      logger.log(Level.INFO, "Charging profile : " + chargingProfile);

    } catch (Exception e) {
      System.out.println("Error" + e);
    }
  }

  private static String findChargingProfile(String responseBody) {

    try {

      DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = domFactory.newDocumentBuilder();
      Document document =
          builder.parse(IOUtils.toInputStream(responseBody, StandardCharsets.UTF_8));
      XPathFactory xPathfactory = XPathFactory.newInstance();
      XPath xpath = xPathfactory.newXPath();
      XPathExpression expr =
          xpath.compile(
              "/Envelope/Body/GetResponse/MOAttributes/getResponseSubscription/services/user-common-data/ucd-operator-configuration/mmtel-charging-profile/text()");

      Object chargingProfile = expr.evaluate(document, XPathConstants.STRING);
      return String.valueOf(chargingProfile);

    } catch (Exception e1) {
      logger.log(Level.SEVERE, "Failed to get charging profile from response.", e1);
      return null;
    }
  }
}
